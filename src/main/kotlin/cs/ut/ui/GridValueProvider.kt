package cs.ut.ui

interface GridValueProvider<T, Row> {
    var fields: MutableList<FieldComponent>

    fun provide(data: T): Row
}