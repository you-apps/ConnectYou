package com.bnyro.contacts.presentation.screens.contacts

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoveToInbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.enums.ContactsSource
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.domain.model.FilterOptions
import com.bnyro.contacts.navigation.NavRoutes
import com.bnyro.contacts.presentation.components.ClickableIcon
import com.bnyro.contacts.presentation.components.NothingHere
import com.bnyro.contacts.presentation.components.TopBarMoreMenu
import com.bnyro.contacts.presentation.features.ConfirmationDialog
import com.bnyro.contacts.presentation.features.FilterDialog
import com.bnyro.contacts.presentation.features.SimImportDialog
import com.bnyro.contacts.presentation.screens.contacts.components.ContactSearchScreen
import com.bnyro.contacts.presentation.screens.contacts.components.ContactsList
import com.bnyro.contacts.presentation.screens.contacts.model.ContactsModel
import com.bnyro.contacts.presentation.screens.contacts.model.state.ContactListState
import com.bnyro.contacts.presentation.screens.editor.EditorScreen
import com.bnyro.contacts.util.BackupHelper
import com.bnyro.contacts.util.Preferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsPage(
    scrollConnection: NestedScrollConnection?,
    onNavigate: (NavRoutes) -> Unit
) {
    val viewModel: ContactsModel = viewModel(factory = ContactsModel.Factory)
    val context = LocalContext.current

    val selectedContacts = remember {
        mutableStateListOf<ContactData>()
    }

    var newContactToInsert by remember {
        mutableStateOf(viewModel.initialContactData)
    }

    var showEditorScreen by rememberSaveable {
        mutableStateOf(false)
    }

    var showDelete by rememberSaveable {
        mutableStateOf(false)
    }

    var filterOptions by remember {
        mutableStateOf(FilterOptions.default())
    }

    var showSearch by rememberSaveable {
        mutableStateOf(false)
    }

    var showFilterDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showImportSimDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val importVcard =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { viewModel.importVcf(context, it) }
        }

    val exportVcard = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(BackupHelper.mimeType)
    ) { uri ->
        uri?.let { viewModel.exportVcf(context, it) }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showEditorScreen = true
                }
            ) {
                Icon(Icons.Default.Create, null)
            }
        }
    ) { pv ->
        Column(
            modifier = Modifier
                .padding(pv)
                .fillMaxSize()
        ) {
            Crossfade(
                targetState = selectedContacts.isEmpty(),
                label = "main layout"
            ) { selectionEmpty ->
                BackHandler(enabled = !selectionEmpty) {
                    selectedContacts.clear()
                }
                when (selectionEmpty) {
                    true -> {
                        TopAppBar(
                            title = {
                                var expanded by rememberSaveable { mutableStateOf(false) }

                                Row(
                                    Modifier
                                        .padding(8.dp)
                                        .clickable {
                                            expanded = true
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(
                                            id = viewModel.contactsSource.stringRes
                                        )
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null
                                    )
                                }

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    ContactsSource.entries.forEachIndexed { index, source ->
                                        DropdownMenuItem(
                                            text = { Text(stringResource(id = source.stringRes)) },
                                            onClick = {
                                                viewModel.contactsSource = source
                                                Preferences.edit {
                                                    putInt(
                                                        Preferences.selectedContactsRepo,
                                                        index
                                                    )
                                                }
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            },
                            actions = {
                                ClickableIcon(
                                    icon = Icons.Default.Search,
                                    contentDescription = R.string.search
                                ) {
                                    showSearch = true
                                }
                                ClickableIcon(
                                    icon = Icons.Default.Sort,
                                    contentDescription = R.string.filter
                                ) {
                                    showFilterDialog = true
                                }
                                TopBarMoreMenu(
                                    options = listOf(
                                        stringResource(R.string.import_vcf),
                                        stringResource(R.string.export_vcf),
                                        stringResource(R.string.import_sim),
                                        stringResource(R.string.settings),
                                        stringResource(R.string.about)
                                    ),
                                    onOptionClick = { index ->
                                        when (index) {
                                            0 -> {
                                                importVcard.launch(BackupHelper.openMimeTypes)
                                            }

                                            1 -> {
                                                exportVcard.launch(BackupHelper.defaultBackupFileName)
                                            }

                                            2 -> {
                                                showImportSimDialog = true
                                            }

                                            3 -> {
                                                onNavigate.invoke(NavRoutes.Settings)
                                            }

                                            4 -> {
                                                onNavigate.invoke(NavRoutes.About)
                                            }
                                        }
                                    }
                                )
                            }
                        )
                    }

                    false -> {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedContacts.containsAll(viewModel.contacts),
                                        onCheckedChange = {
                                            if (selectedContacts.containsAll(viewModel.contacts)) {
                                                selectedContacts.clear()
                                            } else {
                                                selectedContacts.clear()
                                                selectedContacts.addAll(viewModel.contacts)
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = stringResource(
                                            R.string.selected,
                                            selectedContacts.size.toString()
                                        ),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            },
                            actions = {
                                ClickableIcon(
                                    icon = Icons.Default.CopyAll,
                                    contentDescription = R.string.copy
                                ) {
                                    viewModel.copyContacts(context, selectedContacts.toList())
                                    selectedContacts.clear()
                                }
                                ClickableIcon(
                                    icon = Icons.Default.MoveToInbox,
                                    contentDescription = R.string.move
                                ) {
                                    viewModel.moveContacts(context, selectedContacts.toList())
                                    selectedContacts.clear()
                                }
                                ClickableIcon(
                                    icon = Icons.Default.Share,
                                    contentDescription = R.string.share
                                ) {
                                    viewModel.shareTempContacts(context, selectedContacts)
                                    selectedContacts.clear()
                                }
                                ClickableIcon(
                                    icon = Icons.Default.Delete,
                                    contentDescription = R.string.delete
                                ) {
                                    showDelete = true
                                }
                            }
                        )
                    }
                }
            }

            when (
                val contactState = when (viewModel.contactsSource) {
                    ContactsSource.DEVICE -> viewModel.deviceContacts
                    ContactsSource.LOCAL -> viewModel.localContacts
                }
            ) {
                ContactListState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                ContactListState.Empty, ContactListState.Error -> {
                    NothingHere()
                }

                is ContactListState.Success -> {
                    ContactsList(
                        contacts = contactState.contacts,
                        filterOptions = filterOptions,
                        scrollConnection = scrollConnection,
                        selectedContacts = selectedContacts
                    )
                    if (showSearch) {
                        ContactSearchScreen(
                            contacts = contactState.contacts,
                            filterOptions = filterOptions,
                            onDismissRequest = { showSearch = false }
                        )
                    }
                }
            }
        }
    }

    if (showEditorScreen || newContactToInsert != null) {
        EditorScreen(
            contact = newContactToInsert,
            onClose = {
                newContactToInsert = null
                showEditorScreen = false
            },
            isCreatingNewDeviceContact = (viewModel.contactsSource == ContactsSource.DEVICE),
            onSave = {
                viewModel.createContact(context, it)
            }
        )
    }

    if (showDelete) {
        ConfirmationDialog(
            onDismissRequest = {
                showDelete = false
            },
            title = stringResource(R.string.delete_contact),
            text = stringResource(R.string.irreversible)
        ) {
            viewModel.deleteContacts(selectedContacts.toList())
            selectedContacts.clear()
        }
    }

    if (showFilterDialog) {
        FilterDialog(
            onDismissRequest = {
                showFilterDialog = false
            },
            onFilterChanged = {
                Preferences.edit {
                    putInt(Preferences.sortOrderKey, it.sortOder.ordinal)
                    putStringSet(Preferences.hiddenAccountsKey, it.hiddenAccountIdentifiers.toSet())
                    putBoolean(Preferences.favoritesOnlyKey, it.favoritesOnly)
                }
                filterOptions = it
            },
            initialFilters = filterOptions,
            availableAccountTypes = viewModel.getAvailableAccounts(context),
            availableGroups = viewModel.getAvailableGroups()
        )
    }

    if (showImportSimDialog) {
        SimImportDialog {
            showImportSimDialog = false
        }
    }
}
