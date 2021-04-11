import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class BedIndexImplTest {

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun noMatches() {
        val data = listOf(
                BedEntry("chr1", 0, 5),
                BedEntry("chr1", 2, 3),
                BedEntry("chr1", 5, 10)
        )
        val index = BedIndexImpl(data)
        val result = index.search(BedEntry("chr1", 12, 13))
        assertTrue(result.isEmpty())
    }

    @Test
    fun oneChromosomeSeveralMatches() {
        val data = listOf(
                BedEntry("chr1", 0, 5),
                BedEntry("chr1", 2, 3),
                BedEntry("chr1", 5, 10)
        )
        val index = BedIndexImpl(data)
        val result = index.search(BedEntry("chr1", 1, 15))
        assertArrayEquals(
                result.sortedBy { it.start }.toTypedArray(),
                data.subList(1, 3).sortedBy { it.start }.toTypedArray()
        )
    }

    @Test
    fun severalChromosomesOneMatch() {
        val data = listOf(
                BedEntry("chr1", 0, 5),
                BedEntry("chr2", 2, 3),
                BedEntry("chr1", 5, 10)
        )
        val index = BedIndexImpl(data)
        val result = index.search(BedEntry("chr1", 1, 15))
        assertArrayEquals(
                result.sortedBy { it.start }.toTypedArray(),
                data.subList(2, 3).sortedBy { it.start }.toTypedArray()
        )
    }

    @AfterEach
    fun tearDown() {
    }
}