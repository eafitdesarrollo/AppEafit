package co.edu.eafit.appeafit.ui.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import co.edu.eafit.appeafit.R
import java.util.Locale
import java.util.UUID

private class GpaEntry(val id: String = UUID.randomUUID().toString(), name: String, credits: String, grade: String) {
    var name by mutableStateOf(name)
    var credits by mutableStateOf(credits)
    var grade by mutableStateOf(grade)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpaCalculatorScreen(onBack: () -> Unit) {
    val entries = remember { mutableStateListOf(GpaEntry(name = "", credits = "3", grade = "")) }

    val totalCredits = entries.sumOf { it.credits.toDoubleOrNull() ?: 0.0 }
    val average = if (totalCredits == 0.0) 0.0
        else entries.sumOf { (it.credits.toDoubleOrNull() ?: 0.0) * (it.grade.toDoubleOrNull() ?: 0.0) } / totalCredits

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.service_gpa_calculator)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { entries.add(GpaEntry(name = "", credits = "3", grade = "")) }) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar materia")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Surface(color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("Promedio ponderado", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        String.format(Locale.getDefault(), "%.2f", average),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.displayLarge
                    )
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(entries, key = { it.id }) { entry ->
                    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = entry.name,
                                onValueChange = { entry.name = it },
                                placeholder = { Text("Materia") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = entry.credits,
                                onValueChange = { entry.credits = it },
                                placeholder = { Text("Créd.") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(0.6f)
                            )
                            OutlinedTextField(
                                value = entry.grade,
                                onValueChange = { entry.grade = it },
                                placeholder = { Text("Nota") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(0.6f)
                            )
                            IconButton(onClick = { entries.remove(entry) }) {
                                Icon(Icons.Filled.Delete, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }
}
