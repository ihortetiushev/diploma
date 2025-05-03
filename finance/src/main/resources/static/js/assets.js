$(document).ready(function () {
    function toggleFields() {
        const selectedText = $('#category option:selected').text().trim();
        const isStock = selectedText === 'Акції';

        $('#stockFields').toggleClass('d-none', !isStock);
        $('#currentValueGroup').toggleClass('d-none', isStock);
        $('#nameLabel').text(isStock ? 'Ticker' : 'Name');
        if (isStock) {
            $('#currentValue').removeAttr('required');
        } else {
            $('#currentValue').attr('required', 'required');
        }
    }

    $('#category').on('change', toggleFields);
    // Initial check on page load
    toggleFields();
});