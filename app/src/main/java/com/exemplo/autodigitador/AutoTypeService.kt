package com.exemplo.autodigitador

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AutoTypeService : AccessibilityService() {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var job: Job? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!Bridge.armed || Bridge.running) return
        if (event.eventType != AccessibilityEvent.TYPE_VIEW_FOCUSED) return
        val node = event.source ?: return
        if (node.isEditable) startTyping()
    }

    override fun onInterrupt() {
        stop()
    }

    private fun startTyping() {
        Bridge.armed = false
        Bridge.running = true
        Bridge.stopRequested = false
        job = scope.launch {
            delay(600)
            val list = Bridge.messages
            for (i in list.indices) {
                if (Bridge.stopRequested) break
                val msg = list[i]
                val field = findField()
                if (field == null) {
                    Bridge.status = "Campo de texto perdido"
                    break
                }
                Bridge.status = "Digitando ${i + 1} de ${list.size}"
                for (n in 1..msg.length) {
                    if (Bridge.stopRequested) break
                    setText(field, msg.substring(0, n))
                    delay(Bridge.charDelay)
                }
                if (Bridge.stopRequested) break
                delay(250)
                send(field)
                delay(Bridge.msgDelay)
            }
            Bridge.running = false
            if (!Bridge.stopRequested && Bridge.status.startsWith("Digitando")) {
                Bridge.status = "Concluído"
            }
        }
    }

    private fun stop() {
        Bridge.stopRequested = true
        job?.cancel()
        Bridge.running = false
        Bridge.armed = false
        Bridge.status = "Parado"
    }

    private fun findField(): AccessibilityNodeInfo? =
        rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)

    private fun setText(node: AccessibilityNodeInfo, text: String) {
        val args = Bundle()
        args.putCharSequence(
            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text
        )
        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    private fun send(field: AccessibilityNodeInfo) {
        val root = rootInActiveWindow
        val names = listOf("Enviar", "Send")
        for (name in names) {
            val btn = root?.findAccessibilityNodeInfosByText(name)
                ?.firstOrNull { it.isClickable && it.isEnabled }
            if (btn != null) {
                btn.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return
            }
        }
        if (Build.VERSION.SDK_INT >= 30) {
            field.performAction(
                AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER.id
            )
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
