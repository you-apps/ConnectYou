package com.bnyro.contacts.ui.components

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
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.contacts.R
import com.bnyro.contacts.enums.ContactsSource
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.obj.FilterOptions
import com.bnyro.contacts.ui.components.base.ClickableIcon
import com.bnyro.contacts.ui.components.base.OptionMenu
import com.bnyro.contacts.ui.components.dialogs.ConfirmationDialog
import com.bnyro.contacts.ui.components.dialogs.FilterDialog
import com.bnyro.contacts.ui.components.dialogs.SimImportDialog
import com.bnyro.contacts.ui.models.ContactsModel
import com.bnyro.contacts.ui.models.state.ContactListState
import com.bnyro.contacts.ui.screens.AboutScreen
import com.bnyro.contacts.ui.screens.EditorScreen
import com.bnyro.contacts.ui.screens.SettingsScreen
import com.bnyro.contacts.ui.screens.SingleContactScreen
import com.bnyro.contacts.util.BackupHelper
import com.bnyro.contacts.util.Preferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsPage(
    scrollConnection: NestedScrollConnection?
) {
    val viewModel: ContactsModel = viewModel(factory = ContactsModel.Factory)
    val context = LocalContext.current

    val selectedContacts = remember {
        mutableStateListOf<ContactData>()
    }

    var newContactToInsert by remember {
        mutableStateOf(viewModel.initialContactData)
    }

    var showDelete by remember {
        mutableStateOf(false)
    }

    var filterOptions by remember {
        mutableStateOf(FilterOptions.default())
    }

    var showSettings by remember {
        mutableStateOf(false)
    }

    var showSearch by remember {
        mutableStateOf(false)
    }

    var showAbout by remember {
        mutableStateOf(false)
    }

    var showFilterDialog by remember {
        mutableStateOf(false)
    }

    var showImportSimDialog by remember {
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
                    newContactToInsert = ContactData()
                }
            ) {
                Icon(Icons.Default.Create, null)
            }
        }
    ) { pv ->
        Column(
            modifier = Modifier.padding(pv).fillMaxSize()
        ) {
            Crossfade(targetState = selectedContacts.isEmpty(), label = "main layout") { state ->
                when (state) {
                    true -> {
                        TopAppBar(
                            title = {
                                var expanded by remember { mutableStateOf(false) }
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
                                    ContactsSource.values().forEachIndexed { index, source ->
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
                                var expandedOptions by remember {
                                    mutableStateOf(false)
                                }
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
                                ClickableIcon(
                                    icon = Icons.Default.MoreVert,
                                    contentDescription = R.string.more
                                ) {
                                    expandedOptions = !expandedOptions
                                }
                                OptionMenu(
                                    expanded = expandedOptions,
                                    options = listOf(
                                        stringResource(R.string.import_vcf),
                                        stringResource(R.string.export_vcf),
                                        stringResource(R.string.import_sim),
                                        stringResource(R.string.settings),
                                        stringResource(R.string.about)
                                    ),
                                    onDismissRequest = {
                                        expandedOptions = false
                                    },
                                    onSelect = {
                                        when (it) {
                                            0 -> {
                                                importVcard.launch(BackupHelper.openMimeTypes)
                                            }

                                            1 -> {
                                                exportVcard.launch(BackupHelper.backupFileName)
                                            }

                                            2 -> {
                                                showImportSimDialog = true
                                            }

                                            3 -> {
                                                showSettings = true
                                            }

                                            4 -> {
                                                showAbout = true
                                            }
                                        }
                                        expandedOptions = false
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

    if (newContactToInsert != null) {
        EditorScreen(
            contact = newContactToInsert,
            onClose = {
                newContactToInsert = null
            },
            isCreatingNewDeviceContact = (viewModel.contactsSource == ContactsSource.DEVICE),
            onSave = {
                viewModel.createContact(context, it)
            }
        )
    }

    if (showSettings) {
        SettingsScreen {
            showSettings = false
        }
    }

    if (showAbout) {
        AboutScreen {
            showAbout = false
        }
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
                }
                filterOptions = it
            },
            initialFilters = filterOptions,
            availableAccountTypes = viewModel.getAvailableAccounts(),
            availableGroups = viewModel.getAvailableGroups()
        )
    }

    if (showImportSimDialog) {
        SimImportDialog {
            showImportSimDialog = false
        }
    }
    viewModel.initialContactData?.let {
        SingleContactScreen(it) {
            viewModel.initialContactData = null
        }
    }
}
