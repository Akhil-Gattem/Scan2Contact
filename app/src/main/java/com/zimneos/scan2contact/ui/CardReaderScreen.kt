package com.zimneos.scan2contact.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import kotlin.text.Regex
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddIcCall
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PermPhoneMsg
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ScreenSearchDesktop
import androidx.compose.material.icons.filled.Web
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.zimneos.scan2contact.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun DocumentScannerScreen() {
    val context = LocalContext.current
    var showScanner by remember { mutableStateOf(true) }
    var scannedImageUri by remember { mutableStateOf<Uri?>(null) }
    val scope = rememberCoroutineScope()
    val extractedText = remember { mutableStateOf<Map<String, String>?>(null) }
    val isScanning = remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val isFirstLaunch = remember { mutableStateOf(true) }
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(scannedImageUri) {
        scannedImageUri?.let {
            extractedText.value = null
            isScanning.value = true
            isFirstLaunch.value = false
            extractTextFromImage(context, it) { extractedData ->
                extractedText.value = extractedData
                isScanning.value = false
                showDialog.value = extractedData.isNotEmpty()
            }
        }
    }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val resultUri = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                    ?.pages
                    ?.get(0)
                    ?.imageUri
                scannedImageUri = resultUri
                showScanner = false
            }
        }
    )

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Scaffold(
                bottomBar = {
                    BottomNavigationBar(
                        onCameraClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            startDocumentScan(context) { intentSenderRequest ->
                                scannerLauncher.launch(intentSenderRequest)
                            }
                        },
                        onPhoneClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            extractedText.value?.let { contactInfo ->
                                contactInfo["Phone"]?.let { phoneNumber ->
                                    makePhoneCall(context, phoneNumber)
                                }
                            }
                        },
                        onChatClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            extractedText.value?.let { contactInfo ->
                                contactInfo["Phone"]?.let { phoneNumber ->
                                    openSmsApp(context, phoneNumber)
                                }
                            }
                        },
                        onContactsClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch {
                                showDialog.value = true
                            }
                        },
                        onShareClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            extractedText.value?.let { contactInfo ->
                                shareContact(context, contactInfo)
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Image(
                    painter = painterResource(R.drawable.bg),
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.1f),
                    contentDescription = "Background Image",
                    contentScale = ContentScale.FillHeight
                )
                if (showDialog.value) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    extractedText.value?.let { contactInfo ->
                        SaveContactDialog(contactInfo, context, showDialog)
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Top,
                ) {
                    item {
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.scannet),
                                    contentDescription = stringResource(R.string.app_name),
                                    modifier = Modifier.fillMaxWidth().padding(end = 78.dp),
                                    contentScale = ContentScale.FillWidth
                                )
                            }
                    }

                    if (scannedImageUri == null && !isScanning.value) {
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                        item {
                            InfoCard(
                                label = "Name:",
                                value = "Name will show here",
                                icon = Icons.Default.Person
                            )
                        }
                        item {
                            InfoCard(
                                label = "Designation:",
                                value = "Designation will show here",
                                icon = Icons.Default.Work
                            )
                        }
                        item {
                            InfoCard(
                                label = "Phone:",
                                value = "Phone number will show here",
                                icon = Icons.Default.Phone
                            )
                        }
                        item {
                            InfoCard(
                                label = "Secondary Phone:",
                                value = "Secondary Phone number will show here",
                                icon = Icons.Default.PermPhoneMsg
                            )
                        }
                        item {
                            InfoCard(
                                label = "Tertiary Phone:",
                                value = "Tertiary Phone number will show here",
                                icon = Icons.Default.AddIcCall
                            )
                        }
                        item {
                            InfoCard(
                                label = "Email:",
                                value = "Email will show here",
                                icon = Icons.Default.Email
                            )
                        }
                        item {
                            InfoCard(
                                label = "Address:",
                                value = "Address will show here",
                                icon = Icons.Default.LocationOn
                            )
                        }
                        item {
                            InfoCard(
                                label = "Website URL:",
                                value = "Web URL will show here",
                                icon = Icons.Default.Web
                            )
                        }
                    }

                    if (isScanning.value) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else {
                        val extractedData = extractedText.value ?: emptyMap()
                        when {
                            extractedData.isEmpty() -> {
                                if (!isScanning.value && !isFirstLaunch.value) {
                                    item {
                                        Spacer(modifier = Modifier.height(48.dp))
                                        NoBusinessCardMessage()
                                    }
                                }
                            }

                            else -> {
                                item { Spacer(modifier = Modifier.height(12.dp)) }
                                item {
                                    scannedImageUri?.let {
                                        Image(
                                            painter = rememberAsyncImagePainter(it),
                                            contentDescription = "Captured Image",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp)
                                                .aspectRatio(16 / 9f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(
                                                    2.dp,
                                                    Color(0xFF6265FE),
                                                    RoundedCornerShape(8.dp)
                                                )
                                        )
                                    }
                                }
                                item { Spacer(modifier = Modifier.height(24.dp)) }
                                extractedData["Name"]?.takeIf { it.isNotEmpty() }?.let {
                                    item {
                                        InfoCard(
                                            label = "Name:",
                                            value = it,
                                            icon = Icons.Default.Person,
                                            actionIcon = Icons.Default.ContentCopy,
                                            onActionClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                coroutineScope.launch {
                                                    clipboardManager.setText(AnnotatedString(it))
                                                }
                                            }
                                        )
                                    }
                                }
                                extractedData["Designation"]?.takeIf { it.isNotEmpty() }?.let {
                                    item {
                                        InfoCard(
                                            label = "Designation:",
                                            value = it,
                                            icon = Icons.Default.Work,
                                            actionIcon = Icons.Default.PersonSearch,
                                            onActionClick = {
                                                val searchQuery =
                                                    Uri.encode("${extractedData["Name"]} $it")
                                                val intent = Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse("https://www.google.com/search?q=$searchQuery")
                                                )
                                                context.startActivity(intent)
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        )
                                    }
                                }
                                extractedData["Phone"]?.takeIf { it.isNotEmpty() }?.let {
                                    item {
                                        InfoCard(
                                            label = "Phone:",
                                            value = it,
                                            icon = Icons.Default.Phone,
                                            actionIcon = Icons.Default.Whatsapp,
                                            onActionClick = {
                                                extractedData["Phone"]?.let { phoneNumber ->
                                                    openWhatsAppChat(context, phoneNumber)
                                                }
                                            }
                                        )
                                    }
                                }
                                extractedData["Phone2"]?.takeIf { it.isNotEmpty() }?.let {
                                    item {
                                        InfoCard(
                                            label = "Secondary Phone:",
                                            value = it,
                                            icon = Icons.Default.PermPhoneMsg,
                                            actionIcon = Icons.Default.Whatsapp,
                                            onActionClick = {
                                                extractedData["Phone2"]?.let { phoneNumber ->
                                                    openWhatsAppChat(context, phoneNumber)
                                                }
                                            }
                                        )
                                    }
                                }
                                extractedData["Phone3"]?.takeIf { it.isNotEmpty() }?.let {
                                    item {
                                        InfoCard(
                                            label = "Tertiary Phone:",
                                            value = it,
                                            icon = Icons.Default.AddIcCall,
                                            actionIcon = Icons.Default.Whatsapp,
                                            onActionClick = {
                                                extractedData["Phone3"]?.let { phoneNumber ->
                                                    openWhatsAppChat(context, phoneNumber)
                                                }
                                            }
                                        )
                                    }
                                }
                                extractedData["Email"]?.takeIf { it.isNotEmpty() }?.let {
                                    item {
                                        InfoCard(
                                            label = "Email:",
                                            value = it,
                                            icon = Icons.Default.Email,
                                            actionIcon = Icons.Default.Email,
                                            onActionClick = {
                                                context.startActivity(
                                                    Intent(
                                                        Intent.ACTION_VIEW,
                                                        Uri.parse("mailto:${extractedData["Email"]}")
                                                    )
                                                )
                                            }
                                        )
                                    }
                                }
                                extractedData["Address"]?.takeIf { it.isNotEmpty() }?.let {
                                    item {
                                        InfoCard(
                                            label = "Address:",
                                            value = it,
                                            icon = Icons.Default.LocationOn,
                                            actionIcon = Icons.Default.Navigation,
                                            onActionClick = {
                                                val uri = Uri.encode(extractedData["Address"])
                                                val mapIntent = Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse("https://www.google.com/maps/search/?api=1&query=$uri")
                                                )
                                                mapIntent.setPackage("com.google.android.apps.maps")
                                                if (mapIntent.resolveActivity(context.packageManager) != null) {
                                                    context.startActivity(mapIntent)
                                                } else {
                                                    context.startActivity(
                                                        Intent(
                                                            Intent.ACTION_VIEW,
                                                            Uri.parse("https://www.google.com/maps/search/?api=1&query=$uri")
                                                        )
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                                extractedData["Website"]?.takeIf { it.isNotEmpty() }?.let {
                                    item {
                                        InfoCard(
                                            label = "Website URL:",
                                            value = it,
                                            icon = Icons.Default.Web,
                                            actionIcon = Icons.Default.ScreenSearchDesktop,
                                            onActionClick = {
                                                val url = extractedData["Website"]?.let {
                                                    if (it.startsWith("http://") || it.startsWith("https://")) it else "https://$it"
                                                } ?: "https://www.google.com"

                                                try {
                                                    context.startActivity(
                                                        Intent(
                                                            Intent.ACTION_VIEW,
                                                            Uri.parse(url)
                                                        )
                                                    )
                                                } catch (e: Exception) {
                                                    Toast.makeText(
                                                        context,
                                                        "No browser found to open URL",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }

                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun NoBusinessCardMessage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.95f)),
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier.padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Business Card Detected",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun InfoCard(
    label: String,
    value: String,
    icon: ImageVector,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f) // Expands to take up available space
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color(0xFF6265FE),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = label,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF616161),
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = value,
                        fontSize = 14.sp,
                        color = Color(0xFF424242)
                    )
                }
            }
            if (actionIcon != null && onActionClick != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onActionClick) {
                        Icon(
                            imageVector = actionIcon,
                            contentDescription = "Action",
                            tint = Color(0xFF6265FE),
                            modifier = Modifier.size(24.dp)
                        )
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


private fun extractTextFromImage(context: Context, uri: Uri, onResult: (Map<String, String>) -> Unit) {
    try {
        val image: InputImage = InputImage.fromFilePath(context, uri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                Log.d("OCR", visionText.textBlocks.toString())

                CoroutineScope(Dispatchers.Default).launch {
                    val extractedData = extractBusinessCardDetails(visionText)

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


private fun extractBusinessCardDetails(visionText: Text): Map<String, String> {
    val details = mutableMapOf<String, String>()
    var isBusinessCard = false

    val phoneRegex = Regex("^\\+?\\d{7,15}$")
    val websiteRegex = Regex("^(https?://|www\\.)[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}$")
    val keywordRegex = Regex(
        "\\b(Phone|Mobile|Contact|Email|Website|Company|Address|Fax|CEO|Manager|Founder|CTO|Director|VP|Consultant|Designation|Title|Job Title)\\b",
        RegexOption.IGNORE_CASE
    )
    val designationTerms = listOf( // Using list of terms instead of Regex for Levenshtein
        "President",
        "Vice President",
        "VP",
        "Manager",
        "Developer",
        "Analyst",
        "Consultant",
        "Specialist",
        "Coordinator",
        "Executive",
        "Officer",
        "Associate",
        "Representative",
        "Clerk",
        "Technician",
        "Supervisor",
        "Agent",
        "Architect",
        "Designer",
        "Planner",
        "Accountant",
        "Auditor",
        "Librarian",
        "Pharmacist",
        "Therapist",
        "Instructor",
        "Professor",
        "Lecturer",
        "Teacher",
        "Tutor",
        "Counselor",
        "Advisor",
        "Coach",
        "Trainer",
        "Scientist",
        "Researcher",
        "Investigator",
        "Journalist",
        "Editor",
        "Writer",
        "Author",
        "Artist",
        "Musician",
        "Composer",
        "Performer",
        "Producer",
        "Director",
        "Engineer",
        "Analyst",
        "Consultant",
        "Specialist",
        "Coordinator",
        "Executive",
        "Officer",
        "Associate",
        "Representative",
        "Clerk",
        "Technician",
        "Supervisor",
        "Agent",
        "Architect",
        "Designer",
        "Planner",
        "Accountant",
        "Auditor",
        "Pharmacist",
        "Therapist",
        "Instructor",
        "Professor",
        "Lecturer",
        "Teacher",
        "Tutor",
        "Counselor",
        "Advisor",
        "Coach",
        "Trainer",
        "Intern",
        "Trainee",
        "Volunteer",
        "Assistant",
        "Secretary",
        "Receptionist",
        "Admin",
        "Administrator",
        "HR",
        "Marketing",
        "Sales",
        "Finance",
        "Operations",
        "Technology",
        "IT",
        "Legal",
        "Compliance",
        "Risk",
        "Audit",
        "Support",
        "Customer Service",
        "PR",
        "Communications",
        "Business Development",
        "BD",
        "R&D",
        "Research and Development",
        "General Manager",
        "GM",
        "Project Manager",
        "PM",
        "Product Manager",
        "Chief",
        "Chief Business",
        "Chief Business Development Officer",
        "Chief Marketing",
        "Chief Operations",
        "Chief Technology",
        "Chief Executive",
        "Lead",
        "Chief Executive Officer",
        "CEO",
        "Chief Technology Officer",
        "CTO",
        "Chief Financial Officer",
        "CFO",
        "Chief Marketing Officer",
        "CMO",
        "Chief Operating Officer",
        "COO",
        "Chief Information Officer",
        "CIO",
        "Founder",
        "Co-Founder",
        "Owner",
        "Partner",
        "Principal"
    )
    val addressRegex = Regex(
        "\\d{0,5}\\s?[\\w\\s\\-.]+\\s?(?:Street|St|Avenue|Door|Ave|Boulevard|Blvd|Road|Rd|Lane|Ln|Drive|Dr|Court|Ct|Square|Sq|Building|Suite|Floor|Apt|Apartment|Box|P\\.O\\. Box|Post Office Box|PO BOX|PW)\\b.*$",
        RegexOption.IGNORE_CASE
    )
    val postalCodeRegex = Regex("\\b\\d{5}(-\\d{4})?\\b")
    val cityStateRegex = Regex("\\b[A-Z][a-z]+,\\s?[A-Z]{2}\\b")
    val companyKeywordsRegex =
        Regex("\\b(Company|Organization|Corp|Inc|Ltd|GmbH|Pvt\\.? LTD)\\b", RegexOption.IGNORE_CASE)
    val indianStates = listOf(
        "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", "Goa",
        "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala",
        "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland",
        "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura",
        "Uttar Pradesh", "Uttarakhand", "West Bengal"
    )
    val countryNames = listOf(
        "india", "usa", "united states", "uk", "netherlands", "canada", "australia",
        "china", "japan", "france", "germany", "italy", "spain", "brazil", "mexico",
        "russia", "south africa", "singapore", "malaysia", "thailand", "indonesia",
        "philippines", "vietnam", "pakistan", "bangladesh", "egypt", "nigeria", "kenya",
        "south korea"
    )
    val popularIndianCities = listOf(
        "Mumbai", "Delhi", "Bangalore", "Hyderabad", "Ahmedabad", "Chennai", "Kolkata",
        "Surat", "Pune", "Jaipur", "Lucknow", "Kanpur", "Nagpur", "Indore", "Bhopal", "noida",
        "Patna", "Vadodara", "Ghaziabad", "Ludhiana", "Coimbatore", "Agra", "Visakhapatnam",
        "Kochi", "Madurai", "Varanasi", "Meerut", "Faridabad", "Rajkot", "Jamshedpur",
        "Srinagar", "Jabalpur", "Asansol", "Vasai-Virar City", "Allahabad", "Dhanbad",
        "Aurangabad", "Amritsar", "Jodhpur", "Ranchi", "Raipur", "Guwahati", "Solapur",
        "Hubli–Dharwad", "Chandigarh", "Tiruchirappalli", "Bareilly", "Moradabad", "Mysore",
        "Gurgaon", "Aligarh", "Jalandhar", "Jamshedpur", "Udaipur", "Kakinada", "Kollam",
        "Dehradun", "Vijayawada", "Varanasi", "Amritsar", "Aurangabad", "Bhubaneswar",
        "Jodhpur", "Raipur", "Guwahati", "Solapur", "Hubli–Dharwad", "Chandigarh",
        "Tiruchirappalli", "Bareilly", "Moradabad", "Mysore", "Gurgaon", "Aligarh",
        "Jalandhar", "Jamshedpur", "Udaipur", "Kakinada", "Dehradun",
        "Vijayawada"
    )

    val allCities = popularIndianCities + countryNames.map { it ->
        it.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }

    val detectedCompanyNames = mutableListOf<String>()
    var largestFontSize = 0f
    var nameByFontSize = ""
    var nameByUppercase = ""
    val detectedPhones = mutableListOf<String>()
    val detectedEmails = mutableListOf<String>()
    var detectedWebsite: String? = null
    var detectedCompanyDomain: String? = null
    val possibleNames = mutableListOf<String>()
    val addressLines = mutableListOf<String>()
    var detectedDesignation: String? = null

    for (block in visionText.textBlocks) {
        for (line in block.lines) {
            var textBlock = line.text.trim()

            if (textBlock.startsWith("•") || textBlock.startsWith("→") || textBlock.startsWith(".")) {
                textBlock = textBlock.replaceFirst(Regex("^[•→.]+"), "").trim()
                if (textBlock.isEmpty()) {
                    continue
                }
            }


            val labelValuePair = processLabelValuePair(textBlock)
            if (labelValuePair != null) {
                val (label, dataPart) = labelValuePair
                when (label.lowercase()) {
                    "phone", "mobile", "contact" -> {
                        val phoneNumbers = extractPhoneNumbers(dataPart, phoneRegex)
                        detectedPhones.addAll(phoneNumbers)
                        isBusinessCard = true
                    }

                    "email" -> {
                        if (dataPart.contains("@")) {
                            detectedEmails.add(dataPart)
                            detectedCompanyDomain = dataPart.substringAfter("@")
                            isBusinessCard = true
                        }
                    }

                    "website" -> {
                        if (websiteRegex.matches(dataPart)) {
                            detectedWebsite = dataPart
                            isBusinessCard = true
                        }
                    }

                    "address" -> {
                        val isAddressData = addressRegex.containsMatchIn(dataPart) ||
                                postalCodeRegex.containsMatchIn(dataPart) ||
                                cityStateRegex.containsMatchIn(dataPart) ||
                                countryNames.any { dataPart.lowercase().contains(it) } ||
                                indianStates.any { dataPart.lowercase().contains(it) }
                        if (isAddressData) {
                            addressLines.add(dataPart)
                            isBusinessCard = true
                        }
                    }

                    "designation", "title", "job title", "position" -> {
                        val isCity =
                            allCities.any { city -> dataPart.contains(city, ignoreCase = true) }
                        if (!addressRegex.matches(dataPart) && !isCity) { // Address and City check for designation
                            detectedDesignation = dataPart
                            isBusinessCard = true
                        }
                    }
                }
                continue
            }


            if (textBlock.contains("@")) {
                detectedEmails.add(textBlock)
                detectedCompanyDomain = textBlock.substringAfter("@")
                isBusinessCard = true
                continue
            }

            if (websiteRegex.matches(textBlock)) {
                detectedWebsite = textBlock
                isBusinessCard = true
                continue
            }

            val sanitizedPhone = textBlock.replace(Regex("[^+\\d,]"), "")
            val phoneNumbers = sanitizedPhone.split(",").map { it.trim() }.filter { it.matches(phoneRegex) }
            if (phoneNumbers.isNotEmpty()) {
                detectedPhones.addAll(phoneNumbers)
                isBusinessCard = true
            }

            val cityRegex =
                Regex("\\b(${popularIndianCities.joinToString("|")})\\b", RegexOption.IGNORE_CASE)

            val isAddress = addressRegex.containsMatchIn(textBlock) ||
                    postalCodeRegex.containsMatchIn(textBlock) ||
                    cityStateRegex.containsMatchIn(textBlock) ||
                    countryNames.any { country -> textBlock.lowercase().contains(country) } ||
                    indianStates.any { state -> textBlock.lowercase().contains(state) } ||
                    cityRegex.containsMatchIn(textBlock)

            if (isAddress) {
                addressLines.add(textBlock)
                isBusinessCard = true
            }

            val fontSize = line.boundingBox?.height()?.toFloat() ?: 0f
            if (fontSize > largestFontSize) {
                largestFontSize = fontSize
                nameByFontSize = textBlock
            }

            if (textBlock.isNotEmpty() && textBlock == textBlock.uppercase()) {
                nameByUppercase = textBlock
            }

            if (textBlock.isNotEmpty() &&
                !textBlock.contains("@") &&
                !textBlock.contains(keywordRegex) &&
                !lineContainsPhoneNumber(textBlock, phoneRegex) &&
                !websiteRegex.matches(textBlock) &&
                !(addressRegex.containsMatchIn(textBlock) ||
                        postalCodeRegex.containsMatchIn(textBlock) ||
                        cityStateRegex.containsMatchIn(textBlock) ||
                        countryNames.any { country -> textBlock.lowercase().contains(country) }) ||
                indianStates.any { state -> textBlock.lowercase().contains(state) } &&
                textBlock.matches(Regex(".*[a-zA-Z].*"))
            ) {
                possibleNames.add(textBlock)
            }

            if (textBlock.contains(keywordRegex)) {
                isBusinessCard = true
            }
        }
    }

    if (detectedDesignation.isNullOrEmpty()) {
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                var textBlock = line.text.trim()
                if (textBlock.startsWith("•") || textBlock.startsWith("→") || textBlock.startsWith(".")) {
                    textBlock = textBlock.replaceFirst(Regex("^[•→.]+"), "").trim()
                    if (textBlock.isEmpty()) {
                        continue
                    }
                }
                if (detectedEmails.contains(textBlock) || textBlock == detectedWebsite || detectedPhones.contains(
                        textBlock
                    ) || addressLines.contains(textBlock) || possibleNames.contains(textBlock) || detectedCompanyNames.contains(
                        textBlock
                    )
                ) {
                    continue
                }

                val isCity = allCities.any { city -> textBlock.contains(city, ignoreCase = true) }

                if (detectedDesignation.isNullOrEmpty() && !addressLines.contains(textBlock) && !addressRegex.matches(
                        textBlock
                    ) && !isCity
                ) { // Address and City check for designation
                    designationTerms.forEach { term ->
                        if (textBlock.contains(term, ignoreCase = true)) {
                            detectedDesignation = textBlock
                            Log.d(
                                "OCR_DEBUG",
                                "Designation FOUND (Contains): '$detectedDesignation' (Term: '$term')"
                            )
                            return@forEach
                        }
                    }
                }
            }
            if (detectedDesignation != null && detectedDesignation!!.isNotEmpty()) break
        }
    }

    for (block in visionText.textBlocks) {
        for (line in block.lines) {
            var textBlock = line.text.trim()
            if (textBlock.startsWith("•") || textBlock.startsWith("→") || textBlock.startsWith(".")) {
                textBlock = textBlock.replaceFirst(Regex("^[•→.]+"), "").trim()
                if (textBlock.isEmpty()) {
                    continue
                }
            }
            if (detectedEmails.contains(textBlock) || textBlock == detectedWebsite || detectedPhones.contains(
                    textBlock
                ) || addressLines.contains(textBlock) || detectedDesignation == textBlock
            ) {
                continue
            }

            if (companyKeywordsRegex.containsMatchIn(textBlock)) {
                detectedCompanyNames.add(textBlock)
            } else {
                val corporateTerms =
                    Regex("\\b(Corp|Inc|Ltd|GmbH|Pvt\\.? LTD)\\b", RegexOption.IGNORE_CASE)
                if (corporateTerms.containsMatchIn(textBlock)) {
                    detectedCompanyNames.add(textBlock)
                }
            }
        }
    }

    possibleNames.removeAll { possibleName ->
        val companyNameMatcher = detectedCompanyNames.any { cn ->
            LevenshteinDistance.calculate(possibleName, cn) < 4 ||
                    possibleName.contains(cn, ignoreCase = true) ||
                    cn.contains(possibleName, ignoreCase = true)
        }

        val emailDomainPrefix = detectedCompanyDomain?.substringBefore(".")?.lowercase() ?: ""
        val possibleNameLower = possibleName.lowercase()

        val emailDomainMatcher = when {
            emailDomainPrefix.length > 3 -> {
                possibleNameLower.contains(emailDomainPrefix) ||
                        LevenshteinDistance.calculate(possibleNameLower, emailDomainPrefix) <= 2
            }

            emailDomainPrefix.isNotEmpty() -> {
                possibleNameLower.contains(emailDomainPrefix)
            }

            else -> false
        }

        companyNameMatcher || emailDomainMatcher
    }

    addressLines.removeAll {
        detectedCompanyNames.contains(it) || companyKeywordsRegex.containsMatchIn(it) || it == detectedDesignation
    }

    var detectedName = ""
    val emailPrefix = detectedEmails.firstOrNull()?.substringBefore("@") ?: ""
    val normalizedPrefixParts = when {
        emailPrefix.matches(Regex("^[a-zA-Z]{2,4}$")) -> {
            emailPrefix.lowercase(Locale.ROOT).map { it.toString() }
        }

        else -> {
            emailPrefix.replace(Regex("[^a-zA-Z0-9]"), " ").split(" ").map { it.trim().lowercase() }
                .filter { it.isNotEmpty() }
        }
    }

    if (detectedEmails.isNotEmpty() && normalizedPrefixParts.isNotEmpty()) {
        possibleNames.forEach { possibleName ->
            val nameParts = possibleName.split(Regex("[\\s\\-,.]+")).map { it.trim() }
                .filter { it.isNotEmpty() }
            val initials =
                nameParts.mapNotNull { part ->
                    part.firstOrNull()?.toString()
                        ?.lowercase(Locale.ROOT)
                }

            if (containsInOrder(initials, normalizedPrefixParts)) {
                detectedName = possibleName
                return@forEach
            }
        }
    }

    if (detectedName.isEmpty() || detectedName.length < 3) { // Name length check and fallback mechanism
        val longerPossibleNames = possibleNames.filter { it.length >= 3 }
        detectedName = when {
            nameByFontSize.length >= 3 -> nameByFontSize
            nameByUppercase.length >= 3 -> nameByUppercase
            longerPossibleNames.isNotEmpty() -> longerPossibleNames.firstOrNull()
                ?: "" // Prioritize longer names from possible names
            possibleNames.isNotEmpty() -> possibleNames.firstOrNull()
                ?: "" // If no longer names, fallback to any possible name.
            else -> ""
        }
    }


    if (detectedName.isNotEmpty()) {
        val isCompanyNameMatch = detectedCompanyNames.any { cn ->
            LevenshteinDistance.calculate(detectedName, cn) < 4 ||
                    detectedName.contains(cn, ignoreCase = true) ||
                    cn.contains(detectedName, ignoreCase = true)
        }

        val emailDomainPrefix = detectedCompanyDomain?.substringBefore(".")?.lowercase() ?: ""
        val detectedNameLower = detectedName.lowercase()

        val isEmailDomainMatch = when {
            emailDomainPrefix.length > 3 -> {
                detectedNameLower.contains(emailDomainPrefix) ||
                        LevenshteinDistance.calculate(
                            detectedNameLower,
                            emailDomainPrefix
                        ) <= 2
            }

            emailDomainPrefix.isNotEmpty() -> {
                detectedNameLower.contains(emailDomainPrefix)
            }

            else -> false
        }

        if (isCompanyNameMatch || isEmailDomainMatch) {
            detectedName = possibleNames.firstOrNull { name ->
                !detectedCompanyNames.any { cn ->
                    (LevenshteinDistance.calculate(name, cn) < 4) ||
                            name.contains(cn, ignoreCase = true) ||
                            cn.contains(name, ignoreCase = true)
                } &&
                        !(emailDomainPrefix.isNotEmpty() && name.lowercase()
                            .contains(emailDomainPrefix.lowercase())) &&
                        !name.matches(Regex("^\\d+$")) &&
                        !name.contains(
                            Regex(
                                "\\b(${countryNames.joinToString("|")})\\b",
                                RegexOption.IGNORE_CASE
                            )
                        ) &&
                        !name.contains(
                            Regex(
                                "\\b(${popularIndianCities.joinToString("|")})\\b",
                                RegexOption.IGNORE_CASE
                            )
                        )
            } ?: ""
        } else {
            detectedName = possibleNames.firstOrNull { name ->
                !name.contains(
                    Regex(
                        "\\b(${countryNames.joinToString("|")})\\b",
                        RegexOption.IGNORE_CASE
                    )
                ) &&
                        !name.contains(
                            Regex(
                                "\\b(${popularIndianCities.joinToString("|")})\\b",
                                RegexOption.IGNORE_CASE
                            )
                        )
            } ?: ""
        }
    }

    if (isBusinessCard) {
        detectedPhones.takeIf { it.isNotEmpty() }?.let {
            details["Phone"] = it.getOrNull(0) ?: ""
            details["Phone2"] = it.getOrNull(1) ?: ""
            details["Phone3"] = it.getOrNull(2) ?: ""
        }

        detectedEmails.takeIf { it.isNotEmpty() }?.let {
            details["Email"] = it.first().trim()
        }

        detectedWebsite?.let { details["Website"] = it }

        if (addressLines.isNotEmpty()) {
            details["Address"] =
                addressLines.first { it.isNotBlank() && it.matches(Regex(".*[a-zA-Z].*")) }.trim()
        }

        if (detectedName.isNotEmpty()) {
            details["Name"] = detectedName
        }

        detectedDesignation?.let { designation ->
            if (designation.isNotEmpty()) {
                details["Designation"] = designation
            }
        }
    }

    return if (isBusinessCard) details else emptyMap()
}

private fun processLabelValuePair(text: String): Pair<String, String>? {
    val parts = text.split(Regex("[:\\-]"))
    if (parts.size >= 2) {
        val labelPart = parts[0].trim()
        val dataPart = parts.subList(1, parts.size).joinToString(" ").trim()
        if (labelPart.isNotBlank() && dataPart.isNotBlank()) {
            return labelPart to dataPart
        }
    }
    return null
}


fun lineContainsPhoneNumber(text: String, phoneRegex: Regex): Boolean {
    val sanitizedPhone = text.replace(Regex("[^+\\d,]"), "")
    val phoneNumbers = sanitizedPhone.split(",").map { it.trim() }.filter { it.matches(phoneRegex) }
    return phoneNumbers.isNotEmpty()
}

fun containsInOrder(initials: List<String>, prefixParts: List<String>): Boolean {
    if (prefixParts.isEmpty()) return true
    if (initials.size < prefixParts.size) return false

    for (i in 0..initials.size - prefixParts.size) {
        var match = true
        for (j in prefixParts.indices) {
            if (initials[i + j] != prefixParts[j]) {
                match = false
                break
            }
        }
        if (match) return true
    }
    return false
}

object LevenshteinDistance {
    fun calculate(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

        for (i in 0..s1.length) {
            for (j in 0..s2.length) {
                if (i == 0) {
                    dp[i][j] = j
                } else if (j == 0) {
                    dp[i][j] = i
                } else {
                    val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1

                    dp[i][j] = (dp[i - 1][j] + 1).coerceAtMost(dp[i][j - 1] + 1)
                        .coerceAtMost(dp[i - 1][j - 1] + cost)
                }
            }
        }

        return dp[s1.length][s2.length]
    }
}

private fun extractPhoneNumbers(dataPart: String, phoneRegex: Regex): List<String> {
    val sanitizedPhone = dataPart.replace(Regex("[^+\\d,]"), "")
    return sanitizedPhone.split(",").map { it.trim() }.filter { it.matches(phoneRegex) }
}

private fun saveContactToPhone(
    context: Context,
    name: String,
    phone: String,
    phone2: String?,
    phone3: String?,
    email: String,
    address: String?
) {
    val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
        type = ContactsContract.RawContacts.CONTENT_TYPE
        if (name.isNotEmpty()) putExtra(ContactsContract.Intents.Insert.NAME, name)
        if (phone.isNotEmpty()) putExtra(ContactsContract.Intents.Insert.PHONE, phone)
        phone2?.takeIf { it.isNotEmpty() }
            ?.let { putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, it) }
        phone3?.takeIf { it.isNotEmpty() }
            ?.let { putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE, it) }
        if (email.isNotEmpty()) putExtra(ContactsContract.Intents.Insert.EMAIL, email)
        if (address?.isNotEmpty() == true) putExtra(ContactsContract.Intents.Insert.POSTAL, address)
    }
    context.startActivity(intent)
}

@Composable
private fun SaveContactDialog(contactInfo: Map<String, String>, context: Context, showDialog: MutableState<Boolean>) {
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = {
                Text(
                    "Save Contact?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    contactInfo["Name"]?.takeIf { it.isNotEmpty() }?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Person, contentDescription = "Name Icon", modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Name: $it", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    contactInfo["Phone"]?.takeIf { it.isNotEmpty() }?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Phone, contentDescription = "Phone Icon", modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Primary Phone: $it", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    contactInfo["Phone2"]?.takeIf { it.isNotEmpty() }?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Phone, contentDescription = "Phone2 Icon", modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Secondary Phone: $it", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    contactInfo["Phone3"]?.takeIf { it.isNotEmpty() }?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Phone, contentDescription = "Phone3 Icon", modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tertiary Phone: $it", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    contactInfo["Email"]?.takeIf { it.isNotEmpty() }?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Email, contentDescription = "Email Icon", modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Email: $it", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    contactInfo["Website"]?.takeIf { it.isNotEmpty() }?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.url),
                                contentDescription = "Website Icon",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Website: $it", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    contactInfo["Address"]?.takeIf { it.isNotEmpty() }?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = "Address Icon",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Address: $it", style = MaterialTheme.typography.bodyMedium)
                        }

                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        saveContactToPhone(
                            context,
                            contactInfo["Name"] ?: "",
                            contactInfo["Phone"] ?: "",
                            contactInfo["Phone2"]?.takeIf { it.isNotEmpty() },
                            contactInfo["Phone3"]?.takeIf { it.isNotEmpty() },
                            contactInfo["Email"] ?: "",
                            contactInfo["Address"]?.takeIf { it.isNotEmpty() }
                        )
                        showDialog.value = false
                    },
                    modifier = Modifier.padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog.value = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSecondary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.9f)
        )
    }
}


private fun makePhoneCall(context: Context, phoneNumber: String) {
    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
    context.startActivity(dialIntent)
}

private fun openWhatsAppChat(context: Context, phoneNumber: String) {
    val messageIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phoneNumber"))
    context.startActivity(messageIntent)
}

private fun openSmsApp(context: Context, phoneNumber: String) {
    val smsUri = Uri.parse("sms:$phoneNumber")
    val smsIntent = Intent(Intent.ACTION_VIEW, smsUri)
    context.startActivity(smsIntent)
}

private fun shareContact(context: Context, contactInfo: Map<String, String>) {
    val shareText = buildString {
        contactInfo["Name"]?.let { append("Name: $it\n") }
        contactInfo["Phone"]?.let { append("Phone: $it\n") }

        contactInfo["Phone2"]?.takeIf { it.isNotBlank() }?.let { append("Secondary Phone: $it\n") }
        contactInfo["Phone3"]?.takeIf { it.isNotBlank() }?.let { append("Tertiary Phone: $it\n") }

        contactInfo["Email"]?.let { append("Email: $it\n") }
        contactInfo["Address"]?.let { append("Address: $it\n") }
        contactInfo["Website"]?.let { append("Website: $it\n") }
    }

    if (shareText.isNotEmpty()) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Contact"))
    }
}

