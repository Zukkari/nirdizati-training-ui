package cs.ut.ui.adapters

import cs.ut.config.items.ModelParameter
import cs.ut.config.items.Property
import cs.ut.exceptions.NirdizatiRuntimeException
import cs.ut.jobs.JobStatus
import cs.ut.ui.FieldComponent
import cs.ut.ui.GridValueProvider
import cs.ut.util.NirdizatiUtil
import org.zkoss.zul.Label
import org.zkoss.zul.Row
import java.io.File

class AttributeToLabelsAdapter : GridValueProvider<Any, Row> {
    override var fields: MutableList<FieldComponent> = mutableListOf()

    override fun provide(data: Any): Row {
        val row = Row()

        when (data) {
            is Map<*, *> -> {
                val entry = data.entries.first()
                val label = Label(NirdizatiUtil.localizeText("attribute." + entry.key as String))

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
                val label = Label(NirdizatiUtil.localizeText(data.type))
                val value = Label(NirdizatiUtil.localizeText(data.type + "." + data.id))

                fields.add(FieldComponent(label, value))

                row.appendChild(label)
                row.appendChild(value)

                return row
            }

            is JobStatus -> {
                val label = Label(NirdizatiUtil.localizeText("attribute.job_status"))
                val value = Label(data.name)

                fields.add(FieldComponent(label, value))
                row.appendChild(label)
                row.appendChild(value)

                return row
            }

            is Property -> {
                val label = Label(NirdizatiUtil.localizeText("property." + data.id))
                val value = Label(data.property)

                fields.add(FieldComponent(label, value))
                row.appendChild(label)
                row.appendChild(value)

                return row
            }

            else -> throw NirdizatiRuntimeException("Unsupported parameter: $data")
        }
    }
}