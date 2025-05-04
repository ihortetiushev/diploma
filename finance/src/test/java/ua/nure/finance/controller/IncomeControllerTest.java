package ua.nure.finance.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.nure.finance.model.Asset;
import ua.nure.finance.model.Currency;
import ua.nure.finance.model.Income;
import ua.nure.finance.model.IncomeCategory;
import ua.nure.finance.repository.*;
import ua.nure.finance.service.IncomeService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IncomeController.class)
public class IncomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IncomeRepository incomeRepository;
    @MockBean
    private ExpenseRepository expenseRepository;
    @MockBean
    private IncomeCategoryRepository incomeCategoryRepository;
    @MockBean
    private CurrencyRepository currencyRepository;
    @MockBean
    private AssetRepository assetRepository;
    @MockBean
    private IncomeService incomeService;

    private Income createIncome() {
        Income income = new Income();
        income.setId(1L);
        income.setAmount(BigDecimal.TEN);
        income.setAmountMainCurrency(BigDecimal.TEN);
        income.setDescription("Salary");
        income.setOperationDate(LocalDate.now());

        IncomeCategory category = new IncomeCategory();
        category.setId(1L);
        category.setName("Work");
        income.setCategory(category);

        Currency currency = new Currency();
        currency.setCurrencyCode("USD");
        income.setCurrency(currency);

        Asset asset = new Asset();
        asset.setId(1L);
        asset.setName("Card");
        income.setAsset(asset);

        return income;
    }

    @Test
    void testAddIncomeForm() throws Exception {
        when(incomeCategoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(currencyRepository.findAll()).thenReturn(Collections.emptyList());
        when(assetRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/add-income"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-income"));
    }

    @Test
    void testIncomeCategories() throws Exception {
        when(incomeCategoryRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/income-categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("income-categories"));
    }

    @Test
    void testAddIncomeCategory_Valid() throws Exception {
        mockMvc.perform(post("/add-income-category")
                        .param("name", "Bonus"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/income-categories"));
    }

    @Test
    void testUpdateIncomeCategory_Valid() throws Exception {
        mockMvc.perform(post("/update-income-category")
                        .param("name", "Updated Category"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/income-categories"));
    }

    @Test
    void testAddIncome_Invalid() throws Exception {
        mockMvc.perform(post("/add-income")
                        .param("amount", "")  // Missing required field
                )
                .andExpect(status().isOk())
                .andExpect(view().name("add-income"));
    }

    @Test
    void testShowUpdateForm() throws Exception {
        Income income = createIncome();
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(income));
        when(incomeCategoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(currencyRepository.findAll()).thenReturn(Collections.emptyList());
        when(assetRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/income/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-income"))
                .andExpect(model().attributeExists("income"));
    }

    @Test
    void testUpdateIncome_Invalid() throws Exception {
        mockMvc.perform(post("/income/edit/1")
                        .param("amount", "")  // Required field missing
                )
                .andExpect(status().isOk())
                .andExpect(view().name("update-income"));
    }

    @Test
    void testDeleteIncome() throws Exception {
        when(incomeRepository.findById(1L)).thenReturn(Optional.of(createIncome()));

        mockMvc.perform(get("/delete-income/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/transactions"));
    }

    @Test
    void testDeleteIncomeCategory() throws Exception {
        IncomeCategory category = new IncomeCategory();
        category.setId(1L);
        category.setName("Freelance");
        when(incomeCategoryRepository.findById(1L)).thenReturn(Optional.of(category));

        mockMvc.perform(get("/delete-income-category/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/income-categories"));
    }

    @Test
    void testGetIncomeListByDate() throws Exception {
        when(incomeRepository.findByOperationDateBetween(any(), any())).thenReturn(Collections.emptyList());
        when(expenseRepository.findByOperationDateBetween(any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/income"))
                .andExpect(status().isOk())
                .andExpect(view().name("income"));
    }
}
