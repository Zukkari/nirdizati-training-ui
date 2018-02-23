package cs.ut.configuration

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType
import javax.xml.bind.annotation.XmlValue

@XmlRootElement(name = "Value")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = ["identifier", "value"])
data class Value(

    @XmlAttribute(name = "Identifier")
    val identifier: String,

    @XmlValue
    val value: String
) {
    constructor() : this("", "")

    fun doubleValue() = value.toDouble()

    fun intValue() = value.toInt()

    fun booleanValue() = value.toBoolean()
}