interface BedIndex {
    fun search(entry: BedEntry): List<BedEntry>
}