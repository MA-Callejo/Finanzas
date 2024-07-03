package com.kiwi.finanzas.ui.views

import android.content.Context
import android.content.SharedPreferences
import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.ImageButton
import android.widget.Space
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.kiwi.finanzas.db.Agrupado
import com.kiwi.finanzas.db.Tipo
import com.kiwi.finanzas.ui.theme.myBlue
import com.kiwi.finanzas.ui.theme.myGreen
import com.kiwi.finanzas.ui.theme.myRed
import com.kiwi.finanzas.ui.theme.myYellow
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.util.Calendar
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

fun getValidatedNumber(text: String): String {
    // Start by filtering out unwanted characters like commas and multiple decimals
    val textSinComa = text.replace(",", ".")
    val filteredChars = textSinComa.filterIndexed { index, c ->
        c in "0123456789" ||                      // Take all digits
                (c == '.' && textSinComa.indexOf('.') == index) ||
                (c == '-' && index == 0)// Take only the first decimal
    }
    // Now we need to remove extra digits from the input
    return if(filteredChars.contains('.')) {
        val beforeDecimal = filteredChars.substringBefore('.')
        val afterDecimal = filteredChars.substringAfter('.')
        beforeDecimal + "." + afterDecimal.take(2)    // If decimal is present, take first 3 digits before decimal and first 2 digits after decimal
    } else {
        filteredChars                     // If there is no decimal, just take the first 3 digits
    }
}

fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navController: NavHostController, daoEntradas: EntradaDAO, daoTipos: TipoDAO, context: Context) {
    /*
}
@Preview
@Composable
fun prev(){*/
    val tipos by daoTipos.getAll().collectAsState(initial = emptyList())
    //val tipos: List<Tipo> = listOf()
    val currentTime = LocalDateTime.now()
    val gastos by daoEntradas.getAllMes(currentTime.monthValue, currentTime.year).collectAsState(initial = emptyList())
    val gastosHoy by daoEntradas.getAllDia(currentTime.monthValue, currentTime.dayOfMonth, currentTime.year).collectAsState(initial = emptyList())
    val agrupados by daoEntradas.getTotales(currentTime.monthValue, currentTime.year).collectAsState(initial = emptyList())
    //val agrupados: List<Agrupado> = listOf()
    var text by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("Tipo") }
    var tipoColor by remember { mutableStateOf(Color.Gray) }
    var textColor by remember { mutableStateOf(Color.White) }
    var tipoId by remember { mutableIntStateOf(0) }
    var expanded by remember { mutableStateOf(false) }
    var showDetalles by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    var entradaEdit: Entrada? by remember { mutableStateOf(null) }
    val coroutineScope = rememberCoroutineScope()
    val periodo by remember { mutableStateOf(if(getPreference(context,"periodo") >= 0f) getPreference(context,"periodo") else 1f) }
    val gastosPeriodo by if(periodo == 1f) {
        daoEntradas.getGastoPeriodoDia(
            ((currentTime.year-1)*372) + ((currentTime.monthValue - 1)*31) + currentTime.dayOfMonth
        ).collectAsState(initial = emptyList())
    }else{
        if(periodo == 2f){
            val timePeriod = currentTime.minusDays(currentTime.dayOfWeek.value - 1L)
            daoEntradas.getGastoPeriodoSemana(
                ((timePeriod.year-1)*372) + ((timePeriod.monthValue - 1)*31) + timePeriod.dayOfMonth,
            ).collectAsState(initial = emptyList())
        } else {
            val timePeriod = currentTime.minusDays((currentTime.dayOfWeek.value - 1L)+7L)
            daoEntradas.getGastoPeriodoQuincena(
                ((timePeriod.year-1)*372) + ((timePeriod.monthValue - 1)*31) + timePeriod.dayOfMonth,
            ).collectAsState(initial = emptyList())
        }
    }
    if(showDetalles){
        DialogDetalles(onDismis = {
            showDetalles = false
        }, agrupadosComp=agrupados)
    }
    if(showEdit){
        if(entradaEdit != null) {
            DialogEdit(context, entrada = entradaEdit!!, onDismis = {
                showEdit = false
                entradaEdit = null
            }, onEdit = {ent ->
                coroutineScope.launch {
                    daoEntradas.update(ent)
                    showEdit = false
                    entradaEdit = null
                }
            }, tipos = tipos,
                onDelete = {id ->
                    coroutineScope.launch {
                        daoEntradas.delete(id)
                        showEdit = false
                        entradaEdit = null
                    }
                })
        }
    }
    Column(modifier = Modifier.blur(if (showDetalles || showEdit) 16.dp else 0.dp)) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(20.dp, 50.dp, 20.dp, 20.dp)) {
            OutlinedCard {
                Column(modifier = Modifier.padding(5.dp)) {
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
                            imeAction = ImeAction.Next,
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                    )
                    Row(
                        modifier = Modifier.padding(0.dp, 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DropdownMenu(expanded = expanded,
                            onDismissRequest = { expanded = false }) {
                            tipos.filter { t -> t.disponible == 1 }.forEach {
                                DropdownMenuItem(
                                    text = { Text(it.nombre, color = it.textColor()) },
                                    onClick = {
                                        expanded = false
                                        tipo = it.nombre
                                        tipoId = it.id
                                        tipoColor = it.color()
                                    },
                                    modifier = Modifier.background(it.color())
                                )
                            }
                        }
                        OutlinedButton(
                            modifier = Modifier.weight(1F),
                            colors = ButtonDefaults.buttonColors(containerColor = tipoColor),
                            onClick = { expanded = true }) {
                            Text(text = tipo, color = textColor)
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                        OutlinedTextField(
                            modifier = Modifier.weight(0.6F),
                            value = amount,
                            onValueChange = {
                                val valor = getValidatedNumber(it)
                                val valorNum = if (valor == "") 0f else valor.toFloat()
                                amount = valorNum.toString()
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
                                imeAction = ImeAction.Done
                            ),
                        )
                    }
                    IconButton(modifier = Modifier.fillMaxWidth(), onClick = {
                        if (tipoId != 0 && amount != "" && text != "") {
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
                                        cantidad = amount.toDouble(),
                                        tipo = tipoId
                                    )
                                )
                                text = ""
                                amount = ""
                                tipo = "Tipo"
                                tipoId = 0
                                tipoColor = Color.Gray
                            }
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_input_add),
                            contentDescription = ""
                        )
                    }
                }
            }
            var inicio = 90f
            val total1 = agrupados.filter { it.total > 0 }.sumOf { it.total }
            val total3 = gastos.sumOf { it.cantidad }
            val gastoMax = when(periodo){
                1f -> {getPreference(context, "maxDia")/(currentTime.month.length(isLeapYear(currentTime.year)))} // Dias
                2f -> {getPreference(context, "maxDia")/((currentTime.month.length(isLeapYear(currentTime.year)))/7f)} // Semanas
                else -> {getPreference(context, "maxDia")/((currentTime.month.length(isLeapYear(currentTime.year)))/15f)}// Quincena
            }
            val total2 = gastosPeriodo.sumOf { it.cantidad }
            val totalDegree = (total2 / gastoMax) * -360f
            Row(modifier = Modifier.padding(0.dp, 40.dp, 0.dp, 0.dp)) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                Canvas(
                    modifier = Modifier
                        .width(100.dp)
                        .height(100.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    showDetalles = true
                                }
                            )
                        }
                ) {
                    agrupados.filter { it.total > 0 }.forEach {
                        val fin = ((it.total / total1) * 360f).toFloat()
                        drawArc(it.color(), -1f * inicio, -1f * fin, true)
                        inicio += fin
                    }
                    //drawArc(Color.Red, -1f*inicio, -10f, true)
                }
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2f)
                )
                Canvas(
                    modifier = Modifier
                        .width(100.dp)
                        .height(100.dp)
                ) {
                    drawArc(Color.Gray, -90f, -360f, true)
                    drawArc(myBlue, -90f, totalDegree.toFloat(), true)
                }
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
            Row(modifier = Modifier.padding(0.dp, 2.dp, 0.dp, 20.dp)) {
                Text(
                    text = DecimalFormat("0.00€").format(((getPreference(context, "maxDia")/currentTime.month.length(
                        isLeapYear(currentTime.year)
                    )) * currentTime.dayOfMonth) - total3),
                    color = if (((getPreference(context,"maxDia")/currentTime.month.length(
                            isLeapYear(currentTime.year)
                        )) * currentTime.dayOfMonth) - total3 < 0f
                    ) myRed else myGreen,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = DecimalFormat("0.00€").format(gastoMax - total2),
                    color = if (gastoMax - total2 < 0) myRed else myGreen,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                items(gastos) { it ->
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp, 3.dp),
                        colors = CardDefaults.cardColors(containerColor = tipos.first { t -> it.tipo == t.id }
                            .color()),
                        onClick = {
                            entradaEdit = it
                            showEdit = true
                        }
                    ) {
                        Text(
                            text = it.concepto,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp, 5.dp),
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                            color = tipos.first { t -> t.id == it.tipo }.textColor(),
                        )
                        Row {
                            Text(
                                text = DecimalFormat("0.00€").format(it.cantidad),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(10.dp, 5.dp),
                                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                                color = if (it.cantidad > 0) myRed else myGreen
                            )
                            Text(
                                text = "${it.dia}-${it.mes}-${it.anno}",
                                modifier = Modifier.padding(2.dp),
                                color = tipos.first { t -> t.id == it.tipo }.textColor(),
                            )
                        }
                    }
                }
            }
            Row() {
                IconButton(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(10.dp), onClick = { navController.navigate("historico/"+currentTime.year.toString()+"/"+currentTime.monthValue.toString()+"/ ") }) {
                    Icon(
                        painter = painterResource(id = R.drawable.historic),
                        contentDescription = "",
                        tint = Color.White
                    )
                }
                IconButton(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(10.dp), onClick = { navController.navigate("settings") }) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_manage),
                        contentDescription = "",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun DialogDetalles(onDismis: () -> Unit = {}, agrupadosComp: List<Agrupado> = listOf()){
    val agrupados = agrupadosComp.filter { it.total > 0 }
    val agrupadosGanancias = agrupadosComp.filter { it.total <= 0 }
    val total1 = agrupados.sumOf { it.total }
    var inicio = 90f
    AlertDialog(
        content = {
            Column(horizontalAlignment = Alignment.CenterHorizontally){
                Canvas(
                    modifier = Modifier
                        .width(100.dp)
                        .height(100.dp)
                ) {
                    agrupados.forEach {
                        val fin = ((it.total / total1) * 360f).toFloat()
                        drawArc(it.color(), -1f * inicio, -1f * fin, true)
                        inicio += fin
                    }
                    //drawArc(Color.Red, -1f*inicio, -10f, true)
                }
                Spacer(modifier = Modifier.height(20.dp))
                var total = agrupados.sumOf { a -> a.total }
                LazyColumn {
                    items(agrupados){
                        Card(modifier = Modifier.padding(5.dp),
                            colors = CardDefaults.cardColors(containerColor = it.color())) {
                            Row(modifier = Modifier.padding(5.dp)){
                                Text(text = it.nombre, modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f), color = it.textColor())
                                Text(text = String.format("%.2f€", it.total), color = it.textColor())
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(text = String.format("%.0f", (it.total/total)*100)+"%", color = it.textColor())
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                LazyColumn {
                    items(agrupadosGanancias){
                        Card(modifier = Modifier.padding(5.dp),
                            colors = CardDefaults.cardColors(containerColor = it.color())) {
                            Row(modifier = Modifier.padding(5.dp)){
                                Text(text = it.nombre, modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f), color = it.textColor())
                                Text(text = String.format("+%.2f€", it.total*-1f), color = it.textColor())
                            }
                        }
                    }
                }
            }
        },
        onDismissRequest = {onDismis()}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogEdit(context: Context, onDismis: () -> Unit = {}, onDelete: (id: Int) -> Unit = {}, onEdit: (entrada: Entrada) -> Unit = {}, entrada: Entrada, tipos: List<Tipo> = listOf()){
    var text by remember { mutableStateOf(entrada.concepto) }
    var expanded by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf(entrada.cantidad.toString()) }
    var tipo by remember { mutableStateOf(tipos.find { t -> t.id == entrada.tipo}?.nombre ?: "") }
    var tipoColor by remember { mutableStateOf(tipos.find { t -> t.id == entrada.tipo}?.color() ?: Color.Gray) }
    var textColor by remember { mutableStateOf(tipos.find { t -> t.id == entrada.tipo}?.textColor() ?: Color.Black) }
    var tipoId by remember { mutableIntStateOf(entrada.tipo) }

    var year by remember { mutableIntStateOf(entrada.anno) }
    var month by remember { mutableIntStateOf(entrada.mes - 1) }
    var day by remember { mutableIntStateOf(entrada.dia) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
            year = selectedYear
            month = selectedMonth
            day = selectedDay
        }, year, month, day
    )
    AlertDialog(
        content = {
            Column(horizontalAlignment = Alignment.CenterHorizontally){
                OutlinedCard {
                    Column(modifier = Modifier.padding(10.dp)) {
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
                                imeAction = ImeAction.Next,
                                capitalization = KeyboardCapitalization.Sentences
                            ),
                        )
                        Row(
                            modifier = Modifier.padding(0.dp, 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DropdownMenu(expanded = expanded,
                                onDismissRequest = { expanded = false }) {
                                tipos.filter { t -> t.disponible == 1 }.forEach {
                                    DropdownMenuItem(
                                        text = { Text(it.nombre) },
                                        onClick = {
                                            expanded = false
                                            tipo = it.nombre
                                            tipoId = it.id
                                            tipoColor = it.color()
                                            textColor = it.textColor()
                                        },
                                        modifier = Modifier.background(it.color())
                                    )
                                }
                            }
                            OutlinedButton(
                                modifier = Modifier.weight(1F),
                                colors = ButtonDefaults.buttonColors(containerColor = tipoColor),
                                onClick = { expanded = true }) {
                                Text(text = tipo, color = textColor)
                            }
                            Spacer(modifier = Modifier.width(5.dp))
                            OutlinedTextField(
                                modifier = Modifier.weight(0.6F),
                                value = amount,
                                onValueChange = {
                                    val valor = getValidatedNumber(it)
                                    val valorNum = if (valor == "") 0f else valor.toFloat()
                                    amount = valorNum.toString()
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
                                    imeAction = ImeAction.Done
                                ),
                            )
                        }
                        Row {
                            Spacer(modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f))
                            TextButton(onClick = {
                                datePickerDialog.show()
                            }, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                                Text("$day-${month + 1}-$year", color = Color.White)
                            }
                        }
                    }
                }
                Row{
                    TextButton(onClick = {
                        if (tipoId != 0 && amount != "" && text != "") {
                            onDelete(entrada.id)
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = myRed)) {
                        Text("Borrar", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(50.dp))
                    TextButton(onClick = {
                        if (tipoId != 0 && amount != "" && text != "") {
                            onEdit(
                                Entrada(
                                    concepto = text,
                                    anno = year,
                                    mes = month + 1,
                                    dia = day,
                                    hora = entrada.hora,
                                    min = entrada.min,
                                    cantidad = amount.toDouble(),
                                    tipo = tipoId,
                                    id = entrada.id
                                )
                            )
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = myBlue)) {
                        Text("OK", color = Color.White)
                    }
                }
            }
        },
        onDismissRequest = {onDismis()},
    )
}