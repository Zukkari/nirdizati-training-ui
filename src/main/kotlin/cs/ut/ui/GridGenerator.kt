package cs.ut.ui

import org.apache.log4j.Logger
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.IdSpace
import org.zkoss.zul.*

class NirdizatiGrid<T>(val provider: GridValueProvider<T, out Row>) : Grid(), IdSpace {
    private val log = Logger.getLogger(NirdizatiGrid::class.java)
    private val fields = mutableListOf<Component>()

    init {
        provider.fields = fields
        appendChild(Rows())
    }

    fun generate(data: List<T>) {
        log.debug("Row generation start with ${data.size} properties")
        val start = System.currentTimeMillis()
        rows.getChildren<Component>().clear()

        generateRows(data.toMutableList(), rows)

        rows.vflex = "min"

        vflex = "min"
        hflex = "min"
        val end = System.currentTimeMillis()
        log.debug("Row generation finished in ${end - start} ms")
    }

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

    tailrec private fun validateFields(fields: MutableList<Component>, invalid: MutableList<Component>) {
        if (fields.isNotEmpty()) {
            val comp = fields.first()

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

    tailrec private fun gatherValueFromFields(valueMap: MutableMap<String, Any>, fields: MutableList<Component>) {
        if (fields.isNotEmpty()) {
            val field = fields.first()

            when (field) {
                is Intbox -> valueMap[field.id] = field.value
                is Doublebox -> valueMap[field.id] = field.value
                is Combobox -> valueMap[field.id] = field.selectedItem.getValue()
            }

            gatherValueFromFields(valueMap, fields.tail())
        }
    }
}