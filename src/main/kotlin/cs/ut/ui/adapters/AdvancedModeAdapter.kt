package cs.ut.ui.adapters

import cs.ut.configuration.ConfigurationReader
import cs.ut.engine.item.ModelParameter
import cs.ut.ui.FieldComponent
import cs.ut.ui.GridValueProvider
import cs.ut.ui.TooltipParser
import cs.ut.util.COMP_ID
import cs.ut.util.NirdizatiTranslator
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.*

data class GeneratorArgument(val id: String, val params: List<ModelParameter>)

/**
 * Implementation that is used when generating grid for the training view
 */
class AdvancedModeAdapter : GridValueProvider<GeneratorArgument, Row> {
    override var fields: MutableList<FieldComponent> = mutableListOf()

    private val parser: TooltipParser = TooltipParser()

    override fun provide(data: GeneratorArgument): Row {
        val row = Row()

        val label = Label(NirdizatiTranslator.localizeText(data.id)).apply {
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
                            this.label = NirdizatiTranslator.localizeText(param.type + "." + param.id)
                            this.setValue(param)
                            fields.add(FieldComponent(label, this))
                        })
                    })
            })
            row.appendChild(getTooltip(param.id))
        }
        return row
    }

    /**
     * Generate wrapper for tooltip with a tooltip that is shown on hover
     *
     * @param tooltip id of the tooltip to load
     * @return wrapper with a tooltip that is shown on hover
     */
    private fun getTooltip(tooltip: String): A {
        return A().apply {
            this.vflex = "1"
            this.hflex = "min"
            this.iconSclass = icons.valueWithIdentifier("tooltip").value
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

    companion object {
        private val icons = ConfigurationReader.findNode("iconClass")
    }
}