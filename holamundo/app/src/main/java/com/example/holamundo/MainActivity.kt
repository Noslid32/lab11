package com.example.holamundo

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TodoListApp()
        }
    }
}

@Composable
fun TaskItem(task: Task, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(task.text, style = MaterialTheme.typography.bodyLarge)
                task.imageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = "Imagen de la tarea",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(top = 8.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            IconButton(onClick = onEdit) { Text("✏️") }
            IconButton(onClick = onDelete) { Text("❌") }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListApp() {
    var tasks by remember { mutableStateOf(listOf<Task>()) }
    var newTaskText by remember { mutableStateOf("") }
    var newTaskImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        newTaskImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de tareas") }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            OutlinedTextField(
                value = newTaskText,
                onValueChange = {
                    newTaskText = it
                    errorMessage = null
                },
                label = { Text("Nueva tarea") },
                isError = errorMessage != null
            )
            errorMessage?.let {
                Text(text = it, color = Color.Red, modifier = Modifier.padding(top = 4.dp))
            }

            Row {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Seleccionar Imagen", color=Color.Black)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newTaskText.isBlank()) {
                            errorMessage = "La tarea no puede estar vacía"
                        } else {
                            tasks = tasks + Task(newTaskText, newTaskImageUri)
                            newTaskText = ""
                            newTaskImageUri = null
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Agregar tarea")
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(tasks) { task ->
                    TaskItem(task,
                        onEdit = { selectedTask = task },
                        onDelete = { tasks = tasks - task }
                    )
                }
            }
        }

        selectedTask?.let { task ->
            EditTaskDialog(task, onDismiss = { selectedTask = null }) { updatedTask ->
                tasks = tasks.map { if (it == task) updatedTask else it }
                selectedTask = null
            }
        }
    }
}

@Composable
fun EditTaskDialog(task: Task, onDismiss: () -> Unit, onConfirm: (Task) -> Unit) {
    var text by remember { mutableStateOf(task.text) }
    var imageUri by remember { mutableStateOf(task.imageUri) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                if (text.isBlank()) return@Button
                onConfirm(task.copy(text = text, imageUri = imageUri))
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancelar") }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Editar tarea") }
                )
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Seleccionar Imagen")
                }
                imageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = "Imagen de la tarea",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(top = 8.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    )
}

data class Task(val text: String, val imageUri: Uri? = null)
