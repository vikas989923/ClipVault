package com.clipvault.app.ui

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clipvault.app.model.ClipEntry
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ClipVaultApp(
    isAccessibilityEnabled: Boolean,
    onEnableAccessibility: () -> Unit
) {
    val vm: ClipViewModel = viewModel()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF1E1E1E)) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.ContentCopy, contentDescription = "All") },
                    label = { Text("All") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Link, contentDescription = "Links") },
                    label = { Text("Links") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF121212))
        ) {
            // Accessibility warning banner
            if (!isAccessibilityEnabled) {
                AccessibilityBanner(onEnableAccessibility)
            }

            when (selectedTab) {
                0 -> AllClipsScreen(vm)
                1 -> LinksScreen(vm)
            }
        }
    }
}

@Composable
fun AccessibilityBanner(onEnable: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF7B2D2D)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFFFCC00),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "ClipVault is paused — tap to enable",
                color = Color.White,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onEnable) {
                Text("Enable", color = Color(0xFF64B5F6))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AllClipsScreen(vm: ClipViewModel) {
    val clips by vm.clips.collectAsState()
    val query by vm.searchQuery.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "ClipVault",
                color = Color(0xFF1A73E8),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showClearDialog = true }) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear all", tint = Color(0xFFBDBDBD))
            }
        }

        // Search bar
        OutlinedTextField(
            value = query,
            onValueChange = { vm.setQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            placeholder = { Text("Search clips...", color = Color(0xFF757575)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF757575)) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { vm.setQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF757575))
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1A73E8),
                unfocusedBorderColor = Color(0xFF424242),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF1A73E8)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(4.dp))

        if (clips.isEmpty()) {
            EmptyState("Copy any text to start saving it here")
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(clips, key = { it.id }) { clip ->
                    ClipCard(
                        clip = clip,
                        onPin = { vm.togglePin(clip) },
                        onDelete = { vm.delete(clip) },
                        onCopy = { copyToClipboard(context, clip.text) },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear History", color = Color.White) },
            text = { Text("Delete all unpinned clips? Pinned clips will remain.", color = Color(0xFFBDBDBD)) },
            confirmButton = {
                TextButton(onClick = {
                    vm.clearAll()
                    showClearDialog = false
                }) { Text("Clear", color = Color(0xFFEF5350)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = Color(0xFF1A73E8))
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }
}

@Composable
fun LinksScreen(vm: ClipViewModel) {
    val links by vm.linkClips.collectAsState()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Saved Links",
            color = Color(0xFF1A73E8),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        if (links.isEmpty()) {
            EmptyState("Copy any URL and it will appear here")
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(links, key = { it.id }) { clip ->
                    ClipCard(
                        clip = clip,
                        onPin = { vm.togglePin(clip) },
                        onDelete = { vm.delete(clip) },
                        onCopy = { copyToClipboard(context, clip.text) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClipCard(
    clip: ClipEntry,
    onPin: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = { onCopy() },
                onLongClick = { expanded = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (clip.isPinned) Color(0xFF1A2744) else Color(0xFF1E1E1E)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Link badge
                if (clip.isLink) {
                    Text(
                        "🔗 LINK",
                        fontSize = 10.sp,
                        color = Color(0xFF64B5F6),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                Text(
                    text = clip.text,
                    color = Color(0xFFE0E0E0),
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatTimestamp(clip.timestamp),
                    color = Color(0xFF757575),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }

            if (clip.isPinned) {
                Icon(
                    Icons.Default.PushPin,
                    contentDescription = "Pinned",
                    tint = Color(0xFF1A73E8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Long-press dropdown
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Copy") },
                onClick = { onCopy(); expanded = false },
                leadingIcon = { Icon(Icons.Default.ContentCopy, null) }
            )
            DropdownMenuItem(
                text = { Text(if (clip.isPinned) "Unpin" else "Pin") },
                onClick = { onPin(); expanded = false },
                leadingIcon = { Icon(Icons.Default.PushPin, null) }
            )
            DropdownMenuItem(
                text = { Text("Delete", color = Color(0xFFEF5350)) },
                onClick = { onDelete(); expanded = false },
                leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFEF5350)) }
            )
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.ContentPaste,
                contentDescription = null,
                tint = Color(0xFF424242),
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(message, color = Color(0xFF757575), fontSize = 14.sp)
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = android.content.ClipData.newPlainText("ClipVault", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
}

private fun formatTimestamp(ts: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - ts
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else -> SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(ts))
    }
}
