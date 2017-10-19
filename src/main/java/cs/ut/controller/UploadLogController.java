package cs.ut.controller;

import cs.ut.config.MasterConfiguration;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
        uploadLog.setVisible(false);

        chooseFile.addEventListener(Events.ON_UPLOAD, new SerializableEventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                if (event instanceof UploadEvent) {
                    log.debug("Upload event. Analyzing file.");
                    Media uploaded = ((UploadEvent) event).getMedia();
                    if (uploaded == null) {
                        return;
                    }

                    if (SUPPORTED_FORMAT.equalsIgnoreCase(getFileExtension(uploaded.getName()))) {
                        log.debug("Log is in .XES format");
                        fileName.setSclass("");
                        fileName.setValue(uploaded.getName());
                        saveMediaObject(uploaded);
                        uploadLog.setVisible(true);
                    } else {
                        log.debug("Log is not in .XES format");
                        log.debug("Showing error message");
                        fileName.setSclass("error-label");
                        fileName.setValue(Labels.getLabel("upload.wrong.format", new Object[]{uploaded.getName(), getFileExtension(uploaded.getName())}));
                        uploadLog.setVisible(false);
                    }
                }
            }
        });

        uploadLog.addEventListener(Events.ON_CLICK, new SerializableEventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                if (media != null) {

                    Runnable serialization = () -> {
                        File file = new File(String.format("%s%s", MasterConfiguration.getInstance().getUserLogDirectory(), media.getName()));
                        log.debug(String.format("Writing file into : %s", file.getAbsolutePath()));

                        try (InputStream inputStream = media.getStreamData();
                             FileOutputStream fos = new FileOutputStream(file)) {

                            byte[] buffer = new byte[inputStream.available()];
                            int read = inputStream.read(buffer);

                            assert buffer.length == read : "Could not read all available bytes";

                            fos.write(buffer);
                        } catch (IOException e) {
                            log.debug(e);
                        }
                    };

                    serialization.run();
                    log.debug("Serialization thread stared");
                } else {
                    fileName.setSclass("error-label");
                    fileName.setValue(Labels.getLabel("upload.nothing"));
                }
            }
        });
    }

    private void saveMediaObject(Media media) {
        this.media = media;
    }

    private String getFileExtension(String name) {
        int lastIndex = name.lastIndexOf('.');
        if (lastIndex == -1) {
            return "";
        } else {
            return name.substring(lastIndex);
        }
    }
}
