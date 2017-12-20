package cs.ut.config.nodes

import cs.ut.config.MasterConfiguration
import org.junit.Test
import kotlin.properties.Delegates
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class PageConfigurationTest {
    var config by Delegates.notNull<PageConfiguration>()

    init {
        config = MasterConfiguration.pageConfiguration
    }

    @Test
    fun existingPageTest() {
        assertNotNull(config.getPageByName("upload"), "Existing page should be present")
    }

    @Test
    fun nonExistingPageTest() {
        assertFailsWith<NoSuchElementException> {
            config.getPageByName("randomnonexistingpage")
        }
    }
}