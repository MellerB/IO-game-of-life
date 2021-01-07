package com.example.canvastest

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlin.math.log

data class Matrix ( val _count: Int )
{
    var matrix = Array(_count) { BooleanArray(_count) }
    val neighbours = listOf(
            Pair(-1,-1),
            Pair(-1,0),
            Pair(-1,1),
            Pair(0,-1),
            Pair(0,1),
            Pair(1,-1),
            Pair(1,0),
            Pair(1,1)
    )
    val aliveRules = listOf(2,3);
    val deadRules = listOf(3);

    val count = _count;

    @RequiresApi(Build.VERSION_CODES.N)
    fun changeBoolean(row: Int, column: Int ) :Boolean
    {
        matrix[row][column] = !matrix[row][column]
        Log.d("Neighbours: ",getNeighboursNumber(row,column).toString());
        return matrix[row][column];
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getNeighboursNumber(row: Int, column: Int ): Int
    {
        var n = 0

        for(neighbour in neighbours)
        {
                var r = Math.floorMod(row+neighbour.first,count);
                var c = Math.floorMod(column+neighbour.second,count);
                if (matrix[r][c])
                {
                    n++;
                }
        }
        return n;
    }

    @RequiresApi(Build.VERSION_CODES.N)
    public fun nextGeneration()
    {
        var newMatrix = Array(count) { BooleanArray(count) }

        for(row in 0..count-1)
        {
            for(col in 0..count-1)
            {
                var n = getNeighboursNumber(row,col);
                if(matrix[row][col] && (n in aliveRules))
                {
                    newMatrix[row][col] = true;
                }
                else if (n in deadRules)
                {
                    newMatrix[row][col] = true;
                }
            }
        }

        matrix = newMatrix;
    }
}