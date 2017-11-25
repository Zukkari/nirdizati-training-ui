package cs.ut.config.nodes

import cs.ut.config.MasterConfiguration
import org.junit.Test
import java.io.File
import kotlin.properties.Delegates
import kotlin.test.assertTrue

class DirectoryPathConfigurationTest {
    var dirConfig by Delegates.notNull<DirectoryPathConfiguration>()

    companion object {
        const val mustHaveRights = "Must have rights to create directory";
    }

    init {
        dirConfig = MasterConfiguration.getInstance().directoryPathConfiguration
    }

    @Test
    fun createDirTest() {
        val testDir = File("testDir")
        dirConfig.createDirIfAbsent(testDir)

        assertTrue(testDir.exists(), mustHaveRights)
        assertTrue(testDir.delete())
    }
}