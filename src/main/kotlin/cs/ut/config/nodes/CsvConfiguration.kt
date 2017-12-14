package cs.ut.config.nodes

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlElementWrapper
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "csvConfig")
@XmlAccessorType(XmlAccessType.FIELD)
data class CsvConfiguration(
        @field:[
        XmlElementWrapper(name = "userCols")
        XmlElement(name = "col")]
        var userCols: List<String>,

        @field:[
        XmlElementWrapper(name = "caseId")
        XmlElement(name = "id")]
        var caseId: List<String>,

        @field:[
        XmlElementWrapper(name = "activityId")
        XmlElement(name = "id")]
        var activityId: List<String>,

        @field:[
        XmlElementWrapper(name = "resourceId")
        XmlElement(name = "id")]
        var resourceId: List<String>,

        @field:[
        XmlElementWrapper(name = "timestmapFormat")
        XmlElement(name = "format")]
        var timestampFormat: List<String>,

        @XmlElement(name = "splitter")
        var splitter: String,

        @field:[
        XmlElementWrapper(name = "emptyValues")
        XmlElement(name = "id")]
        var emptyValues: List<String>,

        @XmlElement(name = "threshold")
        var threshold: Int,

        @XmlElement(name = "sampleSize")
        var sampleSize: Int,

        @field:[
        XmlElementWrapper(name = "extensions")
        XmlElement(name = "ext")]
        var extensions: List<String>
) {
    constructor() : this(mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), "", mutableListOf(), -1, -1, mutableListOf())
}