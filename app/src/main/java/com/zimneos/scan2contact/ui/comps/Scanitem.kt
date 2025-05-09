package com.zimneos.scan2contact.ui.comps

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zimneos.scan2contact.ui.dialogs.DeleteConfirmationDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalMaterial3Api
@Composable
fun ScanItem(
    name: String,
    title: String,
    company: String,
    time: String,
    statusColor: Color,
    onRecentScanClick: () -> Unit,
    onDelete: () -> Unit,
    isSwipeable: Boolean
) {
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                showDialog = true
                false
            } else {
                false
            }
        }
    )

    DeleteConfirmationDialog(
        showDialog = showDialog,
        onConfirm = {
            onDelete()
            scope.launch { dismissState.reset() }
        },
        onDismiss = {
            showDialog = false
            scope.launch { dismissState.reset() }
        }
    )

    if (isSwipeable) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                val color by animateColorAsState(
                    targetValue = when (dismissState.targetValue) {
                        SwipeToDismissBoxValue.EndToStart -> Color.Red
                        else -> Color.Transparent
                    },
                    label = "background color"
                )
                val scale by animateFloatAsState(
                    targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 1f else 0f,
                    label = "icon scale"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.scale(scale)
                    )
                }
            },
            content = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, shape = RoundedCornerShape(8.dp))
                        .clickable { onRecentScanClick() }
                        .padding(horizontal = 12.dp, vertical = 18.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(statusColor.copy(alpha = 0.1f), shape = CircleShape)
                            .align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Scan Now",
                            tint = statusColor,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = name, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (title.isEmpty()) company else "$title • $company",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = time, style = MaterialTheme.typography.bodySmall)
                }
            },
            enableDismissFromEndToStart = true,
            enableDismissFromStartToEnd = false
        )
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .border(1.dp, Color.LightGray, shape = RoundedCornerShape(8.dp))
                .clickable { onRecentScanClick() }
                .padding(horizontal = 12.dp, vertical = 18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(statusColor.copy(alpha = 0.1f), shape = CircleShape)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Scan Now",
                    tint = statusColor,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(34.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, fontWeight = FontWeight.Bold)
                Text(
                    text = if (title.isEmpty()) company else "$title • $company",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = time, style = MaterialTheme.typography.bodySmall)
        }
    }
}