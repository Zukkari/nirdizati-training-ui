package cs.ut.config.items

import javax.xml.bind.annotation.XmlElement

data class TooltipItem(
    @XmlElement(name = "id")
    val id: String,

    @XmlElement(name = "isHtml")
    val isHtml: Boolean,

    @XmlElement(name = "label")
    val label: String
) {
    constructor() : this("", false, "")
}