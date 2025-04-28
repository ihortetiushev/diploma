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
import ua.nure.finance.model.Currency;
import ua.nure.finance.model.Expense;
import ua.nure.finance.model.ExpenseCategory;
import ua.nure.finance.repository.AssetRepository;
import ua.nure.finance.repository.CurrencyRepository;
import ua.nure.finance.repository.ExpenseCategoryRepository;
import ua.nure.finance.repository.ExpenseRepository;
import ua.nure.finance.service.ExpenseService;

import java.time.LocalDate;

@Controller
public class ExpenseController extends BaseController {
    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private ExpenseCategoryRepository expenseCategoryRepository;
    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private ExpenseService expenseService;
    @Autowired
    private AssetRepository assetRepository;


    @GetMapping("/add-expense")
    public String showAddForm(Expense expense, Model model) {
        Currency defaultCurrency = new Currency();
        defaultCurrency.setCurrencyCode("UAH");
        expense.setCurrency(defaultCurrency);
        expense.setOperationDate(LocalDate.now());
        model.addAttribute("categories", expenseCategoryRepository.findAll());
        model.addAttribute("currencies", currencyRepository.findAll());
        model.addAttribute("assets", assetRepository.findAll());
        return "add-expense";
    }

    @GetMapping("/expense-categories")
    public String expensesCategories(Model model, ExpenseCategory expenseCategory) {
        model.addAttribute("categories", expenseCategoryRepository.findAll());
        return "expense-categories";
    }

    @PostMapping("/add-expense")
    public String addExpense(@Valid Expense expenses, BindingResult result) {
        if (result.hasErrors()) {
            return "add-expense";
        }
        expenseService.saveExpense(expenses);
        return "redirect:/expenses";
    }

    @PostMapping("/add-expenses-category")
    public String addExpensesCategory(@Valid ExpenseCategory expenseCategory, BindingResult result) {
        if (result.hasErrors()) {
            return "expense-categories";
        }

        expenseCategoryRepository.save(expenseCategory);
        return "redirect:/expense-categories";
    }

    @PostMapping("/update-expenses-category")
    public String updateExpenseCategory(@Valid ExpenseCategory expenseCategory,
                                         BindingResult result) {
        if (result.hasErrors()) {
            return "expense-categories";
        }

        expenseCategoryRepository.save(expenseCategory);
        return "redirect:/expense-categories";
    }

    @GetMapping("/delete-expense-category/{id}")
    public String deleteExpenseCategory(@PathVariable("id") long id) {
        ExpenseCategory expensesCategory = expenseCategoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid income category Id:" + id));
        expenseCategoryRepository.delete(expensesCategory);
        return "redirect:/expense-categories";
    }

    @GetMapping("/expense/edit/{id}")
    public String showUpdateForm(@PathVariable("id") long id, Model model) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid expenses id:" + id));

        model.addAttribute("expense", expense);
        model.addAttribute("categories", expenseCategoryRepository.findAll());
        model.addAttribute("currencies", currencyRepository.findAll());
        model.addAttribute("assets", assetRepository.findAll());
        return "update-expense";
    }


    @GetMapping("/expenses-controller")
    public String showIncomeList(Model model) {
        model.addAttribute("expenses", expenseRepository.findAll());
        return "expenses-controller";
    }

    @PostMapping("/expense/edit/{id}")
    public String updateExpense(@PathVariable("id") long id, @Valid Expense expenses,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) {
            expenses.setId(id);
            return "update-expense";
        }

        expenseRepository.save(expenses);
        return "redirect:/transactions";
    }

    @GetMapping("/delete-expenses/{id}")
    public String deleteExpenses(@PathVariable("id") long id, Model model) {
        Expense expenses = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid expenses Id:" + id));
        expenseRepository.delete(expenses);
        return "redirect:/transactions";
    }

    @GetMapping("/")
    public String getExpensesDefaultDates(Model model) {
        return "redirect:/expenses";
    }

    @GetMapping("/expenses")
    public String getExpenses(@RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam(value = "endDate" ,required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                              Model model) {
        return expenses(startDate, endDate, model);
    }

    private String expenses( LocalDate startDate, LocalDate endDate, Model model) {
        endDate = endDate == null ? LocalDate.now() : endDate;
        startDate = startDate == null ? endDate.withDayOfMonth(1) : startDate;
        prepareModel(expenseRepository.findByOperationDateBetween(startDate, endDate), model);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "expenses";
    }

}
