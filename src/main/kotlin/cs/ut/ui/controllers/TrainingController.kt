package cs.ut.ui.controllers

import com.google.common.html.HtmlEscapers
import cs.ut.configuration.ConfigurationReader
import cs.ut.engine.JobManager
import cs.ut.engine.LogManager
import cs.ut.engine.item.ModelParameter
import cs.ut.exceptions.Left
import cs.ut.exceptions.Right
import cs.ut.jobs.Job
import cs.ut.jobs.SimulationJob
import cs.ut.json.JSONService
import cs.ut.json.TrainingConfiguration
import cs.ut.logging.NirdizatiLogger
import cs.ut.providers.ModelParamProvider
import cs.ut.ui.UIComponent
import cs.ut.ui.controllers.modal.ParameterModalController.Companion.FILE
import cs.ut.ui.controllers.modal.ParameterModalController.Companion.IS_RECREATION
import cs.ut.ui.controllers.training.AdvancedModeController
import cs.ut.ui.controllers.training.BasicModeController
import cs.ut.ui.controllers.training.ModeController
import cs.ut.util.Algorithm
import cs.ut.util.Cookies
import cs.ut.util.NirdizatiTranslator
import cs.ut.util.UPLOADED_FILE
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.event.SelectEvent
import org.zkoss.zk.ui.select.SelectorComposer
import org.zkoss.zk.ui.select.annotation.Listen
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zul.A
import org.zkoss.zul.Button
import org.zkoss.zul.Checkbox
import org.zkoss.zul.Combobox
import org.zkoss.zul.Comboitem
import org.zkoss.zul.Doublebox
import org.zkoss.zul.Radio
import org.zkoss.zul.Radiogroup
import org.zkoss.zul.Vlayout
import org.zkoss.zul.Window
import java.io.File

class TrainingController : SelectorComposer<Component>(), Redirectable, UIComponent {
    private val log = NirdizatiLogger.getLogger(TrainingController::class, getSessionId())

    companion object {
        const val LEARNER = "learner"
        const val ENCODING = "encoding"
        const val BUCKETING = "bucketing"
        const val PREDICTION = "predictiontype"


        private val configNode = ConfigurationReader.findNode("defaultValues")
        val DEFAULT: Double = configNode.values.first { it.identifier == "minimum" }.value()
        val AVERAGE = configNode.values.first { it.identifier == "average" }.value

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
    private lateinit var genDataSetParam: A

    @Wire
    private lateinit var radioGroup: Radiogroup

    @Wire
    private lateinit var avgRadio: Radio

    @Wire
    private lateinit var customRadio: Radio

    @Wire
    private lateinit var customBox: Doublebox

    private lateinit var gridController: ModeController

    override fun doAfterCompose(comp: Component?) {
        super.doAfterCompose(comp)

        initClientLogs()
        if (initPredictions()) {
            genDataSetParam.isDisabled = false
            gridController = BasicModeController(gridContainer, getLogFileName())
        }
    }

    /**
     * Get log file name from combo box
     *
     * @return log file name from the client log combo
     */
    private fun getLogFileName(): String = (clientLogs.selectedItem.getValue() as File).nameWithoutExtension

    /**
     * Init predictions combo box
     *
     * @return whether initialization was successful
     */
    private fun initPredictions(): Boolean {
        customBox.isDisabled = true
        avgRadio.isDisabled = true
        customRadio.isDisabled = true

        predictionType.items.clear()
        log.debug("Cleared prediction type items")

        val params: List<ModelParameter> = ModelParamProvider().getPredictionTypes()
        log.debug("Received ${params.size} prediciton types")

        if (clientLogs.itemCount == 0) {
            predictionType.isDisabled = true
            return false
        }

        val logFile: File = clientLogs.selectedItem.getValue<File>() ?: return false

        val res = JSONService
                .getTrainingData(logFile.nameWithoutExtension)

        val dataSetColumns: List<String> =
                when (res) {
                    is Right -> res.result.getAllColumns()
                    is Left -> {
                        log.debug("Error occurred when fetching dataset columns", res.error)
                        listOf()
                    }
                }

        params.forEach {
            val item: Comboitem = predictionType.appendItem(NirdizatiTranslator.localizeText("${it.type}.${it.id}"))
            val modelParam = it.copy()
            item.setValue(modelParam)

            if (modelParam.id == Algorithm.OUTCOME.value) {
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
            if (param.id == Algorithm.OUTCOME.value) {
                customBox.isDisabled = false
                avgRadio.isDisabled = false
                customRadio.isDisabled = false
                log.debug("Prediciton type is ${param.id} generating radio buttons")
            } else {
                log.debug("Clearing thresholdContainer")
                customBox.isDisabled = true
                avgRadio.isDisabled = true
                customRadio.isDisabled = true
            }
        })

        predictionType.selectedItem = predictionType.items[0]
        predictionType.isReadonly = true
        return true
    }

    /**
     * Set up radio buttons for threshold selection
     */
    private fun ModelParameter.setUpRadioButtons() {
        avgRadio.setValue(this.parameter.toDouble())
        customRadio.setValue(DEFAULT)
        customBox.setValue(DEFAULT)

        customBox.addEventListener(Events.ON_CHANGE, { _ ->
            log.debug("New value for custom threshold ${customBox.value}")
            val res: Double? = customBox.value

            if (res == null || res <= 0) {
                customRadio.setValue(DEFAULT)
                customBox.setValue(DEFAULT)
                customBox.errorMessage = NirdizatiTranslator.localizeText("threshold.custom_error", 0)
            } else {
                customBox.clearErrorMessage()
                customBox.setValue(res)
                customRadio.setValue(res)
            }
        })
    }

    /**
     * Load client logs into the combo
     */
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

        Executions.getCurrent().desktop.getAttribute(UPLOADED_FILE)?.apply {
            this as File
            clientLogs.items.forEach {
                if ((it.getValue() as File) == this) {
                    clientLogs.selectedItem = it
                }
            }
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
            true -> {
                gridController.preDestroy()
                AdvancedModeController(gridContainer)
            }
            false -> {
                gridController.preDestroy()
                BasicModeController(gridContainer, getLogFileName())
            }
        }
    }

    @Listen("onClick = #startTraining")
    fun startTraining() {
        if (!gridController.isValid()) return

        val jobParameters = mutableMapOf<String, List<ModelParameter>>()
        jobParameters[PREDICTION] = listOf(predictionType.selectedItem.getValue())
        jobParameters.putAll(gridController.gatherValues())

        if (!jobParameters.validateParameters()) return

        val prediction: ModelParameter = jobParameters[PREDICTION]!!.first()

        if (prediction.id == Algorithm.OUTCOME.value) {
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

    /**
     * Pass jobs to job manager for execution
     *
     * @param jobParameters to generate jobs from
     */
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
                        val config = TrainingConfiguration(encoding, bucketing, learner, pred)
                        jobs.add(
                                SimulationJob(
                                        config,
                                        clientLogs.selectedItem.getValue(),
                                        Cookies.getCookieKey(Executions.getCurrent().nativeRequest)
                                )
                        )
                    }
                }
            }
        }
        log.debug("Generated ${jobs.size} jobs")
        JobManager.deployJobs(
                Cookies.getCookieKey(Executions.getCurrent().nativeRequest),
                jobs
        )
    }

    /**
     * Validate that all the required parameters are present
     *
     * @return whether or not data is present
     */
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
            NirdizatiTranslator.showNotificationAsync(
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