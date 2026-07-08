package com.eventsnap.android.core.ui.mobile.dev

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eventsnap.android.core.data.env.EnvironmentConfig
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun EnvSelectorDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    environmentConfig: EnvironmentConfig = koinInject(),
) {
    val current by environmentConfig.apiBaseUrl.collectAsState()
    var selected by remember { mutableStateOf(current) }
    var customUrl by remember {
        mutableStateOf(
            if (selected !in setOf(environmentConfig.stagingUrl, environmentConfig.prodUrl)) selected else "",
        )
    }
    val scope = rememberCoroutineScope()

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text("Select API environment") },
        text = {
            Column {
                EnvOption("Staging", environmentConfig.stagingUrl, selected) { selected = environmentConfig.stagingUrl }
                EnvOption("Production", environmentConfig.prodUrl, selected) { selected = environmentConfig.prodUrl }
                EnvOption("Custom URL", customUrl, selected) { selected = customUrl }
                OutlinedTextField(
                    value = customUrl,
                    onValueChange = {
                        customUrl = it
                        if (selected !in setOf(environmentConfig.stagingUrl, environmentConfig.prodUrl)) selected = it
                    },
                    label = { Text("Custom base URL") },
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                scope.launch { environmentConfig.setApiBaseUrl(selected) }
                onDismiss()
            }) { Text("Apply") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun EnvOption(
    label: String,
    value: String,
    selected: String,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier.selectable(selected = selected == value, onClick = onSelect).padding(vertical = 4.dp),
    ) {
        RadioButton(selected = selected == value, onClick = onSelect)
        Text("$label  ($value)", modifier = Modifier.padding(start = 8.dp))
    }
}
