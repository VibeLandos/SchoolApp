package com.arzabc.school.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arzabc.school.R
import com.arzabc.school.update.GitHubUpdate
import com.arzabc.school.update.RemoteRelease
import com.arzabc.school.ui.theme.LocalGlassTokens
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private sealed interface UpdateUi {
    data object Checking : UpdateUi
    data object Current : UpdateUi
    data object None : UpdateUi
    data class Available(val release: RemoteRelease) : UpdateUi
    data class Downloading(val release: RemoteRelease) : UpdateUi
    data class Ready(val file: File) : UpdateUi
    data object Failed : UpdateUi
}

@Composable
fun AppUpdateDialog(
    onDismiss: () -> Unit,
    knownRelease: RemoteRelease? = null,
    onlyIfNewer: Boolean = false,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val glass = isGlassStyle()
    val tokens = LocalGlassTokens.current
    val scheme = MaterialTheme.colorScheme
    var ui by remember {
        mutableStateOf<UpdateUi>(
            if (knownRelease != null) UpdateUi.Available(knownRelease) else UpdateUi.Checking,
        )
    }

    LaunchedEffect(knownRelease) {
        if (knownRelease != null) {
            ui = UpdateUi.Available(knownRelease)
            return@LaunchedEffect
        }
        val next = withContext(Dispatchers.IO) {
            runCatching { GitHubUpdate.fetchLatest() }.fold(
                onSuccess = { remote ->
                    when {
                        remote == null -> UpdateUi.None
                        GitHubUpdate.isNewer(remote) -> UpdateUi.Available(remote)
                        else -> UpdateUi.Current
                    }
                },
                onFailure = { UpdateUi.Failed },
            )
        }
        if (onlyIfNewer && next !is UpdateUi.Available) {
            onDismiss()
            return@LaunchedEffect
        }
        ui = next
    }

    if (onlyIfNewer && ui is UpdateUi.Checking) return
    if (onlyIfNewer && ui !is UpdateUi.Available && ui !is UpdateUi.Downloading && ui !is UpdateUi.Ready) {
        return
    }

    val body = when (val state = ui) {
        UpdateUi.Checking -> stringResource(R.string.update_checking)
        UpdateUi.Current -> stringResource(R.string.update_current)
        UpdateUi.None -> stringResource(R.string.update_none)
        is UpdateUi.Available -> stringResource(R.string.update_offer, state.release.versionName)
        is UpdateUi.Downloading -> stringResource(R.string.update_downloading)
        is UpdateUi.Ready -> stringResource(R.string.update_ready)
        UpdateUi.Failed -> stringResource(R.string.update_failed)
    }
    val titleColor = if (glass) tokens.text else scheme.onSurface
    val textColor = if (glass) tokens.textSecondary else scheme.onSurfaceVariant
    val container = if (glass) tokens.card else scheme.surfaceContainerHigh
    val accent = if (glass) tokens.accent else scheme.primary
    val onAccent = if (glass) androidx.compose.ui.graphics.Color.White else scheme.onPrimary

    AlertDialog(
        onDismissRequest = { if (ui !is UpdateUi.Downloading) onDismiss() },
        containerColor = container,
        titleContentColor = titleColor,
        textContentColor = textColor,
        title = { Text(stringResource(R.string.update_title)) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(body, color = textColor)
                if (ui is UpdateUi.Checking || ui is UpdateUi.Downloading) {
                    Spacer(Modifier.height(16.dp))
                    CircularProgressIndicator(color = accent)
                }
            }
        },
        confirmButton = {
            when (val state = ui) {
                is UpdateUi.Available -> Button(
                    onClick = {
                        ui = UpdateUi.Downloading(state.release)
                        scope.launch {
                            ui = withContext(Dispatchers.IO) {
                                runCatching {
                                    GitHubUpdate.downloadApk(context, state.release.apkUrl)
                                }.fold(
                                    onSuccess = { UpdateUi.Ready(it) },
                                    onFailure = { UpdateUi.Failed },
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = onAccent,
                    ),
                ) {
                    Text(stringResource(R.string.update_download))
                }
                is UpdateUi.Ready -> Button(
                    onClick = {
                        if (!GitHubUpdate.canInstall(context)) {
                            GitHubUpdate.openInstallPermissionSettings(context)
                        } else {
                            GitHubUpdate.installApk(context, state.file)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = onAccent,
                    ),
                ) {
                    Text(stringResource(R.string.update_install))
                }
                else -> Unit
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = ui !is UpdateUi.Downloading,
            ) {
                Text(
                    text = stringResource(
                        if (ui is UpdateUi.Available) R.string.update_later else R.string.update_close,
                    ),
                    color = titleColor,
                )
            }
        },
    )
}
