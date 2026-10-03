package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ModelInferenceStatus
import com.example.ui.AppStrings
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange

@Composable
fun StatusBadge(
    status: ModelInferenceStatus,
    activeModelName: String?,
    activeBackend: String,
    isFa: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (dotColor, text, icon) = when (status) {
        ModelInferenceStatus.READY_OFFLINE -> Triple(
            SuccessGreen,
            "${AppStrings.offlineReadyBadge(isFa)} (${activeBackend.ifEmpty { "CPU" }})",
            Icons.Default.CheckCircle
        )
        ModelInferenceStatus.LOADING -> Triple(
            WarningOrange,
            AppStrings.loadingModel(isFa),
            Icons.Default.HourglassEmpty
        )
        ModelInferenceStatus.NO_MODEL_SELECTED -> Triple(
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            AppStrings.noModelLoaded(isFa),
            Icons.Default.Bolt
        )
        ModelInferenceStatus.ERROR -> Triple(
            ErrorRed,
            "Error loading model",
            Icons.Default.ErrorOutline
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(
                1.dp,
                dotColor.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp)
            )
            .testTag("model_status_badge")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            if (status == ModelInferenceStatus.LOADING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(10.dp),
                    strokeWidth = 2.dp,
                    color = WarningOrange
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            val modelLabel = if (!activeModelName.isNullOrEmpty()) "$activeModelName • " else ""
            Text(
                text = "$modelLabel$text",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
