package com.example.canvastest

import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.SavedStateViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity()
{
    private val viewModel: MyViewModel by viewModels {
        SavedStateViewModelFactory(application, this)
    }


    private lateinit var matrixView: CustomView



    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        matrixView = findViewById(R.id.customView)
        matrixView.myMatrix = viewModel.matrix
        matrixView.updateSquareSize()

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

    val borderTreshold = 0.3
    val screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    val screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    val leftBorder = (screenWidth*borderTreshold).toInt()
    val rightBorder = screenWidth-leftBorder
    val multip = 10.0
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
                                            var floatDelta = ((ydelta) * (viewModel.loopDelay*viewModel.loopDelay*viewModel.loopDelay)/1000000000*multip)
                                            if(ydelta<0)
                                                {
                                                        floatDelta-=5
                                                    }
                                            else if (ydelta>0)
                                                {
                                                        floatDelta+=5
                                                    }
                                            viewModel.loopDelay -= floatDelta.toInt()
                                            if (viewModel.loopDelay < 5) {
                                                    viewModel.loopDelay = 5
                                                }
                                            if (viewModel.loopDelay > 1000) {
                                                    viewModel.loopDelay = 1000
                                                }
                                        }
                                    Log.d("delay", "$viewModel.loopDelay")
                                }
                        }
                    xpos = x;
                    ypos = y;
                    lastMeasure=System.currentTimeMillis()
                }

            return super.dispatchTouchEvent(event)
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

        viewModel.matrix.matrix.add(0, MutableList(viewModel.matrix.count){ false } )
        for(i in 1..viewModel.matrix.count - 2)
        {
            viewModel.matrix.matrix[i].add(0,false)
            viewModel.matrix.matrix[i].add(viewModel.matrix.count - 1, false)
        }
        viewModel.matrix.matrix.add(viewModel.matrix.count - 1, MutableList(viewModel.matrix.count){ false })


        matrixView.updateSquareSize()
        matrixView.update()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun reduceMatrixCount()
    {
        if(viewModel.matrix.count >= 10)
        {
            viewModel.matrix.reduceMatrixCount()

            viewModel.matrix.matrix.removeAt(0)
            for(i in 0..viewModel.matrix.count - 1)
            {
                viewModel.matrix.matrix[i].removeAt(0)
                viewModel.matrix.matrix[i].removeAt(viewModel.matrix.count)
            }
            viewModel.matrix.matrix.removeAt(viewModel.matrix.count)

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