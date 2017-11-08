package cs.ut.config.items

import javax.xml.bind.annotation.*

@XmlRootElement(name = "property")
@XmlType(propOrder = arrayOf("id", "property"))
@XmlAccessorType(XmlAccessType.FIELD)
data class Property(
        @XmlAttribute(name = "id")
        var id: String,

        @XmlValue
        var property: String) {

        constructor() : this("", "")
}
