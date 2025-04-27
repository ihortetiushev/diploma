package ua.nure.finance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ua.nure.finance.model.ExpenseCategory;
import ua.nure.finance.model.IncomeCategory;
import ua.nure.finance.model.TransactionView;
import ua.nure.finance.repository.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class TransactionJournalController {

    @Autowired
    private TransactionViewRepository transactionViewRepository;
    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private IncomeCategoryRepository incomeCategoryRepository;
    @Autowired
    private ExpenseCategoryRepository expenseCategoryRepository;
    @Autowired
    private AssetRepository assetRepository;

    @GetMapping("/transactions")
    public String getTransactions(
            @RequestParam(required = false) String typeFilter,
            @RequestParam(required = false) String categoryFilter,
            @RequestParam(required = false) String currencyFilter,
            @RequestParam(required = false) String descriptionFilter,
            @RequestParam(required = false) Long assetFilter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        endDate = endDate == null ? LocalDate.now() : endDate;
        startDate = startDate == null ? endDate.withDayOfMonth(1) : startDate;
        List<TransactionView> transactions = transactionViewRepository.findByOperationDateBetweenOrderByOperationDateDesc(startDate, endDate);

        //Apply filters only if provided
        if (typeFilter != null && !typeFilter.isBlank()) {
            transactions = transactions.stream()
                    .filter(t -> t.getType() != null && t.getType().equalsIgnoreCase(typeFilter))
                    .toList();
        }

        if (categoryFilter != null && !categoryFilter.isBlank()) {
            transactions = transactions.stream()
                    .filter(t -> t.getCategory() != null && t.getCategory().toLowerCase().contains(categoryFilter.toLowerCase()))
                    .toList();
        }

        if (currencyFilter != null && !currencyFilter.isBlank()) {
            transactions = transactions.stream()
                    .filter(t -> t.getCurrency() != null && t.getCurrency().equalsIgnoreCase(currencyFilter))
                    .toList();
        }

        if (descriptionFilter != null && !descriptionFilter.isBlank()) {
            transactions = transactions.stream()
                    .filter(t -> t.getDescription() != null && t.getDescription().toLowerCase().contains(descriptionFilter.toLowerCase()))
                    .toList();
        }
        if (assetFilter != null ) {
            transactions = transactions.stream()
                    .filter(t -> t.getAsset() != null && t.getAsset().getId() == assetFilter)
                    .toList();
        }

        model.addAttribute("transactions", transactions);
        model.addAttribute("typeFilter", typeFilter);
        model.addAttribute("categoryFilter", categoryFilter);
        model.addAttribute("currencyFilter", currencyFilter);
        model.addAttribute("descriptionFilter", descriptionFilter);
        model.addAttribute("assetFilter", assetFilter);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("currencies", currencyRepository.findAll());
        model.addAttribute("assets", assetRepository.findAll());
        model.addAttribute("categoryNames", getAllCategoriesNames());

        return "transactions";
    }

    private Set<String> getAllCategoriesNames() {
        Iterable<ExpenseCategory> expenseCategories = expenseCategoryRepository.findAll();
        Iterable<IncomeCategory> incomeCategories = incomeCategoryRepository.findAll();
        List<ExpenseCategory> expenseCategoriesList = StreamSupport
                .stream(expenseCategories.spliterator(), false)
                .collect(Collectors.toList());

        List<IncomeCategory> incomeCategoriesList = StreamSupport
                .stream(incomeCategories.spliterator(), false)
                .collect(Collectors.toList());
        Set<String> categoryNames = new HashSet<>();

        for (ExpenseCategory category : expenseCategoriesList) {
            categoryNames.add(category.getName());
        }

        for (IncomeCategory category : incomeCategoriesList) {
            categoryNames.add(category.getName());
        }
        return categoryNames;
    }

}