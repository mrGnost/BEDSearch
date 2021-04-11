data class BedTreeNode(
        var data: BedEntry = BedEntry(),
        var left: BedTreeNode? = null,
        var right: BedTreeNode? = null,
        var size: Int = 1,
        val children: MutableList<BedEntry> = mutableListOf()
)