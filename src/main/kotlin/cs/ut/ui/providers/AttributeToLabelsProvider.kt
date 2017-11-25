package cs.ut.ui.providers

import cs.ut.config.items.ModelParameter
import cs.ut.exceptions.NirdizatiRuntimeException
import cs.ut.jobs.JobStatus
import cs.ut.ui.FieldComponent
import cs.ut.ui.GridValueProvider
import org.zkoss.util.resource.Labels
import org.zkoss.zul.Label
import org.zkoss.zul.Row
import java.io.File

class AttributeToLabelsProvider : GridValueProvider<Any, Row> {
    override var fields: MutableList<FieldComponent> = mutableListOf()

    override fun provide(data: Any): Row {
        val row = Row()

        when (data) {
            is Map<*, *> -> {
                val entry = data.entries.first()
                val label = Label(Labels.getLabel("attribute." + entry.key as String))

                val entryVal = entry.value
                val value = when (entryVal) {
                    is File -> Label(entryVal.name)
                    else -> Label(entryVal.toString())
                }

                fields.add(FieldComponent(label, value))
                row.appendChild(label)
                row.appendChild(value)

                return row
            }

            is ModelParameter -> {
                val label = Label(Labels.getLabel(data.type))
                val value = Label(Labels.getLabel(data.type + "." + data.id))

                fields.add(FieldComponent(label, value))

                row.appendChild(label)
                row.appendChild(value)

                return row
            }

            is JobStatus -> {
                val label = Label(Labels.getLabel("attribute.job_status"))
                val value = Label(data.name)

                fields.add(FieldComponent(label, value))
                row.appendChild(label)
                row.appendChild(value)

                return row
            }

            else -> throw NirdizatiRuntimeException("Unsupported parameter: $data")
        }
    }
}