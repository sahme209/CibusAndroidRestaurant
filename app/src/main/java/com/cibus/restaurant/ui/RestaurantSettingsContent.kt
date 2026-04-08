package com.cibus.restaurant.ui

// Settings content — light theme, matching iOS SettingsView.

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cibus.restaurant.api.RetrofitClient
import com.cibus.restaurant.ui.theme.CibusDimens
import com.cibus.restaurant.ui.theme.CibusGreen
import com.cibus.restaurant.ui.theme.CibusRed

/** Settings content — light theme. */
@Composable
fun RestaurantMoreContent(onLogout: () -> Unit = {}) {
    var restaurantName by remember { mutableStateOf("") }
    var partnerName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var soundOnNewOrder by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val resp = RetrofitClient.restaurantApi.getMe()
            if (resp.isSuccessful) {
                restaurantName = resp.body()?.restaurantName ?: ""
                partnerName = resp.body()?.partnerName ?: ""
                email = resp.body()?.email ?: ""
            }
        } catch (_: Exception) {}
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = CibusDimens.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        // Notifications card
        item {
            SettingsCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsSectionHeader(
                        icon = Icons.Default.Notifications,
                        iconColor = Color(0xFFF59E0B),
                        title = "Notifications"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "New Order Alerts",
                                fontSize = CibusDimens.bodySp,
                                color = RestTextPrimary,
                            )
                            Text(
                                "Sound & vibration for incoming orders",
                                fontSize = CibusDimens.labelSp,
                                color = RestTextTertiary,
                            )
                        }
                        Switch(
                            checked = soundOnNewOrder,
                            onCheckedChange = { soundOnNewOrder = it },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = CibusGreen,
                                checkedThumbColor = Color.White,
                            )
                        )
                    }
                }
            }
        }

        // Language card
        item {
            SettingsCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsSectionHeader(
                        icon = Icons.Default.Language,
                        iconColor = Color(0xFF3B82F6),
                        title = "Language"
                    )
                    Text(
                        "Choose your preferred language",
                        fontSize = CibusDimens.captionSp,
                        color = RestTextSecondary,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SettingsLanguageChip(label = "English", selected = true, onClick = {})
                        SettingsLanguageChip(label = "Roman Urdu", selected = false, onClick = {})
                    }
                }
            }
        }

        // Action rows card
        item {
            SettingsCard {
                Column {
                    SettingsActionRow(
                        icon = Icons.Default.HelpOutline,
                        iconColor = CibusGreen,
                        title = "Help & Support",
                        onClick = {}
                    )
                    HorizontalDivider(color = RestDivider, modifier = Modifier.padding(start = 48.dp))
                    SettingsActionRow(
                        icon = Icons.Default.Message,
                        iconColor = Color(0xFF25D366),
                        title = "Chat on WhatsApp",
                        onClick = {}
                    )
                }
            }
        }

        // Version
        item {
            Text(
                "HUBB Merchant v1.0",
                fontSize = CibusDimens.captionSp,
                color = RestTextTertiary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
            )
        }

        // Sign out
        item {
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(CibusDimens.radiusLg),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CibusRed.copy(alpha = 0.08f),
                    contentColor = CibusRed,
                ),
                border = BorderStroke(1.dp, CibusRed.copy(alpha = 0.2f)),
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Log out", fontWeight = FontWeight.Medium)
            }
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(CibusDimens.radiusLg),
        color = RestCardBG,
        border = BorderStroke(1.dp, RestDivider),
    ) {
        Column(Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsSectionHeader(icon: ImageVector, iconColor: Color, title: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        Text(
            title,
            fontSize = CibusDimens.sectionTitleSp,
            fontWeight = FontWeight.SemiBold,
            color = RestTextPrimary,
        )
    }
}

@Composable
private fun SettingsLanguageChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = if (selected) CibusGreen else RestBackground,
        border = BorderStroke(1.dp, if (selected) CibusGreen else RestDivider),
    ) {
        Text(
            label,
            fontSize = CibusDimens.captionSp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else RestTextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun SettingsActionRow(icon: ImageVector, iconColor: Color, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 0.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        Text(
            title,
            fontSize = CibusDimens.bodySp,
            fontWeight = FontWeight.Medium,
            color = RestTextPrimary,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = RestTextTertiary,
            modifier = Modifier.size(18.dp),
        )
    }
}
