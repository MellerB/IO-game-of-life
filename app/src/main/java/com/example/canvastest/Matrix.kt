package com.example.canvastest

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.*



data class Matrix(var count: Int): Parcelable
{
    private val MATRIX_MAX_SIZE = 1000
    private val MATRIX_MIN_SIZE = 10

    var matrix = MutableList(count) { MutableList(count){ false } }

    private val neighbours = listOf(
            Pair(-1, -1),
            Pair(-1, 0),
            Pair(-1, 1),
            Pair(0, -1),
            Pair(0, 1),
            Pair(1, -1),
            Pair(1, 0),
            Pair(1, 1)
    )
    private val aliveRules = listOf(2, 3)
    private val deadRules = listOf(3)

    @RequiresApi(Build.VERSION_CODES.N)
    fun changeBoolean(row: Int, column: Int) :Boolean
    {
        matrix[row][column] = !matrix[row][column]
        Log.d("Neighbours: ", getNeighboursNumber(row, column).toString());
        return matrix[row][column];
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getNeighboursNumber(row: Int, column: Int): Int
    {
        var n = 0

        for(neighbour in neighbours)
        {
                val r = Math.floorMod(row + neighbour.first, count);
                val c = Math.floorMod(column + neighbour.second, count);
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
        val newMatrix = MutableList(count) { MutableList(count){ false }  }
        for(row in 0 until count)
        {
            for(col in 0 until count)
            {
                val n = getNeighboursNumber(row, col);
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

    fun increaseMatrixCount() :Boolean
    {

        if(count < MATRIX_MAX_SIZE) {
            count += 2
            return true
        }
        return false
    }

    fun reduceMatrixCount():Boolean
    {
        if(count > MATRIX_MIN_SIZE) {
            count -= 2
            return true
        }
        return false
    }

    constructor(parcel: Parcel) : this(parcel.readInt())
    {
        for(i in 0 until count)
        {
            matrix[i] = parcel.createBooleanArray()?.toMutableList()!!
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int)
    {
        parcel.writeInt(count)
        for(i in 0 until count)
        {
            parcel.writeBooleanArray(matrix[i].toBooleanArray())
        }
    }

    override fun describeContents(): Int {
        return matrix.count()
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