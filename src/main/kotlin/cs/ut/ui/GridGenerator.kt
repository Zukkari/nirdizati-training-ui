package cs.ut.ui

import cs.ut.config.items.Property
import org.zkoss.util.resource.Labels
import org.zkoss.zul.Grid
import org.zkoss.zul.Label
import org.zkoss.zul.Row
import org.zkoss.zul.Rows

class GridGenerator {

    fun generate(data: List<Property>) : Grid {
        val grid = Grid()
        val rows = Rows()

        data.forEach {
            val row = Row()
            row.appendChild(Label(Labels.getLabel(it.id)))


        }
        return grid
    }
}