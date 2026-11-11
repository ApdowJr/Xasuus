package com.example.ui.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.ActionButton
import com.example.ui.theme.XasuusTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AppLockScreen(onUnlocked: () -> Unit) {
    val colors = XasuusTheme.colors
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var verifying by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize(), color = colors.canvas) {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(76.dp).clip(RoundedCornerShape(25.dp)).background(colors.inkPill),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = colors.onInkPill, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(22.dp))
            Text("Xasuus waa xiran yahay", style = MaterialTheme.typography.headlineSmall, color = colors.ink)
            Spacer(modifier = Modifier.height(7.dp))
            Text("Geli PIN-kaaga si aad u furto qoraalladaada.", style = MaterialTheme.typography.bodyMedium, color = colors.inkMuted, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(22.dp))
            BasicTextField(
                value = pin,
                onValueChange = { pin = it.filter(Char::isDigit).take(8); error = false },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                textStyle = MaterialTheme.typography.headlineSmall.copy(color = colors.ink, textAlign = TextAlign.Center),
                cursorBrush = SolidColor(colors.accent),
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(colors.card).padding(17.dp)
            )
            if (error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("PIN-ku sax ma aha.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(18.dp))
            ActionButton(
                text = "Fur Xasuus",
                onClick = {
                    val submittedPin = pin
                    pin = ""
                    verifying = true
                    scope.launch {
                        val valid = withContext(Dispatchers.Default) { AppPinStore.verify(context, submittedPin) }
                        verifying = false
                        if (valid) onUnlocked() else error = true
                    }
                },
                enabled = pin.length >= 4 && !verifying,
                loading = verifying,
                modifier = Modifier.fillMaxWidth(0.8f),
                container = colors.inkPill,
                contentColor = colors.onInkPill
            )
        }
    }
}
