package cs.ut.controllers

import com.google.common.html.HtmlEscapers
import cs.ut.config.MasterConfiguration
import cs.ut.config.items.ModelParameter
import cs.ut.controllers.training.AdvancedModeController
import cs.ut.controllers.training.BasicModeController
import cs.ut.controllers.training.ModeController
import cs.ut.engine.JobManager
import cs.ut.engine.LogManager
import cs.ut.util.NirdizatiUtil
import org.apache.commons.io.FilenameUtils
import org.apache.log4j.Logger
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.select.SelectorComposer
import org.zkoss.zk.ui.select.annotation.Listen
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Checkbox
import org.zkoss.zul.Combobox
import org.zkoss.zul.Vlayout
import java.io.File

class TrainingController : SelectorComposer<Component>() {
    private val log: Logger = Logger.getLogger(TrainingController::class.java)!!

    companion object {
        const val LEARNER = "learner"
        const val ENCODING = "encoding"
        const val BUCKETING = "bucketing"
        const val PREDICTION = "predictiontype"
    }

    @Wire
    private lateinit var clientLogs: Combobox

    @Wire
    private lateinit var predictionType: Combobox

    @Wire
    private lateinit var advancedMode: Checkbox

    @Wire
    private lateinit var gridContainer: Vlayout

    private lateinit var gridController: ModeController

    override fun doAfterCompose(comp: Component?) {
        super.doAfterCompose(comp)

        initClientLogs()
        initPredictions()

        gridController = BasicModeController(gridContainer, getLogFileName())
    }

    private fun getLogFileName(): String = FilenameUtils.getBaseName((clientLogs.selectedItem.getValue() as File).name)

    private fun initPredictions() {
        val params: List<ModelParameter> = MasterConfiguration.getInstance().modelConfiguration.properties[PREDICTION]!!
        log.debug("Received ${params.size} prediciton types")

        params.forEach {
            val item = predictionType.appendItem(NirdizatiUtil.localizeText("${it.type}.${it.id}"))
            item.setValue(it)
        }

        predictionType.selectedItem = predictionType.items[0]
        predictionType.isReadonly = true
    }

    private fun initClientLogs() {
        val manager = LogManager()
        val files = manager.getAllAvailableLogs()
        log.debug("Found ${files.size} log files")

        val escaper = HtmlEscapers.htmlEscaper()
        files.forEach {
            val item = clientLogs.appendItem(escaper.escape(it.name))
            item.setValue(it)
        }

        if (clientLogs.itemCount > 0) {
            clientLogs.selectedItem = clientLogs.items[0]
        } else {
            clientLogs.isDisabled = true
        }

        clientLogs.width = "250px"
        clientLogs.isReadonly = true

        clientLogs.addEventListener(Events.ON_SELECT, { _ ->
            switchMode()
        })
    }

    @Listen("onCheck = #advancedMode")
    fun switchMode() {
        gridController = when (advancedMode.isChecked) {
            true -> AdvancedModeController(gridContainer)
            false -> BasicModeController(gridContainer, getLogFileName())
        }
    }

    @Listen("onClick = #startTraining")
    fun startTraining() {
        if (!gridController.isValid()) return

        val jobParamters = mutableMapOf<String, List<ModelParameter>>()
        jobParamters.put(PREDICTION, listOf(predictionType.selectedItem.getValue()))
        jobParamters.putAll(gridController.gatherValues())

        if (!jobParamters.validateParameters()) return

        log.debug("Parameters are valid, calling script to train the model")
        val jobThread = Runnable {
            JobManager.logFile = clientLogs.selectedItem.getValue()
            JobManager.generateJobs(jobParamters)
            JobManager.deployJobs()
        }
        jobThread.run()
        log.debug("Job generation thread started")
    }

    private fun Map<String, List<ModelParameter>>.validateParameters(): Boolean {
        var isValid = true
        var msg = ""

        if (this[ENCODING] == null) {
            msg += ENCODING
            isValid = false
        }

        if (this[BUCKETING] == null) {
            msg += if (msg == "") BUCKETING else ", $BUCKETING"
            isValid = false
        }

        if (this[LEARNER] == null) {
            msg += if (msg == "") LEARNER else ", $LEARNER"
            isValid = false
        }

        if (!isValid) {
            Clients.showNotification(
                    Labels.getLabel("training.validation_failed", arrayOf(msg)),
                    "error",
                    self,
                    "bottom_center",
                    -1)
        }

        return isValid
    }
}