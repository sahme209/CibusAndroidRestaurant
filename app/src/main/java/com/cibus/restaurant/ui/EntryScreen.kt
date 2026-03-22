package com.cibus.restaurant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cibus.restaurant.ResL10n
import com.cibus.restaurant.ui.theme.CibusDimens
import com.cibus.restaurant.ui.theme.CibusGreen

/**
 * Premium entry screen shown on app launch when user is not logged in.
 * Uber Eats–level design: hero, clear CTAs, no demo feel.
 */
@Composable
fun EntryScreen(
    onGetStarted: () -> Unit,
    onSignIn: () -> Unit,
) {
    val ctx = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAF9),
                        Color(0xFFF0F7F4),
                        Color.White,
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY,
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(CibusDimens.spacing24)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(Modifier.height(24.dp))

            // Hero content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f, fill = false),
            ) {
                Icon(
                    Icons.Default.Storefront,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = CibusGreen,
                )
                Spacer(Modifier.height(CibusDimens.spacing24))
                Text(
                    "Become a Cibus Partner",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(CibusDimens.spacing12))
                Text(
                    ResL10n.entryTagline(ctx),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(CibusDimens.spacing32))

                // Benefit bullets
                BenefitRow(
                    icon = Icons.Default.Schedule,
                    text = "Flexible hours – open when you want",
                )
                Spacer(Modifier.height(CibusDimens.spacing12))
                BenefitRow(
                    icon = Icons.Default.Payments,
                    text = "Secure payouts, transparent fees",
                )
                Spacer(Modifier.height(CibusDimens.spacing12))
                BenefitRow(
                    icon = Icons.Default.TrendingUp,
                    text = "Reach more customers in your area",
                )
            }

            // CTAs
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CibusDimens.spacing16),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = onGetStarted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CibusGreen),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                ) {
                    Text(
                        ResL10n.entryGetStarted(ctx),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
                TextButton(
                    onClick = onSignIn,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        ResL10n.entrySignInLink(ctx),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BenefitRow(
    icon: ImageVector,
    text: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = CibusGreen,
        )
        Spacer(Modifier.width(CibusDimens.spacing12))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
