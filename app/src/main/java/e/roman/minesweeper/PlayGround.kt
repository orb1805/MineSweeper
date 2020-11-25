package e.roman.minesweeper

interface PlayGround {
    fun reveal(i: Int, j: Int)
    fun shareField():MutableList<MutableList<Int>>
    fun massage(msg: String)
}