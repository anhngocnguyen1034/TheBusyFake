package com.example.thebusysimulator.presentation.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LanguageManager {
    
    // Supported languages
    enum class Language(val code: String, val displayName: String) {
        ENGLISH("en", "English"),
        VIETNAMESE("vi", "Tiếng Việt"),
        SPANISH("es", "Español"),
        FRENCH("fr", "Français"),
        GERMAN("de", "Deutsch"),
        ITALIAN("it", "Italiano"),
        PORTUGUESE("pt", "Português"),
        RUSSIAN("ru", "Русский"),
        JAPANESE("ja", "日本語"),
        KOREAN("ko", "한국어"),
        CHINESE_SIMPLIFIED("zh-CN", "简体中文"),
        CHINESE_TRADITIONAL("zh-TW", "繁體中文"),
        ARABIC("ar", "العربية"),
        HINDI("hi", "हिन्दी"),
        TURKISH("tr", "Türkçe"),
        POLISH("pl", "Polski"),
        DUTCH("nl", "Nederlands"),
        INDONESIAN("id", "Bahasa Indonesia"),
        THAI("th", "ไทย"),
        GREEK("el", "Ελληνικά"),
        CZECH("cs", "Čeština"),
        SWEDISH("sv", "Svenska"),
        NORWEGIAN("no", "Norsk"),
        FINNISH("fi", "Suomi");
        
        companion object {
            fun fromCode(code: String): Language? {
                return values().find { it.code == code }
            }
        }
    }
    
    /**
     * Set app language
     */
    fun setLanguage(context: Context, languageCode: String): Context {
        val locale = when (languageCode) {
            "zh-rCN" -> Locale.SIMPLIFIED_CHINESE
            "zh-rTW" -> Locale.TRADITIONAL_CHINESE
            else -> Locale(languageCode)
        }
        
        val config = Configuration(context.resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            return context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            return context
        }
    }
    
    /**
     * Get current language code
     */
    fun getCurrentLanguage(context: Context): String {
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
        
        return locale.language + if (locale.country.isNotEmpty()) "-r${locale.country}" else ""
    }
    
    /**
     * Get all supported languages
     */
    fun getSupportedLanguages(): List<Language> {
        return Language.values().toList()
    }
}
