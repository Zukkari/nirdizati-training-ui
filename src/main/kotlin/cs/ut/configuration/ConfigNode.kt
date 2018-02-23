package cs.ut.configuration

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElementRef
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

@XmlRootElement(name = "Node")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = ["identifier", "values", "childNodes"])
data class ConfigNode(

    @XmlAttribute(name = "Identifier")
    val identifier: String,

    @XmlElementRef(name = "Value")
    val values: MutableList<Value>,

    @XmlElementRef(name = "Node")
    val childNodes: MutableList<ConfigNode>
) {
    constructor() : this("", mutableListOf(), mutableListOf())

    override fun toString(): String =
        "${this::class.simpleName}[Id: $identifier, Values: ${values.size}, Children: ${childNodes.size}]"

    fun itemList(): List<Value> = childNodes.firstOrNull { it.identifier == itemList }?.values ?: listOf()

    fun itemListValues(): List<String> = itemList().map { it.value }

    fun valueWithIdentifier(identifier: String) = values.first { it.identifier == identifier }

    fun isEnabled() = values.first { it.identifier == IS_ENABLED }.booleanValue()

    companion object {
        const val itemList = "itemList"
        const val IS_ENABLED = "isEnabled"
    }
}