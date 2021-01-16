package com.example.canvastest

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.SavedStateViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


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

        val buttonSave = findViewById<FloatingActionButton>(R.id.buttonSave)
        buttonSave.setOnClickListener {
            showAddItemDialog(this)
        }

        val buttonOpen = findViewById<FloatingActionButton>(R.id.buttonOpen)
        buttonOpen.setOnClickListener {
            showLoadItemDialog(this)
        }

        val buttonMenuShowHide = findViewById<FloatingActionButton>(R.id.menuHideShow)
        buttonMenuShowHide.setOnClickListener {
            if(buttonIncreaseMatrixCount.visibility == View.VISIBLE)
            {
                buttonIncreaseMatrixCount.visibility = View.GONE
                buttonReduceMatrixCount.visibility = View.GONE
                buttonSave.visibility = View.GONE
                buttonOpen.visibility = View.GONE
            }
            else
            {
                buttonIncreaseMatrixCount.visibility = View.VISIBLE
                buttonReduceMatrixCount.visibility = View.VISIBLE
                buttonSave.visibility = View.VISIBLE
                buttonOpen.visibility = View.VISIBLE
            }
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


    val multip = 10.0
        var ypos=0;
        var lastMeasure = System.currentTimeMillis();
        override fun dispatchTouchEvent(event: MotionEvent):Boolean{
            if(System.currentTimeMillis() - lastMeasure > 50) {
                    val x = event.x.toInt()
                    val y = event.y.toInt()
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
                            if (!matrixView.isZoommed()) {
                                var floatDelta =
                                    ((ydelta) * (viewModel.loopDelay * viewModel.loopDelay * viewModel.loopDelay) / 1000000000 * multip)
                                if (ydelta < 0) {
                                    floatDelta -= 5
                                } else if (ydelta > 0) {
                                    floatDelta += 5
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

        viewModel.matrix.matrix.add(0, MutableList(viewModel.matrix.count) { false })
        for(i in 1..viewModel.matrix.count - 2)
        {
            viewModel.matrix.matrix[i].add(0, false)
            viewModel.matrix.matrix[i].add(viewModel.matrix.count - 1, false)
        }
        viewModel.matrix.matrix.add(
            viewModel.matrix.count - 1,
            MutableList(viewModel.matrix.count) { false })

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

    private fun showAddItemDialog(c: Context) {
        val taskEditText = EditText(c)
        val dialog: AlertDialog = AlertDialog.Builder(c)
            .setTitle("Zapis planszy")
            .setMessage("Dodaj komentarz zapisu")
            .setView(taskEditText)
            .setPositiveButton("Dodaj"
            ) { dialog, which ->
                if(taskEditText.text.toString().length > 0)
                {
                    saveCurrentMatrix(taskEditText.text.toString())
                }
            }
            .setNegativeButton("Anuluj", null)
            .create()
        dialog.show()
    }

    fun saveCurrentMatrix(comment: String)
    {
        val copy = Array(viewModel.matrix.count) { BooleanArray(viewModel.matrix.count) }
        for(i in 0..copy.size - 1)
        {
            copy[i] = viewModel.matrix.matrix[i].toBooleanArray()
        }
        val savedData = (SaveListItem(copy, comment))

        viewModel.listOfSaves.add(savedData)
        saveMatrixListRecords()
    }

    fun saveMatrixListRecords()
    {
        val mPrefs = getPreferences(MODE_PRIVATE)
        val prefsEditor = mPrefs.edit()
        val json = Gson().toJson(viewModel.listOfSaves)

        prefsEditor.putString("SavedObject", json)
        prefsEditor.apply()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun showLoadItemDialog(c: Context) {
        loadSavedMatrixListToMenu()
        val builder = AlertDialog.Builder(c)
        builder.setTitle("Wybierz zapis")

        val arrayFromList = Array<CharSequence>(viewModel.listOfSaves.size) {
            viewModel.listOfSaves[it].comment
        }

        var checkedItem = 0
        builder.setSingleChoiceItems(arrayFromList, checkedItem) { dialog, which ->
            checkedItem = which
        }
        builder.setPositiveButton("Załaduj") { dialog, which ->
            loadChosenMatrix(checkedItem)
        }
        builder.setNegativeButton("Usuń") { dialog, which ->
            removeChoosenMatrix(checkedItem)
            saveMatrixListRecords()
        }

        val dialog = builder.create()
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun loadSavedMatrixListToMenu()
    {
        val mPrefs = getPreferences(MODE_PRIVATE)
        val json = mPrefs.getString("SavedObject", "")

        val itemType = object: TypeToken<MutableList<SaveListItem>>() {}.type
        viewModel.listOfSaves = Gson().fromJson(json, itemType)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun loadChosenMatrix(index: Int)
    {
        val copy =
                MutableList(viewModel.listOfSaves[index].matrix.size)
                { MutableList(viewModel.listOfSaves[index].matrix.size){ false }}
        for(i in 0..copy.size - 1)
        {
            copy[i] = viewModel.listOfSaves[index].matrix[i].toMutableList()
        }
        viewModel.matrix.matrix = copy
        viewModel.matrix.count = viewModel.matrix.matrix.size
        matrixView.myMatrix = viewModel.matrix

        matrixView.updateSquareSize()
        matrixView.update()
    }

    fun removeChoosenMatrix(index: Int)
    {
        viewModel.listOfSaves.removeAt(index)
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