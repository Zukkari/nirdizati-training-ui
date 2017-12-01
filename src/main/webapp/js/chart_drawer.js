function scatter_plot(payload, chart_label) {
    var ctx = document.getElementById("chart_canvas").getContext('2d');
    ctx.clear();
    new Chart(ctx, {
        type: 'scatter',
        data: {
            datasets: [{
                label: chart_label,
                data: payload
            }]
        },

        options: {
            scales: {
                xAxes: [{
                    type: 'linear',
                    position: 'bottom'
                }]
            }
        }
    })
}