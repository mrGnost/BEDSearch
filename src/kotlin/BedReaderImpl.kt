import java.nio.file.Files
import java.nio.file.Path

class BedReaderImpl : BedReader {
    override fun createIndex(bedPath: Path, indexPath: Path) {
        val index = BedIndexImpl(readBed(bedPath))
        index.serialize(indexPath)
    }

    override fun loadIndex(indexPath: Path): BedIndex {
        return BedIndexImpl(indexPath)
    }

    /**
     * I didn't really understand what you mean by using bedPath and didn't find how to contact you,
     * so I thought it should be just an intersection of index and bed data (in provided region, of course).
     * If I'm wrong please contact me, I can fix it without any problem (probably).
     */
    override fun findWithIndex(index: BedIndex, bedPath: Path, chromosome: String, start: Int, end: Int): List<BedEntry> {
        val bedData = readBed(bedPath)
        val found = index.search(BedEntry(chromosome, start, end, emptyList()))
        return bedData.intersect(found).toList()
    }

    private fun readBed(path: Path): List<BedEntry> {
        val data = Files.readAllLines(path)
        return data
                .dropWhile { x -> x.split(" ")[0] != "track" }
                .drop(1)
                .map { x ->
                    {
                        val entryData = x.split("\t")
                        BedEntry(entryData[0], entryData[1].toInt(), entryData[2].toInt(), entryData.subList(3, entryData.size))
                    }.invoke()
                }
    }
}