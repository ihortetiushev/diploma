package ua.nure.finance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ua.nure.finance.model.AssetCategory;
import ua.nure.finance.repository.AssetCategoryRepository;

import java.util.Optional;

@Controller
@RequestMapping("/assets-categories")
public class AssetCategoryController {

    private final AssetCategoryRepository categoryRepository;

    public AssetCategoryController(AssetCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Show all categories
    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("category", new AssetCategory()); // for add form
        return "assets-category"; // Thymeleaf template name
    }

    // Save or update category
    @PostMapping("/save")
    public String saveCategory(@ModelAttribute AssetCategory category) {
        Optional<AssetCategory> existing = categoryRepository.findById(category.getId());
        if (existing.isPresent() && existing.get().isReadOnly()) {
            // Don't allow changes
            return "redirect:/assets-categories?error=readonly";
        }
        categoryRepository.save(category);
        return "redirect:/assets-categories";
    }

    // Edit category (populate form)
    @GetMapping("/edit/{id}")
    public String editCategory(@PathVariable Long id, Model model) {
        Optional<AssetCategory> category = categoryRepository.findById(id);
        if (category.isPresent()) {
            model.addAttribute("category", category.get());
        } else {
            model.addAttribute("category", new AssetCategory());
        }
        model.addAttribute("categories", categoryRepository.findAll());
        return "assets-category";
    }

    // Delete category
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        Optional<AssetCategory> category = categoryRepository.findById(id);
        if (category.isPresent() && category.get().isReadOnly()) {
            return "redirect:/assets-categories?error=readonly";
        }
        categoryRepository.deleteById(id);
        return "redirect:/assets-categories";
    }
}