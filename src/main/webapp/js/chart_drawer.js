Chart.defaults.global.legend.display = false;

const graphContainer = "graph-container";
const canvas = "chart_canvas";

function prepareGraphContainer(isHeatMap) {
    let container = document.getElementById(graphContainer);
    if (isHeatMap) {
        let canvas = document.getElementById(canvas);
        container.removeChild(canvas);
    } else {
        let c = document.createElement("canvas");
        c.setAttribute("id", canvas);
        container.appendChild(c);
    }
}

const getCanvasContext = () => {
    let canvas = document.getElementById('chart_canvas');
    return canvas.getContext('2d');
};

const linerDataSetData = (payload, chart_label) => {
    return [{
        label: chart_label,
        data: JSON.parse(payload),
        borderColor: 'rgba(0, 147, 249, 0.4)',
        backgroundColor: 'rgba(0, 147, 249, 0.2)',
        fill: false
    }]
};

const scalesData = (xLabel, yLabel) => {
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
};

const generateLabels = (n_of_events) => {
    let labels = [];
    for (let i = 1; i <= n_of_events; i++) {
        labels.push(i.toString())
    }
    return labels
};

function scatterPlot(payload, chart_label) {
    prepareGraphContainer(false);
    let ctx = getCanvasContext();
    Chart.Scatter(ctx, {
        data: {
            datasets: linerDataSetData(payload, chart_label)
        },
        options: {
            scales: scalesData('Actual', 'Predicted'),
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

function lineChart(payload, chart_label, n_of_events, axis_label) {
    prepareGraphContainer(false);
    let ctx = getCanvasContext();
    Chart.Line(ctx, {
        data: {
            datasets: linerDataSetData(payload, chart_label),
            labels: generateLabels(n_of_events)
        },
        options: {
            elements: {
                line: {
                    tension: 0
                }
            },
            scales: scalesData('Number of events', axis_label)
        }
    })
}

function barChart(payload, chart_label, labels) {
    prepareGraphContainer(false);
    let ctx = getCanvasContext();
    new Chart(ctx, {
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
            responsive: true
        }
    })
}

function heatMap(payload, title, xLabels, yLabels) {
    prepareGraphContainer(true);
    Highcharts.chart(graphContainer, {
        chart: {
            type: 'heatmap',
            plotBorderWidth: 1
        },

        title: {
            text: title
        },

        xAxis: {
            categories: JSON.parse(xLabels)
        },

        yAxis: {
            categories: JSON.parse(yLabels),
            title: null
        },

        colorAxis: {
            min: 0,
            minColor: '#FFFFFF',
            maxColor: Highcharts.getOptions().colors[0]
        },

        legend: {
            align: 'right',
            layout: 'vertical',
            margin: 0,
            verticalAlign: 'top',
            y: 25,
            symbolHeight: 280
        },

        tooltip: {
            formatter: () => {
                `Predicted <b>${this.series.xAxis.categories[this.point.x]} ${this.point.x}</b> times when correct category was <b>${this.series.yAxis.categories[this.point.y]}</b>`
            }
        },

        series: [{
            name: title,
            borderWidth: 1,
            data: JSON.parse(payload),
            dataLabels: {
                enabled: true,
                color: '#000'
            }
        }]
    });
}
