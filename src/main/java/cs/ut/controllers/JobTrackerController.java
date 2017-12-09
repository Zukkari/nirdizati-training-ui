package cs.ut.controllers;

import cs.ut.jobs.Job;
import cs.ut.ui.NirdizatiGrid;
import cs.ut.ui.providers.JobValueProvider;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.ClientInfoEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Hbox;

import java.util.HashMap;
import java.util.Map;

public class JobTrackerController extends SelectorComposer<Component> {
    @Wire
    private Hbox tracker;

    public static final String GRID_ID = "tracker_grid";
    public static final String TRACKER = "tracker";

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        NirdizatiGrid<Job> jobGrid = new NirdizatiGrid<>(new JobValueProvider(tracker));
        ((JobValueProvider) jobGrid.getProvider()).setOriginator(jobGrid);
        jobGrid.setVisible(false);

        jobGrid.setId(GRID_ID);
        tracker.appendChild(jobGrid);
    }
}
