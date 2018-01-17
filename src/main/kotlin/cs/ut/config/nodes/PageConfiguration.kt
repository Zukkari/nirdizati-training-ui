package cs.ut.config.nodes

import cs.ut.config.items.Page
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "pageConfig")
@XmlAccessorType(XmlAccessType.FIELD)
data class PageConfiguration(
    @XmlElement(name = "page")
    var page: MutableList<Page>
) {
    constructor() : this(mutableListOf())

    fun getPageByName(name: String): Page = page.first { it.id == name }
}