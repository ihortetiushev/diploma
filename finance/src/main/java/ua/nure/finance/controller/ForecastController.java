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
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class ForecastController {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;

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
        List<ForecastMonth> months = java.util.stream.IntStream.range(0, 6)
                .mapToObj(index -> {
                    YearMonth month = lastCompletedMonth.minusMonths(5L - index);
                    return new ForecastMonth(
                            month.format(monthFormatter),
                            monthlyIncome.getOrDefault(month, BigDecimal.ZERO),
                            monthlyExpenses.getOrDefault(month, BigDecimal.ZERO));
                })
                .toList();

        model.addAttribute("months", months);
        return "forecast";
    }

    private Map<YearMonth, BigDecimal> sumByMonth(List<Map.Entry<LocalDate, BigDecimal>> transactions) {
        Map<YearMonth, BigDecimal> totals = new HashMap<>();
        for (Map.Entry<LocalDate, BigDecimal> transaction : transactions) {
            totals.merge(YearMonth.from(transaction.getKey()), transaction.getValue(), BigDecimal::add);
        }
        return totals;
    }

    public static class ForecastMonth {
        private final String month;
        private final BigDecimal actualIncome;
        private final BigDecimal expenses;

        public ForecastMonth(String month, BigDecimal actualIncome, BigDecimal expenses) {
            this.month = month;
            this.actualIncome = actualIncome;
            this.expenses = expenses;
        }

        public String getMonth() {
            return month;
        }

        public BigDecimal getActualIncome() {
            return actualIncome;
        }

        public BigDecimal getExpenses() {
            return expenses;
        }
    }
}
