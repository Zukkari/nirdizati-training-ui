package cs.ut.ui.controllers

import com.google.common.html.HtmlEscapers
import cs.ut.config.MasterConfiguration
import cs.ut.config.items.ModelParameter
import cs.ut.engine.JobManager
import cs.ut.engine.LogManager
import cs.ut.jobs.Job
import cs.ut.jobs.SimulationJob
import cs.ut.ui.controllers.modal.ParameterModalController.Companion.FILE
import cs.ut.ui.controllers.modal.ParameterModalController.Companion.IS_RECREATION
import cs.ut.ui.controllers.training.AdvancedModeController
import cs.ut.ui.controllers.training.BasicModeController
import cs.ut.ui.controllers.training.ModeController
import cs.ut.util.CookieUtil
import cs.ut.util.NirdizatiUtil
import cs.ut.util.OUTCOME
import cs.ut.util.readLogColumns
import org.apache.commons.io.FilenameUtils
import org.apache.log4j.Logger
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.event.SelectEvent
import org.zkoss.zk.ui.select.SelectorComposer
import org.zkoss.zk.ui.select.annotation.Listen
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zul.*
import java.io.File
import javax.servlet.http.HttpServletRequest

class TrainingController : SelectorComposer<Component>(), Redirectable {
    private val log: Logger = Logger.getLogger(TrainingController::class.java)!!

    companion object {
        const val LEARNER = "learner"
        const val ENCODING = "encoding"
        const val BUCKETING = "bucketing"
        const val PREDICTION = "predictiontype"

        val DEFAULT = MasterConfiguration.defaultValuesConfiguration.minValue
        val AVERAGE = MasterConfiguration.defaultValuesConfiguration.average.toString()

        const val START_TRAINING = "startTraining"
        const val GENERATE_DATASET = "genDataSetParam"
    }

    @Wire
    private lateinit var clientLogs: Combobox

    @Wire
    private lateinit var predictionType: Combobox

    @Wire
    private lateinit var advancedMode: Checkbox

    @Wire
    private lateinit var gridContainer: Vlayout

    @Wire
    private lateinit var thresholdContainer: Hbox

    @Wire
    private lateinit var genDataSetParam: Button

    private lateinit var radioGroup: Radiogroup

    private lateinit var gridController: ModeController

    override fun doAfterCompose(comp: Component?) {
        super.doAfterCompose(comp)

        radioGroup = Radiogroup()

        initClientLogs()
        if (initPredictions()) {
            genDataSetParam.isDisabled = false
            gridController = BasicModeController(gridContainer, getLogFileName())
        }
    }

    private fun getLogFileName(): String = FilenameUtils.getBaseName((clientLogs.selectedItem.getValue() as File).name)

    private fun initPredictions(): Boolean {
        radioGroup.getChildren<Component>().clear()
        thresholdContainer.getChildren<Component>().clear()
        thresholdContainer.isVisible = false

        predictionType.items.clear()
        log.debug("Cleared prediction type items")

        val params: List<ModelParameter> = MasterConfiguration.modelConfiguration.properties[PREDICTION]!!
        log.debug("Received ${params.size} prediciton types")

        if (clientLogs.itemCount == 0) {
            predictionType.isDisabled = true
            return false
        }

        val logFile: File = clientLogs.selectedItem.getValue<File>() ?: return false

        val dataSetColumns: List<String> = readLogColumns(FilenameUtils.getBaseName(logFile.name))

        params.forEach {
            val item: Comboitem = predictionType.appendItem(NirdizatiUtil.localizeText("${it.type}.${it.id}"))
            val modelParam = ModelParameter(it)
            item.setValue(modelParam)

            if (modelParam.id == OUTCOME) {
                modelParam.setUpRadioButtons()
            }
        }

        dataSetColumns.forEach {
            val modelParameter = ModelParameter(it, it, PREDICTION, true, mutableListOf())
            modelParameter.translate = false
            val item: Comboitem = predictionType.appendItem(modelParameter.id)
            item.setValue(modelParameter)
        }

        predictionType.addEventListener(Events.ON_SELECT, { e ->
            e as SelectEvent<*, *>
            val param = (e.selectedItems.first() as Comboitem).getValue() as ModelParameter
            log.debug("Prediction type model changed to $param")
            if (param.id == OUTCOME) {
                thresholdContainer.isVisible = true
                log.debug("Prediciton type is $OUTCOME generating radio buttons")
            } else {
                log.debug("Clearing thresholdContainer")
                thresholdContainer.isVisible = false
            }
        })

        predictionType.selectedItem = predictionType.items[0]
        predictionType.isReadonly = true
        return true
    }

    private fun ModelParameter.setUpRadioButtons() {
        val avg = radioGroup.appendItem(NirdizatiUtil.localizeText("threshold.avg"), this.parameter)
        avg.setValue(this.parameter.toDouble())
        radioGroup.selectedItem = avg

        val custom = radioGroup.appendItem(NirdizatiUtil.localizeText("threshold.custom"), this.parameter)
        custom.setValue(DEFAULT)
        val customBox = Doublebox()
        customBox.width = "60px"
        customBox.setValue(DEFAULT)
        customBox.style = "padding-top: 10px"
        customBox.vflex = "1"
        customBox.addEventListener(Events.ON_CHANGE, { _ ->
            log.debug("New value for custom threshold ${customBox.value}")
            val res: Double? = customBox.value

            if (res == null || res <= 0) {
                custom.setValue(DEFAULT)
                customBox.setValue(DEFAULT)
                customBox.errorMessage = NirdizatiUtil.localizeText("threshold.custom_error", 0)
            } else {
                customBox.clearErrorMessage()
                custom.setValue(res)
            }
        })

        thresholdContainer.appendChild(radioGroup)
        thresholdContainer.appendChild(customBox)
    }

    private fun initClientLogs() {
        val files = LogManager.getAllAvailableLogs()
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

        clientLogs.isReadonly = true

        clientLogs.addEventListener(Events.ON_SELECT, { _ ->
            switchMode()
            initPredictions()
        })

        // Disable start button if logs are not found so simulation can not be started
        val startButton = Executions.getCurrent().desktop.components.firstOrNull { it.id == START_TRAINING }
        startButton?.let {
            startButton as Button
            if (files.isEmpty()) {
                startButton.isDisabled = true
                advancedMode.isDisabled = true
            }
        }
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

        val jobParameters = mutableMapOf<String, List<ModelParameter>>()
        jobParameters.put(PREDICTION, listOf(predictionType.selectedItem.getValue()))
        jobParameters.putAll(gridController.gatherValues())

        if (!jobParameters.validateParameters()) return

        val prediction: ModelParameter = jobParameters[PREDICTION]!!.first()

        if (prediction.id == OUTCOME) {
            val value = (radioGroup.selectedItem.getValue() as Double)
            prediction.parameter = if (value == -1.0) AVERAGE else value.toString()
        }

        log.debug("Parameters are valid, calling script to train the model")
        val jobThread = Runnable {
            passJobs(jobParameters)
        }
        jobThread.run()
        log.debug("Job generation thread started")
    }

    private fun passJobs(jobParameters: MutableMap<String, List<ModelParameter>>) {
        log.debug("Generating jobs -> $jobParameters")
        val encodings = jobParameters[ENCODING]!!
        val bucketings = jobParameters[BUCKETING]!!
        val predictionTypes = jobParameters[PREDICTION]!!
        val learners = jobParameters[LEARNER]!!

        val jobs: MutableList<Job> = mutableListOf()
        encodings.forEach { encoding ->
            bucketings.forEach { bucketing ->
                learners.forEach { learner ->
                    predictionTypes.forEach { pred ->
                        jobs.add(
                            SimulationJob(
                                encoding,
                                bucketing,
                                learner,
                                pred,
                                clientLogs.selectedItem.getValue(),
                                CookieUtil().getCookieKey(Executions.getCurrent().nativeRequest as HttpServletRequest)
                            )
                        )
                    }
                }
            }
        }
        log.debug("Generated ${jobs.size} jobs")
        JobManager.deployJobs(
            CookieUtil().getCookieKey(Executions.getCurrent().nativeRequest as HttpServletRequest),
            jobs
        )
    }

    private fun Map<String, List<ModelParameter>>.validateParameters(): Boolean {
        var isValid = true
        var msg = ""

        if (this[ENCODING] == null) {
            msg += ENCODING
            isValid = false
        }

        if (this[BUCKETING] == null) {
            msg += if (msg == "") BUCKETING else ", ${BUCKETING}"
            isValid = false
        }

        if (this[LEARNER] == null) {
            msg += if (msg == "") LEARNER else ", ${LEARNER}"
            isValid = false
        }

        if (!isValid) {
            NirdizatiUtil.showNotificationAsync(
                Labels.getLabel("training.validation_failed", arrayOf(msg)),
                Executions.getCurrent().desktop,
                "error"
            )
        }

        return isValid
    }

    @Listen("onClick = #genDataSetParam")
    fun generateNewDatasetParams() {
        genDataSetParam.isDisabled = true
        log.debug("Started new dataset parameter generation for -> ${clientLogs.value}")
        val args = mapOf<String, Any>(FILE to clientLogs.selectedItem.getValue(), IS_RECREATION to true)
        val window: Window = Executions.createComponents(
            "/views/modals/params.zul",
            self,
            args
        ) as Window
        if (self.getChildren<Component>().contains(window)) {
            window.doModal()
        }
    }
}