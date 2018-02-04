package cs.ut.config.nodes

import cs.ut.config.items.TooltipItem
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlElementWrapper
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "configuration")
@XmlAccessorType(XmlAccessType.FIELD)
data class TooltipConfig(

    @field:[
    XmlElementWrapper(name = "tooltips")
    XmlElement(name = "tooltip")]
    val items: MutableList<TooltipItem>
) {
    constructor() : this(mutableListOf())
}