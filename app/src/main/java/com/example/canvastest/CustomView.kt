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

    private lateinit var myBitmap: Bitmap
    private lateinit var myCanvas: Canvas

    private var viewHeight = 2000
    private var viewWidth = 2000

    private var startX = 0f;
    private var startY = 0f;
    private var previousDragX = 0f;
    private var previousDragY = 0f;
    private var tmpX = 0F;
    private var tmpY = 0F;

    private val myListener =  object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            startX = e.getX() - previousDragX
            startY = e.getY() - previousDragY
            return true
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
                        if((tmpX * -1) < 0)
                        {
                            tmpX = 0f
                        }
                        else if((tmpX * -1) > viewWidth - width)
                        {
                            tmpX = (viewWidth - width) * -1f
                        }
                        if((tmpY * -1) < 0)
                        {
                            tmpY = 0f
                        }
                        else if((tmpY * -1) > viewHeight - height)
                        {
                            tmpY = (viewHeight - height) * -1f
                        }
                        scrollTo(-tmpX.toInt(), -tmpY.toInt())
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

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        setColor(Color.CYAN)
    }

    private val paint2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        setColor(Color.GREEN)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (::myBitmap.isInitialized) myBitmap.recycle()
        myBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888)
        myCanvas = Canvas(myBitmap)
        myCanvas.drawColor(Color.RED)

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.save()



        canvas?.drawBitmap(myBitmap, 0f, 0f, null)
        canvas?.drawRect(0F,0F,700F,700F, paint)
        canvas?.drawRect(701F,701F,1300F,1200F, paint2)
        canvas?.restore()
    }
}