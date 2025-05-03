package ua.nure.finance.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.nure.finance.model.*;
import ua.nure.finance.repository.*;
import ua.nure.finance.service.ExpenseService;
import ua.nure.finance.service.IncomeService;
import ua.nure.finance.service.PrivatbankDataImport;
import ua.nure.finance.service.StockPriceService;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/assets")
public class AssetController {

    @Autowired
    private AssetCategoryRepository categoryRepository;
    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private PrivatbankDataImport privatbankDataImport;
    @Autowired
    private IncomeCategoryRepository incomeCategoryRepository;
    @Autowired
    private ExpenseCategoryRepository expenseCategoryRepository;
    @Autowired
    private IncomeService incomeService;
    @Autowired
    private ExpenseService expenseService;
    @Autowired
    private StockPriceService stockPriceService;

    @GetMapping
    public String listAssets(
            @RequestParam(value = "categoryFilter", required = false) String categoryFilter,
            @RequestParam(value = "nameFilter", required = false) String nameFilter,
            @RequestParam(value = "currencyFilter", required = false) String currencyFilter,
            @RequestParam(value = "sort", required = false, defaultValue = "name") String sort,
            @RequestParam(value = "direction", required = false, defaultValue = "asc") String direction,
            Model model) {

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

        List<Asset> assets = assetRepository.findByStatus(Asset.Status.active, Sort.by(sortDirection, sort)).stream()
                .filter(asset -> (categoryFilter == null || asset.getCategory().getName().toLowerCase().contains(categoryFilter.toLowerCase())))
                .filter(asset -> (nameFilter == null || asset.getName().toLowerCase().contains(nameFilter.toLowerCase())))
                .filter(asset -> (currencyFilter == null || asset.getCurrency().getCurrencyCode().toLowerCase().contains(currencyFilter.toLowerCase())))
                .peek(asset -> {
                    if (asset.getInitialPricePerShare() != null && asset.getQuantity() != null) {
                        BigDecimal price = stockPriceService.fetchCurrentPrice(asset.getName());
                        if (price != null) {
                            BigDecimal calculatedCurrentValue = price.multiply(new BigDecimal(asset.getQuantity()));
                            asset.setCurrentValue(calculatedCurrentValue);
                            asset.setLastValuatedDate(LocalDateTime.now());
                            assetRepository.save(asset);
                        }
                    }
                })
                .toList();

        Map<String, BigDecimal[]> totalsByCurrency = new HashMap<>();
        for (Asset asset : assets) {
            String currencyCode = asset.getCurrency().getCurrencyCode();
            BigDecimal initial = asset.getInitialValue() != null ? asset.getInitialValue() : BigDecimal.ZERO;
            BigDecimal current = asset.getCurrentValue() != null ? asset.getCurrentValue() : BigDecimal.ZERO;

            totalsByCurrency.computeIfAbsent(currencyCode, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            totalsByCurrency.get(currencyCode)[0] = totalsByCurrency.get(currencyCode)[0].add(initial);
            totalsByCurrency.get(currencyCode)[1] = totalsByCurrency.get(currencyCode)[1].add(current);
        }

        model.addAttribute("assets", assets);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("asset", new Asset());

        model.addAttribute("categoryFilter", categoryFilter);
        model.addAttribute("nameFilter", nameFilter);
        model.addAttribute("currencyFilter", currencyFilter);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);

        model.addAttribute("totalsByCurrency", totalsByCurrency);

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

    @GetMapping("/edit/{id}")
    public String updateAssetForm(@PathVariable Long id, Model model) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid asset id Id:" + id));
        model.addAttribute("asset", asset);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("currencies", currencyRepository.findAll());
        return "update-asset";
    }

    @PostMapping("/edit/{id}")
    public String updateAsset(@PathVariable Long id, @ModelAttribute("asset") Asset updatedAsset,
                              BindingResult result) {
        if (result.hasErrors()) {
            updatedAsset.setId(id);
            return "update-asset";
        }

        assetRepository.save(updatedAsset);
        return "redirect:/assets";
    }

    @PostMapping("/close/{id}")
    public String closeAsset(@PathVariable Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid asset Id:" + id));
        asset.setStatus(Asset.Status.closed);
        assetRepository.save(asset);
        return "redirect:/assets";
    }

    @PostMapping("/import")
    public String handleImport(@RequestParam("file") MultipartFile file,
                               @RequestParam("fileType") ImportType importType,
                               @RequestParam("assetId") Long assetId, Model model) throws IOException {
        switch (importType) {
            case privatbank -> processPrivat(file, assetId, model);
            case monobank -> processMono(file, assetId, model);
        }

        return "import-bank-statement";
    }

    private void processPrivat(MultipartFile file, Long assetId, Model model) throws IOException {
        BankStatementImportDTO importDTO = privatbankDataImport.importData(file.getInputStream(), assetId);

        model.addAttribute("importDTO", importDTO);
        model.addAttribute("incomeCategories", incomeCategoryRepository.findAll());
        model.addAttribute("expenseCategories", expenseCategoryRepository.findAll());
    }

    private void processMono(MultipartFile file, Long assetId, Model model) {

    }

    @PostMapping("/import/confirm")
    public String confirmImport(@ModelAttribute BankStatementImportDTO importDTO) {
        for (TransactionView op : importDTO.getOperations()) {
            if (TransactionType.INCOME.name().equals(op.getType())) {
                Income income = new Income();
                income.setOperationDate(op.getOperationDate());
                income.setAsset(op.getAsset());
                income.setCategory(incomeCategoryRepository.findById(Long.valueOf(op.getCategory())).get());
                income.setCurrency(currencyRepository.findById(op.getCurrency()).get());
                income.setAmount(op.getAmount());
                income.setAmountMainCurrency(op.getAmount());
                income.setDescription(op.getDescription());
                incomeService.saveIncome(income);
            } else {
                Expense expense = new Expense();
                expense.setOperationDate(op.getOperationDate());
                expense.setAsset(op.getAsset());
                expense.setCategory(expenseCategoryRepository.findById(Long.valueOf(op.getCategory())).get());
                expense.setCurrency(currencyRepository.findById(op.getCurrency()).get());
                expense.setAmount(op.getAmount());
                expense.setAmountMainCurrency(op.getAmount());
                expense.setDescription(op.getDescription());
                expenseService.saveExpense(expense);
            }
        }
        return "redirect:/transactions";
    }
}
