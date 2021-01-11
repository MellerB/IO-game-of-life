package com.example.canvastest

import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*


class MainActivity : AppCompatActivity() {

    val matrix = Matrix(32);
    var loopDelay = 100L;
    val borderTreshold = 0.3

    val screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    val screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    val leftBorder = (screenWidth*borderTreshold).toInt()
    val rightBorder = screenWidth-leftBorder
    val multip = 10.0

    var on = false;

    var xpos=0;
    var ypos=0;
    var lastMeasure = System.currentTimeMillis();
    override fun dispatchTouchEvent(event: MotionEvent):Boolean{
        if(System.currentTimeMillis() - lastMeasure > 50) {
            val x = event.x.toInt()
            val y = event.y.toInt()
            val xdelta = xpos - x
            var ydelta = ypos - y
            if(ydelta<0)
            {
                ydelta-=10
            }
            else if (ydelta>0)
            {
                ydelta+=10
            }

            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    if (x > rightBorder) {
                        var floatDelta = ((ydelta) * (loopDelay*loopDelay*loopDelay)/1000000000*multip)
                        if(ydelta<0)
                        {
                            floatDelta-=5
                        }
                        else if (ydelta>0)
                        {
                            floatDelta+=5
                        }
                        loopDelay -= floatDelta.toInt()
                        if (loopDelay < 5) {
                            loopDelay = 5
                        }
                        if (loopDelay > 1000) {
                            loopDelay = 1000
                        }
                    }
                    Log.d("delay", "$loopDelay")
                }
            }
            xpos = x;
            ypos = y;
            lastMeasure=System.currentTimeMillis()
        }

        return super.dispatchTouchEvent(event)
        }




    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        matrixView = findViewById(R.id.customView)
        matrixView.myMatrix = viewModel.matrix

        val buttonPause = findViewById<FloatingActionButton>(R.id.pause)
        buttonPause.setOnLongClickListener{onPlay()}
        buttonPause.setOnClickListener{handleShortClick()}

        val buttonIncreaseMatrixCount = findViewById<FloatingActionButton>(R.id.sizeUp)
        buttonIncreaseMatrixCount.setOnClickListener {
            increaseMatrixCount()
        }

        val buttonReduceMatrixCount = findViewById<FloatingActionButton>(R.id.sizeDown)
        buttonReduceMatrixCount.setOnClickListener {
            reduceMatrixCount()
        }
        //val buttonNextGen = findViewById<Button>(R.id.next_gen)
        //buttonNextGen.setOnClickListener{nextGeneration()}

        val handler = Handler()
        lateinit var r: Runnable
        r = Runnable {
            nextGenerationIfOn()
            handler.postDelayed(r, viewModel.loopDelay)
        }
        handler.postDelayed(r, 0)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun handleShortClick()
    {
        if (viewModel.on)
        {
            turnOff()
        }
        else
        {
            nextGeneration()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun onPlay(): Boolean {
        if (viewModel.on)
        {
            turnOff()
        } else
        {
            turnOn()
        }
        return viewModel.on;
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun nextGeneration() {
        viewModel.matrix.nextGeneration();
        matrixView.update()
        Log.d("INFO", "gen done ")
    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun nextGenerationIfOff() {
        if(!viewModel.on)
        {
            nextGeneration()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun nextGenerationIfOn() {
        if(viewModel.on)
        {
            nextGeneration()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun turnOn(){
        viewModel.on = true
        val button = findViewById<FloatingActionButton>(R.id.pause)
        button.setImageResource(android.R.drawable.ic_media_pause)

        matrixView.update()
    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun turnOff(){
        viewModel.on = false
        val button = findViewById<FloatingActionButton>(R.id.pause)
        button.setImageResource(android.R.drawable.ic_media_play)

        matrixView.update()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun turnOffIfOn() {
        if(viewModel.on)
        {
            //turnOff()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun increaseMatrixCount()
    {
        viewModel.matrix.increaseMatrixCount()
        val tmp = Array(viewModel.matrix.count) { BooleanArray(viewModel.matrix.count) }
        for(i in 1..tmp.size - 2)
        {
            for(j in 1..tmp.size - 2)
            {
                tmp[i][j] = viewModel.matrix.matrix[i - 1][j - 1]
            }
        }
        viewModel.matrix.matrix = tmp
        matrixView.myMatrix = viewModel.matrix
        matrixView.updateSquareSize()
        matrixView.update()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun reduceMatrixCount()
    {
        if(viewModel.matrix.count >= 10)
        {
            viewModel.matrix.reduceMatrixCount()
            val tmp = Array(viewModel.matrix.count) { BooleanArray(viewModel.matrix.count) }
            for(i in 0..tmp.size - 1)
            {
                for(j in 0..tmp.size - 1)
                {
                    tmp[i][j] = viewModel.matrix.matrix[i + 1][j + 1]
                }
            }
            viewModel.matrix.matrix = tmp
            matrixView.myMatrix = viewModel.matrix
            matrixView.updateSquareSize()
            matrixView.update()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.setSavedMatrix()
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        viewModel.restoreSavedMatrix()
        matrixView.myMatrix = viewModel.matrix
        super.onRestoreInstanceState(savedInstanceState)
    }
}