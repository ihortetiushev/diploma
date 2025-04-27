function setFormAction(action) {
     document.getElementById('searchForm').action = action;
     if (typeof document.getElementById('searchForm').submit === "object") {
         document.getElementById('searchForm').submit.remove();
     }
     document.getElementById('searchForm').submit();
}