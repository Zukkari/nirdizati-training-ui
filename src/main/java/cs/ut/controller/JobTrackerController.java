package cs.ut.controller;

import cs.ut.jobs.Job;
import cs.ut.ui.JobValueProvider;
import cs.ut.ui.NirdizatiGrid;
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

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        NirdizatiGrid<Job> jobGrid = new NirdizatiGrid<>(new JobValueProvider());

        Map<String, String> properties = new HashMap<>();
        properties.put(Labels.getLabel("tracker.job_name"), "140%");
        properties.put(Labels.getLabel("tracker.job_status"), "100%");

        jobGrid.setColumns(properties);
        jobGrid.setSclass("tracker_grid");
        jobGrid.setHflex("min");

        tracker.appendChild(jobGrid);
    }
}
