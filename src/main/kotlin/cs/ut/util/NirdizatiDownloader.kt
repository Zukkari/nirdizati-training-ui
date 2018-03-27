package cs.ut.util

import cs.ut.configuration.ConfigurationReader
import cs.ut.logging.NirdizatiLogger
import org.zkoss.zul.Filedownload
import java.io.File
import java.io.FileInputStream

class NirdizatiDownloader(private val pathToResource: String) {
    init {
        log.debug("Nirdizati downloader created with resource -> $pathToResource")
    }

    fun execute() {
        log.debug("Executing download operation for resource $pathToResource")
        val file = File(pathToResource)
        Filedownload.save(FileInputStream(file), configNode.valueWithIdentifier("mime").value, file.name)
        log.debug("Finished file download")
    }

    companion object {
        private val log = NirdizatiLogger.getLogger(NirdizatiDownloader::class.java)
        private val configNode = ConfigurationReader.findNode("modelDownload")
    }
}