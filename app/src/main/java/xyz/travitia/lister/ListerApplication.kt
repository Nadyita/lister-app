package xyz.travitia.lister

import android.app.Application
import xyz.travitia.lister.data.preferences.SettingsPreferences
import xyz.travitia.lister.data.repository.ListerRepository

class ListerApplication : Application() {

    lateinit var settingsPreferences: SettingsPreferences
    lateinit var repository: ListerRepository

    override fun onCreate() {
        super.onCreate()
        settingsPreferences = SettingsPreferences(this)
        repository = ListerRepository(settingsPreferences)
    }
}

