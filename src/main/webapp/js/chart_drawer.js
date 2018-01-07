let chart = null;
Chart.defaults.global.legend.display = false;

function destroyPlot() {
    const canvas = document.getElementById('chart_canvas');
    const ctx = canvas.getContext('2d');
    if (chart != null) chart.destroy();
    return ctx;
}

function plot_scatter(payload, chart_label) {
    const ctx = destroyPlot();
    chart = Chart.Scatter(ctx, {
        data: {
            datasets: getLinearDatasetData(payload, chart_label)
        },
        options: {
            scales: getScalesData('Actual', 'Predicted'),
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

function getLinearDatasetData(payload, chart_label, labels) {
    return [{
        label: chart_label,
        data: JSON.parse(payload),
        borderColor: 'rgba(0, 147, 249, 0.4)',
        backgroundColor: 'rgba(0, 147, 249, 0.2)',
        fill: false
    }]
}

function getScalesData(xLabel, yLabel) {
    return {
        xAxes: [{
            display: true,
            scaleLabel: {
                display: true,
                labelString: xLabel,
                fontSize: 18,
                fontStyle: 'bold'
            }
        }],
        yAxes: [{
            display: true,
            scaleLabel: {
                display: true,
                labelString: yLabel,
                fontSize: 18,
                fontStyle: 'bold'
            }
        }]
    }
}

function plot_line(payload, chart_label, n_of_events, axis_label) {
    const ctx = destroyPlot();
    chart = Chart.Line(ctx, {
        data: {
            datasets: getLinearDatasetData(payload, chart_label),
            labels: generateLabels(n_of_events)
        },
        options: {
            elements: {
                line: {
                    tension: 0
                }
            },
            scales: getScalesData('Number of events', axis_label)
        }
    })
}

function plot_bar(payload, chart_label, labels) {
    const ctx = destroyPlot();
    chart = new Chart(ctx, {
        type: 'horizontalBar',
        data: {
            labels: JSON.parse(labels),
            datasets: [
                {
                    label: chart_label,
                    data: JSON.parse(payload),
                    backgroundColor: 'rgba(0, 147, 249, 0.4)',
                    borderColor: 'rgba(0, 147, 249, 0.2)',
                    borderWidth: 1
                }
            ]
        },

        options: {
            elements: {
                rectangle: {
                    borderWidth: 2
                }
            },
            reponsive: true
        }
    })
}

function plot_pie(payload) {
    const ctx = destroyPlot();
    chart = new Chart(ctx, {
        type: 'pie',
        data: {
            datasets: [{
                data : JSON.parse(payload),
                backgroundColor: [
                    'rgba(0, 147, 249, 0.4)',
                    'rgba(255, 102, 102, 0.7)'
                ]
            }],
            labels: [
                'Correct prediction',
                'Wrong prediction'
            ]
        },
        options: {
            responsive: true,
            title: {
                display: false
            },
            animation: {
                animateRotate: true,
                animateScale: true
            }
        }
    })
}

function generateLabels(n_of_events) {
    var labels = [];
    for (var i = 1; i <= n_of_events; i++) {
        labels.push(i.toString())
    }
    return labels
}