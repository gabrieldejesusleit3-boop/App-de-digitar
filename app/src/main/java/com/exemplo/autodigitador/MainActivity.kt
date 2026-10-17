package com.exemplo.autodigitador

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg1 = Color(0xFF0F0C29)
private val Bg2 = Color(0xFF302B63)
private val Accent = Color(0xFF7C5CFF)
private val CardBg = Color(0x33FFFFFF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Accent)) {
                Screen()
            }
        }
    }
}

@Composable
fun Screen() {
    val ctx = LocalContext.current
    var text by remember { mutableStateOf("UM!\nDOIS!\nTRÊS!") }
    var charDelay by remember { mutableFloatStateOf(80f) }
    var msgDelay by remember { mutableFloatStateOf(1500f) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Bg1, Bg2)))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("AutoDigitador", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Digita e envia suas mensagens sozinho", color = Color(0xFFB8B5D6))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val dot = when {
                        Bridge.running -> Color(0xFF4ADE80)
                        Bridge.armed -> Color(0xFFFACC15)
                        else -> Color(0xFF94A3B8)
                    }
                    Box(Modifier.size(12.dp).background(dot, RoundedCornerShape(50)))
                    Text(
                        if (Bridge.armed) "Armado: toque na caixa de texto" else Bridge.status,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Mensagens (uma por linha)", color = Color.White, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Velocidade de digitação: ${charDelay.toInt()} ms/letra", color = Color.White)
                    Slider(value = charDelay, onValueChange = { charDelay = it }, valueRange = 20f..400f)
                    Text("Pausa entre mensagens: ${"%.1f".format(msgDelay / 1000)} s", color = Color.White)
                    Slider(value = msgDelay, onValueChange = { msgDelay = it }, valueRange = 500f..10000f)
                }
            }

            Button(
                onClick = {
                    Bridge.messages = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
                    Bridge.charDelay = charDelay.toLong()
                    Bridge.msgDelay = msgDelay.toLong()
                    Bridge.armed = true
                    Bridge.status = "Armado"
                },
                enabled = !Bridge.running,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) { Text("Armar (depois toque no campo)", fontSize = 16.sp) }

            OutlinedButton(
                onClick = {
                    Bridge.stopRequested = true
                    Bridge.armed = false
                    Bridge.running = false
                    Bridge.status = "Parado"
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) { Text("Parar") }

            TextButton(
                onClick = { ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Abrir acessibilidade (ativar o serviço)") }
        }
    }
}
