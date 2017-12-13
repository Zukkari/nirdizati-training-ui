package cs.ut.config.items

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType
import javax.xml.bind.annotation.XmlValue

@XmlRootElement(name = "property")
@XmlType(propOrder = arrayOf("id", "type", "property", "maxValue", "minValue"))
@XmlAccessorType(XmlAccessType.FIELD)
data class Property(
        @XmlAttribute(name = "id")
        var id: String,

        @XmlAttribute(name = "type")
        var type: String,

        @XmlValue
        var property: String,

        @XmlAttribute(name = "maxValue")
        var maxValue: Double,

        @XmlAttribute(name = "minValue")
        var minValue: Double) {

    constructor() : this("", "", "", -1.0, -1.0)

    constructor(property: Property) : this(property.id, property.type, property.property, property.maxValue, property.minValue)
}
