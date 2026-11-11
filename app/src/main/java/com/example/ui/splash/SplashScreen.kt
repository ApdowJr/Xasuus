package com.example.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.toneFor
import com.example.ui.theme.XasuusTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay

/**
 * Splash: a calm paper canvas, an ink app mark that settles in, the wordmark, and a thin
 * three-dot loader. Motion is slow and single-purpose so the app feels composed, not busy.
 */
@Composable
fun SplashScreen(
    onSessionChecked: (isAuthenticated: Boolean, userId: String?) -> Unit
) {
    val colors = XasuusTheme.colors
    val infinite = rememberInfiniteTransition(label = "splash")

    val breathe by infinite.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )
    val dotPhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dots"
    )

    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val reveal by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "reveal"
    )

    LaunchedEffect(Unit) {
        // Brand moment + wait for Firebase Auth session resolution.
        delay(1100)
        val user = Firebase.auth.currentUser
        if (user != null) onSessionChecked(true, user.uid) else onSessionChecked(false, null)
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("splash_screen"),
        color = colors.canvas
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Soft ambient washes in the corners (brand violet + amber) — barely there.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 10.dp)
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                toneFor("purple").wash,
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                toneFor("amber").wash,
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .scale(0.94f + 0.06f * reveal)
                    .alpha(reveal),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .scale(breathe)
                        .size(112.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(colors.canvas)
                        .padding(5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = com.example.R.drawable.xasuus_logo),
                        contentDescription = "Astaanta Xasuus",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(26.dp))

                Text(
                    text = "Xasuus",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 34.sp,
                        letterSpacing = (-1).sp
                    ),
                    color = colors.ink
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Xasuus-qor casri ah oo ku xiran cloud-ka",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                    color = colors.inkMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(44.dp))

                // Three-dot loader, one dot highlighted at a time.
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    repeat(3) { index ->
                        val active = dotPhase.toInt() == index
                        Box(
                            modifier = Modifier
                                .size(width = if (active) 20.dp else 7.dp, height = 7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (active) colors.accent else colors.inkFaint.copy(alpha = 0.35f)
                                )
                        )
                    }
                }
            }

            Text(
                text = "FIREBASE · FIRESTORE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.5.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = colors.inkFaint.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 36.dp)
            )
        }
    }
}
