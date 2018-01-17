package cs.ut.config.nodes

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "defaultConfig")
@XmlAccessorType(XmlAccessType.FIELD)
class DefaultValuesConfiguration(
    @XmlElement(name = "minValue")
    var minValue: Double,

    @XmlElement(name = "average")
    var average: Int
) {
    constructor() : this(-1.0, -1)
}