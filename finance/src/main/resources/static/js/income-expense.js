document.addEventListener('DOMContentLoaded', function () {
        const currencySelect = document.getElementById('currency');
        const amountField = document.getElementById('amount');
        const amountMainField = document.getElementById('amountMainCurrency');

        function updateAmountMainCurrencyField() {
            const selectedCurrency = currencySelect.value;
            if (selectedCurrency === 'UAH') {
                amountMainField.value = amountField.value;
                amountMainField.readOnly = true;
                amountMainField.setAttribute('tabindex', '-1');  // Prevent focus
            } else {
                amountMainField.readOnly = true;
                amountMainField.removeAttribute('tabindex');
            }
        }

        // Update on currency or amount change
        currencySelect.addEventListener('change', updateAmountMainCurrencyField);
        amountField.addEventListener('input', function () {
            if (currencySelect.value === 'UAH') {
                amountMainField.value = amountField.value;
            }
        });

        // Initial call on load
        updateAmountMainCurrencyField();
    });


$(document).ready(function () {
    $('select.form-select').on('change', function () {
        if ($(this).val()) {
            $(this).removeClass('is-invalid');
        }
    });
});