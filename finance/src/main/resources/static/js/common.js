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

function showConfirmCloseAsserDialog(event, path, id) {
    event.preventDefault();
    $("#dialog-confirm").dialog({
      resizable: false,
      height: "auto",
      width: 400,
      modal: true,
      buttons: {
        "Закрити": function() {
          $( this ).dialog( "close" );
          $.ajax({
            url: '/' + path + '/' + id,
            type: 'POST',
            success: function() {
              window.location.reload();
            },
            error: function(xhr, status, error) {
              alert("Error deleting record: " + error);
            }
          });
        },
        "Скасувати": function() {
          $( this ).dialog( "close" );
        }
      }
    });
}

function showImportDialog(event, id) {
    event.preventDefault();
    document.getElementById("assetId").value = id;
    $("#import-dialog").dialog({
      resizable: false,
      height: "auto",
      width: 400,
      modal: true,
      buttons: {
        "Імпортувати": function () {
             $("#uploadForm").submit(); // Submit the form
        },
        "Скасувати": function () {
            $(this).dialog("close");
        }
      }
    });
}
