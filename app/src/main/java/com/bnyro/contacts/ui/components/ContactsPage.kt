package com.bnyro.contacts.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ImportContacts
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MoveToInbox
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.contacts.R
import com.bnyro.contacts.enums.SortOrder
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.obj.FilterOptions
import com.bnyro.contacts.ui.components.base.ClickableIcon
import com.bnyro.contacts.ui.components.base.OptionMenu
import com.bnyro.contacts.ui.components.base.SearchBar
import com.bnyro.contacts.ui.components.dialogs.ConfirmationDialog
import com.bnyro.contacts.ui.components.dialogs.FilterDialog
import com.bnyro.contacts.ui.components.modifier.scrollbar
import com.bnyro.contacts.ui.models.ContactsModel
import com.bnyro.contacts.ui.screens.AboutScreen
import com.bnyro.contacts.ui.screens.EditorScreen
import com.bnyro.contacts.ui.screens.SettingsScreen
import com.bnyro.contacts.util.BackupHelper
import com.bnyro.contacts.util.DeviceContactsHelper
import com.bnyro.contacts.util.PermissionHelper
import com.bnyro.contacts.util.Preferences
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ContactsPage(
    contacts: List<ContactData>?,
    contactToInsert: ContactData?,
    scrollConnection: NestedScrollConnection?,
    bottomBarOffsetHeight: Dp
) {
    val viewModel: ContactsModel = viewModel()
    val context = LocalContext.current

    val selectedContacts = remember {
        mutableStateListOf<ContactData>()
    }

    var newContactToInsert by remember {
        mutableStateOf(contactToInsert)
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

    var showAbout by remember {
        mutableStateOf(false)
    }

    var showFilterDialog by remember {
        mutableStateOf(false)
    }

    val importVcard =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { viewModel.importVcf(context, it) }
        }

    val exportVcard = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/vcard")
    ) { uri ->
        uri?.let { viewModel.exportVcf(context, it) }
    }

    val fabBottomPadding by animateDpAsState(targetValue = bottomBarOffsetHeight)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            val searchQuery = remember {
                mutableStateOf(TextFieldValue())
            }

            Crossfade(targetState = selectedContacts.isEmpty()) { state ->
                when (state) {
                    true -> {
                        SearchBar(
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .padding(top = 15.dp),
                            state = searchQuery
                        ) {
                            Box(
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                var expandedOptions by remember {
                                    mutableStateOf(false)
                                }

                                Row {
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
                                }

                                OptionMenu(
                                    expanded = expandedOptions,
                                    options = listOf(
                                        stringResource(R.string.import_vcf),
                                        stringResource(R.string.export_vcf),
                                        stringResource(R.string.settings),
                                        stringResource(R.string.about)
                                    ),
                                    onDismissRequest = {
                                        expandedOptions = false
                                    },
                                    onSelect = {
                                        when (it) {
                                            0 -> {
                                                importVcard.launch(BackupHelper.vCardMimeTypes)
                                            }

                                            1 -> {
                                                exportVcard.launch("contacts.vcf")
                                            }

                                            2 -> {
                                                showSettings = true
                                            }

                                            3 -> {
                                                showAbout = true
                                            }
                                        }
                                        expandedOptions = false
                                    }
                                )
                            }
                        }
                    }
                    false -> {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedContacts.containsAll(contacts.orEmpty()),
                                        onCheckedChange = {
                                            if (selectedContacts.containsAll(contacts.orEmpty())) {
                                                selectedContacts.clear()
                                            } else {
                                                selectedContacts.clear()
                                                selectedContacts.addAll(contacts.orEmpty())
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

            if (contacts == null) {
                LaunchedEffect(Unit) {
                    if (PermissionHelper.hasPermission(
                            context,
                            Manifest.permission.READ_CONTACTS
                        )
                    ) {
                        return@LaunchedEffect
                    }
                    while (!PermissionHelper.hasPermission(
                            context,
                            Manifest.permission.READ_CONTACTS
                        )
                    ) {
                        delay(100)
                    }
                    viewModel.loadContacts(context)
                }
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else if (contacts.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        modifier = Modifier.size(120.dp),
                        imageVector = Icons.Default.ImportContacts,
                        contentDescription = null
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.nothing_here)
                    )
                }
            } else {
                val state = rememberLazyListState()
                LazyColumn(
                    state = state,
                    modifier = Modifier
                        .padding(end = 5.dp)
                        .scrollbar(state, false)
                        .let { modifier ->
                            scrollConnection?.let { modifier.nestedScroll(it) } ?: modifier
                        }
                ) {
                    val contactGroups = contacts.asSequence().filter {
                        it.displayName.orEmpty().lowercase().contains(
                            searchQuery.value.text.lowercase()
                        )
                    }.filter {
                        !filterOptions.hiddenAccountNames.contains(it.accountName)
                    }.filter {
                        if (filterOptions.visibleGroups.isEmpty()) {
                            true
                        } else {
                            filterOptions.visibleGroups.any { group ->
                                it.groups.contains(group)
                            }
                        }
                    }.sortedBy {
                        when (filterOptions.sortOder) {
                            SortOrder.FIRSTNAME -> it.displayName
                            SortOrder.LASTNAME -> it.alternativeName
                        }
                    }.groupBy {
                        when (filterOptions.sortOder) {
                            SortOrder.FIRSTNAME -> it.displayName
                            SortOrder.LASTNAME -> it.alternativeName
                        }?.firstOrNull()?.uppercase()
                    }

                    contactGroups.forEach { (firstLetter, groupedContacts) ->
                        stickyHeader {
                            CharacterHeader(firstLetter.orEmpty())
                        }
                        items(groupedContacts) {
                            ContactItem(
                                contact = it,
                                sortOrder = filterOptions.sortOder,
                                selected = selectedContacts.contains(it),
                                onSinglePress = {
                                    if (selectedContacts.isEmpty()) {
                                        false
                                    } else {
                                        if (selectedContacts.contains(it)) {
                                            selectedContacts.remove(it)
                                        } else {
                                            selectedContacts.add(it)
                                        }
                                        true
                                    }
                                },
                                onLongPress = {
                                    if (!selectedContacts.contains(it)) selectedContacts.add(it)
                                }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
        FloatingActionButton(
            modifier = Modifier
                .padding(16.dp)
                .padding(bottom = fabBottomPadding)
                .align(Alignment.BottomEnd),
            onClick = {
                newContactToInsert = ContactData()
            }
        ) {
            Icon(Icons.Default.Create, null)
        }
    }

    if (newContactToInsert != null) {
        EditorScreen(
            contact = newContactToInsert,
            onClose = {
                newContactToInsert = null
            },
            isCreatingNewDeviceContact = viewModel.contactsHelper is DeviceContactsHelper,
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
                Preferences.edit { putInt(Preferences.sortOrderKey, it.sortOder.value) }
                filterOptions = it
            },
            initialFilters = filterOptions,
            availableAccountName = viewModel.getAvailableAccountNames(),
            availableGroups = viewModel.getAvailableGroups()
        )
    }
}
