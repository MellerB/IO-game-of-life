package com.example.inzynieria_oprogramowania
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import kotlinx.android.synthetic.main.activity_main.*
//import com.example.inzynieria_oprogramowania.databinding.ActivityMainBinding



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //val binding = ActivityMainBinding.inflate(layoutInflater)
        //setContentView(binding.root)
        setContentView(R.layout.activity_main);

        customView.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> customView.update(event.getX(), event.getY());
                //println(event.getX().toString().plus(" ").plus(event.getY()));
            }

            v?.onTouchEvent(event) ?: true
        }
    }
}
