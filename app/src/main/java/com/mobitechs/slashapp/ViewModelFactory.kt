package com.mobitechs.slashapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobitechs.slashapp.data.repository.AuthRepository
import com.mobitechs.slashapp.ui.viewmodels.AuthOtpVerificationViewModel
import com.mobitechs.slashapp.ui.viewmodels.AuthPhoneViewModel
import com.mobitechs.slashapp.ui.viewmodels.AuthRegisterViewModel
import com.mobitechs.slashapp.ui.viewmodels.BottomMenuRewardViewModel
import com.mobitechs.slashapp.ui.viewmodels.BottomMenuScanViewModel
import com.mobitechs.slashapp.ui.viewmodels.BottomMenuStoreViewModel
import com.mobitechs.slashapp.ui.viewmodels.BottomMenuTransactionViewModel
import com.mobitechs.slashapp.ui.viewmodels.HomeViewModel
import com.mobitechs.slashapp.ui.viewmodels.SplashViewModel


class ViewModelFactory(

    private val context: Context,
    private val authRepository: AuthRepository,
) : ViewModelProvider.Factory {


    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SplashViewModel::class.java) -> {
                SplashViewModel(authRepository) as T
            }

            modelClass.isAssignableFrom(AuthPhoneViewModel::class.java) -> {
                AuthPhoneViewModel(authRepository) as T
            }

            modelClass.isAssignableFrom(AuthOtpVerificationViewModel::class.java) -> {
                AuthOtpVerificationViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(AuthRegisterViewModel::class.java) -> {
                AuthRegisterViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel() as T
            }

            modelClass.isAssignableFrom(BottomMenuTransactionViewModel::class.java) -> {
                BottomMenuTransactionViewModel() as T
            }

            modelClass.isAssignableFrom(BottomMenuScanViewModel::class.java) -> {
                BottomMenuScanViewModel() as T
            }

            modelClass.isAssignableFrom(BottomMenuRewardViewModel::class.java) -> {
                BottomMenuRewardViewModel() as T
            }

            modelClass.isAssignableFrom( BottomMenuStoreViewModel::class.java) -> {
                BottomMenuStoreViewModel() as T
            }


            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}