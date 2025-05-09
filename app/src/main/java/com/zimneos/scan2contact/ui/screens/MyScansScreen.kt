package com.zimneos.scan2contact.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults.filterChipColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zimneos.scan2contact.R
import com.zimneos.scan2contact.ui.comps.ScanItem
import com.zimneos.scan2contact.core.formatDateTime
import com.zimneos.scan2contact.ui.dialogs.ClearAllProfilesDialog
import com.zimneos.scan2contact.viewmodel.RecentScansViewModel
import com.zimneos.scan2contact.viewmodel.RecentScansViewModelFactory
import com.zimneos.scan2contact.viewmodel.UserProfile
import java.time.LocalDateTime

@Composable
fun MyScansScreen(
    navigateToContactScreen: (scannedImageUri: Uri) -> Unit,
    navigateToBackScreen: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("") }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val recentScansViewModel: RecentScansViewModel = viewModel(
        factory = RecentScansViewModelFactory(context)
    )
    val userProfiles by recentScansViewModel.userProfiles.collectAsState()
    val designations by recentScansViewModel.designations.collectAsState()
    var selectedDesignations = emptyList<String>()

    LaunchedEffect(selectedFilter) {
        when (selectedFilter) {
            "Today" -> {
                val now = LocalDateTime.now()
                recentScansViewModel.filterProfiles(
                    selectedDesignations = selectedDesignations,
                    startDate = now.withHour(0).withMinute(0).withSecond(0).withNano(0),
                    endDate = now.withHour(23).withMinute(59).withSecond(59).withNano(999999999)
                )
            }

            "Yesterday" -> {
                val yesterday = LocalDateTime.now().minusDays(1)
                recentScansViewModel.filterProfiles(
                    selectedDesignations = selectedDesignations,
                    startDate = yesterday.withHour(0).withMinute(0).withSecond(0).withNano(0),
                    endDate = yesterday.withHour(23).withMinute(59).withSecond(59)
                        .withNano(999999999)
                )
            }

            "This Week" -> {
                val now = LocalDateTime.now()
                val startOfWeek = now.minusDays(now.dayOfWeek.value.toLong() - 1)
                recentScansViewModel.filterProfiles(
                    selectedDesignations = selectedDesignations,
                    startDate = startOfWeek.withHour(0).withMinute(0).withSecond(0).withNano(0),
                    endDate = now.withHour(23).withMinute(59).withSecond(59).withNano(999999999)
                )
            }

            "" -> {
                recentScansViewModel.filterProfiles(
                    selectedDesignations = selectedDesignations,
                    startDate = null,
                    endDate = null,
                )
            }
        }
    }

    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFE6F1FF)
                        )
                    )
                )
                .padding(paddingValues)
                .padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.Top
        ) {
            item {
                MyScansHeader(
                    designations = designations, recentScansViewModel = recentScansViewModel,
                    selectedFilter = selectedFilter,
                    selectedDesignations = { selectedDesignations = it }
                )
            }
            item {
                Column(modifier = Modifier.padding(start = 18.dp, end = 2.dp)) {
                    LazyRow(
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .offset(x = (-8).dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            AnimatedVisibility(
                                modifier = Modifier.padding(start = 8.dp, end = 4.dp),
                                visible = selectedFilter.isNotEmpty(),
                                enter = scaleIn(animationSpec = tween(500)),
                                exit = scaleOut(animationSpec = tween(500))
                            ) {
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedFilter = ""
                                    },
                                    label = { Text("") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear filter",
                                            tint = Color(0xFF1E88E5),
                                            modifier = Modifier
                                                .size(34.dp)
                                                .padding(2.dp)
                                        )
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    colors = filterChipColors(
                                        containerColor = Color(0xFFF5F9FF),
                                        labelColor = Color(0xFF1565C0),
                                        selectedContainerColor = Color(0xFFF5F9FF),
                                        selectedLabelColor = Color(0xFF1565C0)
                                    )
                                )
                            }
                        }

                        items(listOf("Today", "Yesterday", "This Week")) { filter ->
                            val isSelected = filter == selectedFilter
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedFilter = if (isSelected) "" else filter
                                },
                                label = {
                                    Text(
                                        text = filter,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 6.dp
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color(0xFF000000) else Color(
                                            0xFF000000
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .animateContentSize(
                                        animationSpec = tween(
                                            durationMillis = 500,
                                            easing = FastOutSlowInEasing
                                        )
                                    ),
                                colors = filterChipColors(
                                    containerColor = Color(0xFFE8F0FE),
                                    labelColor = Color(0xFF1565C0),
                                    selectedContainerColor = Color(0xFF2073CC),
                                    selectedLabelColor = Color(0xFFFFFFFF)
                                )
                            )
                        }
                    }
                }
            }
            if (userProfiles.isEmpty()) {
                item {
                    NoDataMessage(
                        message = "No data available with the selected filters"
                    )
                }
            }

            if (userProfiles.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                        RecentScansWithFilters(
                            userProfile = userProfiles.reversed(),
                            onRecentScanClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                navigateToContactScreen(it)
                            },
                            viewModel = recentScansViewModel
                        )
                    }
                }
            }

            if (userProfiles.isNotEmpty()) {
                item {
                    ClearAllButton(
                        recentScansViewModel = recentScansViewModel,
                        navigateToBackScreen = { navigateToBackScreen() }
                    )
                }
            }
        }
    }
}

@Composable
private fun NoDataMessage(
    modifier: Modifier = Modifier,
    message: String
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 150.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val scale by animateFloatAsState(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
        )
        Icon(
            imageVector = Icons.Default.FilterAltOff,
            contentDescription = "No Data Icon",
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .padding(12.dp)
                .background(
                    Color(0xFF4A90E2).copy(alpha = 0.1f),
                    CircleShape
                ),
            tint = Color(0xFF4A90E2)
        )
        val alpha by animateFloatAsState(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 38.sp, fontWeight = FontWeight.Medium
            ),
            color = Color(0xFF333333),
            textAlign = TextAlign.Center,
            fontFamily = FontFamily(Font(R.font.caveat)),
            modifier = Modifier
                .alpha(alpha)
                .padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun ClearAllButton(
    recentScansViewModel: RecentScansViewModel,
    navigateToBackScreen: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        ClearAllProfilesDialog(
            showDialog = { showDialog = it },
            recentScansViewModel =
            recentScansViewModel,
            navigateToBackScreen = navigateToBackScreen
        )
    }

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Button(
            onClick = { showDialog = true },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 80.dp)
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6265FE))
        ) {
            Text(
                text = "Clear all",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentScansWithFilters(
    userProfile: List<UserProfile>,
    onRecentScanClick: (Uri) -> Unit,
    viewModel: RecentScansViewModel
) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        userProfile.forEach {
            it.name?.let { name ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    ScanItem(
                        name = name,
                        title = it.designation ?: "",
                        company = it.phone ?: "",
                        time = formatDateTime(it.scannedTime),
                        statusColor = Color(0xFF6265FE),
                        onRecentScanClick = { it.scannedImage?.let { uri -> onRecentScanClick(uri) } },
                        onDelete = { viewModel.deleteUserProfile(it) },
                        isSwipeable = true
                    )
                }
            }
        }
    }
}

@Composable
private fun MyScansHeader(
    designations: List<String>, recentScansViewModel: RecentScansViewModel, selectedFilter: String,
    selectedDesignations: (List<String>) -> Unit
) {
    var isDropdownVisible by remember { mutableStateOf(false) }
    val checkedStates = remember {
        mutableStateMapOf<String, Boolean>().apply {
            designations.forEach { put(it, false) }
        }
    }
    val checkedItems = checkedStates.filter { it.value }.keys.toList()
    selectedDesignations(checkedItems)

    val now = LocalDateTime.now()
    val startDate = when (selectedFilter) {
        "Today" -> now.withHour(0).withMinute(0).withSecond(0).withNano(0)
        "Yesterday" -> now.minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
        "This Week" -> now.minusDays(now.dayOfWeek.value.toLong() - 1).withHour(0).withMinute(0)
            .withSecond(0).withNano(0)

        else -> null
    }
    val endDate = when (selectedFilter) {
        "Today" -> now.withHour(23).withMinute(59).withSecond(59).withNano(999999999)
        "Yesterday" -> now.minusDays(1).withHour(23).withMinute(59).withSecond(59)
            .withNano(999999999)

        "This Week" -> now.withHour(23).withMinute(59).withSecond(59).withNano(999999999)
        else -> null
    }

    Column(modifier = Modifier.padding(horizontal = 18.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Scans",
                fontFamily = FontFamily(Font(R.font.lancelot)),
                fontSize = 48.sp,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Box(modifier = Modifier.padding(bottom = 8.dp)) {
                IconButton(onClick = { isDropdownVisible = true }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        modifier = Modifier.size(32.dp)
                    )
                }
                DropdownMenu(
                    expanded = isDropdownVisible,
                    onDismissRequest = { isDropdownVisible = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    designations.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
                                    Checkbox(
                                        checked = checkedStates[option] ?: false,
                                        onCheckedChange = { isChecked ->
                                            checkedStates[option] = isChecked
                                            val selectedDesignationsList = checkedStates
                                                .filter { it.value }
                                                .keys
                                                .toList()
                                            recentScansViewModel.filterProfiles(
                                                selectedDesignationsList,
                                                startDate,
                                                endDate
                                            )
                                        }
                                    )
                                    Text(text = option, modifier = Modifier.padding(end = 12.dp))
                                }
                            },
                            onClick = {
                                checkedStates[option] = !(checkedStates[option] ?: false)
                                val selectedDesignationsList = checkedStates
                                    .filter { it.value }
                                    .keys
                                    .toList()
                                recentScansViewModel.filterProfiles(
                                    selectedDesignationsList,
                                    startDate,
                                    endDate
                                )
                            },
                            contentPadding = PaddingValues(2.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}
