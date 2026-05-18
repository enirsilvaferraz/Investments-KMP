package com.eferraz.asset_management.dialog

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.KoinViewModel

internal data class DialogState(
    val isOpen: Boolean = true,
    val holdingId: Long? = null,
)

internal sealed class DialogEvents {
    data object Dismiss : DialogEvents()
}

@KoinViewModel
internal class DialogViewModel : ViewModel() {

    internal val state: StateFlow<DialogState> field = MutableStateFlow(DialogState())

    internal fun dispatch(event: DialogEvents) = when (event) {
        DialogEvents.Dismiss -> state.update { it.copy(isOpen = false) }
    }
}
