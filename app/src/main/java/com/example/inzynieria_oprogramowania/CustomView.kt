package com.example.inzynieria_oprogramowania
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.floor


class CustomView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr)
{
    companion object
    {
        var board: Board = Board(0, 0)
        var squareSize = 0
    }

    private val strokeSize=5f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth=strokeSize
        textAlign = Paint.Align.CENTER
    }

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeWidth=strokeSize
        setColor(Color.WHITE)
        textAlign = Paint.Align.CENTER
    }

    override fun onSizeChanged(xNew: Int, yNew: Int, xOld: Int, yOld: Int) {
        super.onSizeChanged(xNew, yNew, xOld, yOld)

        var tmpHeight = xNew
        var tmpWidth = yNew

        while (tmpWidth != tmpHeight)
        {
            if (tmpWidth > tmpHeight)
                tmpWidth -= tmpHeight
            else
                tmpHeight -= tmpWidth
        }
        squareSize = tmpHeight

        board = Board(yNew/squareSize, xNew/squareSize)
        //println("Rows: "+yNew/squareSize)
        //println("Columns: "+xNew/squareSize)
    }


    override fun onDraw(canvas: Canvas)
    {
        super.onDraw(canvas)

        for(j in 1 until board.columns)
        {
            canvas.drawLine(squareSize.toFloat()*j,0f,squareSize.toFloat()*j, height.toFloat(), paint)
        }
        for(j in 1 until board.rows)
        {
            canvas.drawLine(0f,squareSize.toFloat()*j, width.toFloat(),squareSize.toFloat()*j, paint)
        }

        /*
        for(x in 0 until board.rows)
        {
            for(y in 0 until board.columns)
            {
                if(board.matrix[x][y] == true)
                {
                    canvas.drawRect(y*squareSize.toFloat(), x* squareSize.toFloat(), (y*squareSize)+ squareSize.toFloat(), (x*squareSize)+ squareSize.toFloat(), fill )
                }
            }
        }
        */
    }

    fun update(x: Float, y: Float)
    {
        val matrix_x = floor(x/ squareSize).toInt()
        val matrix_y = floor(y/ squareSize).toInt()

        println("Row: "+matrix_y)
        println("Column: "+matrix_x)
        board.matrix[matrix_y][matrix_x] = board.matrix[matrix_y][matrix_x] == false
    }
}




