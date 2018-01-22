package cs.ut.config.nodes

import cs.ut.config.items.HeaderItem
import javax.xml.bind.annotation.*

@XmlRootElement(name = "headerConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
data class HeaderConfiguration(

    @field:[
    XmlElementWrapper(name = "headerItems")
    XmlElement(name = "headerItem")]
    var headerItems: MutableList<HeaderItem>
) {
    constructor() : this(mutableListOf())
}