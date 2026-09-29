package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.config.TmdbConfig
import com.example.domain.model.MediaItem
import com.example.domain.model.MediaType
import com.example.ui.theme.CineBlack
import com.example.ui.theme.CineBorder
import com.example.ui.theme.CineCyan
import com.example.ui.theme.CineGold
import com.example.ui.theme.CineRed
import com.example.ui.theme.CineSurface
import com.example.ui.theme.CineSurfaceElevated
import com.example.ui.theme.CineTextMuted
import com.example.ui.theme.CineTextPrimary
import com.example.ui.theme.CineTextSecondary
import java.util.Locale

@Composable
fun MediaPosterCard(
    item: MediaItem,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CineSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, CineBorder.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("poster_card_${item.mediaType.name.lowercase()}_${item.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .background(CineSurfaceElevated)
            ) {
                if (item.posterUrl != null) {
                    AsyncImage(
                        model = item.posterUrl,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Movie,
                            contentDescription = null,
                            tint = CineTextMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = CineTextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Bottom gradient vignette over poster
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    CineBlack.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                // Top-left Media Type Badge (MOVIE / TV SHOW)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (item.mediaType == MediaType.MOVIE) CineRed.copy(alpha = 0.92f)
                            else CineCyan.copy(alpha = 0.92f)
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = item.mediaType.displayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.mediaType == MediaType.MOVIE) Color.White else CineBlack,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Top-right My List Bookmark Button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CineBlack.copy(alpha = 0.72f))
                        .testTag("fav_toggle_${item.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Check else Icons.Filled.Add,
                        contentDescription = stringResource(
                            if (isFavorite) R.string.action_in_my_list else R.string.action_add_my_list
                        ),
                        tint = if (isFavorite) CineGold else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Bottom-left Rating + Year Row
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CineBlack.copy(alpha = 0.75f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = CineGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (item.rating > 0.0) {
                                String.format(Locale.US, "%.1f", item.rating)
                            } else {
                                "NR"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = CineTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = item.releaseYear,
                        style = MaterialTheme.typography.labelSmall,
                        color = CineTextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Title Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = CineTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ShimmerPosterPlaceholder(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CineSurfaceElevated)
            .alpha(alpha)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .background(CineSurface)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CineBorder)
            )
        }
    }
}

@Composable
fun ApiNoticeAndErrorCard(
    message: String,
    onRetry: () -> Unit,
    onOpenSetupGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var apiKeyInput by remember { mutableStateOf("") }
    var validationFeedback by remember { mutableStateOf<String?>(null) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CineSurfaceElevated),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CineRed.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CineRed.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Key,
                        contentDescription = null,
                        tint = CineRed
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.api_key_required_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = CineTextPrimary
                    )
                    Text(
                        text = "Official TMDB v3 / v4 API Integration",
                        style = MaterialTheme.typography.labelSmall,
                        color = CineGold
                    )
                }
            }

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = CineTextSecondary
            )

            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = {
                    apiKeyInput = it
                    validationFeedback = null
                },
                label = { Text("Paste TMDB_API_KEY (v3 32-char hex or v4 Bearer Token)") },
                placeholder = { Text("e.g. 32-character TMDB v3 key or eyJ...") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CineRed,
                    unfocusedBorderColor = CineBorder,
                    focusedTextColor = CineTextPrimary,
                    unfocusedTextColor = CineTextPrimary,
                    focusedLabelColor = CineGold,
                    unfocusedLabelColor = CineTextSecondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tmdb_api_key_input")
            )

            validationFeedback?.let { feedback ->
                Text(
                    text = feedback,
                    style = MaterialTheme.typography.bodySmall,
                    color = CineGold
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val trimmed = apiKeyInput.trim()
                        if (trimmed.isNotEmpty()) {
                            if (TmdbConfig.isValidTmdbKeyFormat(trimmed)) {
                                TmdbConfig.saveRuntimeApiKey(context, trimmed)
                                validationFeedback = null
                                onRetry()
                            } else {
                                validationFeedback = "Please paste a valid 32-character hex TMDB v3 API key or a TMDB v4 JWT Read Access Token."
                            }
                        } else {
                            onRetry()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CineRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("retry_connection_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (apiKeyInput.isNotBlank()) "Save Key & Connect" else stringResource(R.string.action_retry)
                    )
                }

                OutlinedButton(
                    onClick = onOpenSetupGuide,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("open_api_setup_guide_button")
                ) {
                    Text(
                        text = "Setup Instructions",
                        color = CineTextPrimary
                    )
                }
            }
        }
    }
}
