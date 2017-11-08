package cs.ut.engine.item

data class Case(val id : String) {

    val attributes = LinkedHashMap<String, MutableSet<String>>()
    val staticCols = mutableSetOf<String>()
    val dynamicCols = mutableSetOf<String>()

    val classifiedColumns = HashMap<String, MutableSet<String>>()
}