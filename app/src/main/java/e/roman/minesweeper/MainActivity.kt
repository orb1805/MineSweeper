package e.roman.minesweeper

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.*
import android.widget.*
import org.w3c.dom.Text
import kotlin.random.Random

class MainActivity : AppCompatActivity(), Drawer {

    private lateinit var drawBtn: Button
    private lateinit var switchBtn: Button
    private lateinit var rowsET: EditText
    private lateinit var bombsET: EditText
    private lateinit var columnsET: EditText
    private lateinit var imageView: ImageView
    private lateinit var text: TextView
    private lateinit var layout: LinearLayout
    private lateinit var childLayout: LinearLayout
    private var markBomb = false
    private var playing = 0 // 0-playing  1-victory  2-lose
    private var firstTap = true
    private var openned = 0
    private lateinit var canvas: Canvas
    private var rows = 0
    private var columns = 0
    private var bombs = 0
    private var size = 1f
    private var mPtrCount = 0
    private var xDown = 0f
    private var yDown = 0f
    private var previousX = 0f
    private var previousY = 0f
    private var dx = 0f
    private var dy = 0f
    private var yes = true
    private val width = 1080f
    private val height = 1080f
    private lateinit var field: MutableList<MutableList<Int>>
    private lateinit var shownField: MutableList<MutableList<Int>>
    private val bitmap = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
    private val paintBlack = Paint()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawBtn = findViewById(R.id.btn_draw)
        switchBtn = findViewById(R.id.switcher_btn)
        rowsET = findViewById(R.id.et_rows)
        columnsET = findViewById(R.id.et_columns)
        bombsET = findViewById(R.id.et_bombs)
        imageView = findViewById(R.id.im_view)
        text = findViewById(R.id.text)
        layout = findViewById(R.id.layout)
        childLayout = findViewById(R.id.child_layout)
        layout.removeView(imageView)
        layout.removeView(switchBtn)
        drawBtn.setOnClickListener {
            if (rowsET.text.isNotEmpty() && columnsET.text.isNotEmpty() && bombsET.text.isNotEmpty()) {
                switchBtn.text = "Открыть поле"
                markBomb = false
                layout.removeView(childLayout)
                layout.removeView(drawBtn)
                layout.addView(imageView)
                layout.addView(switchBtn)
                yes = true
                size = 1f
                dx = 0f
                dy = 0f
                previousX = 0f
                previousY = 0f
                rows = rowsET.text.toString().toInt()
                columns = columnsET.text.toString().toInt()
                bombs = bombsET.text.toString().toInt()
                openned = rows * columns - bombs
                if (bombs <= rows * columns && bombs > 0) {
                    playing = 0
                    firstTap = true
                    this.generate()
                    this.draw(size)
                }
            }
        }
        imageView.setImageBitmap(bitmap)
        canvas = Canvas(bitmap)
        paintBlack.color = Color.BLACK
        val mScaleDetector = ScaleGestureDetector(this, MyPinchListener(this))
        imageView.setOnTouchListener { _, event ->
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_POINTER_DOWN -> mPtrCount++
                MotionEvent.ACTION_POINTER_UP -> mPtrCount--
                MotionEvent.ACTION_DOWN -> mPtrCount++
                MotionEvent.ACTION_UP -> mPtrCount--
            }
            if (mPtrCount == 0) {
                yes = true
                Log.d("checkk", "нолик")
            }
            val x: Float = event.x
            val y: Float = event.y
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    xDown = x
                    yDown = y
                    if (yes) {
                        previousX = 0f
                        previousY = 0f
                        dx -= x
                        dy -= y
                        Log.d("checkk", "опережение")
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (x == xDown && y == yDown) {
                        var dRows = height / rows
                        var dCols = width / columns
                        var transCols = 0f
                        var transRows = 0f
                        if (dRows < dCols) {
                            dCols = dRows
                            transCols = width / 2 - columns * dCols / 2 + dx / size
                            transRows += dy / size
                        }
                        else {
                            dRows = dCols
                            transRows = height / 2 - rows * dRows / 2 + dy / size
                            transCols += dx / size
                        }
                        val i = ((y / size - transRows) / dRows).toInt()
                        val j = ((x / size - transCols) / dCols).toInt()
                        if (i in 0 until rows && j in 0 until columns && playing == 0) {
                            if (firstTap) {
                                firstTap = false
                                regenerate(i, j)
                                open(i, j)
                            } else {
                                if (markBomb)
                                    mark(i, j)
                                else
                                    open(i, j)
                            }
                            draw(size)
                        }
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (mPtrCount > 1) {
                        mScaleDetector.onTouchEvent(event)
                        yes = false
                    }
                    else{
                        if (yes) {
                            dx += x - previousX
                            dy += y - previousY
                            Log.d("checkk", "обработка")
                            if (dx > 0f)
                                dx = 0f
                            if (dy > 0f)
                                dy = 0f
                            draw(size)
                            previousX = x
                            previousY = y
                        }
                    }
                }
            }
            true
        }
        switchBtn.setOnClickListener {
            if (playing != 0){
                layout.removeView(imageView)
                layout.removeView(switchBtn)
                layout.addView(childLayout)
                layout.addView(drawBtn)
            }
            else {
                if (markBomb) {
                    switchBtn.text = "открыть поле"
                    markBomb = false
                } else {
                    switchBtn.text = "отметить бомбу"
                    markBomb = true
                }
            }
        }
    }

    override fun draw(size: Float) {
        this.size = size
        paintBlack.color = Color.BLACK
        canvas.drawColor(Color.WHITE)
        var dRows = height / rows//1100f / rows
        var dCols = width / columns//1100f / columns
        var transCols = 0f
        var transRows = 0f
        if (dRows < dCols) {
            dCols = dRows
            if (dx < -width * size + width)
                dx = -width * size + width
            if (dy < -height * size + height)
                dy = -height * size + height
            transCols = width / 2 - columns * dCols / 2 + dx / size
            transRows += dy / size
        } else {
            if (dx < -width * size + width)
                dx = -width * size + width
            if (dy < -height * size + height)
                dy = -height * size + height
            dRows = dCols
            transRows = height / 2 - rows * dRows / 2 + dy / size
            transCols += dx / size
        }
        paintBlack.textSize = 0.5f * dRows * size
        for (i in 0..rows)
            canvas.drawLine(transCols * this.size, (dRows * i + transRows) * this.size,
                    (dCols * columns + transCols) * this.size, (dRows * i + transRows) * this.size,
                    paintBlack)
        for (i in 0..columns)
            canvas.drawLine((i * dCols + transCols) * this.size, transRows * this.size,
                    (i * dCols + transCols) * this.size, (dRows * rows + transRows) * this.size,
                    paintBlack)
        when (playing) {
            0 -> {
                for (i in field.indices)
                    for (j in field[i].indices) {
                        if (shownField[i][j] != -1) {
                            if (shownField[i][j] != 10) {
                                if (field[i][j] == 9) {
                                    paintBlack.color = Color.RED
                                    canvas.drawText(field[i][j].toString(), (j * dCols + dCols / 3 + transCols) * this.size, ((i + 1) * dRows - dRows / 3 + transRows) * this.size, paintBlack)
                                    paintBlack.color = Color.BLACK
                                    //playing = false
                                    playing = 2
                                    switchBtn.text = "Play again"
                                    draw(size)
                                } else
                                    canvas.drawText(field[i][j].toString(), (j * dCols + dCols / 3 + transCols) * this.size, ((i + 1) * dRows - dRows / 3 + transRows) * this.size, paintBlack)
                            } else {
                                canvas.drawLine((j * dCols + transCols) * this.size, (i * dRows + transRows) * this.size,
                                        ((j + 1) * dCols + transCols) * this.size, ((i + 1) * dRows + transRows) * this.size,
                                        paintBlack)
                                canvas.drawLine((j * dCols + transCols) * this.size, (dRows * (i + 1) + transRows) * this.size,
                                        ((j + 1) * dCols + transCols) * this.size, (i * dRows + transRows) * this.size,
                                        paintBlack)
                            }
                        }
                    }
            }
            1 -> {
                for (i in field.indices)
                    for (j in field[i].indices) {
                        if (field[i][j] == 9) {
                            paintBlack.color = Color.GREEN
                            canvas.drawLine((j * dCols + transCols) * this.size, (i * dRows + transRows) * this.size,
                                    ((j + 1) * dCols + transCols) * this.size, ((i + 1) * dRows + transRows) * this.size,
                                    paintBlack)
                            canvas.drawLine((j * dCols + transCols) * this.size, (dRows * (i + 1) + transRows) * this.size,
                                    ((j + 1) * dCols + transCols) * this.size, (i * dRows + transRows) * this.size,
                                    paintBlack)
                        } else {
                            paintBlack.color = Color.BLACK
                            canvas.drawText(field[i][j].toString(), (j * dCols + dCols / 3 + transCols) * this.size, ((i + 1) * dRows - dRows / 3 + transRows) * this.size, paintBlack)
                        }
                    }
            }
            2 -> {
                for (i in field.indices)
                    for (j in field[i].indices) {
                        if (field[i][j] == 9) {
                            paintBlack.color = Color.RED
                            canvas.drawLine((j * dCols + transCols) * this.size, (i * dRows + transRows) * this.size,
                                    ((j + 1) * dCols + transCols) * this.size, ((i + 1) * dRows + transRows) * this.size,
                                    paintBlack)
                            canvas.drawLine((j * dCols + transCols) * this.size, (dRows * (i + 1) + transRows) * this.size,
                                    ((j + 1) * dCols + transCols) * this.size, (i * dRows + transRows) * this.size,
                                    paintBlack)
                            paintBlack.color = Color.BLACK
                        } else {
                            paintBlack.color = Color.BLACK
                            if (shownField[i][j] != -1) {
                                canvas.drawText(field[i][j].toString(), (j * dCols + dCols / 3 + transCols) * this.size, ((i + 1) * dRows - dRows / 3 + transRows) * this.size, paintBlack)
                            }
                        }
                    }
            }
        }
        imageView.setImageBitmap(bitmap)
    }

    private fun generate(){
        field = mutableListOf()
        shownField = mutableListOf()
        for (i in 0 until rows){
            field.add(mutableListOf())
            shownField.add(mutableListOf())
            for (j in 1..columns){
                field[i].add(0)
                shownField[i].add(-1)
            }
        }
        for (i in 1..bombs) {
            var r = Random.nextInt(0, rows)
            var c = Random.nextInt(0, columns)
            if (field[r][c] == 0 && r != 0 && c != 0)
                field[r][c] = 9
            else {
                r++
                r /= rows
                while (field[r][c] == 9 || (r == 0 && c == 0)) {
                    r++
                    r %= rows
                    if (r == 0) {
                        c++
                        c %= columns
                    }
                }
                field[r][c] = 9
            }
        }
    }

    private fun regenerate(r: Int, c: Int){
        if (!around(r, c)){
            val r1 = if (r > 0)
                r - 1
            else
                r
            val r2 = if (r < rows - 1)
                r + 1
            else
                r
            val c1 = if (c > 0)
                c - 1
            else
                c
            val c2 = if (c < columns - 1)
                c + 1
            else
                c
            for (i1 in r1..r2) {
                for (j1 in c1..c2)
                    if (field[i1][j1] == 9) {
                        var stop = false
                        for (x in field.indices) {
                            for (y in field[x].indices) {
                                if (x !in r1..r2 && y !in c1..c2 && field[x][y] != 9) {
                                    field[x][y] = 9
                                    stop = true
                                    break
                                }
                            }
                            if (stop)
                                break
                        }
                        field[i1][j1] = 0
                    }
            }
        }
        for (x in field.indices)
            for (y in field[x].indices){
                if (field[x][y] == 9){
                    val r1 = if (x > 0)
                        x - 1
                    else
                        x
                    val r2 = if (x < rows - 1)
                        x + 1
                    else
                        x
                    val c1 = if (y > 0)
                        y - 1
                    else
                        y
                    val c2 = if (y < columns - 1)
                        y + 1
                    else
                        y
                    for (i1 in r1..r2){
                        for (j1 in c1..c2)
                            if (field[i1][j1] != 9)
                                field[i1][j1]++
                    }
                }
            }
    }

    private fun around(r: Int, c: Int) : Boolean{
        val r1 = if (r > 0)
            r - 1
        else
            r
        val r2 = if (r < rows - 1)
            r + 1
        else
            r
        val c1 = if (c > 0)
            c - 1
        else
            c
        val c2 = if (c < columns - 1)
            c + 1
        else
            c
        for (i1 in r1..r2){
            for (j1 in c1..c2)
                if (field[i1][j1] == 9)
                    return false
        }
        return true
    }

    private fun open(r: Int, c: Int){
        if (shownField[r][c] == -1) {
            openned--
            if (openned <= 0) {
                playing = 1
                switchBtn.text = "Play again"
            }
            shownField[r][c] = field[r][c]
            if (field[r][c] == 0) {
                val r1 = if (r > 0)
                    r - 1
                else
                    r
                val r2 = if (r < rows - 1)
                    r + 1
                else
                    r
                val c1 = if (c > 0)
                    c - 1
                else
                    c
                val c2 = if (c < columns - 1)
                    c + 1
                else
                    c
                for (i1 in r1..r2) {
                    for (j1 in c1..c2)
                        if (shownField[i1][j1] == -1)
                            open(i1, j1)
                }
            }
        }
    }

    private fun mark(i: Int, j: Int){
        if (shownField[i][j] == -1) {
            shownField[i][j] = 10
        }
        else {
            if (shownField[i][j] == 10)
                shownField[i][j] = -1
        }
    }

}