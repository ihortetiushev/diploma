package ua.nure.finance.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ua.nure.finance.model.Asset;
import ua.nure.finance.model.Currency;
import ua.nure.finance.reposotory.AssetRepository;
import ua.nure.finance.reposotory.AssetCategoryRepository;
import ua.nure.finance.reposotory.CurrencyRepository;

import java.util.List;

@Controller
@RequestMapping("/assets")
public class AssetController {

    @Autowired
    private AssetCategoryRepository categoryRepository;
    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private CurrencyRepository currencyRepository;

    @GetMapping
    public String listAssets(
            @RequestParam(value = "categoryFilter", required = false) String categoryFilter,
            @RequestParam(value = "nameFilter", required = false) String nameFilter,
            @RequestParam(value = "currencyFilter", required = false) String currencyFilter,
            @RequestParam(value = "sort", required = false, defaultValue = "name") String sort,
            @RequestParam(value = "direction", required = false, defaultValue = "asc") String direction,
            Model model) {

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

        List<Asset> assets = assetRepository.findAll(Sort.by(sortDirection, sort)).stream()
                .filter(asset -> (categoryFilter == null || asset.getCategory().getName().toLowerCase().contains(categoryFilter.toLowerCase())))
                .filter(asset -> (nameFilter == null || asset.getName().toLowerCase().contains(nameFilter.toLowerCase())))
                .filter(asset -> (currencyFilter == null || asset.getCurrency().getCurrencyCode().toLowerCase().contains(currencyFilter.toLowerCase())))
                .toList();

        model.addAttribute("assets", assets);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("asset", new Asset());

        model.addAttribute("categoryFilter", categoryFilter);
        model.addAttribute("nameFilter", nameFilter);
        model.addAttribute("currencyFilter", currencyFilter);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);

        return "assets";
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
