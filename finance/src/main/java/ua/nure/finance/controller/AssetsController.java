package ua.nure.finance.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ua.nure.finance.model.Asset;
import ua.nure.finance.model.Currency;
import ua.nure.finance.reposotory.AssetRepository;
import ua.nure.finance.reposotory.AssetsCategoryRepository;
import ua.nure.finance.reposotory.CurrencyRepository;

@Controller
@RequestMapping("/assets")
public class AssetsController {

    @Autowired
    private AssetsCategoryRepository categoryRepository;
    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private CurrencyRepository currencyRepository;

    // Show all categories
    @GetMapping
    public String listAssets(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("asset", new Asset()); // for add form
        model.addAttribute("assets", assetRepository.findAll()); // for add form
        return "assets"; // Thymeleaf template name
    }


    @GetMapping("/add-asset")
    public String addAssetForm(Model model, Asset asset) {
        Currency defaultCurrency = new Currency();
        defaultCurrency.setCurrencyCode("UAH");
        asset.setCurrency(defaultCurrency);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("currencies", currencyRepository.findAll());
        return "add-asset";
    }

    @PostMapping("/add-asset")
    public String addAsset(@Valid Asset asset, BindingResult result) {
        if (result.hasErrors()) {
            return "add-asset";
        }

        assetRepository.save(asset);
        return "redirect:/assets";
    }

}