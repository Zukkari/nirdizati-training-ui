package cs.ut.ui

@FunctionalInterface
interface GridValueProvider<T, Row> {
    var fields: MutableList<FieldComponent>

    fun provide(data: T): Row
}