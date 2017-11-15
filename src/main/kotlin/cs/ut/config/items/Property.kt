package cs.ut.config.items

import javax.xml.bind.annotation.*

@XmlRootElement(name = "property")
@XmlType(propOrder = arrayOf("id", "type", "property"))
@XmlAccessorType(XmlAccessType.FIELD)
data class Property(
        @XmlAttribute(name = "id")
        var id: String,

        @XmlAttribute(name = "type")
        var type: String,

        @XmlValue
        var property: String) {

        constructor() : this("", "","")

        constructor(property: Property) : this(property.id, property.type, property.property)
}
