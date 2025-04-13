package ua.nure.finance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ua.nure.finance.model.Income;
import ua.nure.finance.reposotory.ExpensesRepository;
import ua.nure.finance.reposotory.IncomeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Controller
public class IndexController {
    private final IncomeRepository incomeRepository;
    private final ExpensesRepository expensesRepository;

    public IndexController(IncomeRepository incomeRepository, ExpensesRepository expensesRepository) {
        this.incomeRepository = incomeRepository;
        this.expensesRepository = expensesRepository;
    }

    @GetMapping("/index")
    public String showIncomeList(Model model) {
        Iterable<Income> all = incomeRepository.findAll();
        model.addAttribute("incomes", all);
        model.addAttribute("expenses", expensesRepository.findAll());
        model.addAttribute("chartData", getChartData(all));
        return "index";
    }

    private List<List<Object>> getChartData(Iterable<Income> data) {
        Random random = new Random(System.currentTimeMillis());
        List<List<Object>> chartData = new ArrayList<>();
        data.forEach(income -> {

                    List<Object> dataList = new ArrayList<>();
                    dataList.add(income.getDescription());
                    dataList.add(income.getAmount());
                    chartData.add(dataList);
                }

        );
        return chartData;
/*        return List.of(
                List.of("Mushrooms", 10),
                List.of("Onions", 20),
                List.of("Olives", random.nextInt(5)),
                List.of("Zucchini", random.nextInt(5)),
                List.of("Pepperoni", random.nextInt(5))
        );*/
    }
}
