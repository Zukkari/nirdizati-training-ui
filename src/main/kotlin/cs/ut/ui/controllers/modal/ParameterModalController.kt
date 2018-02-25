package cs.ut.ui.controllers.modal

import com.google.common.html.HtmlEscapers
import cs.ut.configuration.ConfigurationReader
import cs.ut.configuration.Value
import cs.ut.engine.JobManager
import cs.ut.jobs.DataSetGenerationJob
import cs.ut.jobs.UserRightsJob
import cs.ut.logging.NirdizatiLogger
import cs.ut.providers.Dir
import cs.ut.providers.DirectoryConfiguration
import cs.ut.ui.NirdizatiGrid
import cs.ut.ui.UIComponent
import cs.ut.ui.adapters.ColumnRowValueAdapter
import cs.ut.ui.adapters.ComboArgument
import cs.ut.ui.adapters.ComboProvider
import cs.ut.ui.controllers.Redirectable
import cs.ut.ui.controllers.TrainingController.Companion.GENERATE_DATASET
import cs.ut.util.CsvReader
import cs.ut.util.NirdizatiUtil
import cs.ut.util.TIMESTAMP_COL
import cs.ut.util.UPLOADED_FILE
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.event.SerializableEventListener
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zk.ui.util.GenericAutowireComposer
import org.zkoss.zul.A
import org.zkoss.zul.Button
import org.zkoss.zul.Hlayout
import org.zkoss.zul.Window
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class ParameterModalController : GenericAutowireComposer<Component>(), Redirectable, UIComponent {
    private val log = NirdizatiLogger.getLogger(ParameterModalController::class.java, getSessionId())

    @Wire
    private lateinit var modal: Window

    @Wire
    private lateinit var gridSlot: Hlayout

    @Wire
    private lateinit var cancelBtn: Button

    @Wire
    private lateinit var okBtn: Button

    private lateinit var cols: List<String>

    private lateinit var okBtnListener: SerializableEventListener<Event>

    private lateinit var file: File

    private lateinit var csvReader: CsvReader

    private var isRecreation: Boolean = false

    companion object {
        const val FILE = "file"
        const val IS_RECREATION = "isRecreation"
    }

    override fun doAfterCompose(comp: Component?) {
        super.doAfterCompose(comp)

        cols = ConfigurationReader.findNode("csv/userCols").itemListValues()
        log.debug("Read columns from master config: $cols")

        this.file = arg[FILE] as File
        this.isRecreation = arg[IS_RECREATION] as Boolean? ?: false

        csvReader = CsvReader(file)

        log.debug("Received file with name ${file.name}")

        val header = csvReader.readTableHeader().sorted()
        log.debug("Read header of users file: $header")

        if (validateDataPresent(header)) return

        val identifiedColumns = mutableMapOf<String, String>()
        csvReader.identifyUserColumns(header.toMutableList(), identifiedColumns)
        identifiedColumns[TIMESTAMP_COL] = csvReader.getTimeStamp()

        val provider = ColumnRowValueAdapter(header, identifiedColumns)
        val grid = NirdizatiGrid(provider)
        grid.hflex = "min"
        grid.vflex = "1"
        grid.sclass = "max-width max-height"

        grid.generate(cols)

        cancelBtn.addEventListener(Events.ON_CLICK, { _ ->
            if (!isRecreation) {
                Files.delete(Paths.get(file.absolutePath))
                Executions.getCurrent().desktop.components.firstOrNull { it.id == "upload" }?.let {
                    it as Button
                    it.isDisabled = false
                }
            }

            enableGenerateButton()
            modal.detach()
        })

        okBtnListener = SerializableEventListener { _ ->
            okBtn.isDisabled = true
            updateContent(csvReader.generateDataSetParams(grid.gatherValues()))
        }

        okBtn.addEventListener(Events.ON_CLICK, okBtnListener)

        gridSlot.appendChild(grid)
        log.debug("Log parsing successful, showing modal")
    }

    /**
     * Enable generate data set parameters button when window is closed
     */
    private fun enableGenerateButton() {
        Executions.getCurrent().desktop.components.firstOrNull { it.id == GENERATE_DATASET }?.let {
            it as A
            it.isDisabled = false
        }
    }

    /**
     * Update window content based on new data
     *
     * @param params to generate content from
     */
    @Suppress("UNCHECKED_CAST")
    private fun updateContent(params: MutableMap<String, MutableList<String>>) {
        okBtn.isDisabled = false
        log.debug("Updating content with params $params")

        val grid = prepareGrid()

        modal.title = Labels.getLabel("modals.confirm_columns")
        okBtn.removeEventListener(Events.ON_CLICK, okBtnListener)
        log.debug("Removed ok button listener")

        okBtnListener = SerializableEventListener { _ ->
            val accepted = grid.gatherValues() as Map<String, String>
            accepted.forEach { k, v ->
                params.values.forEach {
                    if (k in it) it.remove(k)
                }

                if (v in params) {
                    params[v]!!.add(k)
                } else {
                    params[v] = mutableListOf(k)
                }
            }

            JobManager.runServiceJob(DataSetGenerationJob(params, file))

            if (!isRecreation) {
                NirdizatiUtil.showNotificationAsync(
                    Labels.getLabel("upload.success", arrayOf(HtmlEscapers.htmlEscaper().escape(file.name))),
                    Executions.getCurrent().desktop
                )

                val target = Files.move(
                    Paths.get(file.absolutePath),
                    Paths.get(File(DirectoryConfiguration.dirPath(Dir.USER_LOGS) + file.name).absolutePath),
                    StandardCopyOption.REPLACE_EXISTING
                )

                Executions.getCurrent().desktop.setAttribute(UPLOADED_FILE, target.toFile())
                JobManager.runServiceJob(UserRightsJob(target.toFile()))
                setContent("training", getPage(), 2000, Executions.getCurrent().desktop)
            } else {
                NirdizatiUtil.showNotificationAsync(
                    NirdizatiUtil.localizeText("param.modal.generated"), Executions.getCurrent().desktop
                )
            }

            enableGenerateButton()
            modal.detach()
        }
        okBtn.addEventListener(Events.ON_CLICK, okBtnListener)

        val escaper = HtmlEscapers.htmlEscaper()
        var args = listOf<ComboArgument>()
        val changeable: List<Value> = csvReader.getColumnList()
        changeable.forEach { key ->
            params[key.identifier]?.forEach {
                args += ComboArgument(escaper.escape(it), changeable, key.identifier)
            }
        }

        grid.generate(args)
    }

    /**
     * Prepare grid for generation, set columns, css class and flex
     */
    private fun prepareGrid(): NirdizatiGrid<ComboArgument> {
        val grid = NirdizatiGrid(ComboProvider())
        gridSlot.getChildren<Component>().clear()
        gridSlot.getChildren<Component>().add(grid)

        grid.setColumns(mapOf("param.modal.name" to "", "param.modal.control" to ""))
        grid.mold = "paging"
        grid.pageSize = 10
        grid.hflex = "min"
        grid.vflex = "1"
        grid.sclass = "max-width max-height no-hor-overflow"

        return grid
    }


    /**
     * Make sure data is present in the log, if not show notification to the user
     *
     * @param header where to validate data
     *
     * @return whether data is present or not
     */
    private fun validateDataPresent(header: List<String>): Boolean {
        if (header.isEmpty()) {
            NirdizatiUtil.showNotificationAsync(
                Labels.getLabel(
                    "modals.unknown_separator",
                    arrayOf(HtmlEscapers.htmlEscaper().escape(file.name))
                ),
                Executions.getCurrent().desktop,
                "error"
            )
            modal.detach()
            return true
        }
        return false
    }
}