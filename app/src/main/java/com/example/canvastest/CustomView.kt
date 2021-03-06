package com.example.canvastest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_CANCEL
import android.view.MotionEvent.INVALID_POINTER_ID
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.graphics.createBitmap


private const val mActivePointerId = INVALID_POINTER_ID
class CustomView @kotlin.jvm.JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr)
{
    lateinit var myMatrix: Matrix
    private lateinit var myBitmap: Bitmap
    private lateinit var myCanvas: Canvas

    private var matrixSize = 0 //wielkosc planszy
    private var borderThickness = 2f //szerokosc grida
    private var outerBorderThickness = borderThickness/2
    private var squareSize = 0f //pojedynczy kwadrat

    //zmienne okreslajace ostatnie polozenie palca podczas scroll'owania
    private var startX = 0f
    private var startY = 0f

    //zmienne pozycji początku okna widoku wzgledem calego canvas'u
    private var viewX = 0f
    private var viewY = 0F

    //okresla palec na podstawie ktorego mozemy przesuwac widok
    private var pointerID = 0

    //zmienne okreslajace wielkosci zoom in / zoom out
    private var myScaleFactor = 1f
    private val maxScaleFactor = 2f
    private val minScaleFactor = 1f

    //zmienna pomocnicza okreslajaca czy widok jest aktualnie zoomowany
    private var scalingBool = false

    fun isZoommed(): Boolean {
        return scalingBool || myScaleFactor != minScaleFactor
    }

    //okreslenie wlasnosci malowania na canvasie
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = borderThickness
        setColor(Color.CYAN)
    }

    private val gridCornerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = borderThickness/2
        setColor(Color.CYAN)
    }

    private val squarePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        setColor(Color.RED)
    }
    private val squarePaintBlank = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        setColor(Color.DKGRAY)
    }

    private val myListener =  object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            //zapamietanie gdzie dotknelismy ekran
            startX = e.getX(e.actionIndex)
            startY = e.getY(e.actionIndex)
            //pozyskanie ID z dotkniecia ekranu
            pointerID = e.getPointerId(e.actionIndex)
            return true
        }

        @RequiresApi(Build.VERSION_CODES.N)
        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            //funkcja zmieniajaca boolean w Matrix oraz rysujaca wynik zmiany na ekran
            val column = e?.getX()?.minus(viewX)?.div(squareSize*myScaleFactor)?.toInt()
            val row = e?.getY()?.minus(viewY)?.div(squareSize*myScaleFactor)?.toInt()

            if (column != null && row != null) {
                myMatrix.changeBoolean(row, column)
                drawMatrix()
            }
            return super.onSingleTapUp(e)
        }

    }

    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener()
    {
        //zmienne pomocnicze do obliczenia aktualnej pozycji srodka ekranu podczas zoom'owania
        private var lastScale = minScaleFactor
        private var tmpViewX = 0f
        private var tmpViewY = 0f


        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            lastScale *= detector.scaleFactor
            tmpViewX = viewX / myScaleFactor
            tmpViewY = viewY / myScaleFactor
            return super.onScaleBegin(detector)
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            scalingBool = true
            super.onScaleEnd(detector)
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            myScaleFactor *= detector.scaleFactor
            myScaleFactor = Math.max(minScaleFactor, Math.min(myScaleFactor, maxScaleFactor))

            //wartosc zmiany pozycji srodka ekranu podczas zoom'owania
            val diffX = (detector.focusX / (lastScale)) - (detector.focusX / (myScaleFactor))
            val diffY = (detector.focusY / (lastScale)) - (detector.focusY / (myScaleFactor))

            tmpViewX -= diffX
            tmpViewY -= diffY

            //update pozycji widoku
            viewX = tmpViewX * myScaleFactor
            viewY = tmpViewY * myScaleFactor

            checkEdges()
            lastScale = myScaleFactor
            return super.onScale(detector)
        }
    }

    private val detector: GestureDetector = GestureDetector(context, myListener)
    private val scaleDetector: ScaleGestureDetector = ScaleGestureDetector(context, scaleListener)


    override fun onTouchEvent(event: MotionEvent): Boolean {
        val normalGesture = detector.onTouchEvent(event)
        val zoomedGesture = scaleDetector.onTouchEvent(event)

        normalGesture.let { result ->
            if (!result && !scaleDetector.isInProgress) {
                when (event.action) {
                    //ruch widoku
                    MotionEvent.ACTION_MOVE -> {
                        var pointerIndex = event.findPointerIndex(pointerID)
                        val initialID = pointerID
                        while ( pointerIndex == -1 && pointerID < 8)
                        {
                            pointerID++
                            pointerIndex = event.findPointerIndex(pointerID)
                        }
                        if(pointerIndex == -1)
                        {
                            pointerID = -1
                        }
                        while(pointerIndex == -1 && pointerID < initialID)
                        {
                            pointerID++
                            pointerIndex = event.findPointerIndex(pointerID)
                        }

                        val(x: Float, y: Float) = event.getX(pointerIndex) to event.getY(pointerIndex)

                        if(scalingBool)
                        {
                            scalingBool = false
                        }
                        else
                        {
                            viewX += x - startX
                            viewY += y - startY
                        }
                        startX = x
                        startY = y
                        checkEdges()
                        true
                    }
                    MotionEvent.ACTION_UP, ACTION_CANCEL -> {
                       pointerID = -1
                        true
                    }
                    //zmiana pointerID jezeli palec zostaje sciagniety z ekranu
                    MotionEvent.ACTION_POINTER_UP -> {
                        event.actionIndex.also{ pointerIndex ->
                            event.getPointerId(pointerIndex)
                                    .takeIf { it == pointerID }
                                    ?.run{
                                        val newPointerIndex = if (pointerIndex == 0) 1 else 0
                                        startX = event.getX(newPointerIndex)
                                        startY = event.getY(newPointerIndex)
                                        pointerID = event.getPointerId(newPointerIndex)
                                    }
                        }
                        true
                    }
                    else -> false
                }
            }
            else true
        }
        invalidate()
        return normalGesture || zoomedGesture
    }

    private fun checkEdges()
    {
        if((-viewX) < 0)
        {
            viewX = 0f
        }
        else if((-viewX) > matrixSize * myScaleFactor - width)
        {
            viewX = (matrixSize * myScaleFactor - width) * -1f
        }

        if((-viewY) < 0)
        {
            viewY = 0f
        }
        else if((-viewY) > matrixSize * myScaleFactor - height)
        {
            viewY = (matrixSize * myScaleFactor - height) * -1f

        }
    }

    private fun colorSquare(row: Int, column: Int)
    {
        val currentPaint: Paint = if(myMatrix.matrix[row][column])
        {
            squarePaint
        }
        else
        {
            squarePaintBlank
        }
        myCanvas.drawRect(column * squareSize,
                row * squareSize,
                column * squareSize + squareSize,
                row * squareSize + squareSize, currentPaint)
    }

    private fun drawMatrix()
    {
        myCanvas.drawColor(Color.GRAY)

        for( i in 0..myMatrix.count-1)
        {
            for( j in 0..myMatrix.count-1)
            {
                colorSquare(i,j)
            }
        }

        myCanvas.drawLine(outerBorderThickness/2,
                0f,
                outerBorderThickness/2,
                matrixSize.toFloat(),
                gridCornerPaint)

        for(j in 1 until myMatrix.count)
        {
            myCanvas.drawLine(squareSize * j,
                    0f,
                    squareSize * j,
                    matrixSize.toFloat(),
                    gridPaint)
        }

        myCanvas.drawLine(matrixSize.toFloat() - outerBorderThickness/2,
                0f,
                matrixSize.toFloat() - outerBorderThickness/2,
                matrixSize.toFloat(),
                gridCornerPaint)

        myCanvas.drawLine(0f,
                outerBorderThickness/2,
                matrixSize.toFloat(),
                outerBorderThickness/2,
                gridCornerPaint)

        for(j in 1 until myMatrix.count)
        {
            myCanvas.drawLine(0f,
                    squareSize * j,
                    matrixSize.toFloat(),
                    squareSize * j,
                    gridPaint)
        }
        myCanvas.drawLine(0f,
                matrixSize.toFloat() - outerBorderThickness/2,
                matrixSize.toFloat(),
                matrixSize.toFloat() - outerBorderThickness/2,
                gridCornerPaint)
    }

    //metoda wywolywana podczas zmiany rozmiaru ekranu telefonu, czyli tylko podczas odpalenia programu
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (::myBitmap.isInitialized) myBitmap.recycle()

        //ustalenie parametrow poczatkowych
        matrixSize = height
        myBitmap = createBitmap(matrixSize, matrixSize, Bitmap.Config.ARGB_8888)
        myCanvas = Canvas(myBitmap)
        updateSquareSize()
        update()
        putViewInTheMiddle()
    }

    fun updateSquareSize()
    {
        squareSize = (matrixSize.toFloat() / myMatrix.count)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun update()
    {
        drawMatrix()
        invalidate()
    }

    private fun putViewInTheMiddle()
    {
        startX = (matrixSize - width)/2f
        viewX = -startX
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            save()
            scale(myScaleFactor, myScaleFactor)
            translate(viewX/myScaleFactor, viewY/myScaleFactor)
            drawBitmap(myBitmap, 0f, 0f, null)
            restore()
        }
    }
}