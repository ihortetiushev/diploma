package ua.nure.finance.controller;

import org.springframework.ui.Model;
import ua.nure.finance.model.CategorizedAmount;
import ua.nure.finance.model.TransactionType;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

abstract class BaseController {
    protected void prepareModel(List<? extends CategorizedAmount> expenseData,
                                List<? extends CategorizedAmount> incomeData,
                                Model model, TransactionType type) {
        List<? extends CategorizedAmount> dataList = type == TransactionType.INCOME ? incomeData : expenseData;
        Map<String, BigDecimal> totalsByCategory = getTotalsByCategory(dataList);
        model.addAttribute("totalByCategory", getSortedMap(totalsByCategory));
        model.addAttribute("chartData", getChartData(totalsByCategory));
        BigDecimal totalAmount = totalsByCategory.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("totalAmount", totalAmount);
        BigDecimal income = incomeData.stream().map(i -> i.getAmountMainCurrency())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("totalIncome", income);
        BigDecimal expenses = expenseData.stream().map(e -> e.getAmountMainCurrency())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("totalExpenses", expenses);
        model.addAttribute("diff", income.subtract(expenses));
    }

    protected Map<String, BigDecimal> getSortedMap(Map<String, BigDecimal> totalsByCategory) {
        Map<String, BigDecimal> sortedTotalsByCategory = totalsByCategory.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, // merge function (not needed here)
                        LinkedHashMap::new // keeps insertion order
                ));
        return sortedTotalsByCategory;
    }

    protected Map<String, BigDecimal> getTotalsByCategory(List<? extends CategorizedAmount> data) {
        Map<String, BigDecimal> totals = new HashMap<>();
        for (CategorizedAmount item : data) {
            BigDecimal current = totals.get(item.getCategoryName());
            current = current == null ? new BigDecimal(0) : current;
            BigDecimal newAmount = current.add(item.getAmountMainCurrency());
            totals.put(item.getCategoryName(), newAmount);
        }
        return totals;
    }

    protected List<List<Object>> getChartData(Map<String, BigDecimal> totalsByCategory) {
        List<List<Object>> chartData = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : totalsByCategory.entrySet()) {
            List<Object> dataList = new ArrayList<>();
            dataList.add(entry.getKey());
            dataList.add(entry.getValue());
            chartData.add(dataList);
        }
        return chartData;
    }
}
