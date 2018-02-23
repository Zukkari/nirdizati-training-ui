package cs.ut.providers

import cs.ut.config.items.ModelParameter
import cs.ut.config.items.Property
import cs.ut.configuration.ConfigNode
import cs.ut.configuration.ConfigurationReader
import cs.ut.logging.NirdizatiLogger
import cs.ut.util.PREDICTIONTYPE
import cs.ut.util.readHyperParameterJson

class ModelParamProvider {
    private val config = ConfigurationReader.findNode("models")!!

    var properties: Map<String, List<ModelParameter>> = mutableMapOf()

    init {
        log.debug("Started parsing configuration with node $config")
        parseParameters()
        log.debug("Successfully parsed model parameters from configuration")
    }

    fun getBasicParameters(): List<ModelParameter> {
        val basicParameters = ConfigurationReader.findNode("models/basic")!!.itemListValues()
        return mutableListOf<ModelParameter>().apply {
            basicParameters.forEach { p ->
                this.add(properties.flatMap { it.value }.first { it.id == p })
            }
        }
    }

    fun getPredictionTypes() = properties[PREDICTIONTYPE]!!

    fun getAllProperties(): List<Property> = properties.flatMap { it.value.flatMap { it.properties } }

    private fun parseParameters() {
        val params = mutableListOf<ModelParameter>()
        config.childNodes.first { it.identifier == PARAM_NODE }.childNodes.forEach {
            log.debug("Parsing parameter node: $it")
            params.add(ModelParameter().apply {
                this.enabled = it.values.first { it.identifier == ENABLED }.booleanValue()
                this.id = it.identifier
                this.type = it.values.first { it.identifier == TYPE }.value
                this.parameter = it.values.first { it.identifier == PARAMETER }.value

                val propNode = it.childNodes.firstOrNull { it.identifier == PROPERTIES }
                this.properties = parseProperties(this, propNode)
            })
        }

        properties = params.groupBy { it.type }
    }

    private fun parseProperties(modelParameter: ModelParameter, propNode: ConfigNode?): MutableList<Property> {
        val properties = mutableListOf<Property>()
        log.debug("Parsing properties for $modelParameter")

        if (propNode != null) {
            propNode.childNodes.forEach {
                properties.add(Property().apply {
                    this.id = it.identifier
                    this.type = it.values.firstOrNull { it.identifier == CONTROL }?.value ?: ""
                    this.maxValue = it.values.firstOrNull { it.identifier == MAX }?.doubleValue() ?: -1.0
                    this.minValue = it.values.firstOrNull { it.identifier == MIN }?.doubleValue() ?: -1.0
                    this.property = it.values.firstOrNull { it.identifier == DEFAULT }?.value ?: ""
                })
                log.debug("Parsed property $this from $it")
            }
        } else {
            log.debug("No properties found for $modelParameter -> skipping")
        }

        return properties
    }

    companion object {
        private val log = NirdizatiLogger.getLogger(ModelParamProvider::class.java)

        const val PARAM_NODE = "parameters"
        const val ENABLED = "isEnabled"
        const val TYPE = "type"
        const val PROPERTIES = "properties"
        const val CONTROL = "control"
        const val MAX = "max"
        const val MIN = "min"
        const val DEFAULT = "default"
        const val PARAMETER = "parameter"

        fun getOptimizedParameters() = readHyperParameterJson()
    }
}