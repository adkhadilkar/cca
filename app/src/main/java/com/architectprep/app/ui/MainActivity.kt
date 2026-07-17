package com.architectprep.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.architectprep.app.PrepApplication
import com.architectprep.app.data.prefs.ThemePref
import com.architectprep.app.ui.exam.ExamHomeScreen
import com.architectprep.app.ui.exam.ExamHomeViewModel
import com.architectprep.app.ui.exam.ExamSessionScreen
import com.architectprep.app.ui.exam.ExamSessionViewModel
import com.architectprep.app.ui.exam.ResultsScreen
import com.architectprep.app.ui.exam.ResultsViewModel
import com.architectprep.app.ui.flashcards.FlashcardsScreen
import com.architectprep.app.ui.flashcards.FlashcardsViewModel
import com.architectprep.app.ui.home.HomeScreen
import com.architectprep.app.ui.home.HomeViewModel
import com.architectprep.app.ui.onboarding.OnboardingScreen
import com.architectprep.app.ui.onboarding.OnboardingViewModel
import com.architectprep.app.ui.practice.PracticeHomeScreen
import com.architectprep.app.ui.practice.PracticeHomeViewModel
import com.architectprep.app.ui.practice.PracticeSessionScreen
import com.architectprep.app.ui.practice.PracticeSessionViewModel
import com.architectprep.app.ui.progress.ProgressScreen
import com.architectprep.app.ui.progress.ProgressViewModel
import com.architectprep.app.ui.reference.ExamGuideScreen
import com.architectprep.app.ui.reference.ExamGuideViewModel
import com.architectprep.app.ui.reference.GlossaryScreen
import com.architectprep.app.ui.reference.GlossaryViewModel
import com.architectprep.app.ui.settings.SettingsScreen
import com.architectprep.app.ui.settings.SettingsViewModel
import com.architectprep.app.ui.study.DomainListScreen
import com.architectprep.app.ui.study.DomainListViewModel
import com.architectprep.app.ui.study.LessonDetailScreen
import com.architectprep.app.ui.study.LessonDetailViewModel
import com.architectprep.app.ui.study.LessonListScreen
import com.architectprep.app.ui.study.LessonListViewModel
import com.architectprep.app.ui.theme.ArchitectPrepTheme
import com.architectprep.app.ui.theme.LocalAppColors

private enum class Tab(val route: String, val label: String, val glyph: String) {
    Home("home", "Home", "⌂"),
    Study("study", "Study", "▤"),
    Practice("practice", "Practice", "✎"),
    Exam("exam", "Exam", "◷"),
    Progress("progress", "Progress", "◔")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as PrepApplication
        setContent {
            val prefs by app.userPrefsRepository.prefs.collectAsState(initial = null)
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (prefs?.theme) {
                ThemePref.LIGHT -> false
                ThemePref.DARK -> true
                else -> systemDark
            }
            ArchitectPrepTheme(darkTheme = darkTheme) {
                val p = prefs
                if (p == null) {
                    Box(modifier = Modifier.fillMaxSize().background(LocalAppColors.current.background)) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                } else if (!p.onboarded) {
                    val vm: OnboardingViewModel = viewModel(factory = OnboardingViewModel.Factory(app))
                    OnboardingScreen(vm, onDone = {})
                } else {
                    AppScaffold(app)
                }
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
            // Study now owns a small sub-graph (domain list -> lesson list -> lesson
            // detail -> guide/glossary), all under the "study" route prefix, so the
            // tab bar highlights by prefix rather than an exact route match.
            val currentRoute = backStackEntry?.destination?.route

            Box(modifier = Modifier.fillMaxWidth().background(colors.border).padding(top = 1.dp)) {
                NavigationBar(containerColor = colors.background, tonalElevation = 0.dp) {
                    Tab.entries.forEach { tab ->
                        val selected = currentRoute != null && (currentRoute == tab.route || currentRoute.startsWith("${tab.route}/"))
                        val tint = if (selected) colors.accent else colors.textTertiary
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(Tab.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Text(text = tab.glyph, fontSize = 18.sp, color = tint) },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = tint
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = androidx.compose.ui.graphics.Color.Transparent
                            )
                        )
                    }
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
                HomeScreen(
                    viewModel = vm,
                    onOpenStudy = {
                        navController.navigate(Tab.Study.route) {
                            popUpTo(Tab.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onContinueLesson = { lessonId -> navController.navigate("study/lesson/$lessonId") }
                )
            }
            composable(Tab.Study.route) {
                val vm: DomainListViewModel = viewModel(factory = DomainListViewModel.Factory(app))
                DomainListScreen(
                    viewModel = vm,
                    onDomainClick = { domainId -> navController.navigate("study/domain/$domainId") },
                    onGuideClick = { navController.navigate("study/guide") },
                    onGlossaryClick = { navController.navigate("study/glossary") }
                )
            }
            composable(
                route = "study/domain/{domainId}",
                arguments = listOf(navArgument("domainId") { type = NavType.StringType })
            ) { backStack ->
                val domainId = backStack.arguments?.getString("domainId") ?: return@composable
                val vm: LessonListViewModel = viewModel(
                    key = "lessons-$domainId",
                    factory = LessonListViewModel.Factory(app, domainId)
                )
                LessonListScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onLessonClick = { lessonId -> navController.navigate("study/lesson/$lessonId") }
                )
            }
            composable(
                route = "study/lesson/{lessonId}",
                arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
            ) { backStack ->
                val lessonId = backStack.arguments?.getString("lessonId") ?: return@composable
                val vm: LessonDetailViewModel = viewModel(
                    key = "lesson-$lessonId",
                    factory = LessonDetailViewModel.Factory(app, lessonId)
                )
                LessonDetailScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onNavigateToLesson = { nextId ->
                        navController.navigate("study/lesson/$nextId") { launchSingleTop = true }
                    }
                )
            }
            composable("study/guide") {
                val vm: ExamGuideViewModel = viewModel(factory = ExamGuideViewModel.Factory(app))
                ExamGuideScreen(vm, onBack = { navController.popBackStack() })
            }
            composable("study/glossary") {
                val vm: GlossaryViewModel = viewModel(factory = GlossaryViewModel.Factory(app))
                GlossaryScreen(vm, onBack = { navController.popBackStack() })
            }
            composable(Tab.Practice.route) {
                val vm: PracticeHomeViewModel = viewModel(factory = PracticeHomeViewModel.Factory(app))
                PracticeHomeScreen(
                    viewModel = vm,
                    onDomainClick = { domainId -> navController.navigate("practice/domain/$domainId") },
                    onFlashcardsClick = { navController.navigate("practice/flashcards") }
                )
            }
            composable(
                route = "practice/domain/{domainId}",
                arguments = listOf(navArgument("domainId") { type = NavType.StringType })
            ) { backStack ->
                val domainId = backStack.arguments?.getString("domainId") ?: return@composable
                val vm: PracticeSessionViewModel = viewModel(
                    key = "practice-$domainId",
                    factory = PracticeSessionViewModel.Factory(app, domainId)
                )
                PracticeSessionScreen(viewModel = vm, onExit = { navController.popBackStack() })
            }
            composable("practice/flashcards") {
                val vm: FlashcardsViewModel = viewModel(factory = FlashcardsViewModel.Factory(app))
                FlashcardsScreen(viewModel = vm, onExit = { navController.popBackStack() })
            }
            composable(Tab.Exam.route) {
                val vm: ExamHomeViewModel = viewModel(factory = ExamHomeViewModel.Factory(app))
                ExamHomeScreen(
                    viewModel = vm,
                    onStart = { attemptId -> navController.navigate("exam/session/$attemptId") },
                    onResume = { attemptId -> navController.navigate("exam/session/$attemptId") },
                    onViewResults = { attemptId -> navController.navigate("exam/results/$attemptId") }
                )
            }
            composable(
                route = "exam/session/{attemptId}",
                arguments = listOf(navArgument("attemptId") { type = NavType.StringType })
            ) { backStack ->
                val attemptId = backStack.arguments?.getString("attemptId") ?: return@composable
                val vm: ExamSessionViewModel = viewModel(
                    key = "exam-$attemptId",
                    factory = ExamSessionViewModel.Factory(app, attemptId)
                )
                ExamSessionScreen(
                    viewModel = vm,
                    onSubmitted = {
                        navController.navigate("exam/results/$attemptId") {
                            popUpTo("exam/session/$attemptId") { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = "exam/results/{attemptId}",
                arguments = listOf(navArgument("attemptId") { type = NavType.StringType })
            ) { backStack ->
                val attemptId = backStack.arguments?.getString("attemptId") ?: return@composable
                val vm: ResultsViewModel = viewModel(
                    key = "results-$attemptId",
                    factory = ResultsViewModel.Factory(app, attemptId)
                )
                ResultsScreen(viewModel = vm, onDone = { navController.popBackStack(Tab.Exam.route, inclusive = false) })
            }
            composable(Tab.Progress.route) {
                val vm: ProgressViewModel = viewModel(factory = ProgressViewModel.Factory(app))
                Box(modifier = Modifier.fillMaxSize()) {
                    ProgressScreen(vm)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp, 18.dp)
                            .size(44.dp)
                            .background(colors.surface, androidx.compose.foundation.shape.CircleShape)
                            .border(1.dp, colors.border, androidx.compose.foundation.shape.CircleShape)
                            .clickable { navController.navigate("settings") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "⚙", fontSize = 22.sp, color = colors.textPrimary)
                    }
                }
            }
            composable("settings") {
                val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(app))
                SettingsScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onRedoOnboarding = { vm.redoOnboarding() }
                )
            }
        }
    }
}
