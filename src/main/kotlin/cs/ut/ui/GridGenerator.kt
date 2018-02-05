package cs.ut.ui

import cs.ut.config.items.ModelParameter
import cs.ut.config.items.Property
import cs.ut.logging.NirdLogger
import cs.ut.util.COMP_ID
import cs.ut.util.NirdizatiUtil
import cs.ut.util.PROPERTY
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zul.Checkbox
import org.zkoss.zul.Column
import org.zkoss.zul.Columns
import org.zkoss.zul.Combobox
import org.zkoss.zul.Doublebox
import org.zkoss.zul.Grid
import org.zkoss.zul.Intbox
import org.zkoss.zul.Row
import org.zkoss.zul.Rows
import org.zkoss.zul.impl.NumberInputElement


class FieldComponent(val label: Component, val control: Component)

class NirdizatiGrid<T>(private val provider: GridValueProvider<T, Row>) : Grid() {
    private val log = NirdLogger(NirdLogger.getId(Executions.getCurrent().nativeRequest), this.javaClass)
    val fields = mutableListOf<FieldComponent>()

    init {
        provider.fields = fields
        appendChild(Rows())
    }

    fun generate(data: List<T>, clear: Boolean = true) {
        log.debug("Row generation start with ${data.size} properties")
        val start = System.currentTimeMillis()

        if (clear) {
            rows.getChildren<Component>().clear()
            fields.clear()
        }

        generateRows(data.toMutableList(), rows)

        val end = System.currentTimeMillis()
        log.debug("Row generation finished in ${end - start} ms")
    }

    fun setColumns(properties: Map<String, String>) {
        val cols = Columns()
        cols.vflex = "min"
        appendChild(cols)
        properties.entries.forEach {
            val column = Column(NirdizatiUtil.localizeText(it.key))
            column.id = it.key
            if (it.value.isNotEmpty()) {
                column.hflex = it.value
            }
            columns.appendChild(column)
        }
    }

    fun getComponentByName(name: String) = fields.first { it.label.id == name }

    private tailrec fun generateRows(data: MutableList<T>, rows: Rows) {
        if (data.isNotEmpty()) {
            val row = provider.provide(data.first())
            rows.appendChild(row)
            generateRows(data.tail(), rows)
        }
    }

    fun validate(): Boolean {
        val invalid = mutableListOf<Component>()
        validateFields(fields, invalid)
        return invalid.isEmpty()
    }

    private tailrec fun validateFields(fields: MutableList<FieldComponent>, invalid: MutableList<Component>) {
        if (fields.isNotEmpty()) {
            val comp = fields.first().control

            when (comp) {
                is Intbox -> if (comp.value == null || !isInLimits(comp)) {
                    if (!comp.hasAttribute(PROPERTY)) {
                        comp.errorMessage = NirdizatiUtil.localizeText("training.validation.greater_than_zero")
                    } else {
                        setErrorMsg(comp)
                    }
                    invalid.add(comp)
                }
                is Doublebox -> if (comp.value == null || !isInLimits(comp)) {
                    if (!comp.hasAttribute(PROPERTY)) {
                        comp.errorMessage = NirdizatiUtil.localizeText("training.validation.greater_than_zero")
                    } else {
                        setErrorMsg(comp)
                    }
                    invalid.add(comp)
                }
            }
            validateFields(fields.tail(), invalid)
        }
    }

    private fun setErrorMsg(comp: NumberInputElement) {
        val prop = comp.getAttribute(PROPERTY) as Property

        if (prop.minValue != -1.0 && prop.maxValue != -1.0) {
            comp.errorMessage = NirdizatiUtil.localizeText("training.validation.in_range", prop.minValue, prop.maxValue)
        } else if (prop.minValue != -1.0) {
            comp.errorMessage = NirdizatiUtil.localizeText("training.validation.min_val", prop.minValue)
        } else {
            comp.errorMessage = NirdizatiUtil.localizeText("training.validation.max_val", prop.maxValue)
        }
    }

    private fun isInLimits(comp: Component): Boolean {
        if (!comp.hasAttribute(PROPERTY)) return true

        val prop = comp.getAttribute(PROPERTY) as Property

        if (prop.maxValue == -1.0 && prop.minValue == -1.0) return true

        when (comp) {
            is Intbox -> return isInRange(comp.value, prop.minValue, prop.maxValue)
            is Doublebox -> return isInRange(comp.value, prop.minValue, prop.maxValue)
            else -> throw UnsupportedOperationException("Operation not defined for class $comp")
        }
    }

    private fun <T> MutableList<T>.tail(): MutableList<T> = drop(1).toMutableList()

    fun gatherValues(): MutableMap<String, Any> {
        val valueMap = mutableMapOf<String, Any>()
        gatherValueFromFields(valueMap, fields)
        return valueMap
    }

    @Suppress("UNCHECKED_CAST")
    private tailrec fun gatherValueFromFields(valueMap: MutableMap<String, Any>, fields: MutableList<FieldComponent>) {
        if (fields.isNotEmpty()) {
            val field = fields.first().control
            val id = fields.first().label.getAttribute(COMP_ID) as String

            when (field) {
                is Intbox -> valueMap[id] = field.value
                is Doublebox -> valueMap[id] = field.value
                is Combobox -> valueMap[id] = field.selectedItem.getValue()
                is Checkbox -> {
                    if (field.isChecked) {
                        if (valueMap.containsKey(id)) {
                            (valueMap[id] as MutableList<ModelParameter>).add(field.getValue())
                        } else {
                            valueMap[id] = mutableListOf<ModelParameter>()
                            val params = valueMap[id]
                            when (params) {
                                is MutableList<*> -> (params as MutableList<ModelParameter>).add(field.getValue())
                            }
                        }
                    }
                }
            }

            gatherValueFromFields(valueMap, fields.tail())
        }
    }
}

fun isInRange(num: Number, min: Double = -1.0, max: Double = -1.0): Boolean {
    return if (min != -1.0 && max != -1.0) num.toDouble() in min..max
    else if (max != -1.0) num.toDouble() <= max
    else min <= num.toDouble()
}