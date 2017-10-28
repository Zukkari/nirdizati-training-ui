package cs.ut.controller;

import cs.ut.config.MasterConfiguration;
import cs.ut.manager.LogManager;
import org.apache.log4j.Logger;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.Label;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

/**
 * Controller that responds for the log uploading page.
 */

public class UploadLogController extends SelectorComposer<Component> {
    private static final Logger log = Logger.getLogger(UploadLogController.class);
    private static final String SUPPORTED_FORMAT = ".XES";

    @Wire
    private Label fileName;

    @Wire
    Fileupload chooseFile;

    @Wire
    private Button uploadLog;

    private transient Media media;

    private transient LogManager manager = LogManager.getInstance();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        uploadLog.setVisible(false);
    }


    /**
     * Method that analyzes uploaded file. Checks that the file has required extension.
     *
     * @param event upload event where media should be retrieved from
     */
    @Listen("onUpload = #chooseFile")
    public void analyzeFile(UploadEvent event) {
        log.debug("Upload event. Analyzing file.");
        Media uploaded = event.getMedia();
        if (uploaded == null) {
            return;
        }

        if (SUPPORTED_FORMAT.equalsIgnoreCase(manager.getFileExtension(uploaded.getName()))) {
            log.debug("Log is in .XES format");
            fileName.setSclass("");
            fileName.setValue(uploaded.getName());
            saveMediaObject(uploaded);
            uploadLog.setVisible(true);
        } else {
            log.debug("Log is not in .XES format");
            log.debug("Showing error message");
            fileName.setSclass("error-label");
            fileName.setValue(Labels.getLabel("upload.wrong.format",
                    new Object[]{uploaded.getName(), manager.getFileExtension(uploaded.getName())}));
            uploadLog.setVisible(false);
        }
    }


    /**
     * Log serialization method. Starts a thread that serializes the log using the path configured in configuration.xml
     * Sends notification when process is completed successfully and then redirects the user.
     */
    @Listen("onClick = #uploadLog")
    public void processLog() {
        if (media != null) {

            Runnable serialization = () -> {
                File file = new File(String.format("%s/%s",
                        Paths.get(MasterConfiguration.getInstance().getDirectoryPathProvider().getUserLogDirectory())
                        , media.getName()));

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

                Clients.showNotification(Labels.getLabel("upload.success", new Object[]{media.getName()}), "info", getSelf(), "bottom_right", -1);
                MainPageController.getInstance().setContent("landing", getPage());
            };

            serialization.run();
            log.debug("Serialization thread stared");
        } else {
            fileName.setSclass("error-label");
            fileName.setValue(Labels.getLabel("upload.nothing"));
        }
    }

    /**
     * Saves media object reference so it could be serialized after user saves the log.
     *
     * @param media that should be saved for serialization.
     */
    private void saveMediaObject(Media media) {
        this.media = media;
    }


}
