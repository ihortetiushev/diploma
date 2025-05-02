function showAddDialog() {
    $("#add-dialog").dialog();
}

function showEditDialog(id, value, keywords) {
    $("#edit-id").prop('value', id);
    $("#edit-name").prop('value', value);
    if (keywords) {
        $("#edit-keywords").prop('value', keywords || '');
    }
    $("#edit-dialog").dialog();
}

function showDeleteDialog(path, id) {
    $("#dialog-confirm").dialog({
      resizable: false,
      height: "auto",
      width: 400,
      modal: true,
      buttons: {
        "Delete": function() {
          $( this ).dialog( "close" );
          $.ajax({
            url: '/' + path + '/' + id,
            type: 'GET',
            success: function() {
              window.location.reload();
            },
            error: function(xhr, status, error) {
              alert("Error deleting record: " + error);
            }
          });
        },
        Cancel: function() {
          $( this ).dialog( "close" );
        }
      }
    });
}