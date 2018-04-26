package cs.ut.configuration

import javax.xml.bind.annotation.*

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

    inline fun <reified T> value(): T = when (T::class) {
        Long::class -> value.toLong() as T
        Int::class -> value.toLong() as T
        Boolean::class -> value.toBoolean() as T
        Double::class -> value.toDouble() as T
        String::class -> value as T
        else -> throw IllegalArgumentException("Not supported")
    }
}