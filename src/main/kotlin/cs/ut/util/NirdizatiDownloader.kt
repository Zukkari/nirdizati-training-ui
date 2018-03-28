package cs.ut.util

import cs.ut.configuration.ConfigurationReader
import cs.ut.logging.NirdizatiLogger
import cs.ut.providers.Dir
import cs.ut.providers.DirectoryConfiguration
import org.zkoss.zul.Filedownload
import java.io.File
import java.io.FileInputStream

class NirdizatiDownloader(private val dir: Dir, private val resourceId: String) {
    init {
        log.debug("Nirdizati downloader created with resource -> $resourceId")
    }

    fun execute() {
        log.debug("Executing download operation for resource $resourceId")
        val file = File(DirectoryConfiguration.dirPath(dir))
        val downloadFile = file.listFiles().first { it.name.contains(resourceId) }
        Filedownload.save(FileInputStream(downloadFile), configNode.valueWithIdentifier("mime").value, downloadFile.name)
        log.debug("Finished file download")
    }

    companion object {
        private val log = NirdizatiLogger.getLogger(NirdizatiDownloader::class.java)
        private val configNode = ConfigurationReader.findNode("modelDownload")
    }
}