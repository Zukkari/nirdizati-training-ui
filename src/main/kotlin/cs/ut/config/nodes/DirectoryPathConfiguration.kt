package cs.ut.config.nodes

import cs.ut.exceptions.NirdizatiRuntimeException
import org.apache.commons.io.FileUtils
import java.io.File
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "paths")
@XmlAccessorType(XmlAccessType.FIELD)
class DirectoryPathConfiguration(

        @XmlElement(name = "python")
        var python: String,

        @XmlElement(name = "userLogDirectory")
        var userLogDirectory: String,

        @XmlElement(name = "userModelDirectory")
        var userModelDirectory: String,

        @XmlElement(name = "scriptDirectory")
        var scriptDirectory: String,

        @XmlElement(name = "datasetDirectory")
        var datasetDirectory: String,

        @XmlElement(name = "trainDirectory")
        var trainDirectory: String,

        @XmlElement(name = "pklDirectory")
        var pklDirectory: String,

        @XmlElement(name = "ohpdir")
        var ohpdir: String,

        @XmlElement(name = "detailedDir")
        var detailedDir: String,

        @XmlElement(name = "featureDir")
        var featureDir: String,

        @XmlElement(name = "validationDir")
        var validationDir: String,

        @XmlElement(name = "tmpDir")
        var tmpDir: String) {

    constructor() : this("", "", "", "", "", "", "", "", "", "", "", "")

    fun createTmpDir() {
        val tmpDir = File(tmpDir)
        if (tmpDir.exists()) {
            FileUtils.deleteDirectory(tmpDir)
        }

        if (!tmpDir.mkdir()) {
            throw NirdizatiRuntimeException("No permission to create tmp directory")
        }
    }
}