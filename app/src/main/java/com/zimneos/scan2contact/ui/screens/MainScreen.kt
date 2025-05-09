package com.zimneos.scan2contact.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.zimneos.scan2contact.R
import com.zimneos.scan2contact.ui.comps.LoadingScreen
import com.zimneos.scan2contact.ui.comps.ScanItem
import com.zimneos.scan2contact.core.extractBusinessCardDetails
import com.zimneos.scan2contact.core.formatDateTime
import com.zimneos.scan2contact.ui.dialogs.NoBusinessCardFoundDialog
import com.zimneos.scan2contact.viewmodel.RecentScansViewModel
import com.zimneos.scan2contact.viewmodel.RecentScansViewModelFactory
import com.zimneos.scan2contact.viewmodel.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.SortedSet


@Composable
fun MainScreen(
    navigateToContactScreen: (scannedImageUri: Uri) -> Unit,
    navigateToRecentScreen: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val recentScansViewModel: RecentScansViewModel = viewModel(
        factory = RecentScansViewModelFactory(context)
    )
    val userProfiles = recentScansViewModel.userProfiles.collectAsState().value
    var scannedImageUri by remember { mutableStateOf<Uri?>(null) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val resultUri = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                    ?.pages
                    ?.get(0)
                    ?.imageUri
                scannedImageUri = resultUri
            }
        }
    )
    var showDialog by remember { mutableStateOf(false) }
    val isScanning = remember { mutableStateOf(false) }
    val allExtractedTextSentences: SnapshotStateList<String> = remember {
        mutableStateListOf<String>().also { list ->
            list.addAll(processTextSentences(emptyList()))
        }
    }
    LaunchedEffect(scannedImageUri) {
        scannedImageUri?.let { scannedImage ->
            allExtractedTextSentences.clear()
            isScanning.value = true
            extractTextFromImage(
                context, scannedImage,
                onResult = { extractedData ->
                    val nonEmptyFields = listOf(
                        extractedData.phone,
                        extractedData.phone2,
                        extractedData.phone3,
                        extractedData.email,
                        extractedData.website,
                        extractedData.designation,
                        extractedData.address
                    ).count { it.isNotEmpty() }
                    if (nonEmptyFields >= 2) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        recentScansViewModel.addUserProfile(
                            extractedData.toUserProfile(
                                detectedTexts = allExtractedTextSentences.toMutableList(),
                                scannedImage = scannedImage
                            )
                        )
                        isScanning.value = false
                        navigateToContactScreen(scannedImage)
                    } else {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDialog = true
                    }
                },
                allTextSentences = { texts ->
                    allExtractedTextSentences.addAll(texts)
                    texts.joinToString(" ")
                }
            )
        }
    }

    Scaffold { paddingValues ->
        if (showDialog) {
            NoBusinessCardFoundDialog {
                showDialog = false
            }
            if (isScanning.value) {
                isScanning.value = false
            }
        }
        if (isScanning.value) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LoadingScreen()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Top
            ) {
                item {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Scannect",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontFamily = FontFamily(
                                    Font(R.font.cookie_regular)
                                ),
                                fontWeight = FontWeight.Bold,
                                fontSize = 64.sp,
                                letterSpacing = 2.sp,
                                color = Color(0xFF6265FE)
                            ),
                            modifier = Modifier
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                item {
                    Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Scan Business Card",
                            fontSize = 42.sp,
                            fontFamily = FontFamily(
                                Font(R.font.lancelot)
                            ),
                            modifier = Modifier
                                .fillMaxWidth(),
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                        )
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                startDocumentScan(context) { intentSenderRequest ->
                                    scannerLauncher.launch(intentSenderRequest)
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 90.dp)
                                .padding(top = 8.dp, bottom = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6265FE)),
                            elevation = ButtonDefaults.elevatedButtonElevation(
                                defaultElevation = 8.dp,
                                pressedElevation = 12.dp,
                                disabledElevation = 0.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Scan Now",
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Scan Now",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .border(0.7.dp, Color.Gray, shape = RoundedCornerShape(8.dp)),
                            colors = CardColors(
                                containerColor = Color.White,
                                contentColor = Color.DarkGray,
                                disabledContainerColor = Color.White,
                                disabledContentColor = Color.DarkGray
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Quick Tips",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF333333)
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                val tips = listOf(
                                    "Ensure good lighting for better scan",
                                    "Place card on a contrasting background",
                                    "Hold your phone steady while scanning"
                                )

                                tips.forEachIndexed { _, tip ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircleOutline,
                                            contentDescription = "Tip Icon",
                                            tint = Color(0xFF6265FE),
                                            modifier = Modifier
                                                .padding(end = 8.dp)
                                                .size(20.dp)
                                        )
                                        Text(
                                            text = tip,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .border(0.7.dp, Color.Gray, shape = RoundedCornerShape(8.dp)),
                            colors = CardColors(
                                containerColor = Color.White,
                                contentColor = Color.DarkGray,
                                disabledContainerColor = Color.White,
                                disabledContentColor = Color.DarkGray
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Bonus Tip",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF333333)
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircleOutline,
                                        contentDescription = "Tip Icon",
                                        tint = Color(0xFF6265FE),
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .size(20.dp)
                                    )
                                    Text(
                                        text = "App works offline/No internet needed",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }
                }
                if (userProfiles.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                            Spacer(modifier = Modifier.height(16.dp))
                            RecentScansHeader(
                                onHeadingsClick = { navigateToRecentScreen() },
                                haptic
                            )
                            RecentScans(
                                userProfile = userProfiles,
                                onRecentScanClick = { navigateToContactScreen(it) },
                                haptic = haptic,
                                viewModel = recentScansViewModel
                            )
                            SeeMoreButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                navigateToRecentScreen()
                            })
                        }
                    }
                }
            }
        }
    }
}

private fun startDocumentScan(
    context: Context,
    onScannerReady: (IntentSenderRequest) -> Unit
) {
    val options = GmsDocumentScannerOptions.Builder()
        .setGalleryImportAllowed(true)
        .setPageLimit(1)
        .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
        .setScannerMode(GmsDocumentScannerOptions.CAPTURE_MODE_AUTO)
        .build()

    val scanner = GmsDocumentScanning.getClient(options)

    scanner.getStartScanIntent(context as Activity)
        .addOnSuccessListener { intentSender ->
            onScannerReady(IntentSenderRequest.Builder(intentSender).build())
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to start scanner", Toast.LENGTH_SHORT).show()
        }
}

@SuppressLint("UnrememberedMutableInteractionSource")
@Composable
private fun RecentScansHeader(onHeadingsClick: () -> Unit = {}, haptic: HapticFeedback) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onHeadingsClick()
                },
                indication = null,
                interactionSource = MutableInteractionSource()
            )
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Recent Scans",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            modifier = Modifier.size(26.dp),
            contentDescription = "Next",
            tint = Color(0xFF333333)
        )
    }
}

data class BusinessCardDetails(
    val name: String = "",
    val phone: String = "",
    val phone2: String = "",
    val phone3: String = "",
    val email: String = "",
    val website: String = "",
    val designation: String = "",
    val address: String = ""
)

fun extractTextFromImage(
    context: Context,
    uri: Uri,
    onResult: (BusinessCardDetails) -> Unit,
    allTextSentences: (MutableList<String>) -> Unit

) {
    try {
        val image: InputImage = InputImage.fromFilePath(context, uri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                Log.d("OCR", visionText.textBlocks.toString())

                CoroutineScope(Dispatchers.Default).launch {
                    val extractedData = extractBusinessCardDetails(visionText) { texts ->
                        allTextSentences(texts)
                    }

                    withContext(Dispatchers.Main) {
                        onResult(extractedData)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("OCR", "Text recognition failed", e)
            }
    } catch (e: Exception) {
        Log.e("OCR", "Error processing image", e)
    }
}

private fun processTextSentences(sentences: List<String>): SortedSet<String> {
    return sentences
        .map { sentence ->
            sentence.replace("[^a-zA-Z0-9+@-]".toRegex(), " ")
                .lowercase()
                .replace("\\s+".toRegex(), " ")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
        .filter { it.isNotBlank() }
        .toSortedSet()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentScans(
    userProfile: List<UserProfile>,
    onRecentScanClick: (Uri) -> Unit,
    haptic: HapticFeedback,
    viewModel: RecentScansViewModel
) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        val profiles =  userProfile.takeLast(3).reversed()
        profiles.forEach {
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
                        onRecentScanClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            it.scannedImage?.let { uri -> onRecentScanClick(uri) }
                        },
                        onDelete = {
                            viewModel.deleteUserProfile(userProfile = it)
                        },
                        isSwipeable = false
                    )
                }
            }
        }
    }
}


@Composable
private fun SeeMoreButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .background(Color.Transparent, shape = RoundedCornerShape(30.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF6265FE)
            ),
            elevation = ButtonDefaults.buttonElevation(2.dp)
        ) {
            Text(
                text = "See More >",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


private fun BusinessCardDetails.toUserProfile(
    detectedTexts: List<String> = emptyList(),
    scannedImage: Uri? = null,
    scannedTime: LocalDateTime? = LocalDateTime.now()
): UserProfile {
    return UserProfile(
        detectedTexts = detectedTexts,
        scannedImage = scannedImage,
        scannedTime = scannedTime,
        name = this.name,
        designation = this.designation,
        phone = this.phone,
        secondaryPhone = this.phone2,
        tertiaryPhone = this.phone3,
        email = this.email,
        address = this.address,
        website = this.website
    )
}