document.addEventListener('DOMContentLoaded', () => {
    const categoriesTable = document.getElementById('optimization-categories');
    const addCategoryButton = document.getElementById('add-optimization-category');
    const categoryRowTemplate = categoriesTable.rows[0].cloneNode(true);
    const incomeInput = document.getElementById('income');
    const mandatoryExpensesInput = document.getElementById('mandatoryExpenses');
    const availableFundsInput = document.getElementById('availableFunds');
    const fieldLabels = {
        name: 'Категорія',
        limit: 'Обмеження',
        priority: 'Пріоритет',
        desired: 'Бажано',
        optimal: 'Оптимально',
        result: 'Результат',
        deviation: 'Відхилення'
    };

    const toCents = (value) => {
        const match = value.trim().match(/^(-?)(\d+)(?:\.(\d*))?$/);
        if (!match) {
            return null;
        }

        const fraction = match[3] || '';
        let cents = BigInt(match[2]) * 100n
            + BigInt(fraction.padEnd(2, '0').slice(0, 2));
        if (fraction.length > 2 && fraction[2] >= '5') {
            cents += 1n;
        }
        return match[1] === '-' ? -cents : cents;
    };

    const fromCents = (cents) => {
        const sign = cents < 0n ? '-' : '';
        const absoluteCents = cents < 0n ? -cents : cents;
        return `${sign}${absoluteCents / 100n}.${String(absoluteCents % 100n).padStart(2, '0')}`;
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

    const updateAvailableFunds = () => {
        const income = toCents(incomeInput.value);
        const mandatoryExpenses = toCents(mandatoryExpensesInput.value);
        availableFundsInput.value = income === null || mandatoryExpenses === null
            ? ''
            : fromCents(income - mandatoryExpenses);
    };

    const updateOptimalValues = () => {
        const desiredInputs = [...categoriesTable.querySelectorAll('input[name$=".desired"]')];
        const priorityInputs = [...categoriesTable.querySelectorAll('input[name$=".priority"]')];
        const desiredValues = desiredInputs.map((input) =>
            input.value.trim() === '' ? 0n : toCents(input.value));
        const priorities = priorityInputs.map((input) => Number.parseInt(input.value, 10) || 1);
        const availableFunds = toCents(availableFundsInput.value);
        const optimalInputs = [...categoriesTable.querySelectorAll('input[name$=".optimal"]')];

        if (availableFunds === null || desiredValues.some((value) => value === null)) {
            optimalInputs.forEach((input) => {
                input.value = '';
            });
            categoriesTable.querySelectorAll('input[name$=".deviation"]').forEach((input) => {
                input.value = '';
            });
            categoriesTable.querySelectorAll('input[name$=".result"]').forEach((input) => {
                input.value = '';
            });
            document.getElementById('totalOptimal').value = '';
            document.getElementById('totalDeviation').value = '';
            return;
        }

        const optimalValues = [...desiredValues];
        const totalDesired = desiredValues.reduce((sum, value) => sum + value, 0n);
        let remainingReduction = totalDesired - (availableFunds > 0n ? availableFunds : 0n);
        if (remainingReduction < 0n) {
            remainingReduction = 0n;
        }
        let activeIndexes = desiredValues
            .map((value, index) => value > 0n ? index : -1)
            .filter((index) => index >= 0);

        while (remainingReduction > 0n && activeIndexes.length > 0) {
            const weightFor = (index) => BigInt(60 / priorities[index]);
            const totalWeight = activeIndexes.reduce((sum, index) => sum + weightFor(index), 0n);
            const cappedIndexes = activeIndexes.filter((index) =>
                remainingReduction * weightFor(index) >= desiredValues[index] * totalWeight);

            if (cappedIndexes.length === 0) {
                const shares = activeIndexes.map((index) => {
                    const numerator = remainingReduction * weightFor(index);
                    return {
                        index,
                        cents: numerator / totalWeight,
                        remainder: numerator % totalWeight
                    };
                });
                const allocated = shares.reduce((sum, share) => sum + share.cents, 0n);
                const extraCents = Number(remainingReduction - allocated);
                shares.sort((left, right) => {
                    if (left.remainder === right.remainder) {
                        return left.index - right.index;
                    }
                    return left.remainder > right.remainder ? -1 : 1;
                });
                shares.forEach((share, index) => {
                    const reduction = share.cents + (index < extraCents ? 1n : 0n);
                    optimalValues[share.index] = desiredValues[share.index] - reduction;
                });
                remainingReduction = 0n;
            } else {
                cappedIndexes.forEach((index) => {
                    optimalValues[index] = 0n;
                    remainingReduction -= desiredValues[index];
                });
                activeIndexes = activeIndexes.filter((index) => !cappedIndexes.includes(index));
            }
        }

        let totalOptimal = 0n;
        let totalDeviation = 0n;
        optimalInputs.forEach((input, index) => {
            input.value = desiredInputs[index].value.trim() === '' ? '' : fromCents(optimalValues[index]);
            totalOptimal += optimalValues[index];
            const deviationInput = categoriesTable.querySelectorAll('input[name$=".deviation"]')[index];
            const deviation = desiredInputs[index].value.trim() === ''
                ? 0n
                : desiredValues[index] - optimalValues[index];
            deviationInput.value = desiredInputs[index].value.trim() === '' ? '' : fromCents(deviation);
            const resultInput = categoriesTable.querySelectorAll('input[name$=".result"]')[index];
            resultInput.value = desiredInputs[index].value.trim() === ''
                ? ''
                : deviation < 0n ? 'Не викон.' : 'Викон.';
            totalDeviation += deviation;
        });
        document.getElementById('totalOptimal').value = fromCents(totalOptimal);
        document.getElementById('totalDeviation').value = fromCents(totalDeviation);
    };

    updateTotals();
    updateAvailableFunds();
    updateOptimalValues();

    [incomeInput, mandatoryExpensesInput].forEach((input) => {
        input.addEventListener('input', () => {
            updateAvailableFunds();
            updateOptimalValues();
        });
    });

    categoriesTable.addEventListener('input', (event) => {
        if (event.target.matches('input[name$=".limit"], input[name$=".desired"]')) {
            updateTotals();
        }
        if (event.target.matches('input[name$=".desired"], input[name$=".priority"]')) {
            updateOptimalValues();
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
        updateOptimalValues();
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
        updateOptimalValues();
    });
});
