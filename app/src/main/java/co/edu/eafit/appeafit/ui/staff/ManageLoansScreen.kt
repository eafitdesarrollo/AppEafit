package co.edu.eafit.appeafit.ui.staff

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.edu.eafit.appeafit.R
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.domain.model.Loan
import co.edu.eafit.appeafit.domain.model.Role
import co.edu.eafit.appeafit.domain.model.User
import co.edu.eafit.appeafit.ui.components.EafitCard
import co.edu.eafit.appeafit.ui.components.EmptyState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Gestión de préstamos de biblioteca para Administrativo/Biblioteca:
 * registrar un préstamo nuevo a un estudiante, marcarlo devuelto, o
 * borrarlo -- antes solo el estudiante podía VER y renovar sus propios
 * préstamos; no existía ninguna pantalla para que biblioteca los cree o los
 * cierre, aunque `loans` ya era escribible por staff/admin desde el origen
 * del proyecto. Ver BITACORA.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageLoansScreen(container: AppContainer, onBack: () -> Unit) {
    var loans by remember { mutableStateOf<List<Loan>>(emptyList()) }
    var students by remember { mutableStateOf<List<User>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.forLanguageTag("es-CO")) }

    fun reload() {
        scope.launch { container.loanRepository.listAll().onSuccess { loans = it } }
    }

    LaunchedEffect(Unit) {
        reload()
        container.userRepository.listUsers().onSuccess { users -> students = users.filter { it.role == Role.STUDENT } }
    }

    val studentById = remember(students) { students.associateBy { it.uid } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.service_manage_loans)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.manage_loans_new))
            }
        }
    ) { padding ->
        if (loans.isEmpty()) {
            EmptyState(message = stringResource(R.string.manage_loans_empty), icon = Icons.AutoMirrored.Filled.MenuBook, modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(loans, key = { _, item -> item.id }) { index, loan ->
                    val student = studentById[loan.studentId]
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(250, delayMillis = (index % 12) * 30)) +
                            slideInVertically(tween(250, delayMillis = (index % 12) * 30)) { it / 5 }
                    ) {
                        EafitCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        loan.itemTitle,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        student?.fullName?.ifBlank { student.email } ?: loan.studentId,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        if (loan.returned) {
                                            stringResource(R.string.manage_loans_returned)
                                        } else {
                                            stringResource(R.string.manage_loans_due, dateFormat.format(Date(loan.dueAt)))
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (loan.returned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                }
                                Row {
                                    if (!loan.returned) {
                                        IconButton(onClick = {
                                            scope.launch {
                                                container.loanRepository.markReturned(loan.id)
                                                reload()
                                            }
                                        }) {
                                            Icon(Icons.Filled.CheckCircle, contentDescription = null)
                                        }
                                    }
                                    IconButton(onClick = {
                                        scope.launch {
                                            container.loanRepository.delete(loan.id)
                                            reload()
                                        }
                                    }) {
                                        Icon(Icons.Filled.Delete, contentDescription = null)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        NewLoanDialog(
            students = students,
            onDismiss = { showDialog = false },
            onCreate = { loan ->
                scope.launch {
                    container.loanRepository.create(loan)
                    reload()
                    showDialog = false
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewLoanDialog(
    students: List<User>,
    onDismiss: () -> Unit,
    onCreate: (Loan) -> Unit
) {
    var itemTitle by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var selectedStudent by remember { mutableStateOf<User?>(null) }
    var dueDate by remember { mutableStateOf(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(14)) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es-CO")) }

    val filteredStudents = remember(students, query) {
        if (query.isBlank()) emptyList()
        else students.filter { it.fullName.contains(query, true) || it.email.contains(query, true) }
    }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(stringResource(R.string.manage_loans_new)) },
        text = {
            Column {
                OutlinedTextField(
                    value = itemTitle, onValueChange = { itemTitle = it },
                    label = { Text(stringResource(R.string.manage_loans_item_label)) },
                    singleLine = true, enabled = !isSaving, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.size(8.dp))
                OutlinedTextField(
                    value = selectedStudent?.let { student -> student.fullName.ifBlank { student.email } } ?: query,
                    onValueChange = { query = it; selectedStudent = null },
                    label = { Text(stringResource(R.string.manage_loans_student_label)) },
                    singleLine = true, enabled = !isSaving, modifier = Modifier.fillMaxWidth()
                )
                if (selectedStudent == null && filteredStudents.isNotEmpty()) {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
                        itemsIndexed(filteredStudents, key = { _, item -> item.uid }) { _, student ->
                            Surface(onClick = { selectedStudent = student; query = "" }, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    student.fullName.ifBlank { student.email },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.size(8.dp))
                TextButton(onClick = { if (!isSaving) showDatePicker = true }) {
                    Icon(Icons.Filled.EditCalendar, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(stringResource(R.string.manage_loans_due, dateFormat.format(Date(dueDate))))
                }
            }
        },
        confirmButton = {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                TextButton(
                    enabled = itemTitle.isNotBlank() && selectedStudent != null,
                    onClick = {
                        isSaving = true
                        onCreate(
                            Loan(
                                studentId = selectedStudent!!.uid,
                                itemTitle = itemTitle,
                                loanedAt = System.currentTimeMillis(),
                                dueAt = dueDate,
                                returned = false,
                                renewalCount = 0
                            )
                        )
                    }
                ) { Text(stringResource(R.string.common_save)) }
            }
        },
        dismissButton = {
            TextButton(onClick = { if (!isSaving) onDismiss() }) { Text(stringResource(R.string.common_cancel)) }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dueDate = it }
                    showDatePicker = false
                }) { Text(stringResource(R.string.common_accept)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
