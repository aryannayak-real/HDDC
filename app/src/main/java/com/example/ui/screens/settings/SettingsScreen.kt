package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SettingsEntity
import com.example.data.remote.ClashApiClient
import com.example.data.security.SecurityManager
import com.example.ui.theme.BackgroundNavy
import com.example.ui.theme.BorderActive
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyanHUD
import com.example.ui.theme.GemGreen
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LossRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    settings: SettingsEntity?,
    testResult: ClashApiClient.ConnectionTestResult?,
    isLoading: Boolean,
    onSave: (apiKey: String, proxyHost: String, proxyPort: Int, proxyUser: String, proxyPass: String, useProxy: Boolean) -> Unit,
    onTestConnection: () -> Unit,
    modifier: Modifier = Modifier
) {
    var apiKeyInput by remember { mutableStateOf("") }
    var proxyHostInput by remember { mutableStateOf(settings?.proxyHost ?: "46.173.78.114") }
    var proxyPortInput by remember { mutableStateOf((settings?.proxyPort ?: 4443).toString()) }
    var proxyUserInput by remember { mutableStateOf(settings?.proxyUsername ?: "") }
    var proxyPassInput by remember { mutableStateOf("") }
    var useProxyToggle by remember { mutableStateOf(settings?.useProxy ?: true) }
    var showApiKey by remember { mutableStateOf(false) }
    var showProxyPass by remember { mutableStateOf(false) }

    LaunchedEffect(settings) {
        if (settings != null) {
            proxyHostInput = settings.proxyHost
            proxyPortInput = settings.proxyPort.toString()
            proxyUserInput = settings.proxyUsername
            useProxyToggle = settings.useProxy
        }
    }

    val hasSavedApiKey = !settings?.apiKeyEncrypted.isNullOrEmpty()
    val hasSavedProxyPass = !settings?.proxyPasswordEncrypted.isNullOrEmpty()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundNavy)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "HDDC SECURE SETTINGS & CREDENTIALS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GoldBright,
                letterSpacing = 1.sp
            )
        }

        // Encryption Security Card
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(12.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GemGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = GemGreen, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AES-256-GCM Storage",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Credentials encrypted at rest. Passwords are never displayed in plaintext.",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }

        // Supercell API Key Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(14.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SUPERCELL API KEY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldBright,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (hasSavedApiKey) "✓ Key Configured" else "Missing Key",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasSavedApiKey) GemGreen else LossRed
                    )
                }

                Text(
                    text = "Generate a key at developer.clashofclans.com. Note: whitelisted IP must match your proxy host ($proxyHostInput).",
                    fontSize = 11.sp,
                    color = TextMuted
                )

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    placeholder = {
                        Text(
                            text = if (hasSavedApiKey) "•••••••••••••••••••••••••••••••• (Leave blank to keep existing)" else "Paste Supercell API Token",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showApiKey = !showApiKey }) {
                            Icon(
                                imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Visibility",
                                tint = TextMuted
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        }

        // Node4 Proxy Configuration
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(14.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "NODE4 HTTP PROXY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanHUD,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Route API requests through whitelisted static IP",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }

                    Switch(
                        checked = useProxyToggle,
                        onCheckedChange = { useProxyToggle = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BackgroundNavy,
                            checkedTrackColor = CyanHUD
                        )
                    )
                }

                if (useProxyToggle) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = proxyHostInput,
                            onValueChange = { proxyHostInput = it },
                            label = { Text("Proxy Host / IP") },
                            modifier = Modifier.weight(2f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                        )
                        OutlinedTextField(
                            value = proxyPortInput,
                            onValueChange = { proxyPortInput = it },
                            label = { Text("Port") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                        )
                    }

                    OutlinedTextField(
                        value = proxyUserInput,
                        onValueChange = { proxyUserInput = it },
                        label = { Text("Proxy Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )

                    OutlinedTextField(
                        value = proxyPassInput,
                        onValueChange = { proxyPassInput = it },
                        label = { Text(if (hasSavedProxyPass) "Proxy Password (Configured - Blank to Keep)" else "Proxy Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (showProxyPass) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showProxyPass = !showProxyPass }) {
                                Icon(
                                    imageVector = if (showProxyPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextMuted
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                }
            }
        }

        // Action Buttons: Save & Test
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        val port = proxyPortInput.toIntOrNull() ?: 4443
                        onSave(apiKeyInput, proxyHostInput, port, proxyUserInput, proxyPassInput, useProxyToggle)
                        apiKeyInput = ""
                        proxyPassInput = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = BackgroundNavy
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save & Encrypt Credentials", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onTestConnection,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanHUD),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanHUD)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CyanHUD, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Testing Supercell Connection...")
                    } else {
                        Icon(imageVector = Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Connection Through Proxy", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Connection Test Results Box
        if (testResult != null || settings?.lastTestStatus != "NOT_TESTED") {
            item {
                val isSuccess = testResult?.success ?: (settings?.lastTestStatus == "SUCCESS")
                val message = testResult?.message ?: (settings?.lastTestStatus ?: "")
                val detail = testResult?.errorDetails ?: (settings?.lastTestMessage ?: "")
                val latency = testResult?.latencyMs ?: 0L

                val bannerColor = if (isSuccess) GemGreen else LossRed

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(bannerColor.copy(alpha = 0.15f))
                        .border(1.dp, bannerColor, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = bannerColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isSuccess) "Connection Verified (${latency}ms)" else "Connection Failed",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = bannerColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = message, fontSize = 12.sp, color = TextPrimary)
                            if (detail.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = detail, fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // About HDDC
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "HDDC — Home Dashboard for Clash v1.0", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Text(text = "Personal Clash of Clans Intelligence Platform", fontSize = 11.sp, color = TextMuted)
                Text(text = "Supercell API • Node4 Proxy Engine • Local SQLite Persistence", fontSize = 10.sp, color = TextMuted)
            }
        }
    }
}
