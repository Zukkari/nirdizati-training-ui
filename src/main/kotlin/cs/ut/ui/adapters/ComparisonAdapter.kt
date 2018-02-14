package cs.ut.ui.adapters

import cs.ut.jobs.SimulationJob
import cs.ut.ui.FieldComponent
import cs.ut.ui.GridValueProvider
import cs.ut.ui.controllers.validation.SingleJobValidationController
import cs.ut.util.NirdizatiUtil
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.event.CheckEvent
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.A
import org.zkoss.zul.Checkbox
import org.zkoss.zul.Label
import org.zkoss.zul.Row

class ComparisonAdapter(container: Component) :
    GridValueProvider<SimulationJob, Row> {
    override var fields: MutableList<FieldComponent> = mutableListOf()
    private val adapter = ValidationViewAdapter(null, container)

    private var first = true

    override fun provide(data: SimulationJob): Row {
        return Row().apply {
            this.appendChild(Checkbox().apply {
                this.isChecked = first
                this.isDisabled = first
                if (first) first = false

                this.addEventListener(Events.ON_CHECK, { e ->
                    e as CheckEvent
                    if (e.isChecked) {
                        addDataSet("")
                    } else {
                        removeDataSet("")
                    }
                })
            })
            this.appendChild(Label(NirdizatiUtil.localizeText("${data.bucketing.type}.${data.bucketing.id}")))
            this.appendChild(Label(NirdizatiUtil.localizeText("${data.encoding.type}.${data.encoding.id}")))
            this.appendChild(Label(NirdizatiUtil.localizeText("${data.learner.type}.${data.learner.id}")))
            this.appendChild(A().apply {
                adapter.loadTooltip(this, data)
            })
        }
    }



    fun addDataSet(payload: String) {
        Clients.evalJavaScript("addDataSet('', '$payload')")
    }

    fun removeDataSet(payload: String) {
        Clients.evalJavaScript("removeDataSet('$payload')")
    }
}