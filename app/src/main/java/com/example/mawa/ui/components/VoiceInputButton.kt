package com.example.mawa.ui.components

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mawa.util.VoiceInputParser
import com.example.mawa.util.VoiceParsedResult
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.MawaPrimary
import java.util.Locale

@Composable
fun VoiceInputButton(
    onVoiceResult: (VoiceParsedResult) -> Unit,
    modifier: Modifier = Modifier,
    buttonText: String? = null,
    isPillStyle: Boolean = false,
    testTag: String = "btn_voice_input"
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = spokenMatches?.firstOrNull() ?: ""
            if (recognizedText.isNotBlank()) {
                val parsed = VoiceInputParser.parse(recognizedText)
                onVoiceResult(parsed)
                Toast.makeText(context, "🗣️ শোনা গেছে: \"$recognizedText\"", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun launchVoiceRecognition() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-BD")
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "bn-BD")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "বাংলায় বলুন (যেমন: চা ৬০ টাকা বা রহিম বাকি ৫০০ টাকা)...")
            }
            isListening = true
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            isListening = false
            Toast.makeText(context, "ভয়েস ইনপুট সার্ভিস চালু করা যায়নি", Toast.LENGTH_SHORT).show()
        }
    }

    val transition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    if (isPillStyle) {
        Surface(
            modifier = modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { launchVoiceRecognition() }
                .testTag(testTag),
            color = if (isListening) FinancialPositive else MawaPrimary.copy(alpha = 0.12f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isListening) FinancialPositive else MawaPrimary.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "ভয়েস দিয়ে লিখুন",
                    tint = if (isListening) Color.White else MawaPrimary,
                    modifier = Modifier.size(18.dp).scale(if (isListening) pulseScale else 1.0f)
                )
                if (!buttonText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isListening) "শুনছি..." else buttonText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isListening) Color.White else MawaPrimary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    } else {
        Box(
            modifier = modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (isListening) FinancialPositive else MawaPrimary.copy(alpha = 0.15f))
                .clickable { launchVoiceRecognition() }
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "ভয়েস দিয়ে লিখুন",
                tint = if (isListening) Color.White else MawaPrimary,
                modifier = Modifier
                    .size(22.dp)
                    .scale(if (isListening) pulseScale else 1.0f)
            )
        }
    }
}
