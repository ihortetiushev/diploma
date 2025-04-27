package ua.nure.finance.service;

import org.springframework.stereotype.Service;
import ua.nure.finance.model.Asset;
import ua.nure.finance.model.Expense;
import ua.nure.finance.repository.AssetRepository;
import ua.nure.finance.repository.ExpenseRepository;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final AssetRepository assetRepository;

    public ExpenseService(ExpenseRepository expenseRepository, AssetRepository assetRepository) {
        this.expenseRepository = expenseRepository;
        this.assetRepository = assetRepository;
    }

    public void saveExpense(Expense expense) {
        expenseRepository.save(expense);

        if (expense.getAsset() != null) {
            Asset asset = expense.getAsset();
            asset.setCurrentValue(asset.getCurrentValue().subtract(expense.getAmount()));
            assetRepository.save(asset);
        }
    }
}
