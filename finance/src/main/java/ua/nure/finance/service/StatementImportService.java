package ua.nure.finance.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.nure.finance.model.ExpenseCategory;
import ua.nure.finance.model.IncomeCategory;
import ua.nure.finance.model.TransactionType;
import ua.nure.finance.repository.ExpenseCategoryRepository;
import ua.nure.finance.repository.IncomeCategoryRepository;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatementImportService {
    private final IncomeCategoryRepository incomeCategoryRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;

    private static final Map<String, List<String>> CATEGORY_KEYWORDS = Map.of(
            "Продукти", List.of("вода", "плов", "novus", "м'ясо", "glovo", "атб", "roshen", "сільпо", "хліб"),
            "Господарчі товари", List.of("аврора", "torgovelnyi center", "aliexpress", "eva"),
            "Розваги", List.of("aliexpress"),
            "Тварини", List.of("zoomagazin")
    );

    public Long assignCategory(String description, TransactionType type) {
        String desc = description.toLowerCase();
        Long id = null;
        for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            String category = entry.getKey();
            List<String> keywords = entry.getValue();

            boolean matched = keywords.stream().anyMatch(desc::contains);
            if (matched) {
                id = getCategoryIdByName(category, type);
                break; // stop after first match
            }
        }
        return id;
    }

    private long getCategoryIdByName(String categoryKey, TransactionType type) {
        long id;
        if (TransactionType.INCOME == type) {
            IncomeCategory category = incomeCategoryRepository.findByName(categoryKey).
                    orElseThrow(() -> new IllegalArgumentException("Invalid income category name: " + categoryKey));
            id = category.getId();
        } else {
            ExpenseCategory category = expenseCategoryRepository.findByName(categoryKey).
                    orElseThrow(() -> new IllegalArgumentException("Invalid expense category name: " + categoryKey));
            id = category.getId();
        }
        return id;
    }
}
