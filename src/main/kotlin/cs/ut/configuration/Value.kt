package cs.ut.configuration

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType
import javax.xml.bind.annotation.XmlValue

/**
 * Object that represents "Value" node in the configuration file
 */
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

    /**
     * Get node value as a Double.class
     */
    fun doubleValue() = value.toDouble()

    /**
     * Get node value as a Int.class
     */
    fun intValue() = value.toInt()

    /**
     * Get node value as Boolean.class
     */
    fun booleanValue() = value.toBoolean()
}