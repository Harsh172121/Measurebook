package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("kalanidhan_prefs", Context.MODE_PRIVATE)

    private val _shopName = MutableStateFlow(prefs.getString("shop_name", "Kalanidhan") ?: "Kalanidhan")
    val shopName: StateFlow<String> = _shopName

    private val _ownerName = MutableStateFlow(prefs.getString("owner_name", "") ?: "")
    val ownerName: StateFlow<String> = _ownerName

    private val _shopPhone = MutableStateFlow(prefs.getString("shop_phone", "") ?: "")
    val shopPhone: StateFlow<String> = _shopPhone

    private val _shopAddress = MutableStateFlow(prefs.getString("shop_address", "") ?: "")
    val shopAddress: StateFlow<String> = _shopAddress

    private val _language = MutableStateFlow(prefs.getString("language", "en") ?: "en")
    val language: StateFlow<String> = _language

    private val _theme = MutableStateFlow(prefs.getString("theme", "system") ?: "system")
    val theme: StateFlow<String> = _theme

    fun saveShopInfo(name: String, owner: String, phone: String, address: String) {
        prefs.edit().apply {
            putString("shop_name", name)
            putString("owner_name", owner)
            putString("shop_phone", phone)
            putString("shop_address", address)
            apply()
        }
        _shopName.value = name
        _ownerName.value = owner
        _shopPhone.value = phone
        _shopAddress.value = address
    }

    fun saveLanguage(lang: String) {
        prefs.edit().putString("language", lang).apply()
        _language.value = lang
    }

    fun saveTheme(theme: String) {
        prefs.edit().putString("theme", theme).apply()
        _theme.value = theme
    }
}
