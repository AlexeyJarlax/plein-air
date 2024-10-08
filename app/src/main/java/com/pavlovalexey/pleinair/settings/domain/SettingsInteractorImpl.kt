package com.pavlovalexey.pleinair.settings.domain

import javax.inject.Inject

class SettingsInteractorImpl @Inject constructor(
    private val settingsRepository: SettingsRepository
) : SettingsInteractor {

    override fun loadNightMode(): Boolean {
        return settingsRepository.loadNightMode()
    }

    override fun saveNightMode(value: Boolean) {
        settingsRepository.saveNightMode(value)
    }

    override fun buttonToShareApp() {
        settingsRepository.buttonToShareApp()
    }

    override fun buttonToHelp() {
        settingsRepository.buttonToHelp()
    }

    override fun buttonToSeeUserAgreement() {
        settingsRepository.buttonToSeeUserAgreement()
    }

    override fun buttonToSeePrivacyPolicy() {
        settingsRepository.buttonToSeePrivacyPolicy()
    }

    override fun buttonDonat() {
        settingsRepository.buttonDonat()
    }

    override fun applyTheme() {
        settingsRepository.applyTheme()
    }

    override fun sharePlaylist(message: String) {
        settingsRepository.sharePlaylist(message)
    }

    override fun deleteUserAccount(onAccountDeleted: () -> Unit) {
        settingsRepository.deleteUserAccount(onAccountDeleted)
    }
}