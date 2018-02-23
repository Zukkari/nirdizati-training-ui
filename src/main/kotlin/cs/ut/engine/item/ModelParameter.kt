package cs.ut.config.items

open class ModelParameter(
    var id: String,
    var parameter: String,
    var type: String,
    var enabled: Boolean,
    var properties: MutableList<Property>
) {

    constructor(modelParameter: ModelParameter) : this(
        modelParameter.id,
        modelParameter.parameter,
        modelParameter.type,
        modelParameter.enabled,
        modelParameter.properties
    )

    var translate = true

    fun getTranslateName() = this.type + "." + this.id

    constructor() : this("", "", "", false, mutableListOf())

    init {
        val props = properties

        val copied = mutableListOf<Property>()
        props.forEach { copied.add(Property(it)) }

        properties = copied
    }

    fun getPropety(property: String): Property? {
        return properties.firstOrNull { it.id == property }
    }

    override fun equals(other: Any?): Boolean {
        return other is ModelParameter
                && this.id == other.id
                && this.enabled == other.enabled
                && this.type == other.type
                && this.parameter == other.parameter
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + parameter.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + enabled.hashCode()
        return result
    }

    override fun toString(): String = "$type.$id"
}