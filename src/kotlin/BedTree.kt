/**
 * The randomized binary tree realization for fast search for interval inclusions.
 * I used the 2-dimensional orthogonal search algo and took intervals as points (start = x, end = y),
 * so it is enough to find all points in a square from (start, start) to (end, end).
 * The tree is sorted by start while every node contains an array of children entries sorted by end,
 * so it is easy to find all nodes with start included first and then filter by end.
 */
class BedTree(var root: BedTreeNode) {

    constructor(entry: BedEntry) : this(BedTreeNode(entry))

    fun insert(entry: BedEntry) {
        root = insert(root, entry)
    }

    fun injectArrays(): Array<BedEntry> = injectArrays(root)

    /**
     * This method injects arrays of children entries (sorted by end).
     */
    private fun injectArrays(node: BedTreeNode?): Array<BedEntry> {
        node ?: return emptyArray()
        val leftArray = if (node.left != null) injectArrays(node.left) else emptyArray()
        val rightArray = if (node.right != null) injectArrays(node.right) else emptyArray()
        node.children = fillSortedArray(leftArray, rightArray)
        return node.children
    }

    private fun fillSortedArray(leftArray: Array<BedEntry>, rightArray: Array<BedEntry>): Array<BedEntry> {
        val array = Array(leftArray.size + rightArray.size) { BedEntry() }
        var leftInd = 0
        var rightInd = 0
        while (leftInd < leftArray.size || rightInd < rightArray.size) {
            if (leftInd < leftArray.size &&
                    (rightInd >= rightArray.size ||
                            leftArray[leftInd].end < rightArray[rightInd].end)) {
                array[leftInd + rightInd] = leftArray[leftInd]
                leftInd++
            } else {
                array[leftInd + rightInd] = rightArray[rightInd]
                rightInd++
            }
        }
        return array
    }

    /**
     * Method that implements search logic.
     */
    fun find(entry: BedEntry): List<BedEntry> {
        // We find most common root of the very first node with start included and very last one
        // (This search is way more simple if we understand that the most common root is the first
        // node from the main root which start is included in the interval).
        val commonRoot = findCommonRoot(root, entry)
        commonRoot ?: return mutableListOf()
        // We go from the most common root to the very first node included and to the very last one
        // and collect nodes and subtrees included on our way into two separate places.
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
        // All found nodes we filter by its full occurrence into our interval while in roots
        // we find all such entries among elements of the injected into the root array.
        val foundEntries = nodesAndRoots
                .first
                .map { x -> x.data }
                .filter { x -> x.end >= entry.start && x.end <= entry.end }
                .toMutableList()
        foundEntries.addAll(filterSubtreesByEnd(nodesAndRoots.second, entry))
        // Then we just filter result list by chromosome.
        return foundEntries.filter { x -> x.chromosome == entry.chromosome }
    }

    private fun findCommonRoot(node: BedTreeNode?, entry: BedEntry): BedTreeNode? {
        if (node == null || node.data.start >= entry.start && node.data.start < entry.end)
            return node
        return findCommonRoot(if (node.data.start < entry.start) node.right else node.left, entry)
    }

    /**
     * Method where we search for the very first/last node which start is included into the interval
     * and add nodes and roots on our way to separate places.
     */
    private fun findNodesAndRoots(node: BedTreeNode, entry: BedEntry, isLeftSubtree: Boolean):
            Pair<MutableList<BedTreeNode>, MutableList<BedTreeNode>> {
        // First of all we choose orientation (in which direction we go from root).
        val mainSubtree = if (isLeftSubtree) node.left else node.right
        val secondarySubtree = if (isLeftSubtree) node.right else node.left
        // If current node is included into interval (by start) we add it to the nodes and
        // if the secondary subtree (which is farther from end of region) is not empty we add it to subtrees.
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

    /**
     * In this method we just filter entries included into interval (by end) using binary search
     * (because these arrays were created sorted and weren't changed).
     */
    private fun filterSubtreesByEnd(roots: MutableList<BedTreeNode>, entry: BedEntry): MutableList<BedEntry> {
        val foundEntries = mutableListOf<BedEntry>()
        roots.forEach { x ->
            val left = binarySearch(x.children, entry.start) { m, n -> m < n }
            val right = binarySearch(x.children, entry.end) { m, n -> m <= n } + 1
            foundEntries.addAll(foundEntries.subList(left, right))
        }
        return foundEntries
    }

    private fun binarySearch(list: Array<BedEntry>, value: Int, condition: (y: Int, z: Int) -> Boolean): Int {
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

    /**
     * Randomized binary tree insertion logic ( we need a balanced tree for the orthogonal search).
     */
    private fun insert(node: BedTreeNode?, entry: BedEntry): BedTreeNode {
        node ?: return BedTreeNode(entry)
        // Insert node into root with probability of 1/(number of nodes in tree).
        if ((0..(node.size + 1)).random() == 0)
            return insertRoot(node, entry)
        if (entry.start < node.data.start)
            node.left = insert(node.left, entry)
        else
            node.right = insert(node.right, entry)
        fixSize(node)
        return node
    }

    private fun getSize(node: BedTreeNode?): Int = node?.size ?: 0

    /**
     * Keeping number of nodes in subtrees as a field of the subtree root.
     */
    private fun fixSize(node: BedTreeNode) {
        node.size = getSize(node.left) + getSize(node.right) + 1
    }

    private fun rotateRight(node: BedTreeNode): BedTreeNode {
        val left = node.left ?: return node
        node.left = left.right
        left.right = node
        left.size = node.size
        fixSize(node)
        fixSize(left)
        return left
    }

    private fun rotateLeft(node: BedTreeNode): BedTreeNode {
        val right = node.right ?: return node
        node.right = right.left
        right.left = node
        right.size = node.size
        fixSize(node)
        fixSize(right)
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