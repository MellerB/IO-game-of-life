package com.example.canvastest

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
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
        matrixView.updateSquareSize()
        matrixView.myMatrix = viewModel.matrix
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