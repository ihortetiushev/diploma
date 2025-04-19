package ua.nure.finance.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ua.nure.finance.model.CategorizedAmount;
import ua.nure.finance.reposotory.ExpensesRepository;
import ua.nure.finance.reposotory.IncomeRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class IndexController {
    private final IncomeRepository incomeRepository;
    private final ExpensesRepository expensesRepository;

    public IndexController(IncomeRepository incomeRepository, ExpensesRepository expensesRepository) {
        this.incomeRepository = incomeRepository;
        this.expensesRepository = expensesRepository;
    }

    @GetMapping("/") //Expenses is the default data
    public String getExpenses(Model model) {
        prepareModel(expensesRepository.findAll(), model);
        model.addAttribute("activeButton","expenses-button");
        return "index";
    }

    @GetMapping("/income")
    public String getIncome(Model model) {
        prepareModel(incomeRepository.findAll(), model);
        model.addAttribute("activeButton","income-button");
        return "index";
    }

    private void prepareModel(Iterable<? extends CategorizedAmount> data, Model model) {
        List<CategorizedAmount> dataList = new ArrayList<>();
        data.forEach(dataList::add);
        Map<String, BigDecimal> totalsByCategory = getTotalsByCategory(dataList);
        model.addAttribute("totalByCategory", totalsByCategory);
        model.addAttribute("chartData", getChartData(totalsByCategory));
    }

    Map<String, BigDecimal> getTotalsByCategory(List<? extends CategorizedAmount> data) {
        Map<String, BigDecimal> totals = new HashMap<>();
        for (CategorizedAmount item : data) {
            BigDecimal current = totals.get(item.getCategoryName());
            current = current == null ? new BigDecimal(0) : current;
            BigDecimal newAmount = current.add(item.getAmount());
            totals.put(item.getCategoryName(), newAmount);
        }
        return totals;
    }

    private List<List<Object>> getChartData(Map<String, BigDecimal> totalsByCategory) {
        List<List<Object>> chartData = new ArrayList<>();
        for(Map.Entry<String, BigDecimal> entry: totalsByCategory.entrySet()) {
            List<Object> dataList = new ArrayList<>();
            dataList.add(entry.getKey());
            dataList.add(entry.getValue());
            chartData.add(dataList);
        }
        return chartData;
    }
}
