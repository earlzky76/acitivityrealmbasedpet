package ph.edu.auf.realmdiscussion.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ph.edu.auf.realmdiscussion.components.ItemPet
import ph.edu.auf.realmdiscussion.viewmodels.PetViewModel


@Composable
fun PetScreen(petViewModel: PetViewModel = viewModel()) {

    val pets by petViewModel.pets.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var snackbarShown by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // States for Add Pet Dialog
    var showAddDialog by remember { mutableStateOf(false) }
    var petName by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("") }
    var petAge by remember { mutableStateOf("") }

    // Filter the pet list based on the search query
    val filteredPets = pets.filter { pet ->
        pet.name.contains(searchQuery, ignoreCase = true) ||
                pet.age.toString().contains(searchQuery)
    }

    // Show snackbar messages
    LaunchedEffect(petViewModel.showSnackbar) {
        petViewModel.showSnackbar.collect { message ->
            if (!snackbarShown) {
                snackbarShown = true
                coroutineScope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = message,
                        actionLabel = "Dismiss",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.Dismissed || result == SnackbarResult.ActionPerformed) {
                        snackbarShown = false
                    }
                }
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()) {
        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search by name or age") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        // Add New Pet Button
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text("Add New Pet")
        }

        // Pet List
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            LazyColumn(modifier = Modifier.padding(paddingValues)) {
                itemsIndexed(
                    items = filteredPets,
                    key = { _, item -> item.id }
                ) { _, petContent ->
                    ItemPet(petContent, onRemove = petViewModel::deletePet)
                }
            }
        }
    }

    // Add New Pet Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Pet") },
            text = {
                Column {
                    OutlinedTextField(
                        value = petName,
                        onValueChange = { petName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = petType,
                        onValueChange = { petType = it },
                        label = { Text("Type") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = petAge,
                        onValueChange = { petAge = it },
                        label = { Text("Age") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val age = petAge.toIntOrNull()
                        if (petName.isNotBlank() && petType.isNotBlank() && age != null) {
                            petViewModel.addPet(name = petName, petType = petType, age = age)
                            petName = ""
                            petType = ""
                            petAge = ""
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showAddDialog = false },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
