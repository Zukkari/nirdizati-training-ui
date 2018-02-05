package cs.ut.config.items

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType


@XmlType(propOrder = ["id", "enableHtml", "label"])
@XmlAccessorType(XmlAccessType.FIELD)
data class TooltipItem(
    @XmlElement(name = "id")
    var id: String,

    @XmlElement(name = "enableHtml")
    var enableHtml: Boolean,

    @XmlElement(name = "label")
    var label: String
) {
    constructor() : this("", false, "")
}