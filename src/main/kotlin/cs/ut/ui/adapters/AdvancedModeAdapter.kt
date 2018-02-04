package cs.ut.ui.adapters

import cs.ut.ui.FieldComponent
import cs.ut.ui.GridValueProvider
import cs.ut.ui.TooltipParser
import cs.ut.util.COMP_ID
import cs.ut.util.NirdizatiUtil
import org.zkoss.zul.Checkbox
import org.zkoss.zul.Hlayout
import org.zkoss.zul.Label
import org.zkoss.zul.Row

class AdvancedModeAdapter : GridValueProvider<GeneratorArgument, Row> {
    override var fields: MutableList<FieldComponent> = mutableListOf()

    private val parser: TooltipParser = TooltipParser()

    override fun provide(data: GeneratorArgument): Row {
        val row = Row()

        val label = Label(NirdizatiUtil.localizeText(data.id)).apply {
            this.sclass = "param-label"
            this.setAttribute(COMP_ID, data.params.first().type)
            this.setTooltip(parser.readTooltip(data.id))
        }

        row.appendChild(Hlayout().apply {
            this.appendChild(label)
        })

        data.params.forEach { param ->
            row.appendChild(Hlayout().also {
                it.appendChild(Checkbox().apply {
                    this.label = NirdizatiUtil.localizeText(param.type + "." + param.id)
                    this.setValue(param)
                    fields.add(FieldComponent(label, this))
                    this.setTooltip(parser.readTooltip(param.id))
                })
            })
        }

        return row
    }
}