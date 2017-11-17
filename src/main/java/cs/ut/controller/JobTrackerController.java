package cs.ut.controller;

import cs.ut.jobs.Job;
import cs.ut.ui.JobValueProvider;
import cs.ut.ui.NirdizatiGrid;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Hlayout;

public class JobTrackerController extends SelectorComposer<Component> {
    @Wire
    private Hlayout tracker;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
    }
}
