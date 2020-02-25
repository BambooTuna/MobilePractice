package sample

import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.widget.Button
import android.widget.TextView
import android.widget.LinearLayout
import android.view.View

actual class Sample {
    actual fun checkMe() = 44
}

actual object Platform {
    actual val name: String = "Android"
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val constraintLayout = findViewById(R.id.constraintLayout) as ConstraintLayout
        println(constraintLayout.width)
        val disp = getWindowManager().getDefaultDisplay()
        val p = Point()
        disp.getSize(p)

        val clickEvent = object : View.OnClickListener {
            override fun onClick(v: View?) {
                v?.let { view ->
                    val id = view.id
                    val y = id % 10
                    val x = (id - y) / 10
                    val player =  if ((view as Button).text == BlackCell.name) BlackCell else WhiteCell
                    val result = canBePlacedAt(x, y, player, getCellColorList(constraintLayout))
                    when(result) {
                        Empty -> {
                            val b = constraintLayout.findViewById<Button>(x * 10 + y)
                            b.isClickable = false
                            b.text = ""
                        }
                        is CanReverse ->
                            result.canReverseCellList.forEach { a ->
                                reverseCell(a, player, constraintLayout)
                                reverseCell(id, player, constraintLayout)
                            }
                    }
                }

            }
        }

        for (x in 1 until 9) {
            for (y in 1 until 9) {
                constraintLayout.addView(createButton(x, y, EmptyCell, p.x / 8 - 2, clickEvent))
            }
        }

        reverseCell(44, BlackCell, constraintLayout)
        reverseCell(45, WhiteCell, constraintLayout)
        reverseCell(55, BlackCell, constraintLayout)
        reverseCell(54, WhiteCell, constraintLayout)
    }

    fun createButton(x: Int, y: Int, cell: Cell, standard: Int, handler: View.OnClickListener): Button {
        val button: Button = Button(this)
        button.id = x * 10 + y
        button.setBackgroundColor(cell.color)
        val buttonLayoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(standard - 10, standard - 10)
        buttonLayoutParams.rightMargin = 10
        buttonLayoutParams.topMargin = 10
        button.setLayoutParams(buttonLayoutParams)
        button.x = ((x - 1) * standard + 10).toFloat()
        button.y = ((y - 1) * standard + 10).toFloat()
        button.isClickable = false
        button.setOnClickListener(handler)
        return button
    }

    fun reverseCell(id: Int, color: Cell, constraintLayout: ConstraintLayout) {
        val button = constraintLayout.findViewById<Button>(id)
        button.setBackgroundColor(color.color)
        for (x in 1 until 9) {
            for (y in 1 until 9) {
                val result = canBePlacedAt(x, y, color.opposite, getCellColorList(constraintLayout))
                when(result) {
                    Empty -> {
                        val b = constraintLayout.findViewById<Button>(x * 10 + y)
                        b.isClickable = false
                        b.text = ""
                    }
                    is CanReverse ->
                        result.put.forEach { id ->
                            val b = constraintLayout.findViewById<Button>(id)
                            b.isClickable = true
                            b.text = color.opposite.name
                        }
                }
            }
        }
    }

    fun canBePlacedAt(x: Int, y: Int, playerColor: Cell, cellList: List<List<Cell>>): Reversible {
        return canBePlacedAtLoop(x, y, 1, 0, playerColor, cellList)
            .join(canBePlacedAtLoop(x, y, -1, 0, playerColor, cellList))
            .join(canBePlacedAtLoop(x, y, 0, 1, playerColor, cellList))
            .join(canBePlacedAtLoop(x, y, 0, -1, playerColor, cellList))
            .join(canBePlacedAtLoop(x, y, 1, 1, playerColor, cellList))
            .join(canBePlacedAtLoop(x, y, -1, -1, playerColor, cellList))
            .join(canBePlacedAtLoop(x, y, 1, -1, playerColor, cellList))
            .join(canBePlacedAtLoop(x, y, -1, 1, playerColor, cellList))
    }

    fun canBePlacedAtLoop(x: Int, y: Int, dx: Int, dy: Int, playerColor: Cell, cellList: List<List<Cell>>, r: Reversible = Empty): Reversible {
        val result =
            if (x + dx < 1 || 8 < x + dx || y + dy < 1 || 8 < y + dy) Empty
            else if (r == Empty && cellList[x - 1][y - 1] != EmptyCell) Empty
            else {
                val nextCellColor = cellList[x + dx - 1][y + dy - 1]
                if (nextCellColor == EmptyCell) Empty
                else if (nextCellColor != playerColor) canBePlacedAtLoop(x + dx, y + dy, dx, dy, playerColor, cellList, r.addReversedCell((x + dx) * 10 + y + dy, dx, dy))
                else r
            }
        return result
    }

    fun getCellColorList(constraintLayout: ConstraintLayout): List<List<Cell>> {
        val data = mutableListOf<List<Cell>>()
        for (x in 1 until 9) {
            val yList = mutableListOf<Cell>()
            for (y in 1 until 9) {
                yList.add(getCellColor(x, y, constraintLayout))
            }
            data.add(yList)
        }
        return data
    }

    fun getCellColor(x: Int, y: Int, constraintLayout: ConstraintLayout): Cell {
        return (
                when((constraintLayout.findViewById<Button>(x * 10 + y).getBackground() as ColorDrawable).color) {
                    EmptyCell.color -> EmptyCell
                    BlackCell.color -> BlackCell
                    WhiteCell.color -> WhiteCell
                    else -> EmptyCell
                }
                )
    }

}

interface Cell {
    val color: Int
    val opposite: Cell
    val name: String
}
object EmptyCell: Cell {
    override val color: Int = Color.GREEN
    override val opposite: Cell = this
    override val name: String = "空"
}
object BlackCell: Cell {
    override val color: Int = Color.BLACK
    override val opposite: Cell = WhiteCell
    override val name: String = "黒"
}
object WhiteCell: Cell {
    override val color: Int = Color.WHITE
    override val opposite: Cell = BlackCell
    override val name: String = "白"
}

interface Reversible {
    fun addReversedCell(id: Int, dx: Int, dy: Int): CanReverse
    fun addPut(v: Int): Reversible
    fun join(l: Reversible): Reversible
}
object Empty: Reversible {
    override fun addReversedCell(id: Int, dx: Int, dy: Int): CanReverse {
        return CanReverse(listOf(id), listOf(id - (dx * 10 + dy)))
    }
    override fun addPut(v: Int): Reversible {
        return this
    }
    override fun join(l: Reversible): Reversible {
        return l
    }
}

data class CanReverse(public val canReverseCellList: List<Int>, public val put: List<Int>): Reversible {
    override fun addReversedCell(id: Int, dx: Int, dy: Int): CanReverse {
        return copy(canReverseCellList = this.canReverseCellList + id)
    }
    override fun addPut(v: Int): Reversible {
        return copy(put = this.put + v)
    }
    override fun join(l: Reversible): Reversible {
        return when(l) {
            is Empty -> this
            is CanReverse -> copy(canReverseCellList = this.canReverseCellList + l.canReverseCellList, put = this.put + l.put)
            else -> this
        }
    }
}
