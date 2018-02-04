package cs.ut.ui.controllers

import com.lowagie.text.pdf.codec.Base64
import cs.ut.config.MasterConfiguration
import cs.ut.config.nodes.Dir
import cs.ut.config.nodes.UserPreferences
import cs.ut.engine.NirdizatiThreadPool
import cs.ut.logging.NirdLogger
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.select.SelectorComposer
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zul.Button
import org.zkoss.zul.Textbox
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.nio.charset.Charset

class AdminViewController : SelectorComposer<Component>() {
    val log = NirdLogger(caller = this.javaClass)

    @Wire
    private lateinit var flushConfig: Button

    @Wire
    private lateinit var flushMessages: Button

    @Wire
    private lateinit var killThreadPool: Button

    @Wire
    private lateinit var restartThreadPool: Button

    @Wire
    private lateinit var passwordField: Textbox

    @Wire
    private lateinit var showLogs: Button

    @Wire
    private lateinit var logData: Textbox

    private val config: UserPreferences = MasterConfiguration.userPreferences

    private val logFile: File = MasterConfiguration.dirConfig.dirByName(Dir.LOG_FILE)

    override fun doAfterCompose(comp: Component?) {
        super.doAfterCompose(comp)

        flushConfig.addEventListener(Events.ON_CLICK, { _ ->
            log.debug("Flushing master configuration")
            MasterConfiguration.readConfig()
            log.debug("Master configuration flushed")
        })

        flushMessages.addEventListener(Events.ON_CLICK, { _ ->
            performTask(flushMessages())
        })

        killThreadPool.addEventListener(Events.ON_CLICK, { _ ->
            performTask(killThreadPool())
        })

        restartThreadPool.addEventListener(Events.ON_CLICK, { _ ->
            performTask(restartThreadPool())
        })

        showLogs.addEventListener(Events.ON_CLICK, { _ ->
            performTask(readLogFile())
        })
    }

    private fun performTask(task: Runnable) {
        if (isAuthorized()) {
            passwordField.errorMessage = ""
            task.run()
        } else {
            log.debug("Not authorized: ${passwordField.value}")
            passwordField.errorMessage = "Invalid key"
        }
    }

    private fun restartThreadPool(): Runnable = Runnable {
        log.debug("Restarting threadpool")
        NirdizatiThreadPool.restart()
        log.debug("Successfully restarted threadpool")
    }


    private fun killThreadPool(): Runnable = Runnable {
        log.debug("Killing threadpool")
        NirdizatiThreadPool.shutDown()
        log.debug("Successfully killed threadpool")
    }


    private fun flushMessages(): Runnable = Runnable {
        log.debug("Flushing messages files")
        Labels.reset()
        log.debug("Successfully flushed messages file")
    }

    private fun readLogFile(): Runnable = Runnable {
        if (logFile.exists() && logFile.isFile) {
            logData.isVisible = true
            log.debug("File exists and is a log file")
            logData.value = BufferedReader(FileReader(logFile)).lineSequence().joinToString("\n")
            log.debug("Finished parsing log file")
        }
    }

    private fun isAuthorized(): Boolean {
        return !config.requirePassword ||
                Base64.encodeBytes(
                    (passwordField.value ?: "").toByteArray(Charset.forName("UTF-8"))
                ) == config.authorized
    }

}