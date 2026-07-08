package com.eventsnap.android.core.ui.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

/**
 * Collects a ViewModel's one-shot effect [Flow] while the screen is at least STARTED and
 * routes each effect through [onEffect]. Used by every `<Feature>Screen`.
 */
@Composable
fun <Effect> HandleEffects(
    effects: Flow<Effect>,
    onEffect: (Effect) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(effects, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            effects.collect(onEffect)
        }
    }
}
