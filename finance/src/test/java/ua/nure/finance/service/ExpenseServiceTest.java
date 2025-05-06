package ua.nure.finance.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.nure.finance.model.Asset;
import ua.nure.finance.model.Expense;
import ua.nure.finance.repository.AssetRepository;
import ua.nure.finance.repository.ExpenseRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private ExpenseService expenseService;

    @Test
    void saveExpense_WithAsset_ShouldSaveExpenseAndUpdateAssetValue() {
        // Arrange
        Asset asset = new Asset();
        asset.setId(1L);
        asset.setCurrentValue(new BigDecimal("1000.00"));

        Expense expense = new Expense();
        expense.setId(1L);
        expense.setAmount(new BigDecimal("250.00"));
        expense.setAsset(asset);

        // Act
        expenseService.saveExpense(expense);

        // Assert
        verify(expenseRepository).save(expense);
        assertEquals(new BigDecimal("750.00"), asset.getCurrentValue());
        verify(assetRepository).save(asset);
    }

    @Test
    void saveExpense_WithoutAsset_ShouldOnlySaveExpense() {
        // Arrange
        Expense expense = new Expense();
        expense.setId(2L);
        expense.setAmount(new BigDecimal("99.99"));
        expense.setAsset(null);

        // Act
        expenseService.saveExpense(expense);

        // Assert
        verify(expenseRepository).save(expense);
        verify(assetRepository, never()).save(any());
    }
}
