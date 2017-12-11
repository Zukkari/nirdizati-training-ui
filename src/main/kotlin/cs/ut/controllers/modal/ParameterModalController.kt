package cs.ut.controllers.modal

import com.google.common.html.HtmlEscapers
import cs.ut.config.MasterConfiguration
import cs.ut.controllers.MainPageController
import cs.ut.engine.JobManager
import cs.ut.engine.NirdizatiThreadPool
import cs.ut.jobs.DataSetGenerationJob
import cs.ut.ui.NirdizatiGrid
import cs.ut.ui.providers.ColumnRowValueProvider
import cs.ut.ui.providers.ComboArgument
import cs.ut.ui.providers.ComboProvider
import cs.ut.util.CsvReader
import cs.ut.util.TIMESTAMP_COL
import org.apache.log4j.Logger
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.event.SerializableEventListener
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zk.ui.util.GenericAutowireComposer
import org.zkoss.zul.Button
import org.zkoss.zul.Hlayout
import org.zkoss.zul.Window
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class ParameterModalController : GenericAutowireComposer<Component>() {
    private val log: Logger = Logger.getLogger(ParameterModalController::class.java)!!

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

    companion object {
        const val IGNORE_COL = "ignore"
    }

    override fun doAfterCompose(comp: Component?) {
        super.doAfterCompose(comp)

        cols = MasterConfiguration.getInstance().csvConfiguration.userCols
        log.debug("Read columns from master config: $cols")

        this.file = arg["file"] as File
        csvReader = CsvReader(file)

        log.debug("Received file with name ${file.name}")

        val header = csvReader.readTableHeader().sorted()
        log.debug("Read header of users file: $header")

        if (validateDataPresent(header)) return

        val identifiedColumns = mutableMapOf<String, String>()
        csvReader.identifyUserColumns(header.toMutableList(), identifiedColumns)
        identifiedColumns[TIMESTAMP_COL] = csvReader.getTimeStamp()

        val provider = ColumnRowValueProvider(header, identifiedColumns)
        val grid = NirdizatiGrid(provider)
        grid.hflex = "min"

        grid.generate(cols)

        cancelBtn.addEventListener(Events.ON_CLICK, { _ ->
            Files.delete(Paths.get(file.absolutePath))
            modal.detach()
        })

        okBtnListener = SerializableEventListener { _ ->
            okBtn.isDisabled = true
            updateContent(csvReader.generateDatasetParams(grid.gatherValues()))
        }

        okBtn.addEventListener(Events.ON_CLICK, okBtnListener)

        gridSlot.appendChild(grid)
        log.debug("Log parsing successful, showing modal")
    }

    @SuppressWarnings("unchecked")
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
            NirdizatiThreadPool().execute(DataSetGenerationJob(params, file, execution.desktop))
            Clients.showNotification(Labels.getLabel("upload.success", arrayOf(HtmlEscapers.htmlEscaper().escape(file.getName()))), "info", getPage().getFirstRoot(), "bottom_center", -1)
            MainPageController.getInstance().setContent("landing", getPage())
            modal.detach()
        }
        okBtn.addEventListener(Events.ON_CLICK, okBtnListener)

        val escaper = HtmlEscapers.htmlEscaper()
        var args = listOf<ComboArgument>()
        val changeable: List<String> = csvReader.getColumnList().sortedBy { it.toLowerCase() }
        changeable.forEach { key ->
            val cols = params[key]!!

            cols.forEach {
                args += ComboArgument(escaper.escape(it), changeable + IGNORE_COL, key)
            }
        }

        grid.generate(args)
    }

    private fun prepareGrid(): NirdizatiGrid<ComboArgument> {
        val grid = NirdizatiGrid(ComboProvider())
        gridSlot.getChildren<Component>().clear()
        gridSlot.getChildren<Component>().add(grid)

        grid.setMold("paging")
        grid.setPageSize(10)
        grid.hflex = "min"

        return grid
    }


    private fun validateDataPresent(header: List<String>): Boolean {
        if (header.isEmpty()) {
            Clients.showNotification(
                    Labels.getLabel("modals.unknown_separator",
                            arrayOf(HtmlEscapers.htmlEscaper().escape(file.name))),
                    "error", getPage().firstRoot,
                    "bottom_center"
                    , -1, true
            )
            JobManager.flushJobs()
            modal.detach()
            return true
        }
        return false
    }
}