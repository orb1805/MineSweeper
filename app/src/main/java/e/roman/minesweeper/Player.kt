package e.roman.minesweeper

import android.provider.FontRequest
import android.util.Log
import android.widget.Toast

class Player {

    private var field: MutableList<MutableList<Int>> = mutableListOf()
    private var work: MutableList<MutableList<Int>> = mutableListOf()
    private val bombs: MutableList<MutableList<Int>> = mutableListOf()
    private var rows = 0
    private var cols = 0
    private var mines = 0
    private var curMines = 0
    private var playGround: PlayGround
    private var isWork = true
    private lateinit var a: Array<Int>
    private var isMoved: Boolean = false

    constructor(rows1: Int, cols1: Int, mines1: Int, playGround: PlayGround) {
        //val scanner = Scanner(System.`in`)

        // read game configuration
        /*rows = scanner.nextInt()
        cols = scanner.nextInt()
        mines = scanner.nextInt()*/
        rows = rows1
        cols = cols1
        mines = mines1
        this.playGround = playGround
        for (i in 0 until rows) {
            field.add(mutableListOf())
            bombs.add(mutableListOf())
            for (j in 0 until cols) {
                field[i].add(-10)
                bombs[i].add(0)
            }
        }
        reveal(0, 0)
        work = mutableListOf()
        for (i in field){
            work.add(mutableListOf())
            for (j in i){
                work.last().add(j)
            }
        }
        isWork = true
        curMines = mines
    }

    fun play(field: MutableList<MutableList<Int>>) {
        /*if (field[0][cols - 1] == -10)
            reveal(0, cols - 1, scanner)
        if (field[rows - 1][0] == -10)
            reveal(rows - 1, 0, scanner)
        if (field[rows - 1][cols - 1] == -10)
            reveal(rows - 1, cols - 1, scanner)*/
        this.field = mutableListOf()
        for (i in field.indices) {
            this.field.add(mutableListOf())
            for (j in field[i].indices)
                if (field[i][j] == -1)
                    this.field[i].add(-10)
                else
                    this.field[i].add(field[i][j])
        }
        var str = ""
        work = mutableListOf()
        for (i in field) {
            work.add(mutableListOf())
            str = ""
            for (j in i) {
                if (j == -1)
                    work.last().add(-10)
                else
                     work.last().add(j)
                str += "$j "
            }
            //Log.d("checkk", str)
        }
        curMines = mines
        var revealed = false
        while (/*(curMines > 0 && isWork) || */!revealed) {
            //Log.d("checkk", "go")
            a = stepOne(work)
            isWork = stepTwo(work)
            if (a[0] != -10 && a[1] != -10) {
                //Log.d("checkk", "reveal1")
                //Log.d("checkk", "${a[0]} ${a[1]}")
                reveal(a[0], a[1])
                revealed = true
                /*work = mutableListOf()
                for (i in field) {
                    work.add(mutableListOf())
                    for (j in i) {
                        work.last().add(j)
                    }
                }*/
                isWork = true
                curMines = mines
            }
            if (!isWork) {
                for (i in work.indices)
                    for (j in work[i].indices)
                        if (work[i][j] == -10 && openedAround(i, j, work)) {
                            //Log.d("checkk", "checkHypo")
                            isMoved = checkHypo(i, j)
                            curMines = mines
                            if (isMoved) {
                                Log.d("checkk", "$i $j")
                                Log.d("checkk", "true")
                                reveal(i, j)
                                revealed = true
                                break
                                /*work = mutableListOf()
                                for (k in field) {
                                    work.add(mutableListOf())
                                    for (d in k) {
                                        work.last().add(d)
                                    }
                                }*/
                                isWork = true
                            }
                            else{
                                Log.d("checkk", "$i $j")
                                Log.d("checkk", "false")
                                revealed = true
                                playGround.massage("I dont know((")
                            }
                        }
            }
        }
        /*if (!isWork)
            playGround.massage("I dont know((")
            //println("I dont know((")
        else
            playGround.massage("It's done))")
            //println("It's done))")*/
    }

    private fun reveal(row: Int, col: Int/*, scanner: Scanner*/) {
        //println("1 $row $col")
        /*val count = scanner.nextInt()
        for (i in 1..count) {
            val r = scanner.nextInt()
            val c = scanner.nextInt()
            val v = scanner.nextInt()
            field[r][c] = v*/
        playGround.reveal(row, col)
        //while (!playGround.isFieldReady())
        //field = playGround.shareField()
    }

    private fun stepOne(work: MutableList<MutableList<Int>>): Array<Int> {
        var str = ""
        for (i in work){
            str = ""
            for (j in i)
                str += "$j "
            //Log.d("checkk", str)
        }
        for (i in work.indices)
            for (j in work[i].indices)
                if (work[i][j] != -10) {
                    val count = around(i, j, work)
                    if (count != 0)
                        if (count == work[i][j])
                            makeBombs(i, j, work)
                        else
                            if (work[i][j] == 0)
                                return find(i, j, work)
                }
        return arrayOf(-10, -10)
    }

    private fun stepTwo(work: MutableList<MutableList<Int>>): Boolean {
        var res = false
        for (i in field.indices)
            for (j in field[i].indices)
                if (work[i][j] == 9) {
                    decrease(i, j, work)
                    res = true
                }
        return res
    }

    private fun around(r: Int, c: Int, work: MutableList<MutableList<Int>>): Int {
        var res = 0
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
        val c2 = if (c < cols - 1)
            c + 1
        else
            c
        for (i in r1..r2) {
            for (j in c1..c2)
                if (work[i][j] == -10 || work[i][j] == 9)
                    res++
        }
        return res
    }

    private fun openedAround(r: Int, c: Int, work: MutableList<MutableList<Int>>): Boolean {
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
        val c2 = if (c < cols - 1)
            c + 1
        else
            c
        for (i in r1..r2) {
            for (j in c1..c2)
                if (work[i][j] > 0)
                    return true
        }
        return false
    }

    private fun makeBombs(r: Int, c: Int, work: MutableList<MutableList<Int>>) {
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
        val c2 = if (c < cols - 1)
            c + 1
        else
            c
        for (i in r1..r2) {
            for (j in c1..c2)
                if (work[i][j] == -10)
                    work[i][j] = 9
        }
    }

    private fun decrease(r: Int, c: Int, work: MutableList<MutableList<Int>>) {
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
        val c2 = if (c < cols - 1)
            c + 1
        else
            c
        for (i in r1..r2) {
            for (j in c1..c2)
                if (work[i][j] in -9..8) {
                    work[i][j]--
                }
        }
        work[r][c] = 10
        //println("2 $r $c")
        curMines--
    }

    private fun find(r: Int, c: Int, work: MutableList<MutableList<Int>>): Array<Int> {
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
        val c2 = if (c < cols - 1)
            c + 1
        else
            c
        for (i in r1..r2) {
            for (j in c1..c2)
                if (work[i][j] == -10) {
                    return arrayOf(i, j)
                }
        }
        return arrayOf(-10, -10)
    }

    private fun checkHypo(r: Int, c: Int): Boolean {
        //println("-$r $c-")
        var curWork: MutableList<MutableList<Int>> = mutableListOf()
        val changes: MutableList<MutableList<Int>> = mutableListOf()
        //копируем work в curWork
        for (i in work) {
            curWork.add(mutableListOf())
            for (j in i) {
                curWork.last().add(j)
            }
        }
        //выдвигаем гипотезу
        curWork[r][c] = 9
        var isWork = true
        var a: Array<Int>
        while (isWork) {
            a = stepOne(curWork)
            isWork = stepTwo(curWork)
            if (a[0] != -10 && a[1] != -10) {
                changes.add(mutableListOf())
                changes.last().add(a[0])
                changes.last().add(a[1])
                changes.last().add(8)
                for (i in curWork.indices)
                    for (j in curWork[i].indices)
                        if (curWork[i][j] in -9..-1) {
                            return true
                        }
                curWork = mutableListOf()
                for (i in field) {
                    curWork.add(mutableListOf())
                    for (j in i) {
                        curWork.last().add(j)
                    }
                }
                isWork = true
                for (i in changes)
                    curWork[i[0]][i[1]] = i[2]
            }
        }
        return false
    }
}