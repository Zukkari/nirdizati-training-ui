package cs.ut.ui.providers

import cs.ut.ui.FieldComponent
import cs.ut.ui.GridValueProvider
import org.zkoss.util.resource.Labels
import org.zkoss.zul.Checkbox
import org.zkoss.zul.Label
import org.zkoss.zul.Row

class AdvancedModeProvider : GridValueProvider<GeneratorArgument, Row> {
    override var fields: MutableList<FieldComponent> = mutableListOf()

    override fun provide(data: GeneratorArgument): Row {
        val row = Row()

        val label = Label(Labels.getLabel(data.id))
        row.appendChild(label)

        data.params.forEach {
            val control = Checkbox()
            control.label = Labels.getLabel(it.type + "." + it.id)
            control.id = it.type
            control.setValue(it)
            row.appendChild(control)
            fields.add(FieldComponent(label, control))
        }

        return row
    }
}