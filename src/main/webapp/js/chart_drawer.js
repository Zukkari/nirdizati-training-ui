function plot_scatter(payload, chart_label) {
    console.log('Started chart rendering...');
    var ctx = document.getElementById('chart_canvas').getContext('2d');
    console.log('Cleared canvas');
    Chart.Scatter(ctx, {
        data: {
            datasets: [{
                label: chart_label,
                data: JSON.parse(payload)
            }]
        },

        options: {
            scales: {
                xAxes: [{
                    display: true,
                    scaleLabel: {
                        display: true,
                        labelString: 'Actual',
                        fontSize: 18,
                        fontStyle: 'bold'
                    }
                }],
                yAxes: [{
                    display: true,
                    scaleLabel: {
                        display: true,
                        labelString: 'Predicted',
                        fontSize: 18,
                        fontStyle: 'bold'
                    }
                }]
            },
            tooltips: {
                callbacks: {
                    label: function (tooltipItem, chart) {
                        return 'Difference: ' + (tooltipItem.xLabel - tooltipItem.yLabel)
                    }
                }
            }
        }
    });
    console.log('Finished chart rendering')
}