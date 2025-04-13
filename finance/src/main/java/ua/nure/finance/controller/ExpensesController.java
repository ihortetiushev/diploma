package ua.nure.finance.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ua.nure.finance.model.Expenses;
import ua.nure.finance.reposotory.ExpensesRepository;

@Controller
public class ExpensesController {
    private final ExpensesRepository expensesRepository;

    public ExpensesController(ExpensesRepository expensesRepository) {
        this.expensesRepository = expensesRepository;
    }


    @GetMapping("/add-expenses")
    public String showSignUpForm(Expenses expenses) {
        return "add-expenses";
    }

    @PostMapping("/add-expenses")
    public String addExpenses(@Valid Expenses expenses, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "add-expenses";
        }

        expensesRepository.save(expenses);
        return "redirect:/index";
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
        return "redirect:/index";
    }

    @GetMapping("/delete-expenses/{id}")
    public String deleteExpenses(@PathVariable("id") long id, Model model) {
        Expenses expenses = expensesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid expenses Id:" + id));
        expensesRepository.delete(expenses);
        return "redirect:/index";
    }
}
