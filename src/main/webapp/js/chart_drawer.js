var chart = null;

function plot_scatter(payload, chart_label) {
    var canvas = document.getElementById('chart_canvas');
    var ctx = canvas.getContext('2d');
    if (chart != null) chart.destroy();
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

function plot_line(payload, chart_label) {
    var canvas = document.getElementById('chart_canvas');
    var ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    if (chart != null) chart.destroy();
    chart = Chart.Line(ctx, {
        data: {
            datasets: getLinearDatasetData(payload, chart_label),
            labels: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15"]
        },
        options: {
            scales: getScalesData('Number of events', 'Mean average error')
        }
    })
}