package cs.ut.ui.providers

import cs.ut.config.items.Property
import cs.ut.exceptions.NirdizatiRuntimeException
import cs.ut.ui.FieldComponent
import cs.ut.ui.GridValueProvider
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Component
import org.zkoss.zul.Doublebox
import org.zkoss.zul.Intbox
import org.zkoss.zul.Label
import org.zkoss.zul.Row
import org.zkoss.zul.impl.InputElement

class PropertyValueProvider : GridValueProvider<Property, Row> {
    override var fields: MutableList<FieldComponent> = mutableListOf()

    override fun provide(data: Property): Row {
        val row = Row()

        val label = Label(Labels.getLabel("property." + data.id))
        val control = generateControl(data)

        fields.add(FieldComponent(label, control))
        row.appendChild(label)
        row.appendChild(control)

        return row
    }

    private fun generateControl(prop: Property): Component {
        val obj = Class.forName(prop.type).getConstructor().newInstance()

        when (obj) {
            is Intbox -> obj.value = prop.property.toInt()
            is Doublebox -> obj.setValue(prop.property.toDouble())
            else -> NirdizatiRuntimeException("Uknown element $prop")
        }

        obj as InputElement
        obj.setConstraint("no empty")
        obj.id = prop.id
        obj.width = "60px"

        return obj
    }
}