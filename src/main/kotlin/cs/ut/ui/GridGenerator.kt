package cs.ut.ui

import cs.ut.config.items.ModelParameter
import cs.ut.util.COMP_ID
import org.apache.log4j.Logger
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Component
import org.zkoss.zul.*


class FieldComponent(val label: Label, val control: Component)

class NirdizatiGrid<T>(val provider: GridValueProvider<T, Row>) : Grid() {
    private val log = Logger.getLogger(NirdizatiGrid::class.java)
    val fields = mutableListOf<FieldComponent>()

    init {
        provider.fields = fields
        appendChild(Rows())
    }

    fun generate(data: List<T>, clear: Boolean = true) {
        log.debug("Row generation start with ${data.size} properties")
        val start = System.currentTimeMillis()

        if (clear) rows.getChildren<Component>().clear()

        generateRows(data.toMutableList(), rows)

        val end = System.currentTimeMillis()
        log.debug("Row generation finished in ${end - start} ms")
    }

    fun setColumns(properties: Map<String, String>) {
        appendChild(Columns())
        properties.entries.forEach {
            val column = Column(Labels.getLabel(it.key))
            column.id = it.key
            column.width = it.value
            columns.appendChild(column)
        }
    }

    fun getComponentByName(name: String) = fields.first { it.label.id == name }

    tailrec private fun generateRows(data: MutableList<T>, rows: Rows) {
        if (data.isNotEmpty()) {
            val row = provider.provide(data.first())
            rows.appendChild(row)
            generateRows(data.tail(), rows);
        }
    }

    fun validate(): Boolean {
        val invalid = mutableListOf<Component>()
        validateFields(fields, invalid)
        return invalid.isEmpty()
    }

    tailrec private fun validateFields(fields: MutableList<FieldComponent>, invalid: MutableList<Component>) {
        if (fields.isNotEmpty()) {
            val comp = fields.first().control

            when (comp) {
                is Intbox -> if (comp.value == null || comp.value <= 0) {
                    comp.errorMessage = Labels.getLabel("training.validation.greater_than_zero")
                    invalid.add(comp)
                }
                is Doublebox -> if (comp.value == null || comp.value <= 0.0) {
                    comp.errorMessage = Labels.getLabel("training.validation.greater_than_zero")
                    invalid.add(comp)
                }
            }
            validateFields(fields.tail(), invalid)
        }
    }


    private fun <T> MutableList<T>.tail(): MutableList<T> = drop(1).toMutableList()

    fun gatherValues(): MutableMap<String, Any> {
        val valueMap = mutableMapOf<String, Any>()
        gatherValueFromFields(valueMap, fields)
        return valueMap
    }

    tailrec private fun gatherValueFromFields(valueMap: MutableMap<String, Any>, fields: MutableList<FieldComponent>) {
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