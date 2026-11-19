package ua.nure.finance.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ua.nure.finance.model.OptimizationCategory;
import ua.nure.finance.model.OptimizationTotals;
import ua.nure.finance.repository.OptimizationCategoryRepository;
import ua.nure.finance.repository.OptimizationTotalsRepository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Controller
public class OptimizationController {

    private static final List<String> DEFAULT_CATEGORY_NAMES = List.of(
            "Необов'язкові витрати", "Розваги", "Заощадження", "Різне");

    private final OptimizationCategoryRepository categoryRepository;
    private final OptimizationTotalsRepository totalsRepository;

    public OptimizationController(OptimizationCategoryRepository categoryRepository,
                                  OptimizationTotalsRepository totalsRepository) {
        this.categoryRepository = categoryRepository;
        this.totalsRepository = totalsRepository;
    }

    @GetMapping("/optimization")
    public String getOptimization(@RequestParam(required = false) String saved, Model model) {
        List<OptimizationCategory> categories = categoryRepository.findAllByOrderBySortOrderAscIdAsc();
        if (categories.isEmpty()) {
            categories = DEFAULT_CATEGORY_NAMES.stream()
                    .map(this::newCategory)
                    .toList();
        }
        OptimizationSettingsForm settings = new OptimizationSettingsForm(categories);
        totalsRepository.findById(1L).ifPresent(totals -> {
            settings.setTotalOptimal(totals.getOptimal());
            settings.setTotalDeviation(totals.getDeviation());
            settings.setIncome(totals.getIncome());
            settings.setMandatoryExpenses(totals.getMandatoryExpenses());
        });
        calculateOptimal(settings, settings.getAvailableFunds());
        model.addAttribute("settings", settings);
        model.addAttribute("saved", saved != null);
        return "optimization";
    }

    @PostMapping("/optimization")
    @Transactional
    public String saveOptimization(@Valid @ModelAttribute("settings") OptimizationSettingsForm settings,
                                   BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Перевірте назви категорій і числові значення.");
            return "optimization";
        }

        calculateOptimal(settings, settings.getAvailableFunds());

        List<OptimizationCategory> categories = new ArrayList<>();
        for (int index = 0; index < settings.getCategories().size(); index++) {
            OptimizationCategoryForm form = settings.getCategories().get(index);
            OptimizationCategory category = new OptimizationCategory();
            category.setName(form.getName().trim());
            category.setSpendingLimit(form.getLimit());
            category.setPriority(form.getPriority());
            category.setDesired(form.getDesired());
            category.setOptimal(form.getOptimal());
            category.setResult(form.getResult());
            category.setDeviation(form.getDeviation());
            category.setSortOrder(index);
            categories.add(category);
        }

        categoryRepository.deleteAllInBatch();
        categoryRepository.saveAll(categories);
        OptimizationTotals totals = totalsRepository.findById(1L).orElseGet(OptimizationTotals::new);
        totals.setId(1L);
        totals.setOptimal(settings.getTotalOptimal());
        totals.setDeviation(settings.getTotalDeviation());
        totals.setIncome(settings.getIncome());
        totals.setMandatoryExpenses(settings.getMandatoryExpenses());
        totalsRepository.save(totals);
        return "redirect:/optimization?saved";
    }

    private void calculateOptimal(OptimizationSettingsForm settings, BigDecimal availableFunds) {
        List<OptimizationCategoryForm> categories = settings.getCategories();
        if (availableFunds == null) {
            categories.forEach(category -> {
                category.setOptimal(null);
                category.setDeviation(null);
                category.setResult(null);
            });
            settings.setTotalOptimal(null);
            settings.setTotalDeviation(null);
            return;
        }

        List<BigInteger> desiredCents = categories.stream()
                .map(category -> toCents(category.getDesired()))
                .toList();
        List<BigInteger> optimalCents = new ArrayList<>(desiredCents);
        BigInteger totalDesiredCents = desiredCents.stream().reduce(BigInteger.ZERO, BigInteger::add);
        BigInteger availableCents = toCents(availableFunds).max(BigInteger.ZERO);
        BigInteger remainingReduction = totalDesiredCents.subtract(availableCents).max(BigInteger.ZERO);
        List<Integer> activeIndexes = new ArrayList<>();
        for (int index = 0; index < desiredCents.size(); index++) {
            if (desiredCents.get(index).signum() > 0) {
                activeIndexes.add(index);
            }
        }

        while (remainingReduction.signum() > 0 && !activeIndexes.isEmpty()) {
            BigInteger totalWeight = activeIndexes.stream()
                    .map(index -> priorityWeight(categories.get(index).getPriority()))
                    .reduce(BigInteger.ZERO, BigInteger::add);
            List<Integer> cappedIndexes = new ArrayList<>();
            for (int index : activeIndexes) {
                BigInteger weight = priorityWeight(categories.get(index).getPriority());
                if (remainingReduction.multiply(weight)
                        .compareTo(desiredCents.get(index).multiply(totalWeight)) >= 0) {
                    cappedIndexes.add(index);
                }
            }

            if (cappedIndexes.isEmpty()) {
                List<ReductionShare> shares = new ArrayList<>();
                BigInteger allocatedCents = BigInteger.ZERO;
                for (int index : activeIndexes) {
                    BigInteger[] quotientAndRemainder = remainingReduction
                            .multiply(priorityWeight(categories.get(index).getPriority()))
                            .divideAndRemainder(totalWeight);
                    shares.add(new ReductionShare(index, quotientAndRemainder[0], quotientAndRemainder[1]));
                    allocatedCents = allocatedCents.add(quotientAndRemainder[0]);
                }

                int extraCents = remainingReduction.subtract(allocatedCents).intValueExact();
                shares.sort((left, right) -> right.remainder().compareTo(left.remainder()));
                for (int index = 0; index < shares.size(); index++) {
                    ReductionShare share = shares.get(index);
                    BigInteger reduction = share.cents().add(index < extraCents ? BigInteger.ONE : BigInteger.ZERO);
                    optimalCents.set(share.categoryIndex(), desiredCents.get(share.categoryIndex()).subtract(reduction));
                }
                remainingReduction = BigInteger.ZERO;
            } else {
                for (int index : cappedIndexes) {
                    optimalCents.set(index, BigInteger.ZERO);
                    remainingReduction = remainingReduction.subtract(desiredCents.get(index));
                }
                activeIndexes.removeAll(cappedIndexes);
            }
        }

        BigInteger totalOptimalCents = BigInteger.ZERO;
        BigInteger totalDeviationCents = BigInteger.ZERO;
        for (int index = 0; index < categories.size(); index++) {
            BigDecimal desired = categories.get(index).getDesired();
            BigInteger optimal = optimalCents.get(index);
            categories.get(index).setOptimal(desired == null ? null : fromCents(optimal));
            totalOptimalCents = totalOptimalCents.add(optimal);
            BigInteger deviation = desired == null ? BigInteger.ZERO : desiredCents.get(index).subtract(optimal);
            categories.get(index).setDeviation(desired == null ? null : fromCents(deviation));
            categories.get(index).setResult(desired == null ? null : resultForDeviation(deviation.signum()));
            totalDeviationCents = totalDeviationCents.add(deviation);
        }
        settings.setTotalOptimal(fromCents(totalOptimalCents));
        settings.setTotalDeviation(fromCents(totalDeviationCents));
    }

    private BigInteger toCents(BigDecimal value) {
        return value == null
                ? BigInteger.ZERO
                : value.setScale(2, RoundingMode.HALF_UP).movePointRight(2).toBigIntegerExact();
    }

    private BigDecimal fromCents(BigInteger value) {
        return new BigDecimal(value, 2);
    }

    private BigInteger priorityWeight(Integer priority) {
        return BigInteger.valueOf(60L / priority);
    }

    private String resultForDeviation(int deviationSign) {
        return deviationSign < 0 ? "Не викон." : "Викон.";
    }

    private record ReductionShare(int categoryIndex, BigInteger cents, BigInteger remainder) {
    }

    private OptimizationCategory newCategory(String name) {
        OptimizationCategory category = new OptimizationCategory();
        category.setName(name);
        category.setPriority(1);
        return category;
    }

    public static class OptimizationSettingsForm {
        @Valid
        private List<OptimizationCategoryForm> categories = new ArrayList<>();
        private BigDecimal totalOptimal;
        private BigDecimal totalDeviation;
        @DecimalMin("0")
        private BigDecimal income;
        @DecimalMin("0")
        private BigDecimal mandatoryExpenses;

        public OptimizationSettingsForm() {
        }

        public OptimizationSettingsForm(List<OptimizationCategory> categories) {
            this.categories = categories.stream().map(OptimizationCategoryForm::new).toList();
        }

        public List<OptimizationCategoryForm> getCategories() {
            return categories;
        }

        public void setCategories(List<OptimizationCategoryForm> categories) {
            this.categories = categories;
        }

        public BigDecimal getTotalOptimal() {
            return totalOptimal;
        }

        public void setTotalOptimal(BigDecimal totalOptimal) {
            this.totalOptimal = totalOptimal;
        }

        public BigDecimal getTotalDeviation() {
            return totalDeviation;
        }

        public void setTotalDeviation(BigDecimal totalDeviation) {
            this.totalDeviation = totalDeviation;
        }

        public BigDecimal getIncome() {
            return income;
        }

        public void setIncome(BigDecimal income) {
            this.income = income;
        }

        public BigDecimal getMandatoryExpenses() {
            return mandatoryExpenses;
        }

        public void setMandatoryExpenses(BigDecimal mandatoryExpenses) {
            this.mandatoryExpenses = mandatoryExpenses;
        }

        public BigDecimal getAvailableFunds() {
            if (income == null || mandatoryExpenses == null) {
                return null;
            }
            return income.subtract(mandatoryExpenses);
        }
    }

    public static class OptimizationCategoryForm {
        @NotBlank
        private String name;
        @DecimalMin("0")
        private BigDecimal limit;
        @NotNull
        @Min(1)
        @Max(5)
        private Integer priority;
        @DecimalMin("0")
        private BigDecimal desired;
        private BigDecimal optimal;
        private String result;
        private BigDecimal deviation;

        public OptimizationCategoryForm() {
        }

        public OptimizationCategoryForm(OptimizationCategory category) {
            name = category.getName();
            limit = category.getSpendingLimit();
            priority = category.getPriority() == null ? 1 : category.getPriority();
            desired = category.getDesired();
            optimal = category.getOptimal();
            result = category.getResult();
            deviation = category.getDeviation();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getLimit() {
            return limit;
        }

        public void setLimit(BigDecimal limit) {
            this.limit = limit;
        }

        public Integer getPriority() {
            return priority;
        }

        public void setPriority(Integer priority) {
            this.priority = priority;
        }

        public BigDecimal getDesired() {
            return desired;
        }

        public void setDesired(BigDecimal desired) {
            this.desired = desired;
        }

        public BigDecimal getOptimal() {
            return optimal;
        }

        public void setOptimal(BigDecimal optimal) {
            this.optimal = optimal;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public BigDecimal getDeviation() {
            return deviation;
        }

        public void setDeviation(BigDecimal deviation) {
            this.deviation = deviation;
        }
    }
}
