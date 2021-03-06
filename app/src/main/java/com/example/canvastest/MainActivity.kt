package com.example.canvastest

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
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


        var rulesInput = findViewById<EditText>(R.id.rules)
        rulesInput.setText(viewModel.matrix.aliveRules.toString().filter { it.isDigit() }
                +"/"+
                viewModel.matrix.deadRules.toString().filter { it.isDigit() }
        )

        rulesInput.addTextChangedListener( object:TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {


                var s = rulesInput.selectionStart
                val regex = Regex("/[1234567890/]+/g;")
                var clearedText = regex.replace(text!!.toString(), "")

                if ("/" !in clearedText) {
                    clearedText = "$clearedText/"
                    s+=1
                }

                Log.i("CLEARED", clearedText)
                var t = clearedText.split("/")

                Log.i("t0", t[0].map(Character::getNumericValue).toIntArray().distinct().toString())
                Log.i("t1", t[1].map(Character::getNumericValue).toIntArray().distinct().toString())


                var alive = t[0].map(Character::getNumericValue).toIntArray()
                var dead = t[1].map(Character::getNumericValue).toIntArray()

                if(alive.distinct().size<alive.size || dead.distinct().size<dead.size)
                {
                    s-=1
                }

                viewModel.matrix.aliveRules = alive.distinct().toList()
                viewModel.matrix.deadRules = dead.distinct().toList()


                rulesInput.removeTextChangedListener(this);

                rulesInput.setText(viewModel.matrix.aliveRules.toString().filter { it.isDigit() }
                        +"/"+
                        viewModel.matrix.deadRules.toString().filter { it.isDigit() }
                )

                rulesInput.addTextChangedListener(this);

                if (s>rulesInput.text.length || s<0)
                {
                    s=rulesInput.text.length
                }

                rulesInput.setSelection(s)
            }



        } )




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

        val buttonMenuShowHide = findViewById<FloatingActionButton>(R.id.menuHideShow,)
        buttonMenuShowHide.setOnClickListener {

            loadSavedMatrixListToMenu()
            updateOpenButtonState()
            if(buttonIncreaseMatrixCount.visibility == View.VISIBLE)
            {
                buttonIncreaseMatrixCount.visibility = View.GONE
                buttonReduceMatrixCount.visibility = View.GONE
                buttonSave.visibility = View.GONE
                buttonOpen.visibility = View.GONE
                rulesInput.visibility = View.GONE
            }
            else
            {
                buttonIncreaseMatrixCount.visibility = View.VISIBLE
                buttonReduceMatrixCount.visibility = View.VISIBLE
                buttonSave.visibility = View.VISIBLE
                buttonOpen.visibility = View.VISIBLE
                rulesInput.visibility = View.VISIBLE
            }
        }


        val handler = Handler()
        lateinit var r: Runnable
        r = Runnable {
            nextGenerationIfOn()
            handler.postDelayed(r, viewModel.loopDelay)
        }
        handler.postDelayed(r, 0)


        Log.i("alive", viewModel.matrix.aliveRules.toString())
        Log.i("dead", viewModel.matrix.deadRules.toString())

    }




    private fun updateOpenButtonState()
    {
        val buttonOpen = findViewById<FloatingActionButton>(R.id.buttonOpen)
        if(viewModel.listOfSaves.size == 0)
        {
            (buttonOpen as FloatingActionButton).setEnabled(false);
            buttonOpen.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.material_on_primary_disabled));
        }
        else

        {
            (buttonOpen as FloatingActionButton).setEnabled(true);
            buttonOpen.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.teal_200))
        }
    }

        private val multip = 10.0
        private var ypos = 0;
        private var lastMeasure = System.currentTimeMillis();
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
    fun increaseMatrixCount()
    {
        if(viewModel.matrix.increaseMatrixCount()) {
            viewModel.matrix.matrix.add(0, MutableList(viewModel.matrix.count) { false })
            for (i in 1..viewModel.matrix.count - 2) {
                viewModel.matrix.matrix[i].add(0, false)
                viewModel.matrix.matrix[i].add(viewModel.matrix.count - 1, false)
            }
            viewModel.matrix.matrix.add(
                viewModel.matrix.count - 1,
                MutableList(viewModel.matrix.count) { false })

            matrixView.updateSquareSize()
            matrixView.update()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun reduceMatrixCount()
    {
            if (viewModel.matrix.reduceMatrixCount()) {
                viewModel.matrix.matrix.removeAt(0)
                for (i in 0..viewModel.matrix.count - 1) {
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
                .setTitle("Zapisz jako:")
                .setView(taskEditText)
                .setPositiveButton(
                        "Zapisz",
                ) { dialog, which ->
                    if(taskEditText.text.toString().isNotEmpty())
                    {
                        saveCurrentMatrix(taskEditText.text.toString())
                        updateOpenButtonState()
                    }
                }
                .create()

        dialog.show()

        val buttonParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
        )

        val posButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        posButton.layoutParams = buttonParams

        val inputParams: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        )
        inputParams.setMargins(50, 0, 50, 20)
        taskEditText.layoutParams = inputParams


    }




    private fun saveCurrentMatrix(comment: String)
    {
        val copy = Array(viewModel.matrix.count) { BooleanArray(viewModel.matrix.count) }
        for(i in copy.indices)
        {
            copy[i] = viewModel.matrix.matrix[i].toBooleanArray()
        }


        Log.i("saveded",matrixView.myMatrix.deadRules.toString())
        Log.i("savelive",matrixView.myMatrix.aliveRules.toString())

        val savedData = (SaveListItem(copy, matrixView.myMatrix.deadRules, matrixView.myMatrix.aliveRules, comment))

        viewModel.listOfSaves.add(savedData)
        saveMatrixListRecords()
    }

    private fun saveMatrixListRecords()
    {
        val mPrefs = getPreferences(MODE_PRIVATE)
        val prefsEditor = mPrefs.edit()
        val json = Gson().toJson(viewModel.listOfSaves)

        prefsEditor.putString("SavedObject", json)
        prefsEditor.apply()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun showLoadItemDialog(c: Context) {

        val builder = AlertDialog.Builder(c)
        builder.setTitle("Wczytaj")

        val arrayFromList = Array<CharSequence>(viewModel.listOfSaves.size) {
            viewModel.listOfSaves[it].comment
        }

        var checkedItem = 0
        builder.setSingleChoiceItems(arrayFromList, checkedItem) { dialog, which ->
            checkedItem = which
        }
        builder.setPositiveButton("Wczytaj") { _, _ ->
            if(checkedItem != -1 ) {
                loadChosenMatrix(checkedItem)
            }
        }
        builder.setNegativeButton("Usuń"){ _, which ->
            if(checkedItem != -1 ) {
                removeChoosenMatrix(checkedItem)
                saveMatrixListRecords()
            }
        }

        val dialog = builder.create()


        dialog.show()

        val buttonParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
        )

        buttonParams.setMargins(40, 0, 0, 0)
        val posButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        posButton.layoutParams = buttonParams
    }








    @RequiresApi(Build.VERSION_CODES.N)
    fun loadSavedMatrixListToMenu()
    {

        val mPrefs = getPreferences(MODE_PRIVATE)
        var json = mPrefs.getString("SavedObject", "")
        if (json.isNullOrBlank() || json.isEmpty())
        {
            saveMatrixListRecords()
            json = mPrefs.getString("SavedObject", "")
        }

        val itemType = object: TypeToken<MutableList<SaveListItem>>() {}.type
        viewModel.listOfSaves = Gson().fromJson(json, itemType)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun loadChosenMatrix(index: Int) {
        val copy =
                MutableList(viewModel.listOfSaves[index].matrix.size)
                { MutableList(viewModel.listOfSaves[index].matrix.size) { false } }
        for (i in 0 until copy.size) {
            copy[i] = viewModel.listOfSaves[index].matrix[i].toMutableList()
        }
        viewModel.matrix.matrix = copy
        viewModel.matrix.count = viewModel.matrix.matrix.size
        matrixView.myMatrix = viewModel.matrix

        matrixView.myMatrix.deadRules = viewModel.listOfSaves[index].deadRules
        matrixView.myMatrix.aliveRules = viewModel.listOfSaves[index].aliveRules

        Log.i("ded", matrixView.myMatrix.deadRules.toString())
        matrixView.updateSquareSize()
        matrixView.update()
        var rulesInput = findViewById<EditText>(R.id.rules)

                rulesInput.setText(viewModel.matrix.aliveRules.toString().filter { it.isDigit() }
                        + "/" +
                        viewModel.matrix.deadRules.toString().filter { it.isDigit() }
                )



        Log.i("loadded",viewModel.matrix.deadRules.toString())
        Log.i("loadlive",viewModel.matrix.aliveRules.toString())
            }


    private fun removeChoosenMatrix(index: Int)
    {
        viewModel.listOfSaves.removeAt(index)
        updateOpenButtonState()
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