package ua.nure.finance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ua.nure.finance.model.Asset;
import ua.nure.finance.model.AssetsCategory;
import ua.nure.finance.reposotory.AssetRepository;
import ua.nure.finance.reposotory.AssetsCategoryRepository;

import java.util.Optional;

@Controller
@RequestMapping("/assets")
public class AssetsController {

    @Autowired
    private AssetsCategoryRepository categoryRepository;
    @Autowired
    private AssetRepository assetRepository;

    // Show all categories
    @GetMapping
    public String listAssets(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("asset", new Asset()); // for add form
        return "assets"; // Thymeleaf template name
    }

}