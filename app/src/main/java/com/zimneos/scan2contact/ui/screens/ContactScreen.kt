package com.zimneos.scan2contact.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddIcCall
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PermPhoneMsg
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ScreenSearchDesktop
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Web
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.zimneos.scan2contact.R
import com.zimneos.scan2contact.core.formatDateTime
import com.zimneos.scan2contact.core.isToday
import com.zimneos.scan2contact.core.isYesterday
import com.zimneos.scan2contact.core.makePhoneCall
import com.zimneos.scan2contact.core.openSmsApp
import com.zimneos.scan2contact.core.openWhatsAppChat
import com.zimneos.scan2contact.core.saveContactToPhone
import com.zimneos.scan2contact.core.shareContact
import com.zimneos.scan2contact.viewmodel.UserProfile
import com.zimneos.scan2contact.viewmodel.UserProfileViewModel
import com.zimneos.scan2contact.viewmodel.UserProfileViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(scammedImageUri: Uri) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val userProfileViewModel: UserProfileViewModel = viewModel(
        factory = UserProfileViewModelFactory(context)
    )
    userProfileViewModel.update(scammedImageUri)
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSheetOpen by remember { mutableStateOf(false) }
    var currentField by remember { mutableStateOf("") }
    var currentOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentValue by remember { mutableStateOf("") }
    var onValueChangedCallback by remember { mutableStateOf<(String) -> Unit>({ _ -> }) }

    Scaffold { paddingValues ->
        val userProfile by userProfileViewModel.userProfile.collectAsState()
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
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.Top
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                ContactCard(userProfile, context, haptic)
            }
            listOfNotNull(
                userProfile.name?.takeIf { it.isNotBlank() }?.let { name ->
                    item {
                        InfoCardDetails(
                            allTextSentences = userProfile.detectedTexts,
                            label = "Name",
                            value = name,
                            icon = Icons.Default.Person,
                            actionIcon = Icons.Default.ContentCopy,
                            onActionClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                coroutineScope.launch {
                                    clipboardManager.setText(AnnotatedString(name))
                                }
                            },
                            onValueChanged = { newValue ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                userProfileViewModel.updateName(newValue)
                            },
                            onOpenBottomSheet = { value, options, callback ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentField = "Name"
                                currentValue = value
                                currentOptions = options
                                onValueChangedCallback = callback
                                isSheetOpen = true
                                coroutineScope.launch { bottomSheetState.show() }
                            }
                        )
                    }
                },
                userProfile.designation?.takeIf { it.isNotBlank() }?.let { designation ->
                    item {
                        InfoCardDetails(
                            allTextSentences = userProfile.detectedTexts,
                            label = "Designation",
                            value = designation,
                            icon = Icons.Default.Work,
                            actionIcon = Icons.Default.PersonSearch,
                            onActionClick = {
                                val searchQuery = Uri.encode("${userProfile.name} $designation")
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.linkedin.com/search/results/all/?keywords=$searchQuery")
                                )
                                context.startActivity(intent)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onValueChanged = { newValue ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                userProfileViewModel.updateDesignation(newValue)
                            },
                            onOpenBottomSheet = { value, options, callback ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentField = "Designation"
                                currentValue = value
                                currentOptions = options
                                onValueChangedCallback = callback
                                isSheetOpen = true
                                coroutineScope.launch { bottomSheetState.show() }
                            }
                        )
                    }
                },
                userProfile.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                    item {
                        InfoCardDetails(
                            allTextSentences = userProfile.detectedTexts,
                            label = "Phone",
                            value = phone,
                            icon = Icons.Default.Phone,
                            actionIcon = Icons.Default.Whatsapp,
                            onActionClick = {
                                userProfile.phone?.let { phoneNumber ->
                                    openWhatsAppChat(context, phoneNumber)
                                }
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onValueChanged = { newValue ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                userProfileViewModel.updatePhone(newValue)
                            },
                            onOpenBottomSheet = { value, options, callback ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentField = "Phone"
                                currentValue = value
                                currentOptions = options
                                onValueChangedCallback = callback
                                isSheetOpen = true
                                coroutineScope.launch { bottomSheetState.show() }
                            }
                        )
                    }
                },
                userProfile.secondaryPhone?.takeIf { it.isNotBlank() }?.let { secondaryPhone ->
                    item {
                        InfoCardDetails(
                            allTextSentences = userProfile.detectedTexts,
                            label = "Secondary Phone",
                            value = secondaryPhone,
                            icon = Icons.Default.PermPhoneMsg,
                            actionIcon = Icons.Default.Whatsapp,
                            onActionClick = {
                                userProfile.secondaryPhone?.let { phoneNumber ->
                                    openWhatsAppChat(context, phoneNumber)
                                }
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onValueChanged = { newValue ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                userProfileViewModel.updateSecondaryPhone(newValue)
                            },
                            onOpenBottomSheet = { value, options, callback ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentField = "Secondary Phone"
                                currentValue = value
                                currentOptions = options
                                onValueChangedCallback = callback
                                isSheetOpen = true
                                coroutineScope.launch { bottomSheetState.show() }
                            }
                        )
                    }
                },
                userProfile.tertiaryPhone?.takeIf { it.isNotBlank() }?.let { tertiaryPhone ->
                    item {
                        InfoCardDetails(
                            allTextSentences = userProfile.detectedTexts,
                            label = "Tertiary Phone",
                            value = tertiaryPhone,
                            icon = Icons.Default.AddIcCall,
                            actionIcon = Icons.Default.Whatsapp,
                            onActionClick = {
                                userProfile.tertiaryPhone?.let { phoneNumber ->
                                    openWhatsAppChat(context, phoneNumber)
                                }
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onValueChanged = { newValue ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                userProfileViewModel.updateTertiaryPhone(newValue)
                            },
                            onOpenBottomSheet = { value, options, callback ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentField = "Tertiary Phone"
                                currentValue = value
                                currentOptions = options
                                onValueChangedCallback = callback
                                isSheetOpen = true
                                coroutineScope.launch { bottomSheetState.show() }
                            }
                        )
                    }
                },
                userProfile.email?.takeIf { it.isNotBlank() }?.let { email ->
                    item {
                        InfoCardDetails(
                            allTextSentences = userProfile.detectedTexts,
                            label = "Email",
                            value = email,
                            icon = Icons.Default.Email,
                            actionIcon = Icons.Default.Email,
                            onActionClick = {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("mailto:$email")
                                    )
                                )
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onValueChanged = { newValue ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                userProfileViewModel.updateEmail(newValue)
                            },
                            onOpenBottomSheet = { value, options, callback ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentField = "Email"
                                currentValue = value
                                currentOptions = options
                                onValueChangedCallback = callback
                                isSheetOpen = true
                                coroutineScope.launch { bottomSheetState.show() }
                            }
                        )
                    }
                },
                userProfile.website?.takeIf { it.isNotBlank() }?.let { website ->
                    item {
                        InfoCardDetails(
                            allTextSentences = userProfile.detectedTexts,
                            label = "Website",
                            value = website,
                            icon = Icons.Default.Web,
                            actionIcon = Icons.Default.ScreenSearchDesktop,
                            onActionClick = {
                                val url = website.let {
                                    if (it.startsWith("http://") || it.startsWith("https://")) it else "https://$it"
                                }
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
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onValueChanged = { newValue ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                userProfileViewModel.updateWebsite(newValue)
                            },
                            onOpenBottomSheet = { value, options, callback ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentField = "Website"
                                currentValue = value
                                currentOptions = options
                                onValueChangedCallback = callback
                                isSheetOpen = true
                                coroutineScope.launch { bottomSheetState.show() }
                            }
                        )
                    }
                },
                userProfile.address?.takeIf { it.isNotBlank() }?.let { address ->
                    item {
                        InfoCardDetails(
                            allTextSentences = userProfile.detectedTexts,
                            label = "Address",
                            value = address,
                            icon = Icons.Default.LocationOn,
                            actionIcon = Icons.Default.Navigation,
                            onActionClick = {
                                val uri = Uri.encode(address)
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
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onValueChanged = { newValue ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                userProfileViewModel.updateAddress(newValue)
                            },
                            onOpenBottomSheet = { value, options, callback ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentField = "Address"
                                currentValue = value
                                currentOptions = options
                                onValueChangedCallback = callback
                                isSheetOpen = true
                                coroutineScope.launch { bottomSheetState.show() }
                            }
                        )
                    }
                }
            )
        }

        if (isSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = {
                    isSheetOpen = false
                    coroutineScope.launch { bottomSheetState.hide() }
                },
                sheetState = bottomSheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                BottomSheetContent(
                    currentValue = currentValue,
                    allTextSentences = currentOptions,
                    onValueChanged = { newValue ->
                        onValueChangedCallback(newValue)
                        isSheetOpen = false
                        coroutineScope.launch { bottomSheetState.hide() }
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomSheetContent(
    currentValue: String,
    allTextSentences: List<String>,
    onValueChanged: (String) -> Unit
) {
    var inputText by remember { mutableStateOf(currentValue) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .verticalScroll(scrollState)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f),
                    textStyle = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily(
                            Font(R.font.cookie_regular)
                        )
                    ),
                    placeholder = { Text("Enter text...", color = Color(0xFF999999)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Color(0xFF6265FE)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                AnimatedSendButton(
                    enabled = inputText.isNotEmpty(),
                    onClick = { onValueChanged(inputText) }
                )
            }
        }

        allTextSentences.forEach { option ->
            var sanitizedOption = option
            while (sanitizedOption.isNotEmpty() && !sanitizedOption[0].isLetterOrDigit() && sanitizedOption[0] != '+') {
                sanitizedOption = sanitizedOption.substring(1)
            }
            SuggestionItem(
                text = sanitizedOption,
                onClick = { onValueChanged(sanitizedOption) }
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 8.dp),
                thickness = 0.5.dp,
                color = Color(0xFFE0E0E0)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun AnimatedSendButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(enabled) {
        if (enabled) {
            scale.animateTo(1.1f, animationSpec = tween(200))
            scale.animateTo(1f, animationSpec = tween(200))
        }
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = if (enabled) Color(0xFF6265FE) else Color(0xFFCCCCCC),
                shape = CircleShape
            )
            .clickable(
                enabled = enabled,
                onClick = onClick,
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Confirm",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun SuggestionItem(
    text: String,
    onClick: () -> Unit
) {
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(300))
    }

    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(vertical = 12.dp, horizontal = 12.dp)
            .alpha(alpha.value),
        style = TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF333333),
            fontFamily = FontFamily(
                Font(R.font.aboreto)
            )
        )
    )
}

@Composable
private fun ContactCard(userProfile: UserProfile, context: Context, haptic: HapticFeedback) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = rememberAsyncImagePainter(userProfile.scannedImage),
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

        userProfile.name?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Text(
            text = userProfile.scannedTime?.let { scannedTime ->
                val hour = if (scannedTime.hour % 12 == 0) 12 else scannedTime.hour % 12
                val amPm = if (scannedTime.hour >= 12) "PM" else "AM"
                val timeString = "${hour}:${scannedTime.minute.toString().padStart(2, '0')} $amPm"
                when {
                    isToday(scannedTime) -> "Scanned Today at $timeString"
                    isYesterday(scannedTime) -> "Scanned Yesterday at $timeString"
                    else -> "Scanned on ${formatDateTime(scannedTime)}"
                }
            } ?: "No scan time available",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(top = 2.dp)
        )


        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (userProfile.phone?.isEmpty() != true) {
                        userProfile.phone?.let { contactInfo ->
                            makePhoneCall(context, contactInfo)
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Please scan a card first",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF6265FE).copy(alpha = 0.1f), shape = CircleShape)
            ) {
                Icon(Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF6265FE))
            }

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (userProfile.phone?.isEmpty() != true) {
                        userProfile.phone?.let { contactInfo ->
                            openSmsApp(context, contactInfo)
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Please scan a card first",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF6265FE).copy(alpha = 0.1f), shape = CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Message,
                    contentDescription = "Message",
                    tint = Color(0xFF6265FE)
                )
            }

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    shareContact(context, userProfile)
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF6265FE).copy(alpha = 0.1f), shape = CircleShape)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF6265FE))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                saveContactToPhone(
                    context = context,
                    name = userProfile.name?.takeIf { it.isNotEmpty() } ?: "",
                    phone = userProfile.phone?.takeIf { it.isNotEmpty() } ?: "",
                    phone2 = userProfile.secondaryPhone?.takeIf { it.isNotEmpty() },
                    phone3 = userProfile.tertiaryPhone?.takeIf { it.isNotEmpty() },
                    email = userProfile.email?.takeIf { it.isNotEmpty() } ?: "",
                    address = userProfile.address?.takeIf { it.isNotEmpty() }
                )
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 80.dp)
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6265FE))
        ) {
            Text(
                text = "Save to Contacts",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun InfoCardDetails(
    label: String,
    value: String,
    icon: ImageVector,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null,
    onValueChanged: (String) -> Unit,
    allTextSentences: List<String> = emptyList(),
    onOpenBottomSheet: (String, List<String>, (String) -> Unit) -> Unit = { _, _, _ -> }
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(2.dp),
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
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF6264FE).copy(alpha = 0.25f), shape = CircleShape)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = Color(0xFF6265FE),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = label,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF616161),
                        fontSize = 14.sp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = allTextSentences.isNotEmpty()) {
                                if (allTextSentences.isNotEmpty()) {
                                    onOpenBottomSheet(value, allTextSentences, onValueChanged)
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = value,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            maxLines = 2,
                            fontSize = 16.sp,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (allTextSentences.isNotEmpty()) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                tint = Color(0xFF6265FE),
                                imageVector = Icons.Default.EditNote,
                                contentDescription = "Open bottom sheet"
                            )
                        }
                    }
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