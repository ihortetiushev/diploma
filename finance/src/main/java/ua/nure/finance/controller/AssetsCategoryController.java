package ua.nure.finance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ua.nure.finance.model.AssetsCategory;
import ua.nure.finance.reposotory.AssetsCategoryRepository;

import java.util.Optional;

@Controller
@RequestMapping("/assets-categories")
public class AssetsCategoryController {

    private final AssetsCategoryRepository categoryRepository;

    public AssetsCategoryController(AssetsCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Show all categories
    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("category", new AssetsCategory()); // for add form
        return "assets-category"; // Thymeleaf template name
    }

    // Save or update category
    @PostMapping("/save")
    public String saveCategory(@ModelAttribute AssetsCategory category) {
        categoryRepository.save(category);
        return "redirect:/assets-categories";
    }

    // Edit category (populate form)
    @GetMapping("/edit/{id}")
    public String editCategory(@PathVariable Long id, Model model) {
        Optional<AssetsCategory> category = categoryRepository.findById(id);
        if (category.isPresent()) {
            model.addAttribute("category", category.get());
        } else {
            model.addAttribute("category", new AssetsCategory());
        }
        model.addAttribute("categories", categoryRepository.findAll());
        return "assets-category";
    }

    // Delete category
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryRepository.deleteById(id);
        return "redirect:/assets-categories";
    }
}