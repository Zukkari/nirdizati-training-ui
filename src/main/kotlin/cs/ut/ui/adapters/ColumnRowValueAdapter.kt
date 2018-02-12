package cs.ut.ui.adapters

import cs.ut.ui.FieldComponent
import cs.ut.ui.GridValueProvider
import cs.ut.util.COMP_ID
import cs.ut.util.NirdizatiUtil
import cs.ut.util.RESOURCE_COL
import org.zkoss.zul.Combobox
import org.zkoss.zul.Label
import org.zkoss.zul.Row

class ColumnRowValueAdapter(private val valueList: List<String>, private val identifiedCols: Map<String, String>) :
    GridValueProvider<String, Row> {
    override var fields: MutableList<FieldComponent> = mutableListOf()

    override fun provide(data: String): Row {
        val row = Row()

        val label = Label(NirdizatiUtil.localizeText("modals.param." + data))
        label.setAttribute(COMP_ID, data)
        label.sclass = "param-modal-label"

        val combobox = Combobox()

        val identified = identifiedCols[data]
        combobox.isReadonly = true
        combobox.setConstraint("no empty")

        valueList.forEach {
            val comboItem = combobox.appendItem(it)
            comboItem.setValue(it)

            if (it == identified) combobox.selectedItem = comboItem
        }

        // Add empty value as well if resource column is not present
        if (data == RESOURCE_COL) {
            combobox.appendItem(NirdizatiUtil.localizeText(NO_RESOURCE)).setValue("")
        }


        try {
            combobox.selectedItem
        } catch (e: Throwable) {
            combobox.selectedItem = (combobox.getItemAtIndex(0))
        }

        fields.add(FieldComponent(label, combobox))
        row.appendChild(label)
        row.appendChild(combobox)

        return row
    }

    companion object {
        private const val NO_RESOURCE = "modals.param.no_resource"
    }
}