function showAddDialog() {
    $("#add-dialog").dialog();
}

function showEditDialog(id, value) {
    $("#edit-id").prop('value', id);
    $("#edit-name").prop('value', value);
    $("#edit-dialog").dialog();
}

function showDeleteDialog(id) {
    $("#dialog-confirm").dialog({
      resizable: false,
      height: "auto",
      width: 400,
      modal: true,
      buttons: {
        "Delete": function() {
          $( this ).dialog( "close" );
          $.ajax({
           url: '/delete-income-category/'+id
          });
        },
        Cancel: function() {
          $( this ).dialog( "close" );
        }
      }
    });
}