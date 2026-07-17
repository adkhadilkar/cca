package com.architectprep.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.architectprep.app.PrepApplication
import com.architectprep.app.ui.home.HomeScreen
import com.architectprep.app.ui.home.HomeViewModel
import com.architectprep.app.ui.theme.ArchitectPrepTheme
import com.architectprep.app.ui.theme.LocalAppColors

private enum class Tab(val route: String, val label: String) {
    Home("home", "Home"),
    Study("study", "Study"),
    Practice("practice", "Practice"),
    Exam("exam", "Exam"),
    Progress("progress", "Progress")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as PrepApplication
        setContent {
            ArchitectPrepTheme {
                AppScaffold(app)
            }
        }
    }
}

@Composable
private fun AppScaffold(app: PrepApplication) {
    val navController = rememberNavController()
    val colors = LocalAppColors.current

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            // Flat, single-level graph (5 sibling tabs, no nested graphs), so a
            // direct route match is sufficient — no need for hierarchy walking.
            val currentRoute = backStackEntry?.destination?.route

            NavigationBar(containerColor = colors.surface) {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(Tab.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(iconFor(tab), contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Tab.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Tab.Home.route) {
                val vm: HomeViewModel = viewModel(factory = HomeViewModel.Factory(app))
                HomeScreen(vm)
            }
            composable(Tab.Study.route) { PlaceholderScreen("Study — lessons by domain (M1)") }
            composable(Tab.Practice.route) { PlaceholderScreen("Practice — question bank (M2)") }
            composable(Tab.Exam.route) { PlaceholderScreen("Mock exam — timed, 60Q (M3)") }
            composable(Tab.Progress.route) { PlaceholderScreen("Progress — score history, streak (M4)") }
        }
    }
}

private fun iconFor(tab: Tab) = when (tab) {
    Tab.Home -> Icons.Filled.Home
    Tab.Study -> Icons.Filled.Edit
    Tab.Practice -> Icons.Filled.CheckCircle
    Tab.Exam -> Icons.Filled.DateRange
    Tab.Progress -> Icons.Filled.Star
}

@Composable
private fun PlaceholderScreen(label: String) {
    val colors = LocalAppColors.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = colors.textSecondary)
    }
}
