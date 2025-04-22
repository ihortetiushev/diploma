package ua.nure.finance.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ua.nure.finance.model.CategorizedAmount;
import ua.nure.finance.model.Currency;
import ua.nure.finance.model.Income;
import ua.nure.finance.model.IncomeCategory;
import ua.nure.finance.reposotory.CurrencyRepository;
import ua.nure.finance.reposotory.IncomeCategoryRepository;
import ua.nure.finance.reposotory.IncomeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class IncomeController extends BaseController{

    @Autowired
    private IncomeRepository incomeRepository;
    @Autowired
    private IncomeCategoryRepository incomeCategoryRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @GetMapping("/add-income")
    public String addIncomeForm(Model model, Income income) {
        Currency defaultCurrency = new Currency();
        defaultCurrency.setCurrencyCode("UAH");
        income.setCurrency(defaultCurrency);
        income.setOperationDate(LocalDate.now());
        model.addAttribute("categories", incomeCategoryRepository.findAll());
        model.addAttribute("currencies", currencyRepository.findAll());
        return "add-income";
    }

    @GetMapping("/income-categories")
    public String incomeCategories(Model model, IncomeCategory incomeCategory) {
        model.addAttribute("categories", incomeCategoryRepository.findAll());
        return "income-categories";
    }

    @PostMapping("/add-income-category")
    public String addIncomeCategory(@Valid IncomeCategory incomeCategory, BindingResult result) {
        if (result.hasErrors()) {
            return "income-categories";
        }

        incomeCategoryRepository.save(incomeCategory);
        return "redirect:/income-categories";
    }

    @PostMapping("/add-income")
    public String addIncome(@Valid Income income, BindingResult result) {
        if (result.hasErrors()) {
            return "add-income";
        }

        incomeRepository.save(income);
        return "redirect:/";
    }

    @GetMapping("/edit-income/{id}")
    public String showUpdateForm(@PathVariable("id") long id, Model model) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid income id:" + id));

        model.addAttribute("income", income);
        return "update-income";
    }

    @GetMapping("/income-controller")
    public String showIncomeList(Model model) {
        model.addAttribute("incomes", incomeRepository.findAll());
        return "income-controller";
    }


    @PostMapping("/update-income/{id}")
    public String updateIncome(@PathVariable("id") long id, @Valid Income income,
                               BindingResult result, Model model) {
        if (result.hasErrors()) {
            income.setId(id);
            return "update-income";
        }

        incomeRepository.save(income);
        return "redirect:/";
    }

    @PostMapping("/update-income-category")
    public String updateIncomeCategory(@Valid IncomeCategory incomeCategory,
                               BindingResult result) {
        if (result.hasErrors()) {
            return "income-categories";
        }

        incomeCategoryRepository.save(incomeCategory);
        return "redirect:/income-categories";
    }

    @GetMapping("/delete-income/{id}")
    public String deleteIncome(@PathVariable("id") long id) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid income Id:" + id));
        incomeRepository.delete(income);
        return "redirect:/";
    }

    @GetMapping("/delete-income-category/{id}")
    public String deleteIncomeCategory(@PathVariable("id") long id) {
        IncomeCategory incomeCategory = incomeCategoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid income category Id:" + id));
        incomeCategoryRepository.delete(incomeCategory);
        return "redirect:/income-categories";
    }

    @GetMapping("/income")
    public String getIncome(@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                            Model model) {
        prepareModel(incomeRepository.findAll(), model);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "income";
    }
}
