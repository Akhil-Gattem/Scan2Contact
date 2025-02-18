package com.zimneos.scan2contact.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.zimneos.scan2contact.R

@Composable
fun BottomNavigationBar(
    onCameraClick: () -> Unit,
    onPhoneClick: () -> Unit,
    onChatClick: () -> Unit,
    onContactsClick: () -> Unit,
    onShareClick: () -> Unit
) {
    BottomAppBar(
        containerColor = Color.White,
        modifier = Modifier.height(115.dp).padding(bottom = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onPhoneClick() }) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_call),
                    contentDescription = "Call",
                    modifier = Modifier.size(28.dp),
                    tint = Color.Black
                )
            }
            IconButton(onClick = { onChatClick() }) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_chat),
                    contentDescription = "Chat",
                    modifier = Modifier.size(28.dp),
                    tint = Color.Black
                )
            }
            IconButton(
                onClick = { onCameraClick() },
                modifier = Modifier
                    .size(56.dp)
                    .offset(y = (-14).dp)
                    .background(Color(0xFF6265FE), shape = CircleShape)
                    .padding(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_camera),
                    contentDescription = "Camera",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
            }
            IconButton(onClick = { onContactsClick() }) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_add_contact),
                    contentDescription = "Contacts",
                    modifier = Modifier.size(28.dp),
                    tint = Color.Black
                )
            }
            IconButton(onClick = { onShareClick() }) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_share),
                    contentDescription = "Share",
                    modifier = Modifier.size(28.dp),
                    tint = Color.Black
                )
            }
        }
    }
}