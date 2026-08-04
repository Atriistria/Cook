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

data class Msg(
    val id: String,
    val text: String,
    val isMe: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatArea(
    selectedSessionId: String?,
    modifier: Modifier = Modifier,
    onMenuClick: (() -> Unit)? = null,
) {
    val msgs = listOf(Msg("1", "测试1"), Msg("2", "测试2", true), Msg("3", "测试3"))
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
        if (selectedSessionId.isNullOrBlank()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("选择一个会话开始聊天", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(msgs, key = { it.id }) { msg ->
                    MsgItem(msg)
                }
            }
        }
        ChatInputBar("DeepSeek-v4-flash")
    }

}

@Composable
fun MsgItem(msg: Msg) {
    val alignment = if (msg.isMe) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (msg.isMe) Color(0xFFE8E8E8) else Color.Transparent

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment,
    ) {
        Text(
            text = msg.text,
            modifier = Modifier
                .background(bgColor, RoundedCornerShape(6.dp))
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
                if (isStreaming){}
                else {
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
    ChatArea(null, modifier = Modifier)
}