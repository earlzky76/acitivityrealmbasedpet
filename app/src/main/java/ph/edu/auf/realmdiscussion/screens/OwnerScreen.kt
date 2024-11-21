package ph.edu.auf.realmdiscussion.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ph.edu.auf.realmdiscussion.components.DismissBackground
import ph.edu.auf.realmdiscussion.viewmodels.OwnerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerScreen(ownerViewModel: OwnerViewModel = viewModel()) {
    val owners by ownerViewModel.owners.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var currentOwnerId by remember { mutableStateOf("") }
    var currentOwnerName by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold { paddingValues ->
            LazyColumn(modifier = Modifier.padding(paddingValues)) {
                itemsIndexed(
                    items = owners,
                    key = { _, item -> item.id }
                ) { _, ownerContent ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            when (it) {
                                SwipeToDismissBoxValue.StartToEnd -> {
                                    ownerViewModel.deleteOwner(ownerContent.id)
                                }
                                SwipeToDismissBoxValue.EndToStart -> {
                                    return@rememberSwipeToDismissBoxState false
                                }
                                SwipeToDismissBoxValue.Settled -> {
                                    return@rememberSwipeToDismissBoxState false
                                }
                            }
                            return@rememberSwipeToDismissBoxState true
                        },
                        positionalThreshold = { it * .25f }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = { DismissBackground(dismissState) },
                        content = {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(0.dp, 8.dp)
                                    .clickable {
                                        showEditDialog = true
                                        currentOwnerId = ownerContent.id
                                        currentOwnerName = ownerContent.name
                                    },
                                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                                shape = RoundedCornerShape(5.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = ownerContent.name,
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Owns ${ownerContent.pets.size} pets",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(text = "Edit Owner Name") },
            text = {
                Column {
                    TextField(
                        value = currentOwnerName,
                        onValueChange = { currentOwnerName = it },
                        label = { Text("Owner Name") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    ownerViewModel.updateOwnerName(currentOwnerId, currentOwnerName)
                    showEditDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
