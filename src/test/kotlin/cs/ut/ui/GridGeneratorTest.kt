package cs.ut.ui

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GridGeneratorTest {

    @Test
    fun isInRangeTest() {
        assertTrue(isInRange(10))
        assertTrue(isInRange(5, 1.0, 10.0))
        assertFalse(isInRange(11, 1.0, 10.0))
        assertFalse(isInRange(0, 1.0, 10.0))
    }

    @Test
    fun isInRangeInclusiveTest() {
        assertTrue(isInRange(1, 1.0, 1.0))
        assertTrue(isInRange(1, 1.0, 10.0))
        assertTrue(isInRange(10, 1.0, 10.0))
    }
}