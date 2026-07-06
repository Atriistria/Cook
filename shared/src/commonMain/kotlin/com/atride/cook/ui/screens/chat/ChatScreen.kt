package com.atride.cook.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atride.cook.model.ChatMessage
import com.atride.cook.model.MessageRole
import androidx.compose.ui.tooling.preview.Preview
import com.atride.cook.ui.theme.CookTheme


val availableModels =
    listOf("gpt-4o", "gpt-4o-mini", "claude-opus-4", "deepseek-v4", "gemini-2.5-pro")

@Composable
fun ChatInputBar(
    selectedModel: String,
    onModelChange: (String) -> Unit,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier,
    showModelSelector: Boolean = true
) {
    var text by remember { mutableStateOf("") }
    var modelExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        if (showModelSelector) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { modelExpanded = true },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = selectedModel,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                DropdownMenu(
                    expanded = modelExpanded,
                    onDismissRequest = { modelExpanded = false }
                ) {
                    availableModels.forEach { model ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = model,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (model == selectedModel) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onModelChange(model)
                                modelExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = {
                    Text(
                        "输入消息...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                minLines = 1,
                maxLines = 6,
                textStyle = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onSend(text.trim())
                        text = ""
                    }
                },
                modifier = Modifier.size(44.dp)
            ) {
                Text(
                    text = "\u27A4",
                    fontSize = 22.sp,
                    color = if (text.isNotBlank()) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

// ── ChatBubble ──

@Composable
fun ChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == MessageRole.User
    val bubbleColor = if (isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val onBubbleColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = if (isUser) 48.dp else 0.dp,
                end = if (isUser) 0.dp else 48.dp,
                top = 4.dp,
                bottom = 4.dp
            ),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Column(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(bubbleColor)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            RenderMarkdownContent(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = onBubbleColor
                )
            )
        }
    }
}

@Composable
private fun RenderMarkdownContent(
    text: String,
    style: TextStyle
) {
    val parts = text.split("```")

    for ((index, part) in parts.withIndex()) {
        if (index % 2 == 0) {
            val paragraphs = part.split("\n\n")
            for ((pIndex, paragraph) in paragraphs.withIndex()) {
                if (paragraph.isBlank()) continue
                if (pIndex > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = paragraph.trimStart(),
                    style = style
                )
            }
        } else {
            val lines = part.trimStart().split("\n")
            val codeLang = if (lines.isNotEmpty() && !lines[0].contains(" ")) lines[0] else ""
            val codeStartIndex =
                if (codeLang.isNotEmpty() && codeLang.all { it.isLetterOrDigit() || it == '+' || it == '#' }) 1 else 0
            val codeContent = lines.drop(codeStartIndex).joinToString("\n").trim()

            if (codeContent.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                val codeBg = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
                    Color(0xFF0E0E1A)
                } else {
                    Color(0xFF1E1E2E)
                }
                Text(
                    text = codeContent,
                    style = style.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = style.fontSize * 0.9f
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(codeBg)
                        .padding(12.dp)
                )
            }
        }
    }
}

private fun Color.luminance(): Float {
    return 0.299f * red + 0.587f * green + 0.114f * blue
}

// -- Previews --

@Preview
@Composable
private fun PreviewChatBubbleUser() {
    CookTheme {
        ChatBubble(
            message = ChatMessage(
                id = "preview_user",
                content = "Hello! This is a user message.",
                role = MessageRole.User
            )
        )
    }
}

@Preview
@Composable
private fun PreviewChatBubbleAssistant() {
    CookTheme {
        ChatBubble(
            message = ChatMessage(
                id = "preview_ai",
                content = """Hi there! This is an **assistant** response with a code block:

```kotlin
fun hello() = println("Hello")
""".trimIndent(),
                role = MessageRole.Assistant
            )
        )
    }
}

@Preview
@Composable
private fun PreviewChatInputBar() {
    CookTheme {
        ChatInputBar(
            selectedModel = "gpt-4o",
            onModelChange = {},
            onSend = {},
            showModelSelector = true
        )
    }
}
