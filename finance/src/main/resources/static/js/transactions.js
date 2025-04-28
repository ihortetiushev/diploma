function showDeleteDialog(event, type, id) {
    event.preventDefault();
    var path = 'delete-expenses';
    if (type === 'INCOME') {
        path = 'delete-income';
    }
    $("#dialog-confirm").dialog({
      resizable: false,
      height: "auto",
      width: 400,
      modal: true,
      buttons: {
        "Delete": function() {
          $(this).dialog("close");
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
          $(this).dialog("close");
        }
      }
    });
}
