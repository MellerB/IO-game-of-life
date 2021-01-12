package com.example.canvastest

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class MyViewModel(val handle: SavedStateHandle): ViewModel() {
    var matrix = Matrix(20)
    var loopDelay = 250L
    var on = false


    fun setSavedMatrix()
    {
        handle.set("savedMatrix", matrix)
    }

    fun restoreSavedMatrix()
    {
        matrix = handle.get<Matrix>("savedMatrix")!!
    }
}