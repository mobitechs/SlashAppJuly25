package com.mobitechs.slashapp.ui.viewmodels


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobitechs.slashapp.utils.showToast
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Interface for ViewModels that can show toast messages
 */
interface ToastEventHandler {
    val toastEvent: kotlinx.coroutines.flow.Flow<String>
    fun showToast(message: String)
}

/**
 * Mixin class that provides toast functionality to ViewModels
 */
class ToastEventMixin : ToastEventHandler {
    private val _toastEvent = Channel<String>(Channel.BUFFERED)
    override val toastEvent = _toastEvent.receiveAsFlow()

    override fun showToast(message: String) {
        _toastEvent.trySend(message)
    }
}

/**
 * Base ViewModel with toast functionality
 */
abstract class BaseViewModel : ViewModel(), ToastEventHandler {
    private val toastEventMixin = ToastEventMixin()

    override val toastEvent = toastEventMixin.toastEvent

    override fun showToast(message: String) {
        viewModelScope.launch {
            toastEventMixin.showToast(message)
        }
    }
}


/**
 * Observes toast events from a ViewModel and shows them
 */
@Composable
fun ToastObserver(viewModel: ToastEventHandler) {
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.toastEvent.collect { message ->
            showToast(context, message)
        }
    }
}