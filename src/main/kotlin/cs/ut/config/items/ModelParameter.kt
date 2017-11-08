package cs.ut.config.items

import javax.xml.bind.annotation.*

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

    fun getPropety(property: String): Property? {
        return properties.firstOrNull { it.id == property}
    }
}