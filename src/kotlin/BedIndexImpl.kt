import java.nio.file.Path
import com.google.gson.*
import java.nio.file.Files

class BedIndexImpl(data: List<BedEntry>): BedIndex {
    private var searchTree = BedTree(data.first())

    init {
        for (i in 1 until data.size)
            searchTree.insert(data[i])
        searchTree.injectArrays()
    }

    constructor(path: Path) {
        val json = Files.readString(path)
        searchTree = BedTree(Gson().fromJson(json, BedTreeNode::class.java))
    }

    override fun search(entry: BedEntry): List<BedEntry> = searchTree.find(entry)

    fun serialize(path: Path) {
        val json = Gson().toJson(searchTree.root)
        Files.writeString(path, json)
    }
}