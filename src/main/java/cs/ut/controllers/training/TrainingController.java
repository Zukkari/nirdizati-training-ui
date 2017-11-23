package cs.ut.controllers.training;

import com.google.common.collect.Lists;
import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import cs.ut.config.MasterConfiguration;
import cs.ut.config.items.ModelParameter;
import cs.ut.config.items.Property;
import cs.ut.engine.JobManager;
import cs.ut.manager.LogManager;
import cs.ut.ui.NirdizatiGrid;
import cs.ut.ui.providers.PropertyValueProvider;
import org.apache.log4j.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Vlayout;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TrainingController extends SelectorComposer<Component> {
    private static final Logger log = Logger.getLogger(TrainingController.class);
    protected static final String LEARNER = "learner";
    protected static final String ENCODING = "encoding";
    protected static final String BUCKETING = "bucketing";
    protected static final String PREDICTION = "predictiontype";

    @Wire
    private Combobox clientLogs;

    @Wire
    private Combobox predictionType;

    @Wire
    private Checkbox advancedMode;

    @Wire
    private Button startTraining;

    @Wire
    private Vlayout gridContainer;

    private transient ModeController gridController;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        log.debug("Initialized TrainingController");

        initClientLogs();
        initPredictions();

        gridController = new BasicModeController(gridContainer);
        gridController.init();
    }

    private void initPredictions() {
        List<ModelParameter> params = MasterConfiguration.getInstance().getModelConfiguration().getProperties().remove(PREDICTION);
        log.debug(String.format("Received %s prediction types", params.size()));

        params.forEach(it -> {
            Comboitem item = predictionType.appendItem(Labels.getLabel(it.getType().concat(".").concat(it.getId())));
            item.setValue(it);
        });

        predictionType.setSelectedItem(predictionType.getItemAtIndex(0));
        predictionType.setReadonly(true);
    }

    private void initClientLogs() {
        LogManager manager = LogManager.getInstance();
        List<File> fileNames = manager.getAllAvailableLogs();
        log.debug(String.format("Got %s items for client log combobox", fileNames.size()));

        Escaper escaper = HtmlEscapers.htmlEscaper();

        fileNames.forEach(file -> {
            Comboitem item = clientLogs.appendItem(escaper.escape(file.getName()));
            item.setValue(file);
        });

        if (!clientLogs.getItems().isEmpty()) {
            clientLogs.setSelectedItem(clientLogs.getItemAtIndex(0));
        } else {
            clientLogs.setDisabled(true);
        }
        clientLogs.setWidth("250px");
        clientLogs.setReadonly(true);

        clientLogs.addEventListener(Events.ON_CHANGE, e -> gridController.init());
    }

    @Listen("onCheck = #advancedMode")
    public void switchMode() {
        if (advancedMode.isChecked()) {
            log.debug("enabling advanced mode");
            gridController = new AdvancedModeController(gridContainer);
        } else {
            log.debug("enabling basic mode");
            gridController = new BasicModeController(gridContainer);
        }
        gridController.init();
    }

    @Listen("onClick = #startTraining")
    public void startTraining() {
        if (!gridController.isValid()) {
            return;
        }
        log.debug("Parameters are valid, calling script to construct the model");
        Runnable jobs = () -> {
            JobManager.Manager.setLogFile(clientLogs.getSelectedItem().getValue());
            Comboitem comboitem = predictionType.getSelectedItem();

            Map<String, List<ModelParameter>> jobParameters = new HashMap<>();
            jobParameters.put(PREDICTION, Lists.newArrayList((ModelParameter) comboitem.getValue()));
            jobParameters.putAll(gridController.gatherValues());

            JobManager.Manager.generateJobs(jobParameters);
            JobManager.Manager.deployJobs();
        };

        log.debug("Jobs generated...");
        jobs.run();
    }
}
