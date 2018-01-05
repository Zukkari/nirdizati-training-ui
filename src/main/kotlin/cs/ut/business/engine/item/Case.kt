package cs.ut.business.engine.item

data class Case(val id: String) {

    val attributes = LinkedHashMap<String, MutableSet<String>>()
    val staticCols = mutableSetOf<String>()
    val dynamicCols = mutableSetOf<String>()

    val classifiedColumns = mutableMapOf<String, MutableSet<String>>()
}