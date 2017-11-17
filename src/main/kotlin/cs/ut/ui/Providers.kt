package cs.ut.ui

import cs.ut.config.items.ModelParameter
import cs.ut.config.items.Property
import cs.ut.exceptions.NirdizatiRuntimeException
import cs.ut.jobs.Job
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.WrongValueException
import org.zkoss.zul.*
import org.zkoss.zul.impl.InputElement

interface GridValueProvider<T, Row> {
    var fields: MutableList<Component>

    fun provide(data: T): Row
}

class PropertyValueProvider : GridValueProvider<Property, Row> {
    override var fields: MutableList<Component> = mutableListOf()

    override fun provide(data: Property): Row {
        val row = Row()

        row.appendChild(Label(Labels.getLabel("property." + data.id)))
        row.appendChild(generateControl(data))

        return row
    }

    private fun generateControl(prop: Property): Component {
        val obj = Class.forName(prop.type).getConstructor().newInstance()

        when (obj) {
            is Intbox -> obj.value = prop.property.toInt()
            is Doublebox -> obj.setValue(prop.property.toDouble())
            else -> NirdizatiRuntimeException("Uknown element $prop")
        }

        fields.add(obj as InputElement)
        obj.setConstraint("no empty")
        obj.id = prop.id

        return obj
    }
}

class ModelValueProvider(val valueList: List<Any>) : GridValueProvider<ModelParameter, Row> {
    override var fields: MutableList<Component> = mutableListOf()

    override fun provide(data: ModelParameter): Row {
        TODO()
    }
}

class ColumnRowValueProvider(private val valueList: List<String>,private val identifiedCols: Map<String, String>) : GridValueProvider<String, Row> {
    override var fields: MutableList<Component> = mutableListOf()

    override fun provide(data: String): Row {
        val row = Row()
        row.appendChild(Label(Labels.getLabel("modals.param." + data)))
        val combobox = Combobox()

        val identified = identifiedCols.get(data)

        combobox.id = data
        combobox.isReadonly = true
        combobox.setConstraint("no empty")

        valueList.forEach {
            val comboItem = combobox.appendItem(it)
            comboItem.setValue(it)

            if (it == identified) combobox.selectedItem = comboItem
        }

        try {
            combobox.selectedItem
        } catch (e: WrongValueException) {
            combobox.selectedItem = (combobox.getItemAtIndex(0))
        }

        fields.add(combobox)
        row.appendChild(combobox)

        return row
    }
}

class JobValueProvider : GridValueProvider<Job, Row> {
    override var fields: MutableList<Component> = mutableListOf()

    override fun provide(data: Job): Row {
        TODO()
    }
}