package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.ActiveBanner
import com.example.data.model.AlertLevel
import com.example.ui.theme.ActionGreen
import com.example.ui.theme.StatusCrit
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusOk
import com.example.ui.theme.StatusWarn

@Composable
fun BannerAlert(
  banner: ActiveBanner,
  onActionClick: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val (accentColor, bgColor) = when (banner.level) {
    AlertLevel.CRITICAL -> StatusCrit to StatusCrit.copy(alpha = 0.12f)
    AlertLevel.WARNING -> StatusWarn to StatusWarn.copy(alpha = 0.12f)
    AlertLevel.INFO -> StatusInfo to StatusInfo.copy(alpha = 0.12f)
  }

  val icon = when (banner.level) {
    AlertLevel.CRITICAL -> if (banner.iconName.contains("wifi")) Icons.Default.WifiOff else Icons.Default.Error
    AlertLevel.WARNING -> Icons.Default.Warning
    AlertLevel.INFO -> if (banner.iconName.contains("droplet")) Icons.Default.InvertColors else Icons.Default.CheckCircle
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(bgColor)
      .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
      .testTag("status_banner")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .width(4.dp)
          .height(44.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(accentColor)
      )
      Spacer(modifier = Modifier.width(12.dp))
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = accentColor,
        modifier = Modifier.size(26.dp)
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = banner.title,
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = banner.description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      if (banner.primaryAction != null && banner.primaryActionLabel != null) {
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = { onActionClick(banner.primaryAction) },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (banner.primaryAction == "stop") StatusCrit else accentColor
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("banner_action_button")
        ) {
          Text(
            text = banner.primaryActionLabel,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
          )
        }
      }
    }
  }
}
