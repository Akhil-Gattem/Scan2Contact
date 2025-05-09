package com.zimneos.scan2contact.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.zimneos.scan2contact.R
import com.zimneos.scan2contact.viewmodel.RecentScansViewModel

@Composable
fun ClearAllProfilesDialog(
    showDialog: (Boolean) -> Unit,
    recentScansViewModel: RecentScansViewModel,
    navigateToBackScreen: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { showDialog(false) },
        title = {
            Text(
                text = "Clear All Profiles?",
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
                text = "Are you sure you want to delete all user profiles.",
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
                    recentScansViewModel.deleteAllUserProfiles()
                    navigateToBackScreen()
                    showDialog(false)
                }
            ) {
                Text(
                    text = "Confirm",
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
            TextButton(onClick = { showDialog(false) }) {
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