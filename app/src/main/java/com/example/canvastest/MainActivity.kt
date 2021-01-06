package com.example.canvastest

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

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
        val button = findViewById<Button>(R.id.pause)
        button.setOnClickListener{onPlay()}


        var handler = Handler()
        lateinit var r: Runnable
        r = Runnable {
            nextGenerationIfOn()
            handler.postDelayed(r, loopDelay)
        }
        handler.postDelayed(r, 0)

        }

    @RequiresApi(Build.VERSION_CODES.N)
    fun onPlay() {
        on = !on;
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun nextGeneration() {
        matrix.nextGeneration();
        var cv = findViewById<CustomView>(R.id.customView);
        cv.update()
        Log.d("INFO", "gen done ")
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun nextGenerationIfOn() {
        if(on)
        {
            nextGeneration()
        }
    }
}