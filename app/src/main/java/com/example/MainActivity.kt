package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.DetailBottomSheet
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.ConnectScreen
import com.example.ui.screens.IrrigationScreen
import com.example.ui.screens.NutrientsScreen
import com.example.ui.screens.OverviewScreen
import com.example.ui.screens.PestDiseaseScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.YieldForecastScreen
import com.example.ui.theme.ActionGreen
import com.example.ui.theme.ForestDark
import com.example.ui.theme.SmartAgriTheme
import com.example.ui.theme.StatusCrit
import com.example.ui.theme.StatusOk
import com.example.ui.theme.StatusWarn
import com.example.ui.viewmodel.AgriViewModel
import com.example.util.configureAppCheck

data class BottomNavItem(
  val route: String,
  val label: String,
  val icon: ImageVector,
  val badgeCount: Int = 0
)

class MainActivity : ComponentActivity() {

  private val viewModel: AgriViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Mandated: Configure App Check before setContent, ViewModels or Firebase operations
    configureAppCheck(this, intent)

    enableEdgeToEdge()

    setContent {
      val uiState by viewModel.uiState.collectAsState()

      SmartAgriTheme(themeMode = uiState.themeMode) {
        MainAppContent(viewModel = viewModel)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: AgriViewModel) {
  val navController = rememberNavController()
  val uiState by viewModel.uiState.collectAsState()
  val alerts by viewModel.alerts.collectAsState()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route ?: "overview"

  val unreadAlertsCount = alerts.count { !it.isRead }

  val navItems = listOf(
    BottomNavItem("overview", "Overview", Icons.Default.Agriculture),
    BottomNavItem("irrigation", "Irrigation", Icons.Default.WaterDrop),
    BottomNavItem("nutrients", "Nutrients", Icons.Default.Grass),
    BottomNavItem("pest_detection", "Pest AI", Icons.Default.BugReport),
    BottomNavItem("yield_forecast", "Yield AI", Icons.Default.Insights),
    BottomNavItem("alerts", "Alerts", Icons.Default.Notifications, badgeCount = unreadAlertsCount)
  )

  // Handle system back button to return to overview if on secondary screen
  if (currentRoute != "overview") {
    BackHandler {
      navController.navigate("overview") {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
      }
    }
  }

  val (statusColor, statusLabel) = when {
    uiState.isDemo -> StatusWarn to "Demo"
    uiState.isConnected -> StatusOk to "Live"
    else -> StatusCrit to "Offline"
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val isWideScreen = maxWidth >= 720.dp

    if (isWideScreen && currentRoute != "connect") {
      // DESKTOP & TABLET ADAPTIVE LAYOUT (Side Navigation Rail + Centered Content)
      Row(modifier = Modifier.fillMaxSize()) {
        NavigationRail(
          containerColor = ForestDark,
          contentColor = Color.White,
          header = {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(ActionGreen),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Eco,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(22.dp)
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "SmartAgri",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
              )
            }
          },
          modifier = Modifier.fillMaxHeight().testTag("navigation_rail")
        ) {
          Spacer(modifier = Modifier.height(8.dp))
          navItems.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationRailItem(
              selected = isSelected,
              onClick = {
                if (currentRoute != item.route) {
                  navController.navigate(item.route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                  }
                }
              },
              icon = {
                if (item.badgeCount > 0) {
                  BadgedBox(
                    badge = {
                      Badge(containerColor = StatusCrit, contentColor = Color.White) {
                        Text("${item.badgeCount}")
                      }
                    }
                  ) {
                    Icon(item.icon, contentDescription = item.label)
                  }
                } else {
                  Icon(item.icon, contentDescription = item.label)
                }
              },
              label = { Text(item.label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
              colors = NavigationRailItemDefaults.colors(
                selectedIconColor = ForestDark,
                selectedTextColor = Color.White,
                indicatorColor = ActionGreen,
                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                unselectedTextColor = Color.White.copy(alpha = 0.7f)
              ),
              modifier = Modifier.testTag("rail_item_${item.route}")
            )
          }

          Spacer(modifier = Modifier.weight(1f))

          // Footer: Status chip & Settings
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(Color.White.copy(alpha = 0.15f))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(statusColor))
              Spacer(modifier = Modifier.width(4.dp))
              Text(statusLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
          }

          IconButton(
            onClick = { navController.navigate("settings") },
            modifier = Modifier.padding(bottom = 16.dp).testTag("rail_settings_btn")
          ) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
          }
        }

        // Center Content Workspace
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
          contentAlignment = Alignment.TopCenter
        ) {
          Box(modifier = Modifier.fillMaxSize().widthIn(max = 1100.dp)) {
            AppNavHost(
              navController = navController,
              viewModel = viewModel,
              uiState = uiState,
              alerts = alerts
            )
          }
        }
      }
    } else {
      // MOBILE PHONE COMPACT LAYOUT (Top Bar + Bottom Navigation)
      Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
          if (currentRoute != "connect") {
            CenterAlignedTopAppBar(
              title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(26.dp)
                      .clip(RoundedCornerShape(6.dp))
                      .background(ActionGreen),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.Eco,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "SmartAgri",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                  )
                }
              },
              actions = {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(statusColor))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(statusLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                  }
                }

                IconButton(
                  onClick = { navController.navigate("settings") },
                  modifier = Modifier.testTag("top_settings_btn")
                ) {
                  Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
              },
              colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = ForestDark,
                titleContentColor = Color.White,
                actionIconContentColor = Color.White
              )
            )
          }
        },
        bottomBar = {
          if (currentRoute != "connect") {
            NavigationBar(
              containerColor = MaterialTheme.colorScheme.surface,
              contentColor = MaterialTheme.colorScheme.onSurface,
              tonalElevation = 6.dp,
              modifier = Modifier.testTag("bottom_nav_bar")
            ) {
              navItems.forEach { item ->
                val isSelected = currentRoute == item.route
                NavigationBarItem(
                  selected = isSelected,
                  onClick = {
                    if (currentRoute != item.route) {
                      navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                      }
                    }
                  },
                  icon = {
                    if (item.badgeCount > 0) {
                      BadgedBox(
                        badge = {
                          Badge(containerColor = StatusCrit, contentColor = Color.White) {
                            Text("${item.badgeCount}")
                          }
                        }
                      ) {
                        Icon(item.icon, contentDescription = item.label)
                      }
                    } else {
                      Icon(item.icon, contentDescription = item.label)
                    }
                  },
                  label = {
                    Text(
                      text = item.label,
                      fontSize = 11.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                  },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ForestDark,
                    selectedTextColor = ForestDark,
                    indicatorColor = ActionGreen.copy(alpha = 0.25f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                  ),
                  modifier = Modifier.testTag("nav_item_${item.route}")
                )
              }
            }
          }
        }
      ) { innerPadding ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
          AppNavHost(
            navController = navController,
            viewModel = viewModel,
            uiState = uiState,
            alerts = alerts
          )
        }
      }
    }

    // Detail Bottom Sheet Overlay when any KPI card or Flow Diagram node is tapped
    uiState.selectedDetailSheet?.let { kind ->
      DetailBottomSheet(
        kind = kind,
        status = uiState.status,
        historyPoints = viewModel.history5m.value,
        onDismiss = { viewModel.closeDetailSheet() },
        onUpdateConfig = { key, value -> viewModel.updateConfig(key, value) },
        onStartIrrigation = { sec -> viewModel.startTimedIrrigation(sec) },
        onStopIrrigation = { viewModel.stopIrrigation() },
        onResetFlow = { viewModel.resetFlow() },
        onToggleNutrients = { enabled -> viewModel.setNutrientMonitoring(enabled) }
      )
    }
  }
}

@Composable
fun AppNavHost(
  navController: androidx.navigation.NavHostController,
  viewModel: AgriViewModel,
  uiState: com.example.ui.viewmodel.SmartAgriUiState,
  alerts: List<com.example.data.model.AgriAlert>,
  modifier: Modifier = Modifier
) {
  NavHost(
    navController = navController,
    startDestination = "overview",
    modifier = modifier.fillMaxSize()
  ) {
    composable("overview") {
      OverviewScreen(
        viewModel = viewModel,
        uiState = uiState,
        alerts = alerts,
        onNavigateTo = { route -> navController.navigate(route) }
      )
    }
    composable("irrigation") {
      IrrigationScreen(
        viewModel = viewModel,
        uiState = uiState
      )
    }
    composable("nutrients") {
      NutrientsScreen(
        viewModel = viewModel,
        uiState = uiState
      )
    }
    composable("pest_detection") {
      PestDiseaseScreen(
        viewModel = viewModel
      )
    }
    composable("yield_forecast") {
      YieldForecastScreen(
        viewModel = viewModel
      )
    }
    composable("alerts") {
      AlertsScreen(
        viewModel = viewModel,
        alerts = alerts
      )
    }
    composable("settings") {
      SettingsScreen(
        viewModel = viewModel,
        uiState = uiState,
        onNavigateToConnect = { navController.navigate("connect") }
      )
    }
    composable("connect") {
      ConnectScreen(
        viewModel = viewModel,
        onConnected = { navController.navigate("overview") }
      )
    }
  }
}
