package cs.ut.config.nodes

import cs.ut.config.items.ModelParameter
import cs.ut.config.items.Property
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlElementWrapper
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlTransient

@XmlRootElement(name = "modelConfig")
@XmlAccessorType(XmlAccessType.FIELD)
data class ModelConfiguration(
        @field:[
        XmlElementWrapper(name = "types")
        XmlElement(name = "type")]
        var types: MutableList<String>,

        @field:[
        XmlElementWrapper(name = "modelparams")
        XmlElement(name = "modelparam")]
        var parameters: MutableList<ModelParameter>,

        @field:[
        XmlElementWrapper(name = "basicParams")
        XmlElement(name = "param")]
        var basicParams: MutableList<String>) {

    constructor() : this(mutableListOf(), mutableListOf(), mutableListOf())

    @XmlTransient
    lateinit var basicParameters: List<ModelParameter>

    @XmlTransient
    lateinit var properties: Map<String, List<ModelParameter>>

    fun prepareData() {
        properties = parameters.groupBy { it.type }
        assert(types.containsAll(properties.keys), { "Allowed types are $types but received ${properties.keys}" })

        basicParameters = properties.values.flatMap { it }.filter { it.parameter in basicParams }
        assert(basicParameters.isNotEmpty(), { "Basic paramteres are empty but declared values are $basicParams" })
    }

    fun getAllProperties(): List<Property> = parameters.map { it.properties }.flatMap { it }
}