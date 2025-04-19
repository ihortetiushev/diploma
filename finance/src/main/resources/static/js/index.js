google.charts.load('current', {'packages': ['corechart']});
google.charts.setOnLoadCallback(drawChart);

function drawChart() {
    const data = new google.visualization.DataTable();
    data.addColumn('string', 'Topping');
    data.addColumn('number', 'Slices');
    data.addRows(chartData);
    const options = {
        title: 'Balance',
        width: 400,
        height: 300
    };

    const chart = new google.visualization.PieChart(document.getElementById('chart_div'));
    chart.draw(data, options);
}

$( document ).ready(function() {
    if (activeButton) {
       $('#'+activeButton).addClass('active')
    }
});

function expenses() {
     $('#expenses-button').addClass('active')

     $('#income-button').removeClass('active')
     $('#categories-button').removeClass('active')
     $('#statistics-button').removeClass('active')
     window.location.href='/'
}

function income() {
     $('#income-button').addClass('active')

     $('#expenses-button').removeClass('active')
     $('#categories-button').removeClass('active')
     $('#statistics-button').removeClass('active')
     window.location.href='/income'
}

function statistics() {
     $('#statistics-button').addClass('active')

     $('#income-button').removeClass('active')
     $('#expenses-button').removeClass('active')
     $('#categories-button').removeClass('active')
}

function categories() {
     $('#categories-button').addClass('active')

     $('#income-button').removeClass('active')
     $('#expenses-button').removeClass('active')
     $('#statistics-button').removeClass('active')
}