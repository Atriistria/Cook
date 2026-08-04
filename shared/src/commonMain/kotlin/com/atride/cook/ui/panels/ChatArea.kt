package com.atride.cook.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.atride.cook.model.ChatMessage
import com.atride.cook.model.MessageRole
import com.atride.cook.ui.ChatUiState
import com.atride.cook.ui.ChatViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun ChatArea(
    selectedSessionId: String?,
    modifier: Modifier = Modifier,
    onMenuClick: (() -> Unit)? = null,
    onSessionCreated: (String) -> Unit,
    viewModel: ChatViewModel,
) {
    val uiState by remember(selectedSessionId) {
        if (selectedSessionId != null) viewModel.getChatUiState(selectedSessionId)
        else MutableStateFlow(ChatUiState())
    }.collectAsStateWithLifecycle()

    val messages = uiState.messages
    val streamingContent = uiState.streamingText

    Column(modifier = modifier) {
        TopAppBar(
            title = { Text("这里是标题") },
            navigationIcon = {
                if (onMenuClick != null) {
                    IconButton(onClick = onMenuClick) {
                        Text("\u2630")
                    }
                }
            },
        )
        if (selectedSessionId.isNullOrBlank() && messages.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("选择一个会话开始聊天", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp).padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    MessageItem(msg)
                }
                if (uiState.isGenerating && streamingContent.isNotEmpty()) {
                    item {
                        StreamingMessageItem(streamingContent)
                    }
                }
            }
        }
        ChatInputBar(
            selectedModel = "DeepSeek-v4-flash",
            isStreaming = uiState.isGenerating,
            onSend = { text ->
                val targetId = selectedSessionId ?: Uuid.random().toString().also {
                    onSessionCreated(it)
                }
                viewModel.sendMessage(targetId, text)
            },
            onStop = { viewModel.stopGenerating() },
        )
    }
}

@Composable
fun MessageItem(msg: ChatMessage) {
    val isMe = msg.role == MessageRole.USER
    val alignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (isMe) Color(0xFFE8E8E8) else Color.Transparent

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment,
    ) {
        Text(
            text = msg.content,
            modifier = Modifier
                .background(bgColor, RoundedCornerShape(6.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .widthIn(max = 320.dp),
        )
    }
}

@Composable
fun StreamingMessageItem(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = text,
            modifier = Modifier
                .background(Color.Transparent, RoundedCornerShape(6.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .widthIn(max = 320.dp),
        )
    }
}

@Composable
fun ChatInputBar(
    selectedModel: String,
    isStreaming: Boolean = false,
    onSend: (String) -> Unit = {},
    onStop: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        IconButton(onClick = {}) {
            Text("📎")
        }

        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("输入") }
        )

        TextButton(onClick = {}) {
            Text(selectedModel, maxLines = 1)
        }

        IconButton(
            onClick = {
                if (isStreaming) {
                    onStop()
                } else {
                    if (text.isNotBlank()) {
                        onSend(text)
                        text = ""
                    }
                }
            },
            enabled = isStreaming || text.isNotBlank(),
        ) {
            Text(if (isStreaming) "■" else "➤")
        }
    }

}

@Preview
@Composable
fun ChatAreaPreview() {
}