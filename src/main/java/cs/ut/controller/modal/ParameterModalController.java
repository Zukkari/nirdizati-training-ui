package cs.ut.controller.modal;

import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import cs.ut.config.MasterConfiguration;
import cs.ut.engine.CsvReader;
import cs.ut.engine.JobManager;
import org.apache.log4j.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SerializableEventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.*;

import java.io.File;
import java.util.*;

public class ParameterModalController extends SelectorComposer<Component> {
    private static final Logger log = Logger.getLogger(ParameterModalController.class);

    @Wire
    private Window modal;

    @Wire
    private Grid paramGrid;

    @Wire
    private Button cancelBtn;

    @Wire
    private Button okBtn;

    private List<String> cols = MasterConfiguration.getInstance().getCSVConfiguration().getUserCols();

    private List<Combobox> fields = new ArrayList<>();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        File file = JobManager.getInstance().getCurrentFile();
        log.debug(String.format("Current log file : <%s>", file.getName()));
        List<String> fileColumns = CsvReader.readTableHeader(file);
        log.debug(String.format("Columns present in table: <%s>", fileColumns));
        Collections.sort(fileColumns);

        Map<String, String> identifiedCols = CsvReader.identifyUserColumns(fileColumns);

        Escaper escaper = HtmlEscapers.htmlEscaper();

        Rows rows = new Rows();

        if (fileColumns.isEmpty()) {
            Clients.showNotification(Labels.getLabel(
                    "modals.unknown_separator",
                    new Object[]{escaper.escape(file.getName())}),
                    "error", getSelf(),
                    "bottom_center",
                    -1);

            JobManager.getInstance().flushJobs();
            modal.detach();
            return;
        }

        cols.forEach(it -> {
            Row row = new Row();
            row.appendChild(new Label(Labels.getLabel("modals.param.".concat(it))));
            Combobox combobox = new Combobox();

            String identified = identifiedCols.get(it);

            combobox.setId(it);
            combobox.setReadonly(true);
            combobox.setConstraint("no empty");

            fileColumns.forEach(val -> {
                Comboitem comboitem = combobox.appendItem(escaper.escape(val));
                comboitem.setValue(val);

                if (val.equals(identified)) combobox.setSelectedItem(comboitem);
            });

            if (combobox.getSelectedItem() == null) {
                combobox.setSelectedItem(combobox.getItemAtIndex(0));
            }

            fields.add(combobox);
            row.appendChild(combobox);
            rows.appendChild(row);
        });

        paramGrid.appendChild(rows);

        cancelBtn.addEventListener(Events.ON_CLICK, (SerializableEventListener<Event>) e -> {
            JobManager.getInstance().flushJobs();
            modal.detach();
        });

        okBtn.addEventListener(Events.ON_CLICK, (SerializableEventListener<Event>) e -> {
            CsvReader reader = new CsvReader();
            reader.generateDatasetParams(gatherValues());
            JobManager.getInstance().delployJobs();
        });

        log.debug("Showing modal");
    }


    private Map<String, List<String>> gatherValues() {
        Map<String, List<String>> vals = new HashMap<>();

        fields.forEach(it -> vals.put(it.getId(), Collections.singletonList(it.getSelectedItem().getValue())));

        return vals;
    }
}