package com.bnyro.contacts

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.bnyro.contacts.ui.components.ContactsPage
import com.bnyro.contacts.ui.theme.ConnectYouTheme
import com.bnyro.contacts.util.ContactsHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConnectYouTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val contactsHelper = ContactsHelper(this)
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_CONTACTS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        Toast.makeText(this, "no perms", Toast.LENGTH_SHORT).show()
                        return@Surface
                    }
                    val contacts = contactsHelper.getContactList()
                    ContactsPage(contacts)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ConnectYouTheme {
        Greeting("Android")
    }
}
