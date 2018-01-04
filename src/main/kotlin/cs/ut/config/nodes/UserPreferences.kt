package cs.ut.config.nodes

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlType(propOrder = ["enabled", "userName"])
@XmlAccessorType(XmlAccessType.FIELD)
class UserPreferences(
        @XmlElement
        val enabled: Boolean,

        @XmlElement
        val userName: String
) {
    constructor() : this(false, "")
}