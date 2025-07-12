package com.mobitechs.slashapp.utils



object Constants {
    // API related
//    const val BASE_URL = "https://mobitechs.in/mobitech_laravel_classmate/public/api/"
//    const val BASE_URL = "https://slash-node-api.onrender.com/"
//    const val BASE_URL = "http://localhost:3000/api/v1/"
    const val BASE_URL = "http://192.168.31.252:3000/api/v1/"
//    const val BASE_URL = "http://10.1.3.151:3000/api/v1/"
    const val API_DOMAIN = "mobitechs.in"

    // Certificate pinning (example value - should be replaced with actual certificate hash)
    const val CERTIFICATE_PIN = "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="

    // Shared Preferences
    const val ENCRYPTED_PREFS_FILE_NAME = "slash_app_secure_prefs"
    const val PREF_AUTH_TOKEN = "auth_token"
    const val PREF_USER = "user_data"


    // Timeouts
    const val NETWORK_TIMEOUT = 30L

    // Validation
    const val MIN_PHONE_LENGTH = 10
    const val MAX_PHONE_LENGTH = 10
    const val OTP_LENGTH = 6
    const val MAX_NAME_LENGTH = 50

    // QR Scanner
    const val QR_SCAN_RESULT_OK = "qr_scan_ok"
    const val QR_SCAN_RESULT_ERROR = "qr_scan_error"

    const val QR_DETECTION_THROTTLE_MS = 2000L

}

