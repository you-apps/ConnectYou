package com.bnyro.contacts.ui.components

import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bnyro.contacts.R
import com.bnyro.contacts.enums.SortOrder
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.ui.components.base.ClickableIcon
import com.bnyro.contacts.ui.components.base.OptionMenu
import com.bnyro.contacts.ui.models.ContactsModel
import com.bnyro.contacts.ui.screens.EditorScreen
import com.bnyro.contacts.util.ExportHelper

@Composable
fun ContactsPage(
    contacts: List<ContactData>?,
    showEditorDefault: Boolean
) {
    val viewModel: ContactsModel = viewModel()
    val context = LocalContext.current
    val handler = Handler(Looper.getMainLooper())
    val exportHelper = ExportHelper(context)

    var showEditor by remember {
        mutableStateOf(showEditorDefault)
    }

    var sortOrder by remember {
        mutableStateOf(SortOrder.FIRSTNAME)
    }

    val importVcard = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { exportHelper.importContacts(it) }
    }

    val exportVcard = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/vcard")
    ) { uri ->
        uri?.let { exportHelper.exportContacts(it, contacts.orEmpty()) }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (contacts != null) {
            Column {
                val searchQuery = remember {
                    mutableStateOf(TextFieldValue())
                }

                SearchBar(Modifier.padding(horizontal = 10.dp, vertical = 15.dp), searchQuery) {
                    var expandedSort by remember {
                        mutableStateOf(false)
                    }
                    ClickableIcon(
                        icon = Icons.Default.Sort
                    ) {
                        expandedSort = !expandedSort
                    }
                    OptionMenu(
                        expanded = expandedSort,
                        options = listOf(
                            stringResource(R.string.first_name),
                            stringResource(R.string.surname)
                        ),
                        onDismissRequest = {
                            expandedSort = false
                        },
                        onSelect = {
                            sortOrder = SortOrder.fromInt(it)
                            expandedSort = false
                        }
                    )
                    var expandedOptions by remember {
                        mutableStateOf(false)
                    }
                    ClickableIcon(
                        icon = Icons.Default.MoreVert
                    ) {
                        expandedOptions = !expandedOptions
                    }
                    OptionMenu(
                        expanded = expandedOptions,
                        options = listOf(
                            stringResource(R.string.import_vcf),
                            stringResource(R.string.export_vcf)
                        ),
                        onDismissRequest = {
                            expandedOptions = false
                        },
                        onSelect = {
                            when (it) {
                                0 -> {
                                    importVcard.launch(arrayOf("text/vcard"))
                                }
                                1 -> {
                                    exportVcard.launch("contacts.vcf")
                                }
                            }
                            expandedOptions = false
                        }
                    )
                }

                LazyColumn {
                    items(
                        contacts.filter {
                            it.displayName.orEmpty().lowercase().contains(
                                searchQuery.value.text.lowercase()
                            )
                        }.sortedBy {
                            when (sortOrder) {
                                SortOrder.FIRSTNAME -> it.firstName
                                SortOrder.SURNAME -> it.surName
                            }
                        }
                    ) {
                        ContactItem(it, sortOrder)
                    }
                }
            }
            FloatingActionButton(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd),
                onClick = {
                    showEditor = true
                }
            ) {
                Icon(Icons.Default.Create, null)
            }
        } else {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var showRetry by remember {
                    mutableStateOf(false)
                }
                LaunchedEffect(showRetry) {
                    handler.postDelayed({
                        showRetry = true
                    }, 2000)
                }

                CircularProgressIndicator()
                if (showRetry) {
                    Spacer(modifier = Modifier.height(40.dp))
                    Button(onClick = {
                        viewModel.loadContacts(context)
                        showRetry = false
                    }) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
        }
    }

    if (showEditor) {
        EditorScreen(
            onClose = {
                showEditor = false
            },
            onSave = {
                viewModel.createContact(context, it)
            }
        )
    }
}
