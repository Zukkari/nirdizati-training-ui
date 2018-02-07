package cs.ut.config

import cs.ut.config.items.ModelParameter
import cs.ut.config.nodes.CsvConfiguration
import cs.ut.config.nodes.DefaultValuesConfiguration
import cs.ut.config.nodes.DirectoryConfiguration
import cs.ut.config.nodes.HeaderConfiguration
import cs.ut.config.nodes.ModelConfiguration
import cs.ut.config.nodes.PageConfiguration
import cs.ut.config.nodes.ThreadPoolConfiguration
import cs.ut.config.nodes.TooltipConfig
import cs.ut.config.nodes.UserPreferences
import cs.ut.engine.JobManager
import cs.ut.jobs.StartUpJob
import cs.ut.logging.NirdizatiLogger
import cs.ut.util.readHyperParameterJson
import org.apache.log4j.ConsoleAppender
import org.apache.log4j.FileAppender
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.log4j.PatternLayout
import java.io.File
import java.util.Timer
import javax.xml.bind.JAXBContext
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.concurrent.timerTask

object MasterConfiguration {
    private val log = NirdizatiLogger.getLogger(javaClass)

    val file: File = File(javaClass.classLoader.getResource("configuration.xml").file!!)

    lateinit var dirConfig: DirectoryConfiguration
    lateinit var modelConfiguration: ModelConfiguration

    lateinit var csvConfiguration: CsvConfiguration
    lateinit var threadPoolConfiguration: ThreadPoolConfiguration
    lateinit var headerConfiguration: HeaderConfiguration
    lateinit var pageConfiguration: PageConfiguration
    lateinit var userPreferences: UserPreferences
    lateinit var defaultValuesConfiguration: DefaultValuesConfiguration
    lateinit var tooltipConfig: TooltipConfig

    lateinit var optimizedParams: Map<String, List<ModelParameter>>

    init {
        configureLogger()
        readConfig()
        log.debug("Logger configured successfully")
        Timer().schedule(timerTask { JobManager.runServiceJob(StartUpJob()) }, 10000L)
    }

    /**
     * Configures logger and Enables appenders for Log4j
     */
    private fun configureLogger() {
        Logger.getRootLogger().level = Level.DEBUG
        Logger.getRootLogger().removeAllAppenders()
        Logger.getRootLogger().additivity = false

        val ca = ConsoleAppender()
        ca.layout = PatternLayout("<%d{ISO8601}> <%p> <%F:%L> <%m>%n")
        ca.threshold = Level.DEBUG
        ca.activateOptions()

        Logger.getRootLogger().addAppender(ca)

        val fileAppender = FileAppender()
        fileAppender.layout = PatternLayout("<%d{ISO8601}> <%p> <%F:%L> <%m>%n")
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

    fun readConfig() {
        dirConfig = readClass(DirectoryConfiguration::class.java, "dirs")
        modelConfiguration = readClass(ModelConfiguration::class.java, "modelConfig")
            .also {
                it.prepareData()
            }

        csvConfiguration = readClass(CsvConfiguration::class.java, "csvConfig")
        threadPoolConfiguration = readClass(ThreadPoolConfiguration::class.java, "threadpool")
        headerConfiguration = readClass(HeaderConfiguration::class.java, "headerConfiguration")
        pageConfiguration = readClass(PageConfiguration::class.java, "pageConfig")
        userPreferences = readClass(UserPreferences::class.java, "userPreferences")
        defaultValuesConfiguration = readClass(DefaultValuesConfiguration::class.java, "defaultConfig")
        tooltipConfig = readClass(TooltipConfig::class.java, "configuration")

        optimizedParams = readHyperParameterJson()
    }
}