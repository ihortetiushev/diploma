package ua.nure.finance.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nure.finance.model.Asset;
import ua.nure.finance.model.Expense;
import ua.nure.finance.model.ExpenseCategory;
import ua.nure.finance.model.Income;
import ua.nure.finance.model.IncomeCategory;
import ua.nure.finance.model.OptimizationCategory;
import ua.nure.finance.model.OptimizationTotals;
import ua.nure.finance.repository.AssetCategoryRepository;
import ua.nure.finance.repository.AssetRepository;
import ua.nure.finance.repository.ExpenseCategoryRepository;
import ua.nure.finance.repository.ExpenseRepository;
import ua.nure.finance.repository.IncomeCategoryRepository;
import ua.nure.finance.repository.IncomeRepository;
import ua.nure.finance.repository.OptimizationCategoryRepository;
import ua.nure.finance.repository.OptimizationTotalsRepository;
import ua.nure.finance.service.AiAnalysisService.ChatTurn;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@Service
public class PersonalFinanceAssistantService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final AssetRepository assetRepository;
    private final IncomeCategoryRepository incomeCategoryRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final AssetCategoryRepository assetCategoryRepository;
    private final OptimizationCategoryRepository optimizationCategoryRepository;
    private final OptimizationTotalsRepository optimizationTotalsRepository;
    private final AiAnalysisService aiAnalysisService;
    private final ObjectMapper objectMapper;

    public PersonalFinanceAssistantService(IncomeRepository incomeRepository,
                                           ExpenseRepository expenseRepository,
                                           AssetRepository assetRepository,
                                           IncomeCategoryRepository incomeCategoryRepository,
                                           ExpenseCategoryRepository expenseCategoryRepository,
                                           AssetCategoryRepository assetCategoryRepository,
                                           OptimizationCategoryRepository optimizationCategoryRepository,
                                           OptimizationTotalsRepository optimizationTotalsRepository,
                                           AiAnalysisService aiAnalysisService,
                                           ObjectMapper objectMapper) {
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.assetRepository = assetRepository;
        this.incomeCategoryRepository = incomeCategoryRepository;
        this.expenseCategoryRepository = expenseCategoryRepository;
        this.assetCategoryRepository = assetCategoryRepository;
        this.optimizationCategoryRepository = optimizationCategoryRepository;
        this.optimizationTotalsRepository = optimizationTotalsRepository;
        this.aiAnalysisService = aiAnalysisService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public String answer(List<ChatTurn> history, String question) {
        Map<String, Object> data = Map.of(
                "incomeCategories", incomeCategoryRepository.findAll().stream()
                        .map(category -> new CategoryData(category.getName(), category.getKeywords())).toList(),
                "expenseCategories", expenseCategoryRepository.findAll().stream()
                        .map(category -> new CategoryData(category.getName(), category.getKeywords())).toList(),
                "assetCategories", assetCategoryRepository.findAll().stream()
                        .map(category -> category.getName()).toList(),
                "incomes", toList(incomeRepository.findAll()).stream().map(this::incomeData).toList(),
                "expenses", toList(expenseRepository.findAll()).stream().map(this::expenseData).toList(),
                "assets", assetRepository.findAll().stream().map(this::assetData).toList(),
                "optimizationCategories", optimizationCategoryRepository.findAllByOrderBySortOrderAscIdAsc()
                        .stream().map(this::optimizationCategoryData).toList(),
                "optimizationTotals", optimizationTotalsRepository.findAll().stream()
                        .map(this::optimizationTotalsData).toList());
        try {
            return aiAnalysisService.chat(objectMapper.writeValueAsString(data), history, question);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Не вдалося підготувати фінансові дані для аналізу.", exception);
        }
    }

    private IncomeData incomeData(Income income) {
        return new IncomeData(income.getOperationDate(), income.getCategoryName(), income.getAmount(),
                income.getCurrency().getCurrencyCode(), income.getAmountMainCurrency(), income.getDescription(),
                income.getAsset().getName());
    }

    private ExpenseData expenseData(Expense expense) {
        return new ExpenseData(expense.getOperationDate(), expense.getCategoryName(), expense.getAmount(),
                expense.getCurrency().getCurrencyCode(), expense.getAmountMainCurrency(), expense.getDescription(),
                expense.getAsset().getName());
    }

    private AssetData assetData(Asset asset) {
        return new AssetData(asset.getName(), asset.getCategory().getName(), asset.getDescription(),
                asset.getInitialValue(), asset.getCurrentValue(), asset.getCurrency().getCurrencyCode(),
                asset.getStartDate(), asset.getStatus(), asset.isPrimaryAccount(), asset.getInitialPricePerShare(),
                asset.getQuantity(), asset.getStockExchange());
    }

    private OptimizationCategoryData optimizationCategoryData(OptimizationCategory category) {
        return new OptimizationCategoryData(category.getName(), category.getSpendingLimit(), category.getPriority(),
                category.getDesired(), category.getOptimal(), category.getResult(), category.getDeviation(),
                category.getSortOrder());
    }

    private OptimizationTotalsData optimizationTotalsData(OptimizationTotals totals) {
        return new OptimizationTotalsData(totals.getIncome(), totals.getMandatoryExpenses(), totals.getOptimal(),
                totals.getDeviation());
    }

    private <T> List<T> toList(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false).toList();
    }

    private record CategoryData(String name, String keywords) {
    }

    private record IncomeData(LocalDate date, String category, BigDecimal amount, String currency,
                              BigDecimal amountInMainCurrency, String description, String asset) {
    }

    private record ExpenseData(LocalDate date, String category, BigDecimal amount, String currency,
                               BigDecimal amountInMainCurrency, String description, String asset) {
    }

    private record AssetData(String name, String category, String description, BigDecimal initialValue,
                             BigDecimal currentValue, String currency, LocalDate startDate, Asset.Status status,
                             boolean primaryAccount, BigDecimal initialPricePerShare, Integer quantity,
                             String stockExchange) {
    }

    private record OptimizationCategoryData(String name, BigDecimal spendingLimit, Integer priority,
                                            BigDecimal desired, BigDecimal optimal, String result,
                                            BigDecimal deviation, int sortOrder) {
    }

    private record OptimizationTotalsData(BigDecimal income, BigDecimal mandatoryExpenses, BigDecimal optimal,
                                          BigDecimal deviation) {
    }
}
