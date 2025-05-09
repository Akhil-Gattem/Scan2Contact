package com.zimneos.scan2contact.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import com.zimneos.scan2contact.viewmodel.UserProfile
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun shareContact(context: Context, userProfile: UserProfile) {
    val shareText = buildString {
        userProfile.name?.takeIf { it.isNotEmpty() }?.let { append("Name: $it\n") }
        userProfile.phone?.takeIf { it.isNotEmpty() }?.let { append("Phone: $it\n") }
        userProfile.email?.takeIf { it.isNotEmpty() }?.let { append("Email: $it\n") }
        userProfile.address?.takeIf { it.isNotEmpty() }?.let { append("Address: $it\n") }
        userProfile.website?.takeIf { it.isNotEmpty() }?.let { append("Website: $it\n") }
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

fun makePhoneCall(context: Context, phoneNumber: String) {
    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
    context.startActivity(dialIntent)
}

fun openWhatsAppChat(context: Context, phoneNumber: String) {
    val messageIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phoneNumber"))
    context.startActivity(messageIntent)
}

fun openSmsApp(context: Context, phoneNumber: String) {
    val smsUri = Uri.parse("sms:$phoneNumber")
    val smsIntent = Intent(Intent.ACTION_VIEW, smsUri)
    context.startActivity(smsIntent)
}

fun saveContactToPhone(
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

fun isToday(dateTime: LocalDateTime): Boolean =
    dateTime.toLocalDate() == LocalDate.now()

fun isYesterday(dateTime: LocalDateTime): Boolean =
    dateTime.toLocalDate() == LocalDate.now().minusDays(1)

fun formatDateTime(dateTime: LocalDateTime?): String {
    if (dateTime == null) return "Not available"

    val today = LocalDateTime.now()
    val yesterday = today.minusDays(1)

    return when {
        dateTime.toLocalDate() == today.toLocalDate() -> "Today"
        dateTime.toLocalDate() == yesterday.toLocalDate() -> "Yesterday"
        else -> dateTime.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
    }
}
