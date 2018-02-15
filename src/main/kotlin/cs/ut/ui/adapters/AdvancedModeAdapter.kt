package cs.ut.ui.adapters

import cs.ut.ui.FieldComponent
import cs.ut.ui.GridValueProvider
import cs.ut.ui.TooltipParser
import cs.ut.util.COMP_ID
import cs.ut.util.NirdizatiUtil
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.A
import org.zkoss.zul.Checkbox
import org.zkoss.zul.Hbox
import org.zkoss.zul.Hlayout
import org.zkoss.zul.Label
import org.zkoss.zul.Popup
import org.zkoss.zul.Row

class AdvancedModeAdapter : GridValueProvider<GeneratorArgument, Row> {
    override var fields: MutableList<FieldComponent> = mutableListOf()

    private val parser: TooltipParser = TooltipParser()

    override fun provide(data: GeneratorArgument): Row {
        val row = Row()

        val label = Label(NirdizatiUtil.localizeText(data.id)).apply {
            this.sclass = "param-label"
            this.setAttribute(COMP_ID, data.params.first().type)
        }

        row.appendChild(Hbox().apply {
            this.vflex = "1"
            this.align = "center"
            this.appendChild(label)
        })
        row.appendChild(getTooltip(data.id))

        data.params.forEach { param ->
            row.appendChild(Hlayout().also {
                it.appendChild(
                    Hbox().also {
                        it.align = "center"
                        it.appendChild(Checkbox().apply {
                            this.label = NirdizatiUtil.localizeText(param.type + "." + param.id)
                            this.setValue(param)
                            fields.add(FieldComponent(label, this))
                        })
                    })
            })
            row.appendChild(getTooltip(param.id))
        }
        return row
    }

    private fun getTooltip(tooltip: String): A {
        return A().apply {
            this.vflex = "1"
            this.hflex = "min"
            this.iconSclass = "z-icon-question-circle"
            this.sclass = "validation-btn"

            this.addEventListener(Events.ON_MOUSE_OVER, { _ ->
                desktop.components.firstOrNull { it.id == tooltip && it is Popup }?.detach()
                parser.readTooltip(tooltip).also {
                    this.appendChild(it)
                    it.sclass = "n-popup"
                    it.id = tooltip
                }.open(this, "end_after")
            })
            this.addEventListener(Events.ON_MOUSE_OUT, { _ ->
                desktop.components.forEach { (it as? Popup)?.close() }
            })
        }
    }
}