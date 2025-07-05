package com.mobitechs.slashapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mobitechs.slashapp.ui.screens.AppBottomNavigation
import com.mobitechs.slashapp.ui.screens.AuthOtpVerificationScreen
import com.mobitechs.slashapp.ui.screens.AuthPhoneScreen
import com.mobitechs.slashapp.ui.screens.AuthRegisterScreen
import com.mobitechs.slashapp.ui.screens.BottomMenuRewardScreen
import com.mobitechs.slashapp.ui.screens.BottomMenuScanScreen
import com.mobitechs.slashapp.ui.screens.BottomMenuStoreScreen
import com.mobitechs.slashapp.ui.screens.BottomMenuTransactionScreen
import com.mobitechs.slashapp.ui.screens.HomeScreen
import com.mobitechs.slashapp.ui.screens.SplashScreen
import com.mobitechs.slashapp.ui.theme.SlashTheme
import com.mobitechs.slashapp.ui.viewmodels.AuthOtpVerificationViewModel
import com.mobitechs.slashapp.ui.viewmodels.AuthPhoneViewModel
import com.mobitechs.slashapp.ui.viewmodels.AuthRegisterViewModel
import com.mobitechs.slashapp.ui.viewmodels.BottomMenuRewardViewModel
import com.mobitechs.slashapp.ui.viewmodels.BottomMenuScanViewModel
import com.mobitechs.slashapp.ui.viewmodels.BottomMenuStoreViewModel
import com.mobitechs.slashapp.ui.viewmodels.BottomMenuTransactionViewModel
import com.mobitechs.slashapp.ui.viewmodels.HomeViewModel
import com.mobitechs.slashapp.ui.viewmodels.SplashViewModel
import kotlin.collections.contains


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get repositories from application class
        val app = application as SlashApp


        // Create ViewModel factory
        val viewModelFactory = ViewModelFactory(
            applicationContext,
            app.authRepository,
        )

        setContent {
            SlashTheme {
                AppNavigation(viewModelFactory)
            }
        }
    }
}

@Composable
fun AppNavigation(viewModelFactory: ViewModelFactory) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Bottom navigation visible only on main screens
    val showBottomBar = currentRoute in listOf(
        Screen.HomeScreen.route,
        Screen.BottomMenuTransactionScreen.route,
        Screen.BottomMenuScanScreen.route,
        Screen.BottomMenuRewardScreen.route,
        Screen.BottomMenuStoreScreen.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.SplashScreen.route, //AuthRegisterScreen
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.SplashScreen.route) {
                val viewModel: SplashViewModel = viewModel(factory = viewModelFactory)
                SplashScreen(
                    viewModel = viewModel,
                    onNavigateToHome = {
                        navController.navigate(Screen.HomeScreen.route) {
                            popUpTo(Screen.SplashScreen.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.AuthPhoneScreen.route) {
                            popUpTo(Screen.SplashScreen.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.AuthPhoneScreen.route) {
                val viewModel: AuthPhoneViewModel = viewModel(factory = viewModelFactory)
                AuthPhoneScreen(
                    viewModel = viewModel,
                    navController = navController
//                    onNavigateToOtp = { phoneNumber ->
//                        navController.navigate(Screen.AuthOtpVerificationScreen.route+"/$phoneNumber")
//                    }
                )
            }


            composable(Screen.AuthOtpVerificationScreen.route+"/{phoneNumber}/{otp}/{otpExpiry}") { backStackEntry ->
                val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
                val otp = backStackEntry.arguments?.getString("otp") ?: ""
                val otpExpiry = backStackEntry.arguments?.getString("otpExpiry") ?: ""

                val viewModel: AuthOtpVerificationViewModel = viewModel(factory = viewModelFactory)
                AuthOtpVerificationScreen(
                    viewModel = viewModel,
                    phoneNumber = phoneNumber,
                    otp = otp,
                    otpExpiry = otpExpiry,
                    navController = navController,
                    onBackClick = {
                        navController.popBackStack()
                    },
                )
            }




            // Main screens
            composable(Screen.AuthRegisterScreen.route) {
                val viewModel: AuthRegisterViewModel = viewModel(factory = viewModelFactory)
                AuthRegisterScreen(
                    viewModel = viewModel,
                    navController = navController,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            // Main screens
            composable(Screen.HomeScreen.route) {
                val viewModel: HomeViewModel = viewModel(factory = viewModelFactory)
                HomeScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            composable(Screen.BottomMenuTransactionScreen.route) {
                val viewModel: BottomMenuTransactionViewModel = viewModel(factory = viewModelFactory)
                BottomMenuTransactionScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            composable(Screen.BottomMenuScanScreen.route) {
                val viewModel: BottomMenuScanViewModel = viewModel(factory = viewModelFactory)
                BottomMenuScanScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            composable(Screen.BottomMenuRewardScreen.route) {
                val viewModel: BottomMenuRewardViewModel = viewModel(factory = viewModelFactory)
                BottomMenuRewardScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            composable(Screen.BottomMenuStoreScreen.route) {
                val viewModel: BottomMenuStoreViewModel = viewModel(factory = viewModelFactory)
                BottomMenuStoreScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }




        }
    }
}



// Define navigation destinations
sealed class Screen(val route: String) {
    object SplashScreen : Screen("splashScreen")
    object AuthPhoneScreen : Screen("AuthPhoneScreen")
    object AuthOtpVerificationScreen : Screen("AuthOtpVerificationScreen")
    object AuthRegisterScreen : Screen("AuthRegisterScreen")
    object HomeScreen : Screen("HomeScreen")
    object BottomMenuTransactionScreen : Screen("BottomMenuTransactionScreen")
    object BottomMenuScanScreen : Screen("BottomMenuScanScreen")
    object BottomMenuRewardScreen : Screen("BottomMenuRewardScreen")
    object BottomMenuStoreScreen : Screen("BottomMenuStoreScreen")



}



//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            SlashTheme {
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    SlashApp()
//                }
//            }
//        }
//    }
//}

//
//@Composable
//fun SlashApp() {
//    val navController = rememberNavController()
//
//    NavHost(
//        navController = navController,
//        startDestination = "phone_input"
//    ) {
//        composable("phone_input") {
//            PhoneInputScreen(
//                onNavigateToOtp = { phoneNumber ->
//                    navController.navigate("otp_verification/$phoneNumber")
//                }
//            )
//        }
//
//        composable("otp_verification/{phoneNumber}") { backStackEntry ->
//            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
//            OtpVerificationScreen(
//                phoneNumber = phoneNumber,
//                onBackClick = {
//                    navController.popBackStack()
//                },
//                onVerificationSuccess = {
//                    // Navigate to next screen (signup or home)
//                    // navController.navigate("signup")
//                }
//            )
//        }
//    }
//}


