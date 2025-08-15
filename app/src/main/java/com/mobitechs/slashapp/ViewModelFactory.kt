package com.mobitechs.slashapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobitechs.slashapp.data.repository.AuthRepository
import com.mobitechs.slashapp.data.repository.HomeRepository
import com.mobitechs.slashapp.data.repository.QRScannerRepository
import com.mobitechs.slashapp.data.repository.RewardsRepository
import com.mobitechs.slashapp.data.repository.SpinWheelRepository
import com.mobitechs.slashapp.data.repository.StoreRepository
import com.mobitechs.slashapp.data.repository.TransactionRepository
import com.mobitechs.slashapp.ui.viewmodels.AddReviewViewModel
import com.mobitechs.slashapp.ui.viewmodels.AuthOtpVerificationViewModel
import com.mobitechs.slashapp.ui.viewmodels.AuthPhoneViewModel
import com.mobitechs.slashapp.ui.viewmodels.AuthRegisterViewModel
import com.mobitechs.slashapp.ui.viewmodels.BottomMenuRewardViewModel
import com.mobitechs.slashapp.ui.viewmodels.BottomMenuScanViewModel
import com.mobitechs.slashapp.ui.viewmodels.BottomMenuStoreViewModel
import com.mobitechs.slashapp.ui.viewmodels.BottomMenuTransactionViewModel
import com.mobitechs.slashapp.ui.viewmodels.DailyRewardsViewModel
import com.mobitechs.slashapp.ui.viewmodels.HomeViewModel
import com.mobitechs.slashapp.ui.viewmodels.SplashViewModel
import com.mobitechs.slashapp.ui.viewmodels.StoreDetailsViewModel
import com.mobitechs.slashapp.ui.viewmodels.TransactionDetailsViewModel
import com.mobitechs.slashapp.ui.viewmodels.TransactionViewModel

//import com.mobitechs.slashapp.ui.viewmodels.TransactionViewModel


class ViewModelFactory(

    private val context: Context,
    private val authRepository: AuthRepository,
    private val qRScannerRepository: QRScannerRepository,
    private val homeRepository: HomeRepository,
    private val rewardsRepository: RewardsRepository,
    private val storeRepository: StoreRepository,
    private val transactionRepository: TransactionRepository,
    private val spinWheelRepository: SpinWheelRepository,
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
                HomeViewModel(homeRepository, authRepository) as T
            }

            modelClass.isAssignableFrom(BottomMenuTransactionViewModel::class.java) -> {
                BottomMenuTransactionViewModel(transactionRepository) as T
            }

            modelClass.isAssignableFrom(BottomMenuScanViewModel::class.java) -> {
                BottomMenuScanViewModel(qRScannerRepository) as T
            }

            modelClass.isAssignableFrom(BottomMenuRewardViewModel::class.java) -> {
                BottomMenuRewardViewModel(rewardsRepository) as T
            }

            modelClass.isAssignableFrom(BottomMenuStoreViewModel::class.java) -> {
                BottomMenuStoreViewModel(homeRepository, storeRepository) as T
            }

            modelClass.isAssignableFrom(TransactionViewModel::class.java) -> {
                TransactionViewModel(qRScannerRepository, authRepository) as T
            }

            modelClass.isAssignableFrom(StoreDetailsViewModel::class.java) -> {
                StoreDetailsViewModel(storeRepository) as T
            }

            modelClass.isAssignableFrom(AddReviewViewModel::class.java) -> {
                AddReviewViewModel(storeRepository) as T
            }

            modelClass.isAssignableFrom(TransactionDetailsViewModel::class.java) -> {
                TransactionDetailsViewModel(transactionRepository) as T
            }
            modelClass.isAssignableFrom(DailyRewardsViewModel::class.java) -> {
                DailyRewardsViewModel(spinWheelRepository) as T
            }


            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}