package ua.nure.finance.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ua.nure.finance.model.Income;
import ua.nure.finance.reposotory.ExpensesRepository;
import ua.nure.finance.reposotory.IncomeRepository;

@Controller
public class IncomeController {

    private final IncomeRepository incomeRepository;

    public IncomeController(IncomeRepository incomeRepository, ExpensesRepository expensesRepository) {
        this.incomeRepository = incomeRepository;
    }


    @GetMapping("/add-income")
    public String showSignUpForm(Income income) {
        return "add-income";
    }

    @PostMapping("/add-income")
    public String addIncome(@Valid Income income, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "add-income";
        }

        incomeRepository.save(income);
        return "redirect:/index";
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
        return "redirect:/index";
    }

    @GetMapping("/delete-income/{id}")
    public String deleteIncome(@PathVariable("id") long id, Model model) {
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid income Id:" + id));
        incomeRepository.delete(income);
        return "redirect:/index";
    }
}
