package ua.nure.finance.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ua.nure.finance.model.Expenses;
import ua.nure.finance.model.ExpensesCategory;
import ua.nure.finance.model.IncomeCategory;
import ua.nure.finance.reposotory.ExpensesCategoryRepository;
import ua.nure.finance.reposotory.ExpensesRepository;
import ua.nure.finance.reposotory.IncomeCategoryRepository;

@Controller
public class ExpensesController {
    private final ExpensesRepository expensesRepository;
    private final ExpensesCategoryRepository expensesCategoryRepository;

    public ExpensesController(ExpensesRepository expensesRepository, ExpensesCategoryRepository expensesCategoryRepository) {
        this.expensesRepository = expensesRepository;
        this.expensesCategoryRepository = expensesCategoryRepository;
    }


    @GetMapping("/add-expenses")
    public String showAddForm(Expenses expenses, Model model) {
        model.addAttribute("categories", expensesCategoryRepository.findAll());
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
}
