/**
 * Node of the tree where BEDs are being kept.
 */
data class BedTreeNode(
        var data: BedEntry = BedEntry(),
        var left: BedTreeNode? = null,
        var right: BedTreeNode? = null,
        var size: Int = 1,
        var children: Array<BedEntry> = emptyArray<BedEntry>()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BedTreeNode

        if (data != other.data) return false
        if (left != other.left) return false
        if (right != other.right) return false
        if (size != other.size) return false
        if (!children.contentEquals(other.children)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.hashCode()
        result = 31 * result + (left?.hashCode() ?: 0)
        result = 31 * result + (right?.hashCode() ?: 0)
        result = 31 * result + size
        result = 31 * result + children.contentHashCode()
        return result
    }
}