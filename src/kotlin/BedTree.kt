class BedTree(entry: BedEntry) {
    var root = BedTreeNode(entry)

    constructor(root: BedTreeNode) {
        this.root = root
    }

    fun insert(entry: BedEntry) {
        root = insert(root, entry)
    }

    fun injectArrays(): MutableList<BedEntry> = injectArrays(root)

    private fun injectArrays(node: BedTreeNode?): MutableList<BedEntry> {
        node ?: return mutableListOf()
        node.children.addAll(injectArrays(node.left))
        node.children.addAll(injectArrays(node.right))
        node.children.sortBy { x -> x.end }
        return node.children
    }

    fun find(entry: BedEntry): List<BedEntry> {
        val commonRoot = findCommonRoot(root, entry)
        commonRoot ?: return mutableListOf()
        val nodesAndRoots = Pair<MutableList<BedTreeNode>, MutableList<BedTreeNode>>(
                mutableListOf(commonRoot),
                mutableListOf()
        )
        if (commonRoot.left != null) {
            val left = findNodesAndRoots(commonRoot.left!!, entry, true)
            nodesAndRoots.first.addAll(left.first)
            nodesAndRoots.second.addAll(left.second)
        }
        if (commonRoot.right != null) {
            val right = findNodesAndRoots(commonRoot.right!!, entry, false)
            nodesAndRoots.first.addAll(right.first)
            nodesAndRoots.second.addAll(right.second)
        }
        val foundEntries = nodesAndRoots
                .first
                .map { x -> x.data }
                .filter { x -> x.end >= entry.start && x.end <= entry.end }
                .toMutableList()
        foundEntries.addAll(filterSubtreesByEnd(nodesAndRoots.second, entry))
        return foundEntries.filter { x -> x.chromosome == entry.chromosome }
    }

    private fun findCommonRoot(node: BedTreeNode?, entry: BedEntry): BedTreeNode? {
        if (node == null || node.data.start >= entry.start && node.data.start < entry.end)
            return node
        return findCommonRoot(if (node.data.start < entry.start) node.right else node.left, entry)
    }

    private fun findNodesAndRoots(node: BedTreeNode, entry: BedEntry, isLeftSubtree: Boolean):
            Pair<MutableList<BedTreeNode>, MutableList<BedTreeNode>> {
        val mainSubtree = if (isLeftSubtree) node.left else node.right
        val secondarySubtree = if (isLeftSubtree) node.right else node.left
        if (entry.start < node.data.start) {
            val nodesAndRoots =
                    if (mainSubtree != null)
                        findNodesAndRoots(mainSubtree, entry, isLeftSubtree)
                    else
                        Pair(mutableListOf(), mutableListOf())
            nodesAndRoots.first.add(node)
            if (secondarySubtree != null)
                nodesAndRoots.second.add(secondarySubtree)
            return nodesAndRoots
        } else {
            return if (secondarySubtree != null)
                findNodesAndRoots(secondarySubtree, entry, isLeftSubtree)
            else
                Pair(mutableListOf(), mutableListOf())
        }
    }

    private fun filterSubtreesByEnd(roots: MutableList<BedTreeNode>, entry: BedEntry): MutableList<BedEntry> {
        val foundEntries = mutableListOf<BedEntry>()
        roots.forEach { x ->
            val left = binarySearch(x.children, entry.start) { m, n -> m < n }
            val right = binarySearch(x.children, entry.end) { m, n -> m <= n } + 1
            foundEntries.addAll(foundEntries.subList(left, right))
        }
        return foundEntries
    }

    private fun binarySearch(list: MutableList<BedEntry>, value: Int, condition: (y: Int, z: Int) -> Boolean): Int {
        var left = -1
        var right = list.size
        var middle: Int
        while (right > left + 1) {
            middle = left + (right - left) / 2
            if (!condition(list[middle].end, value))
                left = middle
            else
                right = middle
        }
        return right
    }

    private fun insert(node: BedTreeNode?, entry: BedEntry): BedTreeNode {
        node ?: return BedTreeNode(entry)
        if ((0..(node.size + 1)).random() == 0)
            return insertRoot(root, entry)
        if (entry.start < node.data.start)
            node.left = insert(node.left, entry)
        else
            node.right = insert(node.right, entry)
        fixSize(node)
        return node
    }

    private fun getSize(node: BedTreeNode?): Int = node?.size ?: 0

    private fun fixSize(node: BedTreeNode) {
        node.size = getSize(node.left) + getSize(node.right) + 1
    }

    private fun rotateRight(node: BedTreeNode): BedTreeNode {
        val left = node.left ?: return node
        node.left = left.right
        left.right = node
        left.size = node.size
        fixSize(node)
        return left
    }

    private fun rotateLeft(node: BedTreeNode): BedTreeNode {
        val right = node.right ?: return node
        node.right = right.left
        right.left = node
        right.size = node.size
        fixSize(node)
        return right
    }

    private fun insertRoot(node: BedTreeNode?, entry: BedEntry): BedTreeNode {
        node ?: return BedTreeNode(entry)
        return if (entry.start < node.data.start) {
            node.left = insertRoot(node.left, entry)
            rotateRight(node)
        } else {
            node.right = insertRoot(node.right, entry)
            rotateLeft(node)
        }
    }
}