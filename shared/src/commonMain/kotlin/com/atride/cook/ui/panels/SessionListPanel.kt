package com.atride.cook.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.atride.cook.model.Session
import com.atride.cook.ui.ChatViewModel
import com.atride.cook.ui.DevicePreviews
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SessionListPanel(
    selectedSessionId: String,
    onSessionClick: (String) -> Unit,
    onNewSession: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = koinViewModel()
) {

    val sessionList by viewModel.sessionsFlow.collectAsStateWithLifecycle()

    Column(modifier = modifier) {
        TextButton(onClick = onNewSession) {
            Text("+ 新会话")
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            items(sessionList, key = { it.id }) { session ->
                SessionItem(
                    session = session,
                    isSelected = session.id == selectedSessionId,
                    onClick = { onSessionClick(session.id) }
                )
            }
        }

        TextButton(onClick = {}) {
            Text("⚙ 设置")
        }
    }
}

@Composable
fun SessionItem(
    session: Session,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer
    else
        Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(session.title)
    }
}

@DevicePreviews
@Composable
fun SessionPreview() {
    SessionListPanel("",{}, {})
}