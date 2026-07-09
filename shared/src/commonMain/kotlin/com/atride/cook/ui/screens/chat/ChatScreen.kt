package com.atride.cook.ui.screens.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atride.cook.model.ChatMessage
import com.atride.cook.model.MessageRole
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material.RichText
import androidx.compose.ui.tooling.preview.Preview
import com.atride.cook.ui.theme.CookTheme
import org.koin.compose.viewmodel.koinViewModel


val availableModels =
    listOf("gpt-4o", "gpt-4o-mini", "claude-opus-4", "deepseek-v4", "gemini-2.5-pro")

@Composable
fun ChatInputBar(
    selectedModel: String,
    onModelChange: (String) -> Unit,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier,
    desktopLayout: Boolean = false,
    onAttachClick: (() -> Unit)? = null,
    viewModel: ChatViewModel = koinViewModel()
) {
    var text by remember { mutableStateOf("") }

    if (desktopLayout) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                // Text input area (fills available space)
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp, max = 200.dp),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    decorationBox = { innerTextField ->
                        if (text.isEmpty()) {
                            Text(
                                text = "输入消息...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom toolbar (inside the same container)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // [+] attach button
                    if (onAttachClick != null) {
                        IconButton(
                            onClick = onAttachClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text(
                                text = "+",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(2.dp))
                    }

                    // Model selector
                    ModelSelectorButton(
                        selectedModel = selectedModel,
                        onModelChange = onModelChange,
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Send button
                    FilledIconButton(
                        onClick = {
                            if (text.isNotBlank()) {
                                viewModel.sendIntent(ChatIntent.SendMessage(text.trim()))
                                text = ""
                            }
                        },
                        modifier = Modifier.size(32.dp),
                        enabled = text.isNotBlank(),
                    ) {
                        Text(
                            text = "➤",
                            fontSize = 16.sp,
                        )
                    }
                }
            }
        }
    } else {
        // Mobile: compact row (model selector is in top bar)
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                if (onAttachClick != null) {
                    IconButton(
                        onClick = onAttachClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text(
                            text = "+",
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f).heightIn(min = 40.dp),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    decorationBox = { innerTextField ->
                        if (text.isEmpty()) {
                            Text(
                                text = "输入消息...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                )

                Spacer(modifier = Modifier.width(4.dp))

                FilledIconButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            viewModel.sendIntent(ChatIntent.SendMessage(text.trim()))
                            text = ""
                        }
                    },
                    modifier = Modifier.size(36.dp),
                    enabled = text.isNotBlank(),
                ) {
                    Text(
                        text = "➤",
                        fontSize = 18.sp,
                    )
                }
            }
        }
    }
}
// -- ModelSelectorButton --

@Composable
fun ModelSelectorButton(
    selectedModel: String,
    onModelChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { expanded = true },
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
    ) {
        Text(
            text = selectedModel,
            style = MaterialTheme.typography.labelMedium
        )
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
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
                    expanded = false
                }
            )
        }
    }
}


// ── ChatBubble ──

@Composable
fun ChatBubble(
    message: Message,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == MessageRole.User
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
                text = message.text,
                bubbleTextColor = onBubbleColor
            )
        }
    }
}

@OptIn(ExperimentalRichTextApi::class)
@Composable
private fun RenderMarkdownContent(
    text: String,
    bubbleTextColor: Color
) {
    val parts = text.split("```")

    parts.forEachIndexed { index, part ->
        key(index) {
            if (index % 2 == 0) {
                if (part.isNotBlank()) {
                    val richTextState = rememberRichTextState()
                    LaunchedEffect(part) {
                        richTextState.setMarkdown(part)
                    }
                    CompositionLocalProvider(LocalContentColor provides bubbleTextColor) {
                        RichText(
                            state = richTextState,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            } else {
                val lines = part.trimStart().split("\n")
                val codeLang = if (lines.isNotEmpty() && !lines[0].contains(" ")) lines[0] else ""
                val codeStartIndex =
                    if (codeLang.isNotEmpty() && codeLang.all { it.isLetterOrDigit() || it == '+' || it == '#' }) 1 else 0
                val codeContent = lines.drop(codeStartIndex).joinToString("\n").trim()

                if (codeContent.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    CodeBlock(
                        code = codeContent,
                        language = codeLang
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeBlock(
    code: String,
    language: String
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val codeBg = if (isDark) Color(0xFF1E1E2E) else Color(0xFFF5F5F5)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(codeBg)
    ) {
        if (language.isNotBlank()) {
            Text(
                text = language,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isDark) Color(0xFF888888) else Color(0xFF666666)
                )
            )
        }
        Text(
            text = code,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(12.dp),
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                color = if (isDark) Color(0xFFE0E0E0) else Color(0xFF333333),
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        )
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
            message = Message(
                id = "preview_user",
                text = "Hello! This is a user message.",
                sender = MessageRole.User
            )
        )
    }
}

@Preview
@Composable
private fun PreviewChatBubbleAssistant() {
    CookTheme {
        ChatBubble(
            message = Message(
                id = "preview_ai",
                text = """Hi there! This is an **assistant** response with a code block:

```kotlin
fun hello() = println("Hello")
""".trimIndent(),
                sender = MessageRole.Assistant
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
            desktopLayout = false
        )
    }
}
