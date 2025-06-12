package com.rafiarya0114.katalogmotor.ui.screen

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.rafiarya0114.katalogmotor.R
import com.rafiarya0114.katalogmotor.model.Katalog

@Composable
fun DisplayAlertDialog(
    katalog: Katalog,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.hapus_katalog_title))
        },
        text = {
            Text(text = stringResource(R.string.hapus_katalog_body, katalog.judul))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.hapus))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.batal))
            }
            }
        )
}