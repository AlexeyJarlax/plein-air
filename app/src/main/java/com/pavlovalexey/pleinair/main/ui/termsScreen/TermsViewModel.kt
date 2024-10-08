package com.pavlovalexey.pleinair.main.ui.termsScreen

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavlovalexey.pleinair.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TermsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    @Inject
    lateinit var sharedPreferences: SharedPreferences
    val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
    val termsOfPrivacy = context.getString(R.string.terms_of_privacy, context.getString(R.string.pleinair), currentDate)
    val termsOfAgreement = context.getString(R.string.terms_of_agreement, context.getString(R.string.pleinair), currentDate)

    var privacyPolicyContent by mutableStateOf(context.getString(R.string.load_privacy_policy))
        private set
    var userAgreementContent by mutableStateOf(context.getString(R.string.load_user_policy))
        private set

    var isTermsLoaded by mutableStateOf(false)
        private set

    init {
        loadAgreementTexts()
    }

    private fun loadAgreementTexts() {
        viewModelScope.launch {
            loadTextFromUrl(context.getString(R.string.privacy_policy_url)) { text ->
                privacyPolicyContent = text
                checkTermsLoaded()
            }
            loadTextFromUrl(context.getString(R.string.user_agreement_url)) { text ->
                userAgreementContent = text
                checkTermsLoaded()
            }
        }
    }

    private suspend fun loadTextFromUrl(url: String, onTextLoaded: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val text = URL(url).readText()
                withContext(Dispatchers.Main) {
                    onTextLoaded(text)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val fallbackText = when (url) {
                        context.getString(R.string.privacy_policy_url) -> context.getString(R.string.pp_reserv)
                        context.getString(R.string.user_agreement_url) -> context.getString(R.string.ua_reserv)
                        else -> "Ошибка загрузки текста соглашений. Подписание невозможно."
                    }
                    onTextLoaded(fallbackText)
                }
            }
        }
    }

    private fun checkTermsLoaded() {
        isTermsLoaded = privacyPolicyContent.length > 100 && userAgreementContent.length > 100
    }

    fun markAsSigned(accepted: Boolean) {
        Log.d("TermsActivity", "Saving terms accepted: $accepted")
        with(sharedPreferences.edit()) {
            putBoolean("all_terms_accepted", accepted)
            apply()
        }
    }

    fun checkIfSigned(): Boolean {
        return sharedPreferences.getBoolean("all_terms_accepted", false)
    }
}