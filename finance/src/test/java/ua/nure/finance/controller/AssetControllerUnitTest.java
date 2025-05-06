package ua.nure.finance.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.nure.finance.controller.AssetController;
import ua.nure.finance.model.*;
import ua.nure.finance.repository.CurrencyRepository;
import ua.nure.finance.repository.ExpenseCategoryRepository;
import ua.nure.finance.repository.IncomeCategoryRepository;
import ua.nure.finance.service.ExpenseService;
import ua.nure.finance.service.IncomeService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetControllerUnitTest {

    @InjectMocks
    private AssetController assetController;

    @Mock
    private IncomeService incomeService;

    @Mock
    private ExpenseService expenseService;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private IncomeCategoryRepository incomeCategoryRepository;

    @Mock
    private ExpenseCategoryRepository expenseCategoryRepository;

    @Test
    void testConfirmImport_WithIncomeAndExpense() {
        // Mock data
        Asset asset = new Asset();
        asset.setId(1L);

        Currency currency = new Currency();
        currency.setCurrencyCode("USD");

        IncomeCategory incomeCategory = new IncomeCategory();
        incomeCategory.setId(100L);

        ExpenseCategory expenseCategory = new ExpenseCategory();
        expenseCategory.setId(200L);

        TransactionView incomeTx = new TransactionView();
        incomeTx.setType("INCOME");
        incomeTx.setOperationDate(LocalDate.now());
        incomeTx.setAsset(asset);
        incomeTx.setCurrency("USD");
        incomeTx.setCategory("100");
        incomeTx.setAmount(new BigDecimal("500.00"));
        incomeTx.setDescription("Salary");

        TransactionView expenseTx = new TransactionView();
        expenseTx.setType("EXPENSE");
        expenseTx.setOperationDate(LocalDate.now());
        expenseTx.setAsset(asset);
        expenseTx.setCurrency("USD");
        expenseTx.setCategory("200");
        expenseTx.setAmount(new BigDecimal("100.00"));
        expenseTx.setDescription("Groceries");

        BankStatementImportDTO dto = new BankStatementImportDTO();
        dto.setOperations(List.of(incomeTx, expenseTx));

        when(currencyRepository.findById("USD")).thenReturn(Optional.of(currency));
        when(incomeCategoryRepository.findById(100L)).thenReturn(Optional.of(incomeCategory));
        when(expenseCategoryRepository.findById(200L)).thenReturn(Optional.of(expenseCategory));

        // Act
        String view = assetController.confirmImport(dto);

        // Assert
        assertEquals("redirect:/transactions", view);
        verify(incomeService).saveIncome(any(Income.class));
        verify(expenseService).saveExpense(any(Expense.class));
    }
}
