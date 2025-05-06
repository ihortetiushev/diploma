package ua.nure.finance.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import ua.nure.finance.model.*;
import ua.nure.finance.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionJournalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionViewRepository transactionViewRepository;

    @MockBean
    private CurrencyRepository currencyRepository;

    @MockBean
    private IncomeCategoryRepository incomeCategoryRepository;

    @MockBean
    private ExpenseCategoryRepository expenseCategoryRepository;

    @MockBean
    private AssetRepository assetRepository;

    @Test
    void testGetTransactions_WithNoFilters() throws Exception {
        TransactionView view = new TransactionView();
        view.setId(1L);
        view.setType("INCOME");
        view.setOperationDate(LocalDate.now());
        view.setAmount(BigDecimal.valueOf(100));
        view.setCurrency("USD");
        view.setCategory("Salary");
        view.setDescription("Test income");

        Asset asset = new Asset();
        asset.setId(1L);
        asset.setName("Test Asset");
        view.setAsset(asset);

        when(transactionViewRepository.findByOperationDateBetweenOrderByOperationDateDesc(any(), any()))
                .thenReturn(List.of(view));
        when(currencyRepository.findAll()).thenReturn(Collections.emptyList());
        when(assetRepository.findByStatus(any(), any())).thenReturn(Collections.emptyList());
        when(assetRepository.findByStatusAndCategory_Name(any(), any())).thenReturn(Collections.emptyList());
        when(incomeCategoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(expenseCategoryRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(view().name("transactions"))
                .andExpect(model().attributeExists("transactions"))
                .andExpect(model().attributeExists("currencies"))
                .andExpect(model().attributeExists("assets"))
                .andExpect(model().attributeExists("accounts"))
                .andExpect(model().attributeExists("categoryNames"));
    }

    @Test
    void testGetTransactions_FilterByType() throws Exception {
        TransactionView income = new TransactionView();
        income.setId(1L);
        income.setType("INCOME");
        income.setOperationDate(LocalDate.now());
        income.setCurrency("USD");
        income.setCategory("Salary");
        income.setAmount(BigDecimal.valueOf(100));
        income.setDescription("Monthly salary");
        income.setAsset(new Asset());

        TransactionView expense = new TransactionView();
        expense.setId(2L);
        expense.setType("EXPENSE");
        expense.setOperationDate(LocalDate.now());
        expense.setCurrency("USD");
        expense.setCategory("Food");
        expense.setAmount(BigDecimal.valueOf(50));
        expense.setDescription("Groceries");
        expense.setAsset(new Asset());

        when(transactionViewRepository.findByOperationDateBetweenOrderByOperationDateDesc(any(), any()))
                .thenReturn(List.of(income, expense));
        when(currencyRepository.findAll()).thenReturn(Collections.emptyList());
        when(assetRepository.findByStatus(any(), any())).thenReturn(Collections.emptyList());
        when(assetRepository.findByStatusAndCategory_Name(any(), any())).thenReturn(Collections.emptyList());
        when(incomeCategoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(expenseCategoryRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/transactions").param("typeFilter", "INCOME"))
                .andExpect(status().isOk())
                .andExpect(view().name("transactions"))
                .andExpect(model().attributeExists("transactions"))
                .andExpect(model().attribute("typeFilter", "INCOME"))
                .andExpect(model().attributeExists("currencies"));
    }

    @Test
    void testGetTransactions_FilterByCategory() throws Exception {
        TransactionView view = new TransactionView();
        view.setId(1L);
        view.setType("EXPENSE");
        view.setCategory("Transport");
        view.setCurrency("EUR");
        view.setOperationDate(LocalDate.now());
        view.setAmount(BigDecimal.valueOf(20));
        view.setDescription("Train ticket");
        view.setAsset(new Asset());

        when(transactionViewRepository.findByOperationDateBetweenOrderByOperationDateDesc(any(), any()))
                .thenReturn(List.of(view));
        when(currencyRepository.findAll()).thenReturn(Collections.emptyList());
        when(assetRepository.findByStatus(any(), any())).thenReturn(Collections.emptyList());
        when(assetRepository.findByStatusAndCategory_Name(any(), any())).thenReturn(Collections.emptyList());
        when(incomeCategoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(expenseCategoryRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/transactions").param("categoryFilter", "trans"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("transactions"))
                .andExpect(model().attribute("categoryFilter", "trans"));
    }

}
