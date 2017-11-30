package cs.ut.controllers.modal;

import com.google.common.collect.Lists;
import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import cs.ut.config.MasterConfiguration;
import cs.ut.controllers.MainPageController;
import cs.ut.engine.JobManager;
import cs.ut.engine.NirdizatiThreadPool;
import cs.ut.jobs.DataSetGenerationJob;
import cs.ut.ui.GridValueProvider;
import cs.ut.ui.NirdizatiGrid;
import cs.ut.ui.providers.ColumnRowValueProvider;
import cs.ut.util.ConstKt;
import cs.ut.util.CsvReader;
import org.apache.log4j.Logger;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SerializableEventListener;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericAutowireComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Window;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParameterModalController extends GenericAutowireComposer<Component> {
    private static final Logger log = Logger.getLogger(ParameterModalController.class);

    @Wire
    private Window modal;

    @Wire
    private Hlayout gridSlot;

    private NirdizatiGrid<String> grid;

    @Wire
    private Button cancelBtn;

    @Wire
    private Button okBtn;

    private Rows rows;

    private List<String> cols = MasterConfiguration.getInstance().getCSVConfiguration().getUserCols();

    private List<Combobox> fields = new ArrayList<>();

    private SerializableEventListener<Event> okBtnListener;

    private Map<String, List<String>> identifiedColumns;

    private File file;

    private transient CsvReader csvReader;

    private static final String ignoreColumn = "params.ignore";

    @AfterCompose
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        this.file = (File) arg.get("file");
        csvReader = new CsvReader(file);

        log.debug(String.format("Modal received file <%s>", file));
        log.debug(String.format("Current log file : <%s>", file.getName()));
        List<String> fileColumns = csvReader.readTableHeader();
        log.debug(String.format("Columns present in table: <%s>", fileColumns));
        Collections.sort(fileColumns);

        Map<String, String> identifiedCols = new HashMap<>();
        csvReader.identifyUserColumns(fileColumns, identifiedCols);

        Escaper escaper = HtmlEscapers.htmlEscaper();

        GridValueProvider<String, Row> provider = new ColumnRowValueProvider(fileColumns, identifiedCols);
        grid = new NirdizatiGrid<>(provider);
        grid.setHflex("min");

        if (fileColumns.isEmpty()) {
            Clients.showNotification(Labels.getLabel(
                    "modals.unknown_separator",
                    new Object[]{escaper.escape(file.getName())}),
                    "error", getPage().getFirstRoot(),
                    "bottom_center",
                    -1);

            JobManager.Manager.flushJobs();
            modal.detach();
            return;
        }

        grid.generate(cols, true);

        cancelBtn.addEventListener(Events.ON_CLICK, (SerializableEventListener<Event>) e -> {
            Files.delete(Paths.get(file.getAbsolutePath()));
            modal.detach();
        });

        okBtnListener = e -> {
            okBtn.setDisabled(true);
            updateContent(csvReader.generateDatasetParams(grid.gatherValues()));
        };

        okBtn.addEventListener(Events.ON_CLICK, okBtnListener);

        gridSlot.appendChild(grid);
        log.debug("Showing modal");
    }

    private void updateContent(Map<String, List<String>> parameters) {
        okBtn.setDisabled(false);
        identifiedColumns = parameters;
        fields = new ArrayList<>();

        modal.setTitle(Labels.getLabel("modals.confirm_columns"));

        okBtn.removeEventListener(Events.ON_CLICK, okBtnListener);
        okBtnListener = e -> {
            Map<String, List<String>> acceptedParameters = gatherAcceptedValues();
            acceptedParameters.forEach((k, v) -> identifiedColumns.put(k, v));
            new NirdizatiThreadPool().execute(new DataSetGenerationJob(identifiedColumns, file, execution.getDesktop()));
            Clients.showNotification(Labels.getLabel("upload.success", new Object[]{HtmlEscapers.htmlEscaper().escape(file.getName())}), "info", getPage().getFirstRoot(), "bottom_right", -1);
            MainPageController.getInstance().setContent(ConstKt.PAGE_TRAINING, getPage());
            modal.detach();
        };

        okBtn.addEventListener(Events.ON_CLICK, okBtnListener);

        rows = new Rows();
        grid.getChildren().clear();
        grid.appendChild(rows);
        grid.setMold("paging");
        grid.setPageSize(10);

        Escaper escaper = HtmlEscapers.htmlEscaper();
        List<String> changeableVals = csvReader.getColumnList();
        changeableVals.sort(String::compareToIgnoreCase);

        changeableVals.forEach(key -> {
            List<String> columns = identifiedColumns.get(key);
            columns.sort(String::compareToIgnoreCase);

            columns.forEach(col -> {
                Row row = new Row();
                row.appendChild(new Label(escaper.escape(col)));

                Combobox combobox = new Combobox();
                combobox.setReadonly(true);
                combobox.setId(col);
                combobox.setConstraint("no empty");

                changeableVals.forEach(item -> {
                    Comboitem comboitem = combobox.appendItem(Labels.getLabel("params.".concat(item)));
                    comboitem.setValue(item);
                    if (item.equalsIgnoreCase(key)) combobox.setSelectedItem(comboitem);
                });

                Comboitem comboitem = combobox.appendItem(Labels.getLabel(ignoreColumn));
                comboitem.setValue(ignoreColumn);

                row.appendChild(combobox);
                rows.appendChild(row);
                fields.add(combobox);
            });
        });
    }

    private Map<String, List<String>> gatherAcceptedValues() {
        Map<String, List<String>> vals = new HashMap<>();

        fields.forEach(field -> {
            if (!ignoreColumn.equalsIgnoreCase(field.getSelectedItem().getValue())) {
                if (!vals.containsKey(field.getSelectedItem().getValue())) {
                    vals.put(field.getSelectedItem().getValue(), Lists.newArrayList(field.getId()));
                } else {
                    vals.get(field.getSelectedItem().getValue()).add(field.getId());
                }
            }
        });

        csvReader.getColumnList().forEach(col -> {
            if (!vals.containsKey(col)) {
                vals.put(col, new ArrayList<>());
            }
        });

        return vals;
    }
}
