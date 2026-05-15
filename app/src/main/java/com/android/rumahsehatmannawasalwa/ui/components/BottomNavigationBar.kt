package com.android.rumahsehatmannawasalwa.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
// ... (icons remain same, omitted for brevity in replace call if possible, but better show full)
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.android.rumahsehatmannawasalwa.ui.theme.*

@Composable
fun BottomNavigationBar(
    navController: NavController,
    role: String
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(50.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.2.dp, GreenPrimary)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val items = when (role.lowercase()) {
                    "patient", "pasien" -> listOf(
                        Triple("Home", "patient_home", Icons.Default.Home),
                        Triple("Janji Temu", "patient_appointment", Icons.Default.DateRange),
                        Triple("Riwayat", "patient_record", Icons.Default.History),
                        Triple("Profil", "patient_profile", Icons.Default.Person)
                    )
                    "therapist", "terapis" -> listOf(
                        Triple("Home", "therapist_home", Icons.Default.Home),
                        Triple("Janji Temu", "therapist_appointment", Icons.Default.DateRange),
                        Triple("Riwayat", "therapist_patient_list", Icons.Default.History),
                        Triple("Profil", "therapist_profile", Icons.Default.Person)
                    )
                    "admin" -> listOf(
                        Triple("Home", "admin_home", Icons.Default.Home),
                        Triple("Janji Temu", "admin_appointment", Icons.Default.DateRange),
                        Triple("Riwayat", "admin_patient_list", Icons.Default.History),
                        Triple("Profil", "patient_profile", Icons.Default.Person)
                    )
                    else -> emptyList()
                }
                items.forEach { (label, route, icon) ->
                    SultanNavigationItem(
                        label = label,
                        icon = icon,
                        selected = currentRoute == route,
                        onClick = {
                            if (currentRoute != route) {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SultanNavigationItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val contentColor by animateColorAsState(
        targetValue = if (selected) GreenPrimary else BodyGray,
        animationSpec = tween(300)
    )

    Box(
        modifier = Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(if (selected) GreenPrimary.copy(alpha = 0.1f) else Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = null, 
                onClick = onClick
            )
            .padding(horizontal = if (selected) 14.dp else 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Text(
                    text = label,
                    color = GreenPrimary,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}
