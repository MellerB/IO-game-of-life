package com.example.canvastest
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Switch
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewCompat
import kotlin.math.floor


class CustomView @kotlin.jvm.JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr)
{
    private lateinit var myMatrix: Matrix
    private lateinit var myBitmap: Bitmap
    private lateinit var myCanvas: Canvas

    private var borderThickness = 1f
    private var squareSize = 0f
    private var startX = 0f
    private var startY = 0f
    private var previousDragX = 0f
    private var previousDragY = 0f
    private var tmpX = 0f
    private var tmpY = 0F

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = borderThickness
        setColor(Color.CYAN)
    }

    private val squarePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        setColor(Color.RED)
    }

    private val squarePaintBlank = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        setColor(Color.GRAY)
    }

    private val myListener =  object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            startX = e.getX() - previousDragX
            startY = e.getY() - previousDragY
            return true
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            val column = e?.getX()?.minus(previousDragX)?.div(squareSize)?.toInt()
            val row = e?.getY()?.div(squareSize)?.toInt()

            if (column != null && row != null) {
                myMatrix.changeBoolean(row, column)
                colorSquare(row, column)
            }
            return super.onSingleTapConfirmed(e)
        }
    }
    private val detector: GestureDetector = GestureDetector(context, myListener)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return detector.onTouchEvent(event).let { result ->
            if (!result) {
                when (event.action) {
                    MotionEvent.ACTION_MOVE -> {
                        tmpX = event.getX() - startX
                        tmpY = event.getY() - startY
                        checkEdges()
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        previousDragX = tmpX
                        previousDragY = tmpY
                        true
                    }
                    else -> false
                }
            } else true
        }
    }

    private fun checkEdges()
    {
        if((tmpX * -1) < 0)
        {
            tmpX = 0f
        }
        else if((tmpX * -1) > height - width)
        {
            tmpX = (height - width) * -1f
        }

        if((tmpY * -1) < 0)
        {
            tmpY = 0f
        }
        else if((tmpY * -1) > height - height)
        {
            tmpY = (height - height) * -1f
        }
        scrollTo(-tmpX.toInt(), -tmpY.toInt())
    }

    private fun colorSquare( row: Int, column: Int)
    {
        if(myMatrix.matrix[row][column])
        {
            myCanvas.drawRect(column * squareSize + borderThickness, row * squareSize + borderThickness, column * squareSize + squareSize - borderThickness, row * squareSize + squareSize - borderThickness, squarePaint)
        }
        else
        {
            myCanvas.drawRect(column * squareSize + borderThickness, row * squareSize + borderThickness, column * squareSize + squareSize - borderThickness, row * squareSize + squareSize - borderThickness, squarePaintBlank)
        }
        invalidate()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (::myBitmap.isInitialized) myBitmap.recycle()
        myMatrix = Matrix(10)
        myBitmap = Bitmap.createBitmap(height, height, Bitmap.Config.ARGB_8888)
        myCanvas = Canvas(myBitmap)
        myCanvas.drawColor(Color.GRAY)
        squareSize = (height/myMatrix.count).toFloat()
        for(j in 1 until myMatrix.count)
        {
            myCanvas.drawLine(squareSize * j,0f,squareSize * j, height.toFloat(), gridPaint)
        }
        for(j in 1 until myMatrix.count)
        {
            myCanvas.drawLine(0f,squareSize * j, height.toFloat(),squareSize * j, gridPaint)
        }
        scrollTo((height - width)/2 ,0)
        previousDragX = -(height - width)/2f
        startX = (height - width)/2f
        tmpX = -startX
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.save()
        canvas?.drawBitmap(myBitmap, 0f,0f, null)
        canvas?.restore()
    }
}