package com.bnyro.contacts.presentation.features

import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.model.ContactsGroup
import com.bnyro.contacts.presentation.components.ClickableIcon
import com.bnyro.contacts.presentation.screens.contacts.model.ContactsModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsDialog(
    onDismissRequest: () -> Unit,
    participatingGroups: List<ContactsGroup>,
    onContactGroupsChanges: (participatingGroups: List<ContactsGroup>) -> Unit
) {
    val contactsModel: ContactsModel = viewModel(factory = ContactsModel.Factory)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var contactGroups by remember {
        mutableStateOf(contactsModel.getAvailableGroups())
    }
    var groupToDelete by remember {
        mutableStateOf<ContactsGroup?>(null)
    }
    var showCreateGroup by remember {
        mutableStateOf(false)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            DialogButton(text = stringResource(R.string.okay)) {
                onDismissRequest.invoke()
            }
        },
        dismissButton = {
            DialogButton(text = stringResource(R.string.new_group)) {
                showCreateGroup = true
            }
        },
        title = {
            Text(stringResource(R.string.manage_groups))
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(300.dp)
            ) {
                items(contactGroups, key = ContactsGroup::rowId) { group ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = participatingGroups.contains(
                                group
                            ),
                            onCheckedChange = { newValue ->
                                onContactGroupsChanges(
                                    if (newValue) participatingGroups + group else participatingGroups - group
                                )
                            }
                        )
                        Text(
                            modifier = Modifier.weight(1f),
                            text = group.title
                        )
                        ClickableIcon(
                            icon = Icons.Default.Delete,
                            contentDescription = R.string.delete_group
                        ) {
                            groupToDelete = group
                        }
                    }
                }
            }
        }
    )

    groupToDelete?.let {
        ConfirmationDialog(
            onDismissRequest = { groupToDelete = null },
            title = stringResource(R.string.delete_group),
            text = stringResource(R.string.irreversible)
        ) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    contactsModel.contactsRepository.deleteGroup(it)
                }
                contactGroups = contactGroups - it
                if (participatingGroups.contains(it)) {
                    onContactGroupsChanges(participatingGroups - it)
                }
            }
        }
    }

    if (showCreateGroup) {
        var title by remember {
            mutableStateOf("")
        }

        AlertDialog(
            onDismissRequest = { showCreateGroup = false },
            confirmButton = {
                DialogButton(text = stringResource(R.string.okay)) {
                    if (title.isNotBlank() && contactGroups.none { it.title == title }) {
                        scope.launch {
                            val group = withContext(Dispatchers.IO) {
                                contactsModel.contactsRepository.createGroup(title)
                            }
                            group?.let { contactGroups = contactGroups + it }
                            showCreateGroup = false
                        }
                    } else {
                        Toast.makeText(
                            context,
                            R.string.group_already_exists,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            title = {
                Text(stringResource(R.string.new_group))
            },
            text = {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = {
                        Text(stringResource(R.string.title))
                    }
                )
            }
        )
    }
}
