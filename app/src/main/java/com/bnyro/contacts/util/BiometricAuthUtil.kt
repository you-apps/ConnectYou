package com.bnyro.contacts.util

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.bnyro.contacts.R
import com.bnyro.contacts.ui.activities.BaseActivity

object BiometricAuthUtil {
    private const val ALLOWED_AUTHENTICATORS =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

    @RequiresApi(Build.VERSION_CODES.P)
    fun requestAuth(context: Context, onResult: (Boolean) -> Unit) {
        val executor = ContextCompat.getMainExecutor(context)

        val biometricPrompt = BiometricPrompt(context as BaseActivity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onResult(false)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onResult(true)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onResult(false)
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.biometric_authentication))
            .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}