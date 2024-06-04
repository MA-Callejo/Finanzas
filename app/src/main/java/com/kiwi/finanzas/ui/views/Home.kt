package com.kiwi.finanzas.ui.views

import android.widget.ImageButton
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.kiwi.finanzas.R
import com.kiwi.finanzas.db.Entrada
import com.kiwi.finanzas.db.EntradaDAO
import com.kiwi.finanzas.db.TipoDAO
import androidx.compose.foundation.background
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@Composable
fun Home(navController: NavHostController, daoEntradas: EntradaDAO, daoTipos: TipoDAO) {
    val tipos by daoTipos.getAll().collectAsState(initial = emptyList())
    var text by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("Tipo") }
    var tipoColor by remember { mutableStateOf(Color.Gray) }
    var tipoId by remember { mutableIntStateOf(0) }
    var expanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    OutlinedCard (modifier = Modifier.padding(20.dp, 50.dp, 20.dp, 20.dp)){
        Column(modifier = Modifier.padding(5.dp),) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = text,
                onValueChange = {
                    text = it
                },
                placeholder = {
                    Text(
                        text = "Concepto",
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                shape = RoundedCornerShape(16.dp),
                maxLines = 1,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
            )
            Row(modifier = Modifier.padding(0.dp, 5.dp)){
                DropdownMenu(expanded = expanded,
                    onDismissRequest = { expanded = false }) {
                    tipos.forEach{
                        DropdownMenuItem(
                            text = { Text(it.nombre) },
                            onClick = {
                                expanded = false
                                tipo = it.nombre
                                tipoId = it.id
                                tipoColor = Color(android.graphics.Color.parseColor(it.color))
                            },
                            modifier = Modifier.background(Color(android.graphics.Color.parseColor(it.color))))
                    }
                }
                OutlinedButton(modifier = Modifier.weight(1F), colors = ButtonDefaults.buttonColors(containerColor = tipoColor), onClick = { expanded = true }) {
                    Text(text = tipo)
                }
                Spacer(modifier = Modifier.width(5.dp))
                OutlinedTextField(
                    modifier = Modifier.weight(0.6F),
                    value = amount,
                    onValueChange = {
                                    amount = it
                    },
                    placeholder = {
                        Text(
                            text = "Precio"
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 1,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                )
            }
            IconButton(modifier = Modifier.fillMaxWidth(), onClick = {
                if(tipoId != 0 && amount != "" && text != "") {
                    coroutineScope.launch {
                        val current = LocalDateTime.now()
                        daoEntradas.insert(
                            Entrada(
                                concepto = text,
                                anno = current.year,
                                mes = current.monthValue,
                                dia = current.dayOfMonth,
                                hora = current.hour,
                                min = current.minute,
                                cantidad = amount.toFloat(),
                                tipo = tipoId
                            )
                        )
                        text = ""
                        amount = ""
                        tipo = ""
                        tipoId = 0
                        tipoColor = Color.Gray
                    }
                }
            }) {
                Icon(painter = painterResource(id = android.R.drawable.ic_input_add), contentDescription = "")
            }
        }
    }
}