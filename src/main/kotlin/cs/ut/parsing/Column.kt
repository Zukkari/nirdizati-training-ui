package cs.ut.parsing

import cs.ut.util.Columns


data class Column(val name: String, var values: Set<String>) {

    fun isNumeric(): Boolean = TODO()

    fun category(): Columns = TODO()
}