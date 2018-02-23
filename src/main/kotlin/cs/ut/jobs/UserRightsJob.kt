package cs.ut.jobs

import cs.ut.configuration.ConfigurationReader
import cs.ut.providers.Dir
import cs.ut.providers.DirectoryConfiguration
import org.apache.log4j.Logger
import java.io.File
import java.nio.charset.Charset

class UserRightsJob(private val f: File) : Job() {
    override fun execute() {
        log.debug("Starting ACL job for $id")

        log.debug("Setting permission ${configNode.valueWithIdentifier("acp").value}")
        updateACL(f)

        log.debug("Changing rights for training JSON")
        val name: String = f.nameWithoutExtension

        val path = "${DirectoryConfiguration.dirPath(Dir.DATA_DIR)}$name.json"
        log.debug("Looking for file -> $path")

        updateACL(File(path))
        log.debug("Changing rights for JSON successfully finished")
    }


    companion object {
        private val configNode = ConfigurationReader.findNode("userPreferences")!!

        val log = Logger.getLogger(UserRightsJob::class.java)!!

        fun updateACL(f: File) {
            if (!configNode.isEnabled()) {
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
                configNode.valueWithIdentifier("acp").value,
                f.absolutePath
            )
            pb.inheritIO()
            log.debug("Running -> ${pb.command()}")

            val process = pb.start()
            process.waitFor()
        }

        private fun updateOwnership(f: File) {
            val userName = configNode.valueWithIdentifier("userName").value
            val userGroup = configNode.valueWithIdentifier("userGroup").value

            log.debug("Updating ownership for $f -> " +
                    "new owner $userName:$userGroup")
            val pb = ProcessBuilder(
                "sudo",
                "-S",
                "chown",
                "$userName:$userGroup",
                f.absolutePath
            )

            log.debug("Command -> ${pb.command()}")
            val process = pb.start()
            val sudo = configNode.valueWithIdentifier("sudo").value
            if (sudo.isNotEmpty()) {
                process.outputStream.write((sudo + "\n\r").toByteArray(Charset.forName("UTF-8")))
            }

            process.waitFor()
            log.debug("Ownership updated for $f")
        }
    }
}