package com.kiwi.finanzas

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kiwi.finanzas.db.DataBase
import com.kiwi.finanzas.ui.theme.FinanzasTheme
import com.kiwi.finanzas.ui.views.Historico
import com.kiwi.finanzas.ui.views.Home
import com.kiwi.finanzas.ui.views.Settings

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
        composable("home") { Home(navController, daoEntradas, daoTipos, context) }
        //composable("historico") { Historico(daoEntradas, daoTipos, context) }
        composable("settings") { Settings(daoTipos, context) }
        composable("historico/{anno}/{mes}/{dia}") { backStackEntry ->
            val anno = backStackEntry.arguments?.getString("anno")?.toIntOrNull()
            val mes = backStackEntry.arguments?.getString("mes")?.toIntOrNull()
            val dia = backStackEntry.arguments?.getString("dia")?.toIntOrNull()
            Historico(navController, anno, mes, dia, daoEntradas, daoTipos, context)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FinanzasTheme {
        //Greeting("Android")
    }
}