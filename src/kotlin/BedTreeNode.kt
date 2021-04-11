/**
 * Node of the tree where BEDs are being kept.
 */
data class BedTreeNode(
        var data: BedEntry = BedEntry(),
        var left: BedTreeNode? = null,
        var right: BedTreeNode? = null,
        var size: Int = 1,
        var children: Array<BedEntry> = emptyArray()
)