package net.android.st069_fakecallphoneprank.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale


object LocaleHelper {

    private const val PREFS_NAME = "language_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    /**
     * Get saved language
     */
    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    }

    /**
     * Save selected language
     */
    fun setLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }

    /**
     * Apply locale to Context
     * Use in attachBaseContext()
     */
    fun setLocale(context: Context): Context {
        val language = getLanguage(context)
        return updateResources(context, language)
    }

    /**
     * Change language and update resources
     */
    fun changeLanguage(context: Context, languageCode: String) {
        setLanguage(context, languageCode)
        updateResources(context, languageCode)
    }

    /**
     * Update resources with new language
     */
    private fun updateResources(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(
                configuration,
                context.resources.displayMetrics
            )
            context
        }
    }
}