package cs.ut.charts

abstract class Chart(val name: String, protected val payload: String) : Renderable {
    companion object {
        const val NAMESPACE = "chart_data."
    }

    open fun getCaption(): String = NAMESPACE + name
}