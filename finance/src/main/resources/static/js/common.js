function setFormAction(action) {
     document.getElementById('searchForm').action = action;
     if (typeof document.getElementById('searchForm').submit === "object") {
         document.getElementById('searchForm').submit.remove();
     }
     document.getElementById('searchForm').submit();
}

function cleanEmptyFieldsAndSubmit(form) {
    const inputs = form.querySelectorAll("input, select");
    inputs.forEach(input => {
        if (input.value.trim() === '') {
            input.removeAttribute('name');
        }
    });
    form.submit();
}