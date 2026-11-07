package ua.nure.finance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ua.nure.finance.model.Expense;
import ua.nure.finance.model.Income;
import ua.nure.finance.repository.ExpenseRepository;
import ua.nure.finance.repository.IncomeRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class ForecastController {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private static final BigDecimal INCOME_CURRENT_MONTH_WEIGHT = new BigDecimal("0.4");
    private static final BigDecimal INCOME_PREVIOUS_MONTH_WEIGHT = new BigDecimal("0.6");
    private static final BigDecimal EXPENSE_CURRENT_MONTH_WEIGHT = new BigDecimal("0.3");
    private static final BigDecimal EXPENSE_PREVIOUS_MONTH_WEIGHT = new BigDecimal("0.7");

    @Autowired
    public ForecastController(IncomeRepository incomeRepository, ExpenseRepository expenseRepository) {
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
    }

    @GetMapping("/forecast")
    public String getForecast(Model model) {
        LocalDate today = LocalDate.now();
        YearMonth lastCompletedMonth = YearMonth.from(today).minusMonths(1);
        LocalDate startDate = lastCompletedMonth.minusMonths(5).atDay(1);
        LocalDate endDate = lastCompletedMonth.atEndOfMonth();

        List<Income> incomes = incomeRepository
                .findByAsset_PrimaryAccountTrueAndOperationDateBetween(startDate, endDate);
        List<Expense> expenses = expenseRepository
                .findByAsset_PrimaryAccountTrueAndOperationDateBetween(startDate, endDate);

        Map<YearMonth, BigDecimal> monthlyIncome = sumByMonth(
                incomes.stream().map(income -> Map.entry(income.getOperationDate(), income.getAmountMainCurrency())).toList());
        Map<YearMonth, BigDecimal> monthlyExpenses = sumByMonth(
                expenses.stream().map(expense -> Map.entry(expense.getOperationDate(), expense.getAmountMainCurrency())).toList());

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale.forLanguageTag("uk"));
        List<ForecastMonth> months = new ArrayList<>();
        BigDecimal previousSmoothedIncome = null;
        BigDecimal previousSmoothedExpenses = null;
        for (int index = 0; index < 6; index++) {
            YearMonth month = lastCompletedMonth.minusMonths(5L - index);
            BigDecimal actualIncome = monthlyIncome.getOrDefault(month, BigDecimal.ZERO);
            BigDecimal actualExpenses = monthlyExpenses.getOrDefault(month, BigDecimal.ZERO);
            BigDecimal smoothedIncome = smooth(actualIncome, previousSmoothedIncome,
                    INCOME_CURRENT_MONTH_WEIGHT, INCOME_PREVIOUS_MONTH_WEIGHT);
            BigDecimal smoothedExpenses = smooth(actualExpenses, previousSmoothedExpenses,
                    EXPENSE_CURRENT_MONTH_WEIGHT, EXPENSE_PREVIOUS_MONTH_WEIGHT);
            months.add(new ForecastMonth(month.format(monthFormatter), actualIncome, smoothedIncome,
                    actualExpenses, smoothedExpenses));
            previousSmoothedIncome = smoothedIncome;
            previousSmoothedExpenses = smoothedExpenses;
        }

        model.addAttribute("months", months);
        model.addAttribute("forecastSmoothedIncome", previousSmoothedIncome);
        model.addAttribute("forecastSmoothedExpenses", previousSmoothedExpenses);
        model.addAttribute("forecastBalance", previousSmoothedIncome.subtract(previousSmoothedExpenses));
        return "forecast";
    }

    private Map<YearMonth, BigDecimal> sumByMonth(List<Map.Entry<LocalDate, BigDecimal>> transactions) {
        Map<YearMonth, BigDecimal> totals = new HashMap<>();
        for (Map.Entry<LocalDate, BigDecimal> transaction : transactions) {
            totals.merge(YearMonth.from(transaction.getKey()), transaction.getValue(), BigDecimal::add);
        }
        return totals;
    }

    private BigDecimal smooth(BigDecimal actualValue, BigDecimal previousSmoothedValue,
                              BigDecimal currentMonthWeight, BigDecimal previousMonthWeight) {
        if (previousSmoothedValue == null) {
            return actualValue.setScale(2, RoundingMode.HALF_UP);
        }
        return actualValue.multiply(currentMonthWeight)
                .add(previousSmoothedValue.multiply(previousMonthWeight))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static class ForecastMonth {
        private final String month;
        private final BigDecimal actualIncome;
        private final BigDecimal smoothedIncome;
        private final BigDecimal expenses;
        private final BigDecimal smoothedExpenses;

        public ForecastMonth(String month, BigDecimal actualIncome, BigDecimal smoothedIncome,
                             BigDecimal expenses, BigDecimal smoothedExpenses) {
            this.month = month;
            this.actualIncome = actualIncome;
            this.smoothedIncome = smoothedIncome;
            this.expenses = expenses;
            this.smoothedExpenses = smoothedExpenses;
        }

        public String getMonth() {
            return month;
        }

        public BigDecimal getActualIncome() {
            return actualIncome;
        }

        public BigDecimal getSmoothedIncome() {
            return smoothedIncome;
        }

        public BigDecimal getExpenses() {
            return expenses;
        }

        public BigDecimal getSmoothedExpenses() {
            return smoothedExpenses;
        }
    }
}
