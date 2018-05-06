package cs.ut.ui.adapters

import cs.ut.charts.ChartGenerator
import cs.ut.charts.LineChart
import cs.ut.jobs.SimulationJob
import cs.ut.ui.FieldComponent
import cs.ut.ui.GridValueProvider
import cs.ut.ui.controllers.validation.SingleJobValidationController
import cs.ut.util.NirdizatiTranslator
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.event.CheckEvent
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.*

/**
 * Adapter which is used when generating comparison grid in validation view
 */
class ComparisonAdapter(container: Component, private val controller: SingleJobValidationController) :
    GridValueProvider<SimulationJob, Row> {
    override var fields: MutableList<FieldComponent> = mutableListOf()
    private val adapter = ValidationViewAdapter(null, container)

    private var first = true

    override fun provide(data: SimulationJob): Row {
        return Row().apply {
            this.appendChild(Hlayout().apply {
                this.hflex = "1"
                this.vflex = "1"

                this.appendChild(
                    Checkbox().apply {
                        this.isChecked = first
                        this.isDisabled = first
                        this.setValue(data)
                        if (first) first = false

                        this.addEventListener(Events.ON_CHECK, { e ->
                            e as CheckEvent
                            if (e.isChecked) {
                                addDataSet(data.id, getPayload(data, controller.accuracyMode))
                            } else {
                                removeDataSet(data.id)
                            }
                        })
                        controller.checkBoxes.add(this)
                    })

                this.appendChild(Hlayout().apply {
                    this.sclass = "color-box c${data.id}"
                })
            })

            val config = data.configuration

            this.appendChild(Label(NirdizatiTranslator.localizeText("${config.bucketing.type}.${config.bucketing.id}")))
            this.appendChild(Label(NirdizatiTranslator.localizeText("${config.encoding.type}.${config.encoding.id}")))
            this.appendChild(Label(NirdizatiTranslator.localizeText("${config.learner.type}.${config.learner.id}")))
            this.appendChild(A().apply {
                adapter.loadTooltip(this, data)
            })
        }
    }

    companion object {
        /**
         * Get chart payload for a given job
         * @param job to get payload for
         * @param accuracyMode that is used in current controller
         *
         * @return payload for a chart
         */
        fun getPayload(job: SimulationJob, accuracyMode: String): String {
            return ChartGenerator(job).getCharts()
                .first { it::class.java == LineChart::class.java && it.name == accuracyMode }
                .payload
        }

        /**
         * Add data set on client side
         * @param label data set to add
         * @param payload data set payload which will be added
         */
        fun addDataSet(label: String, payload: String) {
            Clients.evalJavaScript("addDataSet('$label', '$payload')")
        }

        /**
         * Remove data set on client side
         * @param label data set to remove
         */
        fun removeDataSet(label: String) {
            Clients.evalJavaScript("removeDataSet('$label')")
        }
    }
}