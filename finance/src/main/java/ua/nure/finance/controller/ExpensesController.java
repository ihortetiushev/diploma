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
import ua.nure.finance.model.Expenses;
import ua.nure.finance.model.ExpensesCategory;
import ua.nure.finance.reposotory.CurrencyRepository;
import ua.nure.finance.reposotory.ExpensesCategoryRepository;
import ua.nure.finance.reposotory.ExpensesRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ExpensesController extends BaseController {
    @Autowired
    private ExpensesRepository expensesRepository;
    @Autowired
    private ExpensesCategoryRepository expensesCategoryRepository;
    @Autowired
    private CurrencyRepository currencyRepository;

    @GetMapping("/add-expenses")
    public String showAddForm(Expenses expenses, Model model) {
        Currency defaultCurrency = new Currency();
        defaultCurrency.setCurrencyCode("UAH");
        expenses.setCurrency(defaultCurrency);
        expenses.setOperationDate(LocalDate.now());
        model.addAttribute("categories", expensesCategoryRepository.findAll());
        model.addAttribute("currencies", currencyRepository.findAll());
        return "add-expenses";
    }

    @GetMapping("/expenses-categories")
    public String expensesCategories(Model model, ExpensesCategory expensesCategory) {
        model.addAttribute("categories", expensesCategoryRepository.findAll());
        return "expenses-categories";
    }

    @PostMapping("/add-expenses")
    public String addExpenses(@Valid Expenses expenses, BindingResult result) {
        if (result.hasErrors()) {
            return "add-expenses";
        }

        expensesRepository.save(expenses);
        return "redirect:/";
    }

    @PostMapping("/add-expenses-category")
    public String addExpensesCategory(@Valid ExpensesCategory expensesCategory, BindingResult result) {
        if (result.hasErrors()) {
            return "expenses-categories";
        }

        expensesCategoryRepository.save(expensesCategory);
        return "redirect:/expenses-categories";
    }

    @PostMapping("/update-expenses-category")
    public String updateExpensesCategory(@Valid ExpensesCategory expensesCategory,
                                         BindingResult result) {
        if (result.hasErrors()) {
            return "expenses-categories";
        }

        expensesCategoryRepository.save(expensesCategory);
        return "redirect:/expenses-categories";
    }

    @GetMapping("/delete-expenses-category/{id}")
    public String deleteExpensesCategory(@PathVariable("id") long id) {
        ExpensesCategory expensesCategory = expensesCategoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid income category Id:" + id));
        expensesCategoryRepository.delete(expensesCategory);
        return "redirect:/expenses-categories";
    }

    @GetMapping("/edit-expenses/{id}")
    public String showUpdateForm(@PathVariable("id") long id, Model model) {
        Expenses expenses = expensesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid expenses id:" + id));

        model.addAttribute("expenses", expenses);
        return "update-expenses";
    }


    @GetMapping("/expenses-controller")
    public String showIncomeList(Model model) {
        model.addAttribute("expenses", expensesRepository.findAll());
        return "expenses-controller";
    }

    @PostMapping("/update-expenses/{id}")
    public String updateExpenses(@PathVariable("id") long id, @Valid Expenses expenses,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) {
            expenses.setId(id);
            return "update-expenses";
        }

        expensesRepository.save(expenses);
        return "redirect:/";
    }

    @GetMapping("/delete-expenses/{id}")
    public String deleteExpenses(@PathVariable("id") long id, Model model) {
        Expenses expenses = expensesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid expenses Id:" + id));
        expensesRepository.delete(expenses);
        return "redirect:/";
    }

    @GetMapping("/")
    public String getExpensesDefaultDates(Model model) {
        LocalDate today = LocalDate.now();
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        return expenses(firstOfMonth, today, model);
    }

    @GetMapping("/expenses")
    public String getExpenses(@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                              Model model) {
        return expenses(startDate, endDate, model);
    }

    private String expenses( LocalDate startDate, LocalDate endDate, Model model) {
        prepareModel(expensesRepository.findByOperationDateBetween(startDate, endDate), model);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "expenses";
    }

}
