package com.kiwi.finanzas

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kiwi.finanzas.db.DataBase
import com.kiwi.finanzas.ui.theme.FinanzasTheme
import com.kiwi.finanzas.ui.views.Home

class Main : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanzasTheme {
                Greeting(this)
            }
        }
    }
}

@Composable
fun Greeting(context: Context) {
    val database = DataBase.getDatabase(context)
    val daoEntradas = database.entryDao()
    val daoTipos = database.typeDao()
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { Home(navController, daoEntradas, daoTipos) }
        /*composable("details/{itemId}") { backStackEntry ->
            DetailsScreen(backStackEntry.arguments?.getString("itemId"), dao)
        }*/
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FinanzasTheme {
        //Greeting("Android")
    }
}