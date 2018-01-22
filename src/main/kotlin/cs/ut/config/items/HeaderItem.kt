package cs.ut.config.items

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlType(propOrder = ["label", "redirect", "enabled", "position", "icon"])
@XmlAccessorType(XmlAccessType.FIELD)
data class HeaderItem(
    @XmlElement(name = "label") var label: String,
    @XmlElement(name = "redirect") var redirect: String,
    @XmlElement(name = "position") var position: Int,
    @XmlElement(name = "enabled") var enabled: Boolean,
    @XmlElement(name = "icon") var icon: String
) {

    constructor() : this("", "", 0, false, "")
}