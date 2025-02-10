package com.zimneos.scan2contact.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.zimneos.scan2contact.R
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch


@Composable
private fun InfoText(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, color = Color.Black, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
    }
}


@Composable
fun DocumentScannerScreen() {
    val context = LocalContext.current
    var showScanner by remember { mutableStateOf(true) }
    var scannedImageUri by remember { mutableStateOf<Uri?>(null) }
    val customFont = FontFamily(Font(R.font.regular))
    val scope = rememberCoroutineScope()
    val extractedText = remember { mutableStateOf<Map<String, String>?>(null) }
    val isScanning = remember { mutableStateOf(true) }
    val showDialog = remember { mutableStateOf(false) }

    LaunchedEffect(scannedImageUri) {
        scannedImageUri?.let {
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
                            startDocumentScan(context) { intentSenderRequest ->
                                scannerLauncher.launch(intentSenderRequest)
                            }
                        },
                        onPhoneClick = {
                            extractedText.value?.let { contactInfo ->
                                contactInfo["Phone"]?.let { phoneNumber ->
                                    makePhoneCall(context, phoneNumber)
                                }
                            }
                        },
                        onChatClick = {
                            extractedText.value?.let { contactInfo ->
                                contactInfo["Phone"]?.let { phoneNumber ->
                                    openChat(context, phoneNumber)
                                }
                            }
                        },
                        onContactsClick = {
                            scope.launch {
                                showDialog.value = true
                            }
                        },
                        onShareClick = {
                            extractedText.value?.let { contactInfo ->
                                shareContact(context, contactInfo)
                            }
                        }
                    )

                }
            ) { paddingValues ->
                Image(
                    painter = painterResource(R.drawable.bg),
                    modifier = Modifier.fillMaxSize().alpha(0.2f),
                    contentDescription = "Background Image",
                    contentScale = ContentScale.FillHeight
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Top,
                ) {
                    item {
                        if (showDialog.value) {
                            extractedText.value?.let { contactInfo ->
                                SaveContactDialog(contactInfo, context, showDialog)
                            }
                        }
                    }
                    item {
                        Text(
                            text = stringResource(R.string.app_name),
                            fontFamily = customFont,
                            color = Color.DarkGray,
                            lineHeight = 48.sp,
                            fontSize = 58.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, Color.Gray, RoundedCornerShape(1.dp))
                                .background(Color.LightGray)
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
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
                                    .border(2.dp, Color.Gray, RoundedCornerShape(8.dp))
                            )
                        }
                    }


                    val extractedData = extractedText.value ?: emptyMap()

                    if (scannedImageUri == null) {
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                        item { InfoText(label = "Name:", value = "Name will show here") }
                        item { InfoText(label = "Phone:", value = "Phone number will show here") }
                        item {
                            InfoText(
                                label = "Secondary Phone:",
                                value = "Secondary Phone number will show here"
                            )
                        }
                        item {
                            InfoText(
                                label = "Tertiary Phone:",
                                value = "Tertiary Phone number will show here"
                            )
                        }
                        item { InfoText(label = "Email:", value = "Email will show here") }
                        item { InfoText(label = "Address:", value = "Address will show here") }
                        item { InfoText(label = "Website URL:", value = "Web URL will show here") }
                    } else {
                        item { Spacer(modifier = Modifier.height(18.dp)) }
                        extractedData["Name"]?.takeIf { it.isNotEmpty() }?.let {
                            item { InfoText(label = "Name:", value = it) }
                        }
                        extractedData["Phone"]?.takeIf { it.isNotEmpty() }?.let {
                            item { InfoText(label = "Phone:", value = it) }
                        }
                        extractedData["Phone2"]?.takeIf { it.isNotEmpty() }?.let {
                            item { InfoText(label = "Secondary Phone:", value = it) }
                        }
                        extractedData["Phone3"]?.takeIf { it.isNotEmpty() }?.let {
                            item { InfoText(label = "Tertiary Phone:", value = it) }
                        }
                        extractedData["Email"]?.takeIf { it.isNotEmpty() }?.let {
                            item { InfoText(label = "Email:", value = it) }
                        }
                        extractedData["Address"]?.takeIf { it.isNotEmpty() }?.let {
                            item { InfoText(label = "Address:", value = it) }
                        }
                        extractedData["Website"]?.takeIf { it.isNotEmpty() }?.let {
                            item { InfoText(label = "Website URL:", value = it) }
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


private fun extractTextFromImage(context: Context, uri: Uri, onResult: (Map<String, String>) -> Unit) {
    try {
        val image: InputImage = InputImage.fromFilePath(context, uri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val extractedData = extractBusinessCardDetails(visionText)
                onResult(extractedData)
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
        "\\b(Phone|Mobile|Contact|Email|Website|Company|Address|Fax|CEO|Manager|Founder|CTO|Director|VP|Consultant)\\b",
        RegexOption.IGNORE_CASE
    )

    var largestFontSize = 0f
    var nameByFontSize = ""
    var nameByUppercase = ""
    val detectedPhones = mutableListOf<String>()
    val detectedEmails = mutableListOf<String>()
    var detectedWebsite: String? = null
    var detectedCompanyDomain: String? = null
    val possibleNames = mutableListOf<String>()
    var detectedAddress: String? = null

    for (block in visionText.textBlocks) {
        for (line in block.lines) {
            val textBlock = line.text.trim()

            // Skip bullet points, arrows, or dot-prefixed text
            if (textBlock.startsWith("•") || textBlock.startsWith("→") || textBlock.startsWith(".")) {
                continue // Skip this text
            }

            // Check for email and extract the domain
            if (textBlock.contains("@")) {
                detectedEmails.add(textBlock)
                detectedCompanyDomain = textBlock.substringAfter("@") // Extract domain from email
                isBusinessCard = true
                continue
            }

            // Check for website
            if (websiteRegex.matches(textBlock)) {
                detectedWebsite = textBlock
                isBusinessCard = true
                continue
            }

            // Check for keywords related to business card
            if (textBlock.contains(keywordRegex)) {
                isBusinessCard = true
            }

            // Extract phone numbers
            val sanitizedPhone =
                textBlock.replace(Regex("[^+\\d,]"), "") // Retain commas for multiple numbers
            val phoneNumbers =
                sanitizedPhone.split(",").map { it.trim() }.filter { it.matches(phoneRegex) }
            if (phoneNumbers.isNotEmpty()) {
                detectedPhones.addAll(phoneNumbers)
                isBusinessCard = true
            }

            // Track largest font size for name extraction
            val fontSize = line.boundingBox?.height()?.toFloat() ?: 0f
            if (fontSize > largestFontSize) {
                largestFontSize = fontSize
                nameByFontSize = textBlock
            }

            // Check for all uppercase words to detect name
            if (textBlock.isNotEmpty() && textBlock == textBlock.uppercase()) {
                nameByUppercase = textBlock
            }

            // Collect potential names (excluding email and all-caps names)
            if (textBlock.isNotEmpty() && textBlock != textBlock.uppercase() && !textBlock.contains("@")) {
                possibleNames.add(textBlock)
            }

            // Detect address-like text (e.g., contains street names or numbers)
            if (textBlock.contains("Street") || textBlock.contains("Avenue") || textBlock.contains("Blvd")) {
                detectedAddress = textBlock
            }
        }
    }

    // If business card is detected, extract details
    if (isBusinessCard && detectedPhones.isNotEmpty()) {
        details["Phone"] = detectedPhones.getOrNull(0) ?: ""  // Primary Phone
        details["Phone2"] = detectedPhones.getOrNull(1) ?: "" // Secondary Phone
        details["Phone3"] = detectedPhones.getOrNull(2) ?: "" // Tertiary Phone
    }

    if (detectedEmails.isNotEmpty()) {
        details["Email"] = detectedEmails.joinToString(", ")
    }

    if (detectedWebsite != null) {
        details["Website"] = detectedWebsite
    }

    // Extract the name using the most appropriate method (font size or uppercase)
    var detectedName = when {
        nameByFontSize.isNotEmpty() -> nameByFontSize
        nameByUppercase.isNotEmpty() -> nameByUppercase
        else -> ""
    }

    // Avoid using company email domain, all-caps names, and address-like text as contact name
    if (detectedName.isNotEmpty()) {
        if (detectedCompanyDomain?.let { detectedName.contains(it, ignoreCase = true) } == true ||
            detectedName == detectedName.uppercase() ||
            detectedName.contains("Street") || detectedName.contains("Avenue") || detectedName.contains("Blvd")) {
            // Skip using all-caps company name, address-like text, or name matching the email domain
            detectedName = ""
        }
    }

    // Check for Name-Email Similarity (email prefix and extracted name)
    if (detectedName.isEmpty() && detectedEmails.isNotEmpty()) {
        val emailPrefix = detectedEmails.first().substringBefore("@")
        possibleNames.firstOrNull { it.contains(emailPrefix, ignoreCase = true) }?.let {
            detectedName = it
        }
    }

    // If no valid name found, try alternative names from the text
    if (detectedName.isEmpty()) {
        detectedName = possibleNames.firstOrNull { it.isNotEmpty() } ?: ""
    }

    if (detectedName.isNotEmpty()) {
        details["Name"] = detectedName
    }

    return if (isBusinessCard) details else emptyMap()
}


private fun saveContactToPhone(
    context: Context,
    name: String,
    phone: String,
    phone2: String?,
    phone3: String?,
    email: String
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
                            contactInfo["Email"] ?: ""
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

private fun openChat(context: Context, phoneNumber: String) {
    val messageIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phoneNumber"))
    context.startActivity(messageIntent)
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

