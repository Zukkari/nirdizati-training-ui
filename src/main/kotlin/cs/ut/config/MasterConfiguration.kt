package cs.ut.config

import cs.ut.config.items.ModelParameter
import cs.ut.config.nodes.*
import cs.ut.util.readHyperParameterJson
import org.apache.log4j.*
import java.io.File
import javax.xml.bind.JAXBContext
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

object MasterConfiguration {
    private val log: Logger = Logger.getLogger(MasterConfiguration::class.java)!!

    val file: File = File(javaClass.classLoader.getResource("configuration.xml").file!!)

    val directoryPathConfiguration by lazy { readClass(DirectoryPathConfiguration::class.java, "paths") }
    val modelConfiguration by lazy {
        val element = readClass(ModelConfiguration::class.java, "modelConfig")
        element.prepareData()
        element
    }

    val csvConfiguration by lazy { readClass(CsvConfiguration::class.java, "csvConfig") }
    val threadPoolConfiguration by lazy { readClass(ThreadPoolConfiguration::class.java, "threadpool") }
    val headerConfiguration by lazy { readClass(HeaderConfiguration::class.java, "headerConfiguration") }
    val pageConfiguration by lazy { readClass(PageConfiguration::class.java, "pageConfig") }
    val userPreferences by lazy { readClass(UserPreferences::class.java, "userPreferences") }
    val defaultValuesConfiguration by lazy { readClass(DefaultValuesConfiguration::class.java, "defaultConfig") }

    val optimizedParams: Map<String, List<ModelParameter>> by lazy { readHyperParameterJson() }


    init {
        configureLogger()
        log.debug("Logger configured successfully")
    }

    /**
     * Configures logger and Enables appenders for Log4j
     */
    private fun configureLogger() {
        Logger.getRootLogger().removeAllAppenders()
        Logger.getRootLogger().additivity = false

        val ca = ConsoleAppender()
        ca.layout = PatternLayout("<%d{ISO8601}> <%p> <%C{1}.class:%L> <%m>%n")
        ca.threshold = Level.DEBUG
        ca.activateOptions()

        Logger.getRootLogger().addAppender(ca)

        val fileAppender = FileAppender()
        fileAppender.layout = PatternLayout("<%d{ISO8601}> <%p> <%C{1}.class:%L> <%m>%n")
        fileAppender.name = "nirdizati_ui_log.log"
        fileAppender.file = "nirdizati_ui_log.log"
        fileAppender.threshold = Level.DEBUG
        fileAppender.append = true
        fileAppender.activateOptions()

        Logger.getRootLogger().addAppender(fileAppender)
    }

    private fun <T> readClass(clazz: Class<T>, tag: String): T {
        log.debug("Reading class $clazz from configuration: $file from tag $tag")

        val dbf = DocumentBuilderFactory.newInstance()
        dbf.isNamespaceAware = true

        val db: DocumentBuilder = dbf.newDocumentBuilder()
        val doc = db.parse(file)
        log.debug("Successfully parsed document")

        val node = doc.getElementsByTagName(tag)
        log.debug("Successfully found node with tag $tag")

        val jaxbContext = JAXBContext.newInstance(clazz)
        return jaxbContext.createUnmarshaller().unmarshal(node.item(0), clazz).value
    }
}