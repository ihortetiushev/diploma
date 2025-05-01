package ua.nure.finance.controller;

import org.springframework.ui.Model;
import ua.nure.finance.model.CategorizedAmount;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class BaseController {
    protected void prepareModel(Iterable<? extends CategorizedAmount> data, Model model) {
        List<CategorizedAmount> dataList = new ArrayList<>();
        data.forEach(dataList::add);
        Map<String, BigDecimal> totalsByCategory = getTotalsByCategory(dataList);
        model.addAttribute("totalByCategory", totalsByCategory);
        model.addAttribute("chartData", getChartData(totalsByCategory));
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
