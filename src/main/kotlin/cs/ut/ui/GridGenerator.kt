package cs.ut.ui

import cs.ut.configuration.ConfigNode
import cs.ut.configuration.ConfigurationReader
import cs.ut.engine.item.ModelParameter
import cs.ut.engine.item.Property
import cs.ut.logging.NirdizatiLogger
import cs.ut.util.COMP_ID
import cs.ut.util.GridColumns
import cs.ut.util.NirdizatiTranslator
import cs.ut.util.PROPERTY
import org.zkoss.zk.ui.Component
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

/**
 * Data class that stores grid components for easy data collection
 */
data class FieldComponent(val label: Component, val control: Component)

/**
 * Custom ZK grid implementation that allows to generate grid with custom row providers
 */
class NirdizatiGrid<in T>(private val provider: GridValueProvider<T, Row>, private val namespace: String = "") : Grid(), UIComponent {
    private val log = NirdizatiLogger.getLogger(NirdizatiGrid::class.java, getSessionId())
    private val configNode = if (namespace.isNotBlank()) ConfigurationReader.findNode("grids/$namespace") else ConfigNode()

    val fields = mutableListOf<FieldComponent>()

    init {
        provider.fields = fields
        appendChild(Rows())
    }

    /**
     * Generate grid rows using the data provided
     *
     * @param data to generate rows with
     * @param clear whether or not existing data should be cleared before appending new data
     */
    fun generate(data: Collection<T>, clear: Boolean = true, reversedInsert: Boolean = false) {
        log.debug("Row generation start with ${data.size} properties")
        val start = System.currentTimeMillis()

        if (clear) {
            rows.getChildren<Component>().clear()
            fields.clear()
        }

        generateRows(data.toMutableList(), rows, reversedInsert)

        val end = System.currentTimeMillis()
        log.debug("Row generation finished in ${end - start} ms")
    }

    /**
     * Set grid columns.
     * @param properties where key is column name and value is column width
     */
    fun setColumns(properties: Map<String, String>) {
        val cols = Columns()
        appendChild(cols)
        properties.entries.forEach {
            val column = if (it.key.isNotBlank()) Column(NirdizatiTranslator.localizeText(it.key)) else Column()
            column.id = it.key
            if (it.value.isNotEmpty()) {
                column.hflex = it.value
            }
            cols.appendChild(column)
        }

        if (namespace.isNotBlank()) {
            log.debug("Namespace for grid -> $namespace")
            setSortingRules()
            columns.menupopup = "auto"
        }
    }

    private fun setSortingRules() {
        val sortable = configNode.childNodes.first { it.identifier == GridColumns.SORTABLE.value }
        val values = sortable.itemListValues()

        if (sortable.isEnabled()) {
            columns.getChildren<Column>().forEach {
                if (it.id in values) {
                    it.setSort("auto")
                }
            }
        }
    }

    private tailrec fun generateRows(data: MutableList<T>, rows: Rows, reversedInsert: Boolean) {
        if (data.isNotEmpty()) {
            val row = provider.provide(data.first())

            if (reversedInsert) {
                rows.insertBefore(row, rows.firstChild)
            } else {
                rows.appendChild(row)
            }

            generateRows(data.tail(), rows, reversedInsert)
        }
    }

    /**
     * Validate that data in the grid is correct according to component definitions
     */
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
                        comp.errorMessage = NirdizatiTranslator.localizeText("training.validation.greater_than_zero")
                    } else {
                        setErrorMsg(comp)
                    }
                    invalid.add(comp)
                }
                is Doublebox -> if (comp.value == null || !isInLimits(comp)) {
                    if (!comp.hasAttribute(PROPERTY)) {
                        comp.errorMessage = NirdizatiTranslator.localizeText("training.validation.greater_than_zero")
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
            comp.errorMessage = NirdizatiTranslator.localizeText("training.validation.in_range", prop.minValue, prop.maxValue)
        } else if (prop.minValue != -1.0) {
            comp.errorMessage = NirdizatiTranslator.localizeText("training.validation.min_val", prop.minValue)
        } else {
            comp.errorMessage = NirdizatiTranslator.localizeText("training.validation.max_val", prop.maxValue)
        }
    }

    private fun isInLimits(comp: Component): Boolean {
        if (!comp.hasAttribute(PROPERTY)) return true

        val prop = comp.getAttribute(PROPERTY) as Property

        if (prop.maxValue == -1.0 && prop.minValue == -1.0) return true

        return when (comp) {
            is Intbox -> isInRange(comp.value, prop.minValue, prop.maxValue)
            is Doublebox -> isInRange(comp.value, prop.minValue, prop.maxValue)
            else -> throw UnsupportedOperationException("Operation not defined for class $comp")
        }
    }

    private fun <T> MutableList<T>.tail(): MutableList<T> = drop(1).toMutableList()

    /**
     * Gather values from the grid
     *
     * @return map with collected elements from the grid
     */
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

/**
 * Whether given value is in specific range with default arguments
 */
fun isInRange(num: Number, min: Double = -1.0, max: Double = -1.0): Boolean {
    return if (min != -1.0 && max != -1.0) num.toDouble() in min..max
    else if (max != -1.0) num.toDouble() <= max
    else min <= num.toDouble()
}