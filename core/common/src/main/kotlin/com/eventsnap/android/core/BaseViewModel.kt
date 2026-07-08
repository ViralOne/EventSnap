package com.eventsnap.android.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Base MVI view model. Subclasses provide an initial [State] and an [onAction] handler.
 * State is exposed as a hot [StateFlow]; side effects flow through a single-consumer [Channel].
 */
abstract class BaseViewModel<State : ViewState, Action : ViewAction, Effect : ViewSideEffect>(
    initialState: State,
) : ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _actions = MutableSharedFlow<Action>(extraBufferCapacity = 64)

    private val _effects = Channel<Effect>(Channel.BUFFERED)
    val effects: Flow<Effect> = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            _actions.onEach(::onAction).collect()
        }
    }

    /** Subclasses override to react to user actions (mutate state, fire effects, etc.). */
    protected abstract suspend fun onAction(action: Action)

    /** Send an action from the UI. Non-suspending — actions are buffered. */
    fun setAction(action: Action) {
        _actions.tryEmit(action)
    }

    /** Replace state. */
    protected fun setState(reduce: State.() -> State) {
        _state.value = _state.value.reduce()
    }

    /** Emit a side effect. */
    protected suspend fun setEffect(effect: Effect) {
        _effects.send(effect)
    }
}

private suspend fun <T> Flow<T>.collect() = collect {}
