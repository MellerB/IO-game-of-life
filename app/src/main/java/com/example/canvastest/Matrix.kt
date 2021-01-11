package com.example.canvastest

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.annotation.RequiresApi
import kotlin.math.log

data class Matrix ( val _count: Int ): Parcelable
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

    var count = _count;


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
                val r = Math.floorMod(row+neighbour.first,count);
                val c = Math.floorMod(column+neighbour.second,count);
                if (matrix[r][c])
                {
                    n++;
                }
        }
        return n;
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun nextGeneration()
    {
        val newMatrix = Array(count) { BooleanArray(count) }

        for(row in 0..count-1)
        {
            for(col in 0..count-1)
            {
                val n = getNeighboursNumber(row,col);
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

    fun increaseMatrixCount()
    {
        count += 2
    }

    fun reduceMatrixCount()
    {
        count -= 2
    }

    constructor(parcel: Parcel) : this(parcel.readInt())
    {
        for(i in 0..count - 1)
        {
            parcel.readBooleanArray(matrix[i])
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int)
    {
        parcel.writeInt(count)
        for(i in 0..count - 1)
        {
            parcel.writeBooleanArray(matrix[i])
        }
    }

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    companion object CREATOR : Parcelable.Creator<Matrix> {
        override fun createFromParcel(parcel: Parcel): Matrix {
            return Matrix(parcel)
        }

        override fun newArray(size: Int): Array<Matrix?> {
            return arrayOfNulls(size)
        }
    }
}