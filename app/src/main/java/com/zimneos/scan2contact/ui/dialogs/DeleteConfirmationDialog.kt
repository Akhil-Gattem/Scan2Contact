package com.zimneos.scan2contact.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.zimneos.scan2contact.R

@ExperimentalMaterial3Api
@Composable
fun DeleteConfirmationDialog(
    showDialog: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Confirm Deletion?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    fontFamily = FontFamily(
                        Font(R.font.caveat)
                    )
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this scan item.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    fontFamily = FontFamily(
                        Font(R.font.caveat)
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    }
                ) {
                    Text(
                        "Delete",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        fontFamily = FontFamily(
                            Font(R.font.regular)
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        "Cancel",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        fontFamily = FontFamily(
                            Font(R.font.regular)
                        )
                    )
                }
            }
        )
    }
}