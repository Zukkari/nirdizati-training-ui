package cs.ut.ui.adapters

import cs.ut.ui.FieldComponent
import cs.ut.ui.GridValueProvider
import cs.ut.util.COMP_ID
import cs.ut.util.IdentColumns
import cs.ut.util.NirdizatiTranslator
import org.zkoss.zul.Combobox
import org.zkoss.zul.Label
import org.zkoss.zul.Row

/**
 * Adapter that is used when generating data set parameter modal
 */
class ColumnRowValueAdapter(private val valueList: List<String>, private val identifiedCols: Map<String, String>) :
    GridValueProvider<String, Row> {
    override var fields: MutableList<FieldComponent> = mutableListOf()

    override fun provide(data: String): Row {
        val row = Row()

        val label = Label(NirdizatiTranslator.localizeText("modals.param." + data))
        label.setAttribute(COMP_ID, data)
        label.sclass = "param-modal-label"

        val comboBox = Combobox()

        val identified = identifiedCols[data]
        comboBox.isReadonly = true
        comboBox.setConstraint("no empty")

        valueList.forEach {
            val comboItem = comboBox.appendItem(it)
            comboItem.setValue(it)

            if (it == identified) comboBox.selectedItem = comboItem
        }

        // Add empty value as well if resource column is not present
        if (data == IdentColumns.RESOURCE.value) {
            comboBox.appendItem(NirdizatiTranslator.localizeText(NO_RESOURCE)).setValue("")
        }


        try {
            comboBox.selectedItem
        } catch (e: Throwable) {
            comboBox.selectedItem = (comboBox.getItemAtIndex(0))
        }

        fields.add(FieldComponent(label, comboBox))
        row.appendChild(label)
        row.appendChild(comboBox)

        return row
    }

    companion object {
        private const val NO_RESOURCE = "modals.param.no_resource"
    }
}