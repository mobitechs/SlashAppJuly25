package com.mobitechs.slashapp

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.mobitechs.slashapp.data.api.ApiService
import com.mobitechs.slashapp.data.api.AuthInterceptor
import com.mobitechs.slashapp.data.api.RetrofitClient
import com.mobitechs.slashapp.data.local.SharedPrefsManager
import com.mobitechs.slashapp.data.repository.AuthRepository
import com.razorpay.Checkout

class SlashApp : Application() {

    companion object {
        private const val TAG = "slashApp"
        private const val PREFS_FRESH_INSTALL = "fresh_install_prefs"
        private const val KEY_INSTALL_ID = "install_id"
    }

    // Lazily instantiated dependencies
    val gson by lazy { Gson() }
    val sharedPrefsManager by lazy { SharedPrefsManager(applicationContext, gson) }
    val authInterceptor by lazy { AuthInterceptor(sharedPrefsManager) }
    val apiService by lazy {
        RetrofitClient.getRetrofitInstance(authInterceptor).create(ApiService::class.java)
    }


    lateinit var appContext: Context

    val authRepository by lazy { AuthRepository(apiService, sharedPrefsManager) }


    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        // Check for fresh install and clear data if needed
        checkAndHandleFreshInstall()

        // Initialize Razorpay SDK
        Checkout.preload(applicationContext)
    }

    private fun checkAndHandleFreshInstall() {
        try {
            val prefs = getSharedPreferences(PREFS_FRESH_INSTALL, Context.MODE_PRIVATE)
            val packageInfo = packageManager.getPackageInfo(packageName, 0)

            // Use first install time as unique identifier
            val currentInstallId = packageInfo.firstInstallTime.toString()
            val savedInstallId = prefs.getString(KEY_INSTALL_ID, null)

            if (savedInstallId == null || savedInstallId != currentInstallId) {
                Log.d(TAG, "Fresh install detected. Clearing all data...")

                // This is a fresh install or reinstall
                clearAllAppData()

                // Save the new install ID
                prefs.edit().putString(KEY_INSTALL_ID, currentInstallId).apply()

                Log.d(TAG, "All data cleared for fresh install")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking fresh install", e)
        }
    }

    private fun clearAllAppData() {
        // Clear all SharedPreferences
        clearAllSharedPreferences()

        // Clear cache
        clearCache()

        // Clear internal storage
        clearInternalStorage()
    }

    private fun clearAllSharedPreferences() {
        try {
            val prefsDir = java.io.File(applicationInfo.dataDir, "shared_prefs")
            if (prefsDir.exists() && prefsDir.isDirectory) {
                prefsDir.listFiles()?.forEach { file ->
                    // Don't clear the fresh install tracking preferences
                    if (file.name != "$PREFS_FRESH_INSTALL.xml") {
                        val prefName = file.name.removeSuffix(".xml")
                        getSharedPreferences(prefName, Context.MODE_PRIVATE)
                            .edit()
                            .clear()
                            .apply()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing shared preferences", e)
        }
    }

    private fun clearCache() {
        try {
            cacheDir.deleteRecursively()
            codeCacheDir?.deleteRecursively()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache", e)
        }
    }

    private fun clearInternalStorage() {
        try {
            filesDir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    file.deleteRecursively()
                } else {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing internal storage", e)
        }
    }
}