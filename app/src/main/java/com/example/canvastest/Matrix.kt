package com.example.canvastest

data class Matrix ( val count: Int )
{
    val matrix = Array(count) { BooleanArray(count) }

    fun changeBoolean( row: Int, column: Int )
    {
        matrix[row][column] = !matrix[row][column]
    }
}