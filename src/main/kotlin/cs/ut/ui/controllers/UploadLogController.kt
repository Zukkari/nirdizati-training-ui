package cs.ut.ui.controllers

import cs.ut.config.MasterConfiguration
import org.apache.commons.io.FilenameUtils
import org.apache.log4j.Logger
import org.zkoss.util.media.Media
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.UploadEvent
import org.zkoss.zk.ui.select.SelectorComposer
import org.zkoss.zk.ui.select.annotation.Listen
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zul.Button
import org.zkoss.zul.Label
import org.zkoss.zul.Vbox
import org.zkoss.zul.Window
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset

class UploadLogController : SelectorComposer<Component>(), Redirectable {
    private val log: Logger = Logger.getLogger(UploadLogController::class.java)!!

    @Wire
    private lateinit var fileName: Label

    @Wire
    private lateinit var upload: Button

    @Wire
    private lateinit var fileNameCont: Vbox

    private lateinit var media: Media

    private val allowedExtensions = MasterConfiguration.csvConfiguration.extensions


    /**
     * Method that analyzes uploaded file. Checks that the file has required extension.
     *
     * @param event upload event where media should be retrieved from
     */
    @Listen("onUpload = #chooseFile")
    fun analyzeFile(event: UploadEvent) {
        log.debug("Upload event. Analyzing file")

        val uploaded = event.media ?: return

        if (FilenameUtils.getExtension(uploaded.name) in allowedExtensions) {
            log.debug("Log is in allowed format")
            fileNameCont.sclass = "file-upload"
            fileName.value = uploaded.name
            media = uploaded
            upload.isVisible = true
        } else {
            log.debug("Log is not in allowed format -> showing error")
            fileNameCont.sclass = "file-upload-err"
            fileName.value = Labels.getLabel(
                    "upload.wrong.format",
                    arrayOf(uploaded.name, FilenameUtils.getExtension(uploaded.name))
            )
            upload.isVisible = false
        }
    }

    @Listen("onClick = #upload")
    fun processLog() {
        val runnable = Runnable {
            val tmpDir = MasterConfiguration.directoryPathConfiguration.tmpDir
            val file = File(tmpDir + media.name)
            log.debug("Creating file: ${file.absolutePath}")
            file.createNewFile()

            FileOutputStream(file).use {
                it.write(media.stringData.replace("/", "-").toByteArray(Charset.forName("UTF-8")))
            }

            val args = mapOf("file" to file)
            val window: Window = Executions.createComponents(
                    "/views/modals/params.zul",
                    self,
                    args
            ) as Window
            if (self.getChildren<Component>().contains(window)) {
                window.doModal()
                upload.isDisabled = true
            }
        }
        runnable.run()
        log.debug("Started training file generation thread")
    }
}