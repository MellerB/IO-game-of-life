package com.example.inzynieria_oprogramowania

data class Board(var rows: Int, var columns: Int)
{
    var matrix : Array<BooleanArray> = Array(rows, { BooleanArray(columns)})
}

