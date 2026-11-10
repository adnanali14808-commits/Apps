package com.example.ui.dashboard

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.HiddenAppEntity
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun HiddenAppsView(
    hiddenApps: List<HiddenAppEntity>,
    searchQuery: String,
    getAppIcon: (String) -> Bitmap?,
    onLaunchApp: (HiddenAppEntity) -> Unit,
    onOpenConcealmentGuidance: (HiddenAppEntity) -> Unit,
    onUnhideApp: (HiddenAppEntity) -> Unit,
    onOpenAppPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredApps = remember(hiddenApps, searchQuery) {
        if (searchQuery.isBlank()) {
            hiddenApps
        } else {
            hiddenApps.filter {
                it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    if (hiddenApps.isEmpty()) {
        EmptyHiddenAppsState(onOpenAppPicker = onOpenAppPicker, modifier = modifier)
    } else if (filteredApps.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hidden apps match '$searchQuery'",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier
                .fillMaxSize()
                .testTag("hidden_apps_grid")
        ) {
            items(filteredApps, key = { it.packageName }) { app ->
                val icon = remember(app.packageName) { getAppIcon(app.packageName) }
                HiddenAppCard(
                    app = app,
                    iconBitmap = icon,
                    onLaunch = { onLaunchApp(app) },
                    onConcealment = { onOpenConcealmentGuidance(app) },
                    onUnhide = { onUnhideApp(app) }
                )
            }
        }
    }
}

@Composable
private fun HiddenAppCard(
    app: HiddenAppEntity,
    iconBitmap: Bitmap?,
    onLaunch: () -> Unit,
    onConcealment: () -> Unit,
    onUnhide: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, ObsidianBorder, RoundedCornerShape(16.dp))
            .clickable { onLaunch() }
            .testTag("hidden_app_card_${app.packageName}"),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Row with status badge and menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Concealment Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (app.isConcealedFromLauncher)
                                NeonEmerald.copy(alpha = 0.15f)
                            else
                                ElectricAmber.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (app.isConcealedFromLauncher) "CONCEALED" else "VISIBLE IN OS",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (app.isConcealedFromLauncher) NeonEmerald else ElectricAmber
                    )
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("app_menu_${app.packageName}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier
                            .background(ObsidianSurfaceVariant)
                            .border(1.dp, ObsidianBorder)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Launch App", color = TextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NeonEmerald)
                            },
                            onClick = {
                                menuExpanded = false
                                onLaunch()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Concealment Info", color = TextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = CyberCyan)
                            },
                            onClick = {
                                menuExpanded = false
                                onConcealment()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Unhide from Vault", color = CrimsonAlert) },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = CrimsonAlert)
                            },
                            onClick = {
                                menuExpanded = false
                                onUnhide()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // App Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianSurfaceVariant)
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (iconBitmap != null) {
                    Image(
                        bitmap = iconBitmap.asImageBitmap(),
                        contentDescription = app.appName,
                        modifier = Modifier.size(46.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(CyberCyan.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.appName.take(1).uppercase(),
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // App Name
            Text(
                text = app.appName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Launches Count
            Text(
                text = "${app.launchCount} launches",
                fontSize = 11.sp,
                color = TextTertiary
            )
        }
    }
}

@Composable
private fun EmptyHiddenAppsState(
    onOpenAppPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(ObsidianSurface)
                .border(2.dp, NeonEmerald.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = NeonEmerald,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "No Hidden Apps Yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tap the plus (+) button or below to import installed applications into your private vault.",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onOpenAppPicker,
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonEmerald,
                contentColor = ObsidianBlack
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("empty_state_add_app_button")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Application", fontWeight = FontWeight.Bold)
        }
    }
}
