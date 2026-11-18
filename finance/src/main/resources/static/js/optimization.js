document.addEventListener('DOMContentLoaded', () => {
    const categoriesTable = document.getElementById('optimization-categories');
    const addCategoryButton = document.getElementById('add-optimization-category');
    const categoryRowTemplate = categoriesTable.rows[0].cloneNode(true);
    const fieldLabels = {
        name: 'Категорія',
        limit: 'Обмеження',
        priority: 'Пріоритет',
        desired: 'Бажано',
        optimal: 'Оптимально',
        result: 'Результат',
        deviation: 'Відхилення'
    };

    const updateTotals = () => {
        ['limit', 'desired'].forEach((field) => {
            const values = [...categoriesTable.querySelectorAll(`input[name$=".${field}"]`)]
                .map((input) => input.value.trim())
                .filter((value) => value !== '')
                .map(Number)
                .filter(Number.isFinite);
            const totalCell = document.querySelector(`[data-total-for="${field}"]`);
            totalCell.textContent = values.length
                ? String(values.reduce((sum, value) => sum + value, 0))
                : '—';
        });
    };

    updateTotals();

    categoriesTable.addEventListener('input', (event) => {
        if (event.target.matches('input[name$=".limit"], input[name$=".desired"]')) {
            updateTotals();
        }
    });

    addCategoryButton.addEventListener('click', () => {
        const categoryIndex = categoriesTable.rows.length;
        const newRow = categoryRowTemplate.cloneNode(true);

        newRow.querySelectorAll('input').forEach((input) => {
            const fieldName = input.name.match(/\.([^.]+)$/)[1];
            input.name = input.name.replace(/categories\[\d+\]/, `categories[${categoryIndex}]`);
            input.value = fieldName === 'priority' ? '1' : '';
            input.setAttribute('aria-label', `${fieldLabels[fieldName]}: нова категорія`);
        });

        categoriesTable.appendChild(newRow);
        updateTotals();
        newRow.querySelector('input[type="text"]').focus();
    });

    categoriesTable.addEventListener('click', (event) => {
        const deleteButton = event.target.closest('.delete-optimization-category');
        if (!deleteButton) {
            return;
        }

        deleteButton.closest('tr').remove();
        categoriesTable.querySelectorAll('tr').forEach((row, categoryIndex) => {
            row.querySelectorAll('input').forEach((input) => {
                input.name = input.name.replace(/categories\[\d+\]/, `categories[${categoryIndex}]`);
            });
        });
        updateTotals();
    });
});
