import java.nio.file.Path
import com.google.gson.*
import java.nio.file.Files

class BedIndexImpl(private var searchTree: BedTree): BedIndex {

    constructor(data: List<BedEntry>): this(BedTree(data.first())) {
        for (i in 1 until data.size)
            searchTree.insert(data[i])
        searchTree.injectArrays()
        BedIndexImpl(searchTree)
    }

    /**
     * Deserialization of BedTree root.
     */
    constructor(path: Path):
            this(BedTree(Gson().fromJson(Files.readString(path), BedTreeNode::class.java)))

    override fun search(entry: BedEntry): List<BedEntry> = searchTree.find(entry)

    fun serialize(path: Path) {
        val json = Gson().toJson(searchTree.root)
        Files.writeString(path, json)
    }
}