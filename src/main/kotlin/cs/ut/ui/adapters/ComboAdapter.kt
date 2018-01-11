package cs.ut.ui.adapters

import cs.ut.ui.FieldComponent
import cs.ut.ui.GridValueProvider
import cs.ut.util.COMP_ID
import cs.ut.util.NirdizatiUtil
import org.zkoss.zul.Combobox
import org.zkoss.zul.Label
import org.zkoss.zul.Row

data class ComboArgument(val caption: String, val values: List<Any>, val selected: String)

class ComboProvider : GridValueProvider<ComboArgument, Row> {
    override lateinit var fields: MutableList<FieldComponent>

    override fun provide(data: ComboArgument): Row {
        val label = Label(data.caption)
        label.setAttribute(COMP_ID, data.caption)
        label.sclass = "display-block"

        val combobox = Combobox()
        combobox.sclass = "max-width max-height"
        data.values.forEach {
            val item = combobox.appendItem(NirdizatiUtil.localizeText("params." + it))
            item.setValue(it)

            if (it == data.selected) {
                combobox.selectedItem = item
            }
        }

        combobox.isReadonly = true

        val row = Row()
        row.appendChild(label)
        row.appendChild(combobox)
        fields.add(FieldComponent(label, combobox))

        return row
    }
}