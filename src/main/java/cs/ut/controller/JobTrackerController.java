package cs.ut.controller;

import cs.ut.jobs.Job;
import cs.ut.ui.NirdizatiGrid;
import cs.ut.ui.providers.JobValueProvider;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Hbox;

import java.util.HashMap;
import java.util.Map;

public class JobTrackerController extends SelectorComposer<Component> {
    @Wire
    private Hbox tracker;

    public static final String GRID_ID = "tracker_grid";

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        NirdizatiGrid<Job> jobGrid = new NirdizatiGrid<>(new JobValueProvider());
        jobGrid.setId(GRID_ID);

        Map<String, String> properties = new HashMap<>();
        properties.put(Labels.getLabel("tracker.job_name"), "240%");
        properties.put(Labels.getLabel("tracker.job_status"), "140%");

        jobGrid.setColumns(properties);
        jobGrid.setSclass(GRID_ID);
        jobGrid.setHflex("min");

        jobGrid.setVflex("1");
        jobGrid.getRows().setVflex("1");

        tracker.appendChild(jobGrid);
    }
}
