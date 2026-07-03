package com.atride.cook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.atride.cook.model.ChatMessage
import com.atride.cook.model.MessageRole

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
            val codeStartIndex = if (codeLang.isNotEmpty() && codeLang.all { it.isLetterOrDigit() || it == '+' || it == '#' }) 1 else 0
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
