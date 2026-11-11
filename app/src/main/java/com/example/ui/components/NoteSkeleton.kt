package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.XasuusTheme

/**
 * Shimmer brush used across every skeleton in the app. It sweeps a soft highlight across a
 * recessed base so loading states feel calm rather than flashy.
 */
@Composable
fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = -320f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val colors = XasuusTheme.colors
    val base = colors.recessed
    val highlight = if (colors.isDark) {
        androidx.compose.ui.graphics.Color(0xFF37324A)
    } else {
        androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    }

    return Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(translateAnim - 320f, translateAnim - 320f),
        end = Offset(translateAnim, translateAnim)
    )
}

@Composable
fun NoteCardSkeleton(
    brush: Brush,
    bodyLines: Int = 2,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(XasuusTheme.colors.card)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(brush))
                Spacer(Modifier.width(6.dp))
                Box(
                    Modifier
                        .width(54.dp)
                        .height(9.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
            }
            Box(
                Modifier
                    .width(34.dp)
                    .height(9.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }

        Spacer(Modifier.height(14.dp))

        Box(
            Modifier
                .fillMaxWidth(0.88f)
                .height(15.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(brush)
        )
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .fillMaxWidth(0.6f)
                .height(15.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(brush)
        )

        Spacer(Modifier.height(12.dp))

        repeat(bodyLines.coerceIn(1, 3)) { index ->
            Box(
                Modifier
                    .fillMaxWidth(if (index == bodyLines - 1) 0.62f else 1f)
                    .height(9.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(Modifier.height(6.dp))
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                Modifier
                    .width(44.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Box(
                Modifier
                    .width(30.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}

@Composable
fun NotesSkeletonLayout(
    isGrid: Boolean,
    modifier: Modifier = Modifier
) {
    val brush = rememberShimmerBrush()
    val profiles = listOf(2, 3, 1, 3, 2, 1)

    if (isGrid) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp,
            modifier = modifier.testTag("notes_skeleton_grid")
        ) {
            items(profiles) { lines ->
                NoteCardSkeleton(brush = brush, bodyLines = lines)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier.testTag("notes_skeleton_list")
        ) {
            items(profiles) { lines ->
                NoteCardSkeleton(brush = brush, bodyLines = lines)
            }
        }
    }
}
