package ua.nure.finance.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.nure.finance.model.AssetCategory;
import ua.nure.finance.repository.AssetCategoryRepository;

import java.util.Optional;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssetCategoryController.class)
public class AssetCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssetCategoryRepository categoryRepository;

    @Test
    void testListCategories() throws Exception {
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/assets-categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("assets-category"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("category"));
    }

    @Test
    void testSaveCategory_NotReadOnly() throws Exception {
        AssetCategory category = new AssetCategory();
        category.setId(1L);
        category.setName("Cash");
        category.setReadOnly(false);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        mockMvc.perform(post("/assets-categories/save")
                        .param("id", "1")
                        .param("name", "Cash"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/assets-categories"));

        verify(categoryRepository).save(any());
    }

    @Test
    void testSaveCategory_ReadOnly() throws Exception {
        AssetCategory readOnlyCategory = new AssetCategory();
        readOnlyCategory.setId(1L);
        readOnlyCategory.setName("Cash");
        readOnlyCategory.setReadOnly(true);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(readOnlyCategory));

        mockMvc.perform(post("/assets-categories/save")
                        .param("id", "1")
                        .param("name", "Cash"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/assets-categories?error=readonly"));

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void testEditCategory_Found() throws Exception {
        AssetCategory category = new AssetCategory();
        category.setId(1L);
        category.setName("Bank");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/assets-categories/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("assets-category"))
                .andExpect(model().attributeExists("category"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    void testEditCategory_NotFound() throws Exception {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/assets-categories/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("assets-category"))
                .andExpect(model().attributeExists("category"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    void testDeleteCategory_ReadOnly() throws Exception {
        AssetCategory readOnlyCategory = new AssetCategory();
        readOnlyCategory.setId(1L);
        readOnlyCategory.setReadOnly(true);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(readOnlyCategory));

        mockMvc.perform(get("/assets-categories/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/assets-categories?error=readonly"));

        verify(categoryRepository, never()).deleteById(1L);
    }

    @Test
    void testDeleteCategory_NotReadOnly() throws Exception {
        AssetCategory category = new AssetCategory();
        category.setId(1L);
        category.setReadOnly(false);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        mockMvc.perform(get("/assets-categories/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/assets-categories"));

        verify(categoryRepository).deleteById(1L);
    }
}
