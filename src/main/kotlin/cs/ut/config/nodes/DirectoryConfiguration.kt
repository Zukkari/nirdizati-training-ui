package cs.ut.config.nodes

import cs.ut.config.items.Directory
import java.io.File
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "dirs")
@XmlAccessorType(XmlAccessType.FIELD)
class DirectoryConfiguration {

    @XmlElement(name = "dir")
    private val dirs: MutableList<Directory> = mutableListOf()

    fun dirByName(dir: Dir): File = File(dirs.first { it.id == dir.value() }.path)

    fun dirPath(dir: Dir): String = dirs.first { it.id == dir.value() }.path
}

enum class Dir(private val id: String) {
    PYTHON("python"),
    USER_LOGS("userLogDirectory"),
    USER_MODEL("userModelDirectory"),
    SCRIPT_DIR("scriptDirectory"),
    TRAIN_DIR("trainDirectory"),
    DATA_DIR("datasetDirectory"),
    PKL_DIR("pklDirectory"),
    OHP_DIR("ohpdir"),
    DETAIL_DIR("detailedDir"),
    FEATURE_DIR("featureDir"),
    VALIDATION_DIR("validationDir"),
    TMP_DIR("tmpDir"),
    CORE_DIR("coreDir");

    fun value(): String = id
}