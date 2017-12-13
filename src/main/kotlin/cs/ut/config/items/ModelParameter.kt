package cs.ut.config.items

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlElementWrapper
import javax.xml.bind.annotation.XmlType

@XmlType(propOrder = arrayOf("id", "parameter", "type", "enabled", "properties"))
@XmlAccessorType(XmlAccessType.FIELD)
data class ModelParameter(
        @field:[XmlElement(name = "id")] var id: String,
        @field:[XmlElement(name = "parameter")] var parameter: String,
        @field:[XmlElement(name = "type")] var type: String,
        @field:[XmlElement(name = "enabled")] var enabled: Boolean,
        @field:[XmlElementWrapper(name = "properties")
        XmlElement(name = "property")] var properties: MutableList<Property>) {

    constructor(modelParameter: ModelParameter) : this(
            modelParameter.id,
            modelParameter.parameter,
            modelParameter.type,
            modelParameter.enabled,
            modelParameter.properties
    )

    constructor() : this("", "", "", false, mutableListOf())

    init {
        val props = properties

        val copied = mutableListOf<Property>()
        props.forEach { copied.add(Property(it)) }

        properties = copied
    }

    fun getPropety(property: String): Property? {
        return properties.firstOrNull { it.id == property}
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
}