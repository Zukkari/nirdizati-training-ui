package cs.ut.config.items

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlType(propOrder = ["id", "path"])
@XmlAccessorType(XmlAccessType.FIELD)
class Directory(
    @XmlElement
    var id: String,

    @XmlElement
    var path: String
) {
    constructor() : this("", "")
}