function plot_scatter(payload, chart_label) {
    var ctx = document.getElementById('chart_canvas').getContext('2d');
    Chart.Scatter(ctx, {
        data: {
            datasets: [{
                label: chart_label,
                data: JSON.parse(payload),
                borderColor: 'rgba(0, 147, 249, 0.4)',
                backgroundColor: 'rgba(0, 147, 249, 0.2)'
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
}