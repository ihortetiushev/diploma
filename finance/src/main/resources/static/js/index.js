google.charts.load('current', {'packages': ['corechart']});
google.charts.setOnLoadCallback(drawChart);

function drawChart() {
    const data = new google.visualization.DataTable();
    data.addColumn('string', 'Topping');
    data.addColumn('number', 'Slices');
    data.addRows(chartData);
    const options = {
        width: 450,
        height: 450,
        chartArea: {width: 450, height: 450},
        is3D: true,
        backgroundColor: 'transparent'
    };

    const chart = new google.visualization.PieChart(document.getElementById('chart_div'));
    chart.draw(data, options);
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