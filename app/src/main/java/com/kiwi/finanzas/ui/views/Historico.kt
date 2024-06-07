package com.kiwi.finanzas.ui.views

import android.R
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiwi.finanzas.Mes
import com.kiwi.finanzas.db.Entrada
import com.kiwi.finanzas.db.EntradaDAO
import com.kiwi.finanzas.db.TipoDAO
import com.kiwi.finanzas.ui.theme.myGreen
import com.kiwi.finanzas.ui.theme.myRed
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Historico(daoEntradas: EntradaDAO, daoTipos: TipoDAO, context: Context) {
    val tipos by daoTipos.getAll().collectAsState(initial = emptyList())
    var showEdit by remember { mutableStateOf(false) }
    val currentTime = LocalDateTime.now()
    var entradaEdit: Entrada? by remember { mutableStateOf(null) }
    var dia: Int? by remember { mutableStateOf(currentTime.dayOfMonth) }
    var diaExpanded by remember { mutableStateOf(false) }
    var mes: Int? by remember { mutableStateOf(currentTime.monthValue) }
    var mesExpanded by remember { mutableStateOf(false) }
    var anno: Int? by remember { mutableStateOf(currentTime.year) }
    var annoExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    val entradas by if(anno != null){
        if(mes != null){
            if(dia != null){
                daoEntradas.getAllDiaFiltro(mes!!, dia!!, anno!!, "%$text%")
                    .collectAsState(initial = emptyList())
            }else {
                daoEntradas.getAllMesFiltro(mes!!, anno!!, "%$text%")
                    .collectAsState(initial = emptyList())
            }
        }else {
            daoEntradas.getAllAnnoFiltro(anno!!, "%$text%").collectAsState(initial = emptyList())
        }
    }else{
        daoEntradas.getAllFiltro("%$text%").collectAsState(initial = emptyList())
    }

    val createFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.bufferedWriter().use { writer ->
                    writer.write("Concepto,Fecha,Importe,Tipo")
                    writer.newLine()
                    entradas.forEach { entry ->
                        writer.write("${entry.concepto},${entry.anno}-${entry.mes}-${entry.dia},${entry.cantidad},${tipos.find { t -> t.id == entry.tipo }?.nombre}")
                        writer.newLine()
                    }
                }
            }
        }
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
    Column(modifier = Modifier.blur(if (showEdit) 16.dp else 0.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 50.dp, 20.dp, 20.dp)
        ) {

            Row (modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)){
                Column (modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()){
                    DropdownMenu(expanded = annoExpanded,onDismissRequest = { annoExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Todos") },
                            onClick = {
                                anno = null
                                mes = null
                                dia = null
                                annoExpanded = false
                            }
                        )
                        for(i in 2000 .. currentTime.year){
                            DropdownMenuItem(
                                text = { Text(i.toString()) },
                                onClick = {
                                    anno = i
                                    annoExpanded = false
                                }
                            )
                        }
                    }
                    OutlinedButton(modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                        onClick = {
                            annoExpanded = true
                        }) {
                        Text(text = (if(anno!=null) anno.toString() else "Todos"))
                    }
                }
                Text(text = " / ")
                Column (modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()){
                    if(anno != null){
                        DropdownMenu(expanded = mesExpanded,onDismissRequest = { mesExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Todos") },
                                onClick = {
                                    mes = null
                                    mesExpanded = false
                                }
                            )
                            for(i in 1 .. 12){
                                DropdownMenuItem(
                                    text = { Text(Mes.obtenerPorIndice(i-1).nombre) },
                                    onClick = {
                                        mes = i
                                        mesExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedButton(modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                        onClick = {
                            mesExpanded = true
                        }) {
                        Text(text = (if(mes!=null) Mes.obtenerPorIndice(mes!! - 1).nombre else "Todos"))
                    }
                }
                Text(text = " / ")
                Column (modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()){
                    if(mes != null){
                        DropdownMenu(expanded = diaExpanded,onDismissRequest = { diaExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Todos") },
                                onClick = {
                                    dia = null
                                    diaExpanded = false
                                }
                            )
                            for(i in 1 .. Mes.obtenerPorIndice(mes!! - 1).dias){
                                DropdownMenuItem(
                                    text = { Text(i.toString()) },
                                    onClick = {
                                        dia = i
                                        diaExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedButton(modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                        onClick = {
                            diaExpanded = true
                        }) {
                        Text(text = (if(dia!=null) dia.toString() else "Todos"))
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth()){
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = text,
                    onValueChange = {
                        text = it
                    },
                    placeholder = {
                        Text(
                            text = "Buscador",
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 1,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                items(entradas) { it ->
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
                    .padding(10.dp), onClick = { createFileLauncher.launch("registros.csv") }) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.sym_contact_card),
                        contentDescription = "",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
