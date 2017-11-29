package cs.ut.ui.providers

import cs.ut.config.items.ModelParameter
import cs.ut.jobs.Job
import cs.ut.jobs.SimulationJob
import cs.ut.ui.FieldComponent
import cs.ut.ui.GridValueProvider
import org.zkoss.util.resource.Labels
import org.zkoss.zul.Hbox
import org.zkoss.zul.Label
import org.zkoss.zul.Row
import org.zkoss.zul.Vlayout

class JobValueProvider(val parent: Hbox) : GridValueProvider<Job, Row> {
    override var fields: MutableList<FieldComponent> = mutableListOf()

    override fun provide(data: Job): Row {
        val status = Label(data.status.name)

        val row = Row()
        val label = formJobLabel(data)

        row.appendChild(label)
        row.appendChild(status)
        row.setValue(data)

        fields.add(FieldComponent(label, status))

        return row
    }

    private fun formJobLabel(job: Job): Vlayout {
        job as SimulationJob

        val encoding = job.encoding
        val bucketing = job.bucketing
        val learner = job.learner

        val label = Label(Labels.getLabel(encoding.type + "." + encoding.id) + " " +
                Labels.getLabel(bucketing.type + "." + bucketing.id) + " " +
                Labels.getLabel(learner.type + "." + learner.id))
        label.style = "font-weight: bold;"

        val bottom = formHyperparamRow(learner)

        val vlayout = Vlayout()
        vlayout.appendChild(label)
        vlayout.appendChild(bottom)

        return vlayout
    }

    private fun formHyperparamRow(learner: ModelParameter): Label {
        var label = ""
        val iterator = learner.properties.iterator()

        while (iterator.hasNext()) {
            val prop = iterator.next()
            label += Labels.getLabel("property." + prop.id) + ": " + prop.property + "\n"
        }

        val res = Label(label)
        res.style = "font-size: 8px"
        res.isMultiline = true
        res.isPre = true
        return res
    }
}