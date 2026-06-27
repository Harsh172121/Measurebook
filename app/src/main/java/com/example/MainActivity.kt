package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

object Screen {
    const val Dashboard = "dashboard"
    const val CustomerList = "customer_list"
    const val CustomerAddEdit = "customer_add_edit?customerId={customerId}"
    const val CustomerDetail = "customer_detail/{customerId}"
    const val MeasurementAddEdit = "measurement_add_edit/{customerId}?measurementId={measurementId}"
    const val Settings = "settings"
    
    fun customerAdd() = "customer_add_edit?customerId="
    fun customerEdit(id: String) = "customer_add_edit?customerId=$id"
    fun customerDetail(id: String) = "customer_detail/$id"
    fun measurementAdd(customerId: String) = "measurement_add_edit/$customerId?measurementId="
    fun measurementEdit(customerId: String, mId: String) = "measurement_add_edit/$customerId?measurementId=$mId"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val tailorViewModel: TailorViewModel = viewModel()
            val theme by tailorViewModel.theme.collectAsState()

            MyApplicationTheme(theme = theme) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Navigation setup
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Dashboard
                    ) {
                        // 1. Dashboard Screen
                        composable(Screen.Dashboard) {
                            DashboardScreen(
                                viewModel = tailorViewModel,
                                onNavigateToCustomerList = {
                                    navController.navigate(Screen.CustomerList)
                                },
                                onNavigateToCustomerAdd = {
                                    navController.navigate(Screen.customerAdd())
                                },
                                onNavigateToCustomerDetail = { id ->
                                    navController.navigate(Screen.customerDetail(id))
                                },
                                onNavigateToSettings = {
                                    navController.navigate(Screen.Settings)
                                }
                            )
                        }

                        // 2. Customer List Screen
                        composable(Screen.CustomerList) {
                            CustomerListScreen(
                                viewModel = tailorViewModel,
                                onNavigateToCustomerDetail = { id ->
                                    navController.navigate(Screen.customerDetail(id))
                                },
                                onNavigateToCustomerAdd = {
                                    navController.navigate(Screen.customerAdd())
                                },
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 3. Customer Add / Edit Screen
                        composable(
                            route = Screen.CustomerAddEdit,
                            arguments = listOf(navArgument("customerId") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            })
                        ) { backStackEntry ->
                            val customerId = backStackEntry.arguments?.getString("customerId")
                            CustomerAddEditScreen(
                                viewModel = tailorViewModel,
                                customerId = customerId,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onSuccess = { id ->
                                    // Pop add_edit and replace or open detail
                                    navController.popBackStack()
                                    if (customerId == null) {
                                        navController.navigate(Screen.customerDetail(id))
                                    }
                                }
                            )
                        }

                        // 4. Customer Detail Screen
                        composable(
                            route = Screen.CustomerDetail,
                            arguments = listOf(navArgument("customerId") {
                                type = NavType.StringType
                            })
                        ) { backStackEntry ->
                            val customerId = backStackEntry.arguments?.getString("customerId") ?: ""
                            CustomerDetailScreen(
                                viewModel = tailorViewModel,
                                customerId = customerId,
                                onNavigateToEditCustomer = { id ->
                                    navController.navigate(Screen.customerEdit(id))
                                },
                                onNavigateToAddMeasurement = { cId ->
                                    navController.navigate(Screen.measurementAdd(cId))
                                },
                                onNavigateToEditMeasurement = { cId, mId ->
                                    navController.navigate(Screen.measurementEdit(cId, mId))
                                },
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 5. Measurement Add / Edit Screen
                        composable(
                            route = Screen.MeasurementAddEdit,
                            arguments = listOf(
                                navArgument("customerId") { type = NavType.StringType },
                                navArgument("measurementId") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val customerId = backStackEntry.arguments?.getString("customerId") ?: ""
                            val measurementId = backStackEntry.arguments?.getString("measurementId")
                            MeasurementAddEditScreen(
                                viewModel = tailorViewModel,
                                customerId = customerId,
                                measurementId = measurementId,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onSuccess = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 6. Settings Screen
                        composable(Screen.Settings) {
                            SettingsScreen(
                                viewModel = tailorViewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
