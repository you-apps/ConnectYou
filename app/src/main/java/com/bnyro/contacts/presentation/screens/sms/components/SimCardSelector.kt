package com.bnyro.contacts.presentation.screens.sms.components

import android.telephony.SubscriptionInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.bnyro.contacts.util.Preferences
import com.bnyro.contacts.util.rememberPreference

@Composable
fun SimCardSelector(
    subscriptions: List<SubscriptionInfo>,
    onSubscriptionIndexChange: (SubscriptionInfo) -> Unit
) {
    var selectedSubscriptionId by rememberPreference(Preferences.selectedSimSubscriptionIdKey, -1)

    if (subscriptions.size >= 2) {
        var currentSubscriptionIndex by remember {
            mutableIntStateOf(0)
        }
        LaunchedEffect(Unit) {
            currentSubscriptionIndex =
                subscriptions.indexOfFirst { it.subscriptionId == selectedSubscriptionId }
                    .takeIf { it != -1 } ?: 0
            onSubscriptionIndexChange(subscriptions[currentSubscriptionIndex])
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(
                onClick = {
                    currentSubscriptionIndex =
                        (currentSubscriptionIndex + 1) % subscriptions.size
                    onSubscriptionIndexChange(subscriptions[currentSubscriptionIndex])
                    selectedSubscriptionId = subscriptions[currentSubscriptionIndex].subscriptionId
                }
            ) {
                Text(
                    text = "SIM ${subscriptions[currentSubscriptionIndex].simSlotIndex + 1} - ${subscriptions[currentSubscriptionIndex].displayName}"
                )
            }
        }
    }
}