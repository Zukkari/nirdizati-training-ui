package cs.ut.config.items

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlType(propOrder = arrayOf("id", "uri"))
data class Page(
    @XmlElement(name = "id")
    var id: String,

    @XmlElement(name = "uri")
    var uri: String
) {

    constructor() : this("", "")
}