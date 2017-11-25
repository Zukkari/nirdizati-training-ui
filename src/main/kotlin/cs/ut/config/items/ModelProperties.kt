package cs.ut.config.items

import javax.xml.bind.annotation.*

@XmlRootElement(name = "modelConfig")
@XmlAccessorType(XmlAccessType.FIELD)
data class ModelProperties(
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
}