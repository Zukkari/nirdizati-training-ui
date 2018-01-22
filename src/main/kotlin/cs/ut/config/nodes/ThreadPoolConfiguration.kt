package cs.ut.config.nodes

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlType(propOrder = ["core", "max", "keepAlive", "capacity"])
@XmlAccessorType(XmlAccessType.FIELD)
class ThreadPoolConfiguration(
    @XmlElement(name = "core") var core: Int,
    @XmlElement(name = "max") var max: Int,
    @XmlElement(name = "keepAlive") var keepAlive: Int,
    @XmlElement(name = "capacity") var capacity: Int
) {

    constructor() : this(-1, -1, -1, -1)
}