package cs.ut.controller.modal;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import cs.ut.jobs.JobStatus;
import cs.ut.jobs.SimulationJob;
import cs.ut.ui.AttributeToLabelsProvider;
import cs.ut.ui.NirdizatiGrid;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.GenericAutowireComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Window;

import java.util.List;

public class JobInformationModalController extends GenericAutowireComposer<Component> {

    @Wire
    private Window jobInfo;

    @Wire
    private Hlayout grid;

    @Wire
    private Button closeBtn;

    private SimulationJob job;

    private NirdizatiGrid<Object> gridImpl;

    @Listen("onClick = #deployBtn")
    public void deploy() {
        /* TODO */
    }

    @Override
    @AfterCompose
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        this.job = (SimulationJob) arg.get("data");
        this.gridImpl = new NirdizatiGrid<>(new AttributeToLabelsProvider());

        List<?> listOfProperties = Lists.newArrayList(
                ImmutableMap.of("create_time", job.getCreateTime()),
                ImmutableMap.of("start_time", job.getStatus().equals(JobStatus.PENDING) ? "" : job.getStartTime()),
                ImmutableMap.of("complete_time", job.getStatus().equals(JobStatus.COMPLETED) ? job.getCompleteTime() : ""),
                job.getStatus(),
                job.getEncoding(),
                job.getBucketing(),
                job.getLearner(),
                job.getOutcome(),
                ImmutableMap.of("log_file", job.getLogFile())
        );

        this.gridImpl.generate(listOfProperties, true);
        this.gridImpl.getRows().setVflex("1");

        this.grid.appendChild(gridImpl);

        closeBtn.addEventListener(Events.ON_CLICK, e -> jobInfo.detach());
    }
}
