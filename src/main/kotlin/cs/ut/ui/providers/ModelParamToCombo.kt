package cs.ut.ui.providers

import cs.ut.config.items.ModelParameter
import cs.ut.ui.FieldComponent
import cs.ut.ui.GridValueProvider
import org.zkoss.util.resource.Labels
import org.zkoss.zul.Combobox
import org.zkoss.zul.Label
import org.zkoss.zul.Row

data class GeneratorArgument(val id: String, val params: List<ModelParameter>)

class ModelParamToCombo : GridValueProvider<GeneratorArgument, Row> {
    override var fields: MutableList<FieldComponent> = mutableListOf()

    override fun provide(data: GeneratorArgument): Row {
        val row = Row()

        val label = Label(Labels.getLabel(data.id))

        val comboBox = Combobox()
        comboBox.isReadonly = true
        comboBox.setConstraint("no empty")

        data.params.forEach {
            val comboItem = comboBox.appendItem(Labels.getLabel(it.type + "." + it.id))
            comboItem.setValue(it)
        }

        fields.add(FieldComponent(label, comboBox))
        row.appendChild(label)
        row.appendChild(comboBox)

        return row
    }
}