package com.exemplo.autodigitador

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object Bridge {
    var messages: List<String> = emptyList()
    var charDelay: Long = 80L
    var msgDelay: Long = 1500L

    var armed by mutableStateOf(false)
    var running by mutableStateOf(false)
    var status by mutableStateOf("Parado")
    var stopRequested = false
}
