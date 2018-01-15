package cs.ut.jobs

import cs.ut.config.MasterConfiguration
import cs.ut.config.nodes.Dir
import org.apache.commons.io.FilenameUtils
import org.apache.log4j.Logger
import java.io.File
import java.nio.charset.Charset

class UserRightsJob(private val f: File) : Job() {
    private val conf = MasterConfiguration.dirConfig

    override fun execute() {
        log.debug("Starting ACL job for $id")

        log.debug("Setting permission ${prefs.acp}")
        updateACL(f)

        log.debug("Changing rights for training JSON")
        val name: String = FilenameUtils.getBaseName(f.name)

        val path = "${conf.dirPath(Dir.DATA_DIR)}$name.json"
        log.debug("Looking for file -> $path")

        updateACL(File(path))
        log.debug("Changing rights for JSON successfully finished")
    }


    companion object {
        private val prefs = MasterConfiguration.userPreferences

        val log = Logger.getLogger(UserRightsJob::class.java)!!

        fun updateACL(f: File) {
            if (!prefs.enabled) {
                log.debug("ACL updating is disabled -> skipping")
                return
            }

            updateOwnership(f)
            updateRights(f)
        }

        private fun updateRights(f: File) {
            log.debug("Updating ACL -> $f")
            val pb = ProcessBuilder(
                    "chmod",
                    prefs.acp,
                    f.absolutePath)
            pb.inheritIO()
            log.debug("Running -> ${pb.command()}")

            val process = pb.start()
            process.waitFor()
        }

        private fun updateOwnership(f: File) {
            log.debug("Updating ownership for $f -> new owner ${prefs.userName}:${prefs.userGroup}")
            val pb = ProcessBuilder(
                    "sudo",
                    "-S",
                    "chown",
                    "${prefs.userName}:${prefs.userGroup}",
                    f.absolutePath)

            log.debug("Command -> ${pb.command()}")
            val process = pb.start()
            if (prefs.sudo.isNotEmpty()) {
                process.outputStream.write((prefs.sudo + "\n\r").toByteArray(Charset.forName("UTF-8")))
            }

            process.waitFor()
            log.debug("Ownership updated for $f")
        }
    }
}