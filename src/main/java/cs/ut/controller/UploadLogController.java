package cs.ut.controller;

import org.apache.log4j.Logger;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SerializableEventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;

public class UploadLogController extends SelectorComposer<Component> {
    private static final Logger log = Logger.getLogger(UploadLogController.class);
    private static final String SUPPORTED_FORMAT = ".XES";

    @Wire
    Label fileName;

    @Wire
    Button chooseFile;

    @Wire
    Button uploadLog;

    Media media;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        configureButtons();
    }

    private void configureButtons() {
        chooseFile.addEventListener(Events.ON_UPLOAD, new SerializableEventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                if (event instanceof UploadEvent) {
                    log.debug("Upload event. Analyzing file.");
                    Media uploaded = ((UploadEvent) event).getMedia();
                    if (uploaded == null) {
                        return;
                    }

                    if (SUPPORTED_FORMAT.equals(uploaded.getFormat())) {
                        log.debug("Log is in .XES format");
                        saveMediaObject(uploaded);
                    } else {
                        log.debug("Log is not in .XES format");
                        log.debug("Showing error message");
                        fileName.setSclass("error-label");
                        fileName.setValue(Labels.getLabel("upload.wrong.format", new Object[]{uploaded.getName(), uploaded.getFormat()}));
                    }
                }
            }
        });

        uploadLog.addEventListener(Events.ON_CLICK, new SerializableEventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {

            }
        });
    }

    private void saveMediaObject(Media media) {
        this.media = media;
    }
}
