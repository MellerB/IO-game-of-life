package com.example.canvastest

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.internal.ContextUtils.getActivity


class MainActivity : AppCompatActivity() {

    val matrix = Matrix(20);
    val loopDelay = 250L;
    var on = false;


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val matrixView = findViewById<CustomView>(R.id.customView)
        matrixView.myMatrix = matrix
        val buttonPause = findViewById<FloatingActionButton>(R.id.pause)
        buttonPause.setOnLongClickListener{onPlay()}
        buttonPause.setOnClickListener{handleShortClick()}
        //val buttonNextGen = findViewById<Button>(R.id.next_gen)
        //buttonNextGen.setOnClickListener{nextGeneration()}


        var handler = Handler()
        lateinit var r: Runnable
        r = Runnable {
            nextGenerationIfOn()
            handler.postDelayed(r, loopDelay)
        }
        handler.postDelayed(r, 0)

        }

    @RequiresApi(Build.VERSION_CODES.N)
    fun handleShortClick()
    {
        if (on)
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
        if (on)
        {
            turnOff()
        } else
        {
            turnOn()
        }
        return on;
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun nextGeneration() {
        matrix.nextGeneration();
        var cv = findViewById<CustomView>(R.id.customView);
        cv.update()
        Log.d("INFO", "gen done ")
    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun nextGenerationIfOff() {
        if(!on)
        {
            nextGeneration()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun nextGenerationIfOn() {
        if(on)
        {
            nextGeneration()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun turnOn(){
        on=true;
        var button = findViewById<FloatingActionButton>(R.id.pause)
        button.setImageResource(android.R.drawable.ic_media_pause)

        var cv = findViewById<CustomView>(R.id.customView);
        cv.update()
    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun turnOff(){
        on=false;
        var button = findViewById<FloatingActionButton>(R.id.pause)
        button.setImageResource(android.R.drawable.ic_media_play)
        var cv = findViewById<CustomView>(R.id.customView);
        cv.update()
    }

    fun turnOffIfOn() {
        if(on)
        {
            turnOff()
        }
    }
}