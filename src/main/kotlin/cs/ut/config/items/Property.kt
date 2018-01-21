package cs.ut.config.items

import javax.xml.bind.annotation.*

@XmlRootElement(name = "property")
@XmlType(propOrder = ["id", "type", "property", "maxValue", "minValue"])
@XmlAccessorType(XmlAccessType.FIELD)
open class Property(
        @XmlAttribute(name = "id")
        var id: String,

        @XmlAttribute(name = "type")
        var type: String,

        @XmlValue
        var property: String,

        @XmlAttribute(name = "maxValue")
        var maxValue: Double,

        @XmlAttribute(name = "minValue")
        var minValue: Double
) {

    constructor() : this("", "", "", -1.0, -1.0)

    constructor(property: Property) : this(
            property.id,
            property.type,
            property.property,
            property.maxValue,
            property.minValue
    )

    operator fun component1(): String = id

    operator fun component2(): String = type

    operator fun component3(): String = property
}

object EmptyProperty : Property("N/A", "N/A", "N/A", 0.0, 0.0)
