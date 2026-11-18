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
        });
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
        totalsRepository.save(totals);
        return "redirect:/optimization?saved";
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
