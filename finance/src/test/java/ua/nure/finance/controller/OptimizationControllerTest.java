package ua.nure.finance.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.nure.finance.model.OptimizationCategory;
import ua.nure.finance.model.OptimizationTotals;
import ua.nure.finance.repository.OptimizationCategoryRepository;
import ua.nure.finance.repository.OptimizationTotalsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(OptimizationController.class)
class OptimizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OptimizationCategoryRepository categoryRepository;

    @MockBean
    private OptimizationTotalsRepository totalsRepository;

    @Test
    void getOptimizationDisplaysEditableCategoriesAndSaveButton() throws Exception {
        when(categoryRepository.findAllByOrderBySortOrderAscIdAsc()).thenReturn(List.of());

        mockMvc.perform(get("/optimization"))
                .andExpect(status().isOk())
                .andExpect(view().name("optimization"))
                .andExpect(content().string(containsString("Оптимізація")))
                .andExpect(content().string(containsString("Необов&#39;язкові витрати")))
                .andExpect(content().string(containsString("name=\"categories[0].priority\" value=\"1\"")))
                .andExpect(content().string(containsString("max=\"5\"")))
                .andExpect(content().string(containsString("Бажано")))
                .andExpect(content().string(containsString("Зберегти")))
                .andExpect(content().string(containsString("/js/optimization.js")));
    }

    @Test
    void getOptimizationDisplaysPersistedCategoryValues() throws Exception {
        OptimizationCategory category = new OptimizationCategory();
        category.setName("Food");
        category.setSpendingLimit(new java.math.BigDecimal("5000"));
        category.setPriority(2);
        category.setDesired(new java.math.BigDecimal("4500"));
        category.setOptimal(new java.math.BigDecimal("4200"));
        category.setResult("Виконано");
        category.setDeviation(new java.math.BigDecimal("300"));
        when(categoryRepository.findAllByOrderBySortOrderAscIdAsc()).thenReturn(List.of(category));
        OptimizationTotals totals = new OptimizationTotals();
        totals.setId(1L);
        totals.setOptimal(new java.math.BigDecimal("8000"));
        totals.setDeviation(new java.math.BigDecimal("500"));
        when(totalsRepository.findById(1L)).thenReturn(java.util.Optional.of(totals));

        mockMvc.perform(get("/optimization"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"categories[0].name\" value=\"Food\"")))
                .andExpect(content().string(containsString("name=\"categories[0].limit\" value=\"5000\"")))
                .andExpect(content().string(containsString("name=\"categories[0].priority\" value=\"2\"")))
                .andExpect(content().string(containsString("name=\"categories[0].desired\" value=\"4500\"")))
                .andExpect(content().string(containsString("name=\"categories[0].optimal\" value=\"4200\"")))
                .andExpect(content().string(containsString("name=\"categories[0].result\" value=\"Виконано\"")))
                .andExpect(content().string(containsString("name=\"categories[0].deviation\" value=\"300\"")))
                .andExpect(content().string(containsString("name=\"totalOptimal\" value=\"8000\"")))
                .andExpect(content().string(containsString("name=\"totalDeviation\" value=\"500\"")));
    }

    @Test
    void saveOptimizationPersistsCategoriesAndRedirects() throws Exception {
        AtomicReference<List<OptimizationCategory>> savedCategories = new AtomicReference<>();
        when(categoryRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<OptimizationCategory> categories = invocation.getArgument(0);
            savedCategories.set(new ArrayList<>(categories));
            return categories;
        });
        AtomicReference<OptimizationTotals> savedTotals = new AtomicReference<>();
        when(totalsRepository.save(org.mockito.ArgumentMatchers.any(OptimizationTotals.class)))
                .thenAnswer(invocation -> {
                    OptimizationTotals totals = invocation.getArgument(0);
                    savedTotals.set(totals);
                    return totals;
                });

        mockMvc.perform(post("/optimization")
                        .param("categories[0].name", "Продукти")
                        .param("categories[0].limit", "5000")
                        .param("categories[0].priority", "2")
                        .param("categories[0].desired", "4500")
                        .param("categories[0].optimal", "4200")
                        .param("categories[0].result", "Виконано")
                        .param("categories[0].deviation", "300")
                        .param("categories[1].name", "Транспорт")
                        .param("categories[1].priority", "5")
                        .param("totalOptimal", "8000")
                        .param("totalDeviation", "500"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/optimization?saved"));

        verify(categoryRepository).deleteAllInBatch();
        List<OptimizationCategory> categories = savedCategories.get();
        assertEquals(2, categories.size());
        assertEquals("Продукти", categories.get(0).getName());
        assertEquals(0, categories.get(0).getSpendingLimit().compareTo(new java.math.BigDecimal("5000")));
        assertEquals(2, categories.get(0).getPriority());
        assertEquals(0, categories.get(0).getDesired().compareTo(new java.math.BigDecimal("4500")));
        assertEquals(0, categories.get(0).getOptimal().compareTo(new java.math.BigDecimal("4200")));
        assertEquals("Виконано", categories.get(0).getResult());
        assertEquals(0, categories.get(0).getDeviation().compareTo(new java.math.BigDecimal("300")));
        assertEquals(1, categories.get(1).getSortOrder());
        assertEquals(5, categories.get(1).getPriority());
        assertNull(categories.get(1).getSpendingLimit());
        assertEquals(1L, savedTotals.get().getId());
        assertEquals(0, savedTotals.get().getOptimal().compareTo(new java.math.BigDecimal("8000")));
        assertEquals(0, savedTotals.get().getDeviation().compareTo(new java.math.BigDecimal("500")));
    }

    @Test
    void invalidCategoryDoesNotReplaceSavedCategories() throws Exception {
        mockMvc.perform(post("/optimization")
                        .param("categories[0].name", " ")
                        .param("categories[0].limit", "-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("optimization"))
                .andExpect(content().string(containsString("Перевірте назви категорій")));

        verify(categoryRepository, never()).deleteAllInBatch();
        verify(categoryRepository, never()).saveAll(anyList());
    }

    @Test
    void priorityAboveFiveDoesNotSaveCategories() throws Exception {
        mockMvc.perform(post("/optimization")
                        .param("categories[0].name", "Продукти")
                        .param("categories[0].priority", "6"))
                .andExpect(status().isOk())
                .andExpect(view().name("optimization"))
                .andExpect(content().string(containsString("Перевірте назви категорій")));

        verify(categoryRepository, never()).deleteAllInBatch();
        verify(categoryRepository, never()).saveAll(anyList());
    }
}
