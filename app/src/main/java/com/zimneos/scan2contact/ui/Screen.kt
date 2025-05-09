package com.zimneos.scan2contact.ui

import kotlinx.serialization.Serializable

@Serializable
object Home

@Serializable
object Recent

@Serializable
data class Details(val scannedImageUri: String)