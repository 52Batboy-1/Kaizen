package com.kaizen.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.kaizen.app.data.*
import com.kaizen.app.ui.*
import com.kaizen.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private enum class CoachTab { CHAT, JOURNAL, GOALS, WINS }

private val moodEmoji = listOf("😔", "😐", "🙂", "😄", "🔥")
private val moodColor = listOf(
    Color(0xFFF87171), Color(0xFFFACC15), Color(0xFF60A5FA), Color(0xFF4ADE80), Color(0xFFFF9F43)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachScreen(
    state: KaizenUiState,
    journalForm: JournalForm,
    goalForm: GoalForm,
    winForm: WinForm,
    chatInput: String,
    chatMessages: List<ChatMessage>,
    chatLoading: Boolean,
    onJournalText: (String) -> Unit,
    onJournalMood: (Int) -> Unit,
    onJournalTags: (String) -> Unit,
    onSubmitJournal: () -> Unit,
    onOpenAddJournal: () -> Unit,
    onOpenEditJournal: (JournalEntry) -> Unit,
    onDismissJournal: () -> Unit,
    onDeleteJournal: (JournalEntry) -> Unit,
    onGoalTitle: (String) -> Unit,
    onGoalDescription: (String) -> Unit,
    onGoalTargetDate: (String) -> Unit,
    onSubmitGoal: () -> Unit,
    onOpenAddGoal: () -> Unit,
    onOpenEditGoal: (Goal) -> Unit,
    onDismissGoal: () -> Unit,
    onCompleteGoal: (Goal) -> Unit,
    onDeleteGoal: (Goal) -> Unit,
    onWinTitle: (String) -> Unit,
    onWinDescription: (String) -> Unit,
    onSubmitWin: () -> Unit,
    onOpenAddWin: () -> Unit,
    onDismissWin: () -> Unit,
    onDeleteWin: (Win) -> Unit,
    onChatInput: (String) -> Unit,
    onSendChat: () -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var activeTab by remember { mutableStateOf(CoachTab.CHAT) }
    val uriHandler = LocalUriHandler.current

    Column(modifier = modifier.fillMaxSize()) {

        // ── Sub-tab bar ───────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 22.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CoachTab.values().forEach { tab ->
                val sel = tab == activeTab
                val icon = when (tab) {
                    CoachTab.CHAT    -> "✦"
                    CoachTab.JOURNAL -> "📓"
                    CoachTab.GOALS   -> "🎯"
                    CoachTab.WINS    -> "🏆"
                }
                Surface(
                    modifier = Modifier.weight(1f).clickable { activeTab = tab },
                    shape = RoundedCornerShape(12.dp),
                    color = if (sel) K.Gold.copy(0.15f) else K.Card,
                    border = BorderStroke(1.dp, if (sel) K.Gold.copy(0.5f) else K.Border),
                ) {
                    Text(
                        "$icon ${tab.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        modifier = Modifier.padding(vertical = 10.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                        color = if (sel) K.Gold else K.Muted,
                    )
                }
            }
            Surface(
                modifier = Modifier.size(36.dp).clickable {
                    uriHandler.openUri("https://52batboy-1.github.io/Kaizen/")
                },
                shape  = RoundedCornerShape(10.dp),
                color  = K.Card,
                border = BorderStroke(1.dp, K.Border),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("↗", fontSize = 16.sp, color = K.Muted)
                }
            }
        }

        // ── Tab content ───────────────────────────────────────────────────
        AnimatedContent(
            targetState = activeTab,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
            modifier = Modifier.fillMaxSize(),
        ) { tab ->
            when (tab) {
                CoachTab.CHAT    -> ChatTab(
                    state        = state,
                    chatInput    = chatInput,
                    chatMessages = chatMessages,
                    chatLoading  = chatLoading,
                    onInput      = onChatInput,
                    onSend       = onSendChat,
                    onClear      = onClearChat,
                )
                CoachTab.JOURNAL -> JournalTab(state, onOpenAddJournal, onOpenEditJournal, onDeleteJournal)
                CoachTab.GOALS   -> GoalsTab(state, onOpenAddGoal, onOpenEditGoal, onCompleteGoal, onDeleteGoal)
                CoachTab.WINS    -> WinsTab(state, onOpenAddWin, onDeleteWin)
            }
        }
    }

    // ── Journal sheet ─────────────────────────────────────────────────────
    if (state.showAddJournal) {
        ModalBottomSheet(
            onDismissRequest = onDismissJournal,
            containerColor   = K.Card,
            dragHandle       = { BottomSheetDefaults.DragHandle(color = K.Border) },
        ) {
            JournalSheet(
                form      = journalForm,
                isEditing = state.editingJournal != null,
                onText    = onJournalText,
                onMood    = onJournalMood,
                onTags    = onJournalTags,
                onSubmit  = onSubmitJournal,
                onDismiss = onDismissJournal,
            )
        }
    }

    // ── Goal sheet ────────────────────────────────────────────────────────
    if (state.showAddGoal) {
        ModalBottomSheet(
            onDismissRequest = onDismissGoal,
            containerColor   = K.Card,
            dragHandle       = { BottomSheetDefaults.DragHandle(color = K.Border) },
        ) {
            GoalSheet(
                form      = goalForm,
                isEditing = state.editingGoal != null,
                onTitle   = onGoalTitle,
                onDesc    = onGoalDescription,
                onDate    = onGoalTargetDate,
                onSubmit  = onSubmitGoal,
                onDismiss = onDismissGoal,
            )
        }
    }

    // ── Win sheet ─────────────────────────────────────────────────────────
    if (state.showAddWin) {
        ModalBottomSheet(
            onDismissRequest = onDismissWin,
            containerColor   = K.Card,
            dragHandle       = { BottomSheetDefaults.DragHandle(color = K.Border) },
        ) {
            WinSheet(
                form      = winForm,
                onTitle   = onWinTitle,
                onDesc    = onWinDescription,
                onSubmit  = onSubmitWin,
                onDismiss = onDismissWin,
            )
        }
    }
}

// ── Chat tab ──────────────────────────────────────────────────────────────

@Composable
private fun ChatTab(
    state: KaizenUiState,
    chatInput: String,
    chatMessages: List<ChatMessage>,
    chatLoading: Boolean,
    onInput: (String) -> Unit,
    onSend: () -> Unit,
    onClear: () -> Unit,
) {
    val listState = rememberLazyListState()
    val totalItems = chatMessages.size + if (chatLoading) 1 else 0

    LaunchedEffect(totalItems) {
        if (totalItems > 0) listState.animateScrollToItem(totalItems - 1)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state               = listState,
            modifier            = Modifier.weight(1f),
            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (chatMessages.isEmpty() && !chatLoading) {
                item { QuickPromptChips(state, onInput, onSend) }
            } else {
                items(chatMessages) { msg -> ChatBubble(msg) }
                if (chatLoading) item { LoadingBubble() }
            }
        }

        ChatInputBar(
            value     = chatInput,
            onValue   = onInput,
            onSend    = onSend,
            enabled   = !chatLoading,
            showClear = chatMessages.isNotEmpty(),
            onClear   = onClear,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickPromptChips(
    state: KaizenUiState,
    onInput: (String) -> Unit,
    onSend: () -> Unit,
) {
    val prompts = remember(state.hasWhoopData, state.activeInjuries.size, state.goals.size) {
        buildList {
            if (state.hasWhoopData) add("How should I train today?")
            add("Review my training week")
            add("What should I focus on right now?")
            if (state.activeInjuries.isNotEmpty()) add("Injury modification advice")
            if (state.goals.any { it.status == GoalStatus.ACTIVE }) add("Am I on track for my goals?")
            add("Suggest a new goal to set")
            add("How's my sleep affecting performance?")
        }
    }

    Column(
        modifier              = Modifier.fillMaxWidth().padding(top = 32.dp, bottom = 16.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(12.dp),
    ) {
        Surface(shape = RoundedCornerShape(14.dp), color = K.Gold.copy(0.12f)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp)) {
                Text("✦", fontSize = 30.sp, color = K.Gold)
            }
        }
        Text("Kaizen Coach", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = K.Text)
        Text(
            "Powered by Claude ${state.currentTier.label} context",
            fontSize = 12.sp, color = K.Muted,
        )
        Spacer(Modifier.height(4.dp))
        FlowRow(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement   = Arrangement.spacedBy(8.dp),
        ) {
            prompts.forEach { prompt ->
                Surface(
                    modifier = Modifier.clickable { onInput(prompt); onSend() },
                    shape    = RoundedCornerShape(100.dp),
                    color    = K.Card,
                    border   = BorderStroke(1.dp, K.Border),
                ) {
                    Text(
                        prompt,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        fontSize = 12.sp,
                        color    = K.Text,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage) {
    val isUser = msg.role == "user"
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment     = Alignment.Bottom,
    ) {
        if (!isUser) {
            Surface(
                shape    = RoundedCornerShape(8.dp),
                color    = K.Gold.copy(0.12f),
                modifier = Modifier.size(28.dp),
            ) {
                Box(contentAlignment = Alignment.Center) { Text("✦", fontSize = 13.sp, color = K.Gold) }
            }
            Spacer(Modifier.width(8.dp))
        }
        Surface(
            shape = RoundedCornerShape(
                topStart    = if (isUser) 16.dp else 4.dp,
                topEnd      = if (isUser) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd   = 16.dp,
            ),
            color  = if (isUser) K.Gold.copy(0.18f) else K.Card,
            border = BorderStroke(1.dp, if (isUser) K.Gold.copy(0.35f) else K.Border),
            modifier = Modifier.widthIn(max = 290.dp),
        ) {
            Text(
                msg.content,
                modifier  = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                fontSize  = 14.sp,
                color     = K.Text,
                lineHeight = 20.sp,
            )
        }
        if (isUser) Spacer(Modifier.width(8.dp))
    }
}

@Composable
private fun LoadingBubble() {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        Surface(shape = RoundedCornerShape(8.dp), color = K.Gold.copy(0.12f), modifier = Modifier.size(28.dp)) {
            Box(contentAlignment = Alignment.Center) { Text("✦", fontSize = 13.sp, color = K.Gold) }
        }
        Spacer(Modifier.width(8.dp))
        Surface(
            shape  = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            color  = K.Card,
            border = BorderStroke(1.dp, K.Border),
        ) {
            Row(
                modifier              = Modifier.padding(horizontal = 18.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                CoachLoadingDots()
            }
        }
    }
}

@Composable
private fun CoachLoadingDots() {
    val inf = rememberInfiniteTransition(label = "dots")
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { i ->
            val alpha by inf.animateFloat(0.2f, 1f,
                infiniteRepeatable(tween(500, i * 150), RepeatMode.Reverse), label = "dot$i")
            Box(Modifier.size(7.dp).clip(RoundedCornerShape(100.dp)).background(K.Muted.copy(alpha)))
        }
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    onValue: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean,
    showClear: Boolean,
    onClear: () -> Unit,
) {
    val canSend = enabled && value.isNotBlank()
    Surface(color = K.Card2, border = BorderStroke(1.dp, K.Border)) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (showClear) {
                Surface(
                    modifier = Modifier.size(34.dp).clickable { onClear() },
                    shape    = RoundedCornerShape(100.dp),
                    color    = K.Card,
                    border   = BorderStroke(1.dp, K.Border),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✕", fontSize = 12.sp, color = K.Muted)
                    }
                }
            }
            Surface(
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(100.dp),
                color    = K.Card,
                border   = BorderStroke(1.dp, K.Border),
            ) {
                BasicTextField(
                    value           = value,
                    onValueChange   = onValue,
                    modifier        = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    textStyle       = TextStyle(fontSize = 14.sp, color = K.Text),
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction      = ImeAction.Send,
                    ),
                    keyboardActions = KeyboardActions(onSend = { if (canSend) onSend() }),
                    decorationBox   = { inner ->
                        if (value.isEmpty()) Text("Ask your coach...", fontSize = 14.sp, color = K.Muted.copy(0.4f))
                        else inner()
                    },
                )
            }
            Surface(
                modifier = Modifier.size(40.dp).clickable(enabled = canSend) { onSend() },
                shape    = RoundedCornerShape(100.dp),
                color    = if (canSend) K.Gold else K.Card,
                border   = BorderStroke(1.dp, if (canSend) K.Gold else K.Border),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("↑", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = if (canSend) Color(0xFF1A0E00) else K.Muted)
                }
            }
        }
    }
}

// ── Journal tab ───────────────────────────────────────────────────────────

@Composable
private fun JournalTab(
    state: KaizenUiState,
    onAdd: () -> Unit,
    onEdit: (JournalEntry) -> Unit,
    onDelete: (JournalEntry) -> Unit,
) {
    LazyColumn(
        contentPadding      = PaddingValues(start = 22.dp, top = 0.dp, end = 22.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    "${state.journalEntries.size} entr${if (state.journalEntries.size != 1) "ies" else "y"}",
                    color = K.Muted, fontSize = 13.sp,
                )
                Button(
                    onClick        = onAdd,
                    shape          = RoundedCornerShape(100.dp),
                    colors         = ButtonDefaults.buttonColors(containerColor = K.Gold, contentColor = Color(0xFF1A0E00)),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                ) {
                    Text("+ Entry", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        if (state.journalEntries.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 60.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("📓", fontSize = 48.sp)
                        Text("No entries yet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = K.Text)
                        Text("Tap + Entry to start journaling.", fontSize = 12.sp, color = K.Muted)
                    }
                }
            }
        } else {
            items(state.journalEntries, key = { it.id }) { entry ->
                JournalEntryCard(entry, onEdit = { onEdit(entry) }, onDelete = { onDelete(entry) })
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun JournalEntryCard(entry: JournalEntry, onEdit: () -> Unit, onDelete: () -> Unit) {
    val mood   = (entry.mood - 1).coerceIn(0, 4)
    val mColor = moodColor[mood]
    val date   = runCatching { LocalDate.parse(entry.date).format(DateTimeFormatter.ofPattern("EEE, MMM d")) }.getOrElse { entry.date }
    val tags   = entry.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }

    Surface(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onEdit, onLongClick = onDelete),
        shape    = RoundedCornerShape(16.dp),
        color    = K.Card,
        border   = BorderStroke(1.dp, mColor.copy(0.25f)),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(moodEmoji[mood], fontSize = 20.sp)
                    Text(date, fontSize = 11.sp, color = K.Muted)
                }
                Surface(shape = RoundedCornerShape(100.dp), color = mColor.copy(0.15f)) {
                    Text(moodEmoji[mood], modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 10.sp, color = mColor)
                }
            }
            Text(entry.text, fontSize = 14.sp, color = K.Text, lineHeight = 20.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
            if (tags.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tags.take(4).forEach { tag ->
                        Surface(shape = RoundedCornerShape(100.dp), color = K.Gold.copy(0.12f)) {
                            Text("#$tag", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 10.sp, color = K.GoldDim)
                        }
                    }
                }
            }
        }
    }
}

// ── Goals tab ─────────────────────────────────────────────────────────────

@Composable
private fun GoalsTab(
    state: KaizenUiState,
    onAdd: () -> Unit,
    onEdit: (Goal) -> Unit,
    onComplete: (Goal) -> Unit,
    onDelete: (Goal) -> Unit,
) {
    val active    = state.goals.filter { it.status == GoalStatus.ACTIVE }
    val completed = state.goals.filter { it.status == GoalStatus.COMPLETED }

    LazyColumn(
        contentPadding      = PaddingValues(start = 22.dp, top = 0.dp, end = 22.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text("${active.size} active", color = K.Muted, fontSize = 13.sp)
                Button(
                    onClick        = onAdd,
                    shape          = RoundedCornerShape(100.dp),
                    colors         = ButtonDefaults.buttonColors(containerColor = K.Gold, contentColor = Color(0xFF1A0E00)),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                ) {
                    Text("+ Goal", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        if (state.goals.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 60.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🎯", fontSize = 48.sp)
                        Text("No goals yet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = K.Text)
                        Text("Set a goal to stay on track.", fontSize = 12.sp, color = K.Muted)
                    }
                }
            }
        }

        if (active.isNotEmpty()) {
            item { SectionLabel("ACTIVE") }
            items(active, key = { it.id }) { goal ->
                GoalCard(goal, state.habits, onEdit = { onEdit(goal) }, onComplete = { onComplete(goal) }, onDelete = { onDelete(goal) })
            }
        }

        if (completed.isNotEmpty()) {
            item { SectionLabel("COMPLETED") }
            items(completed, key = { it.id }) { goal ->
                GoalCard(goal, state.habits, onEdit = {}, onComplete = {}, onDelete = { onDelete(goal) })
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GoalCard(
    goal: Goal,
    habits: List<HabitWithCompletions>,
    onEdit: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
) {
    val isCompleted  = goal.status == GoalStatus.COMPLETED
    val accentColor  = if (isCompleted) K.Health else K.Night
    val linkedHabits = goal.linkedHabitIds
        .split(",").mapNotNull { it.trim().toLongOrNull() }
        .mapNotNull { id -> habits.find { it.habit.id == id } }
    val dateLabel = runCatching {
        if (goal.targetDate.isBlank()) null
        else LocalDate.parse(goal.targetDate).format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }.getOrNull()

    Surface(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onEdit, onLongClick = onDelete),
        shape    = RoundedCornerShape(16.dp),
        color    = K.Card,
        border   = BorderStroke(1.dp, accentColor.copy(if (isCompleted) 0.5f else 0.3f)),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    goal.title,
                    fontSize       = 15.sp,
                    fontWeight     = FontWeight.Bold,
                    color          = if (isCompleted) K.Muted else K.Text,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    modifier       = Modifier.weight(1f),
                )
                if (isCompleted) {
                    Text("✓", fontSize = 18.sp, color = K.Health, fontWeight = FontWeight.Bold)
                } else {
                    TextButton(onClick = onComplete, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("Done ✓", fontSize = 11.sp, color = K.Health)
                    }
                }
            }
            if (goal.description.isNotBlank()) {
                Text(goal.description, fontSize = 12.sp, color = K.Muted, lineHeight = 17.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (dateLabel != null) {
                    Surface(shape = RoundedCornerShape(100.dp), color = K.Card2) {
                        Text("📅 $dateLabel", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 10.sp, color = K.Muted)
                    }
                }
                linkedHabits.take(2).forEach { hwc ->
                    Surface(shape = RoundedCornerShape(100.dp), color = hwc.habit.category.color().copy(0.12f)) {
                        Text(
                            "${hwc.habit.category.emoji} ${hwc.habit.name}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 10.sp, color = hwc.habit.category.color(),
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

// ── Wins tab ──────────────────────────────────────────────────────────────

@Composable
private fun WinsTab(
    state: KaizenUiState,
    onAdd: () -> Unit,
    onDelete: (Win) -> Unit,
) {
    LazyColumn(
        contentPadding      = PaddingValues(start = 22.dp, top = 0.dp, end = 22.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text("${state.wins.size} win${if (state.wins.size != 1) "s" else ""}", color = K.Muted, fontSize = 13.sp)
                Button(
                    onClick        = onAdd,
                    shape          = RoundedCornerShape(100.dp),
                    colors         = ButtonDefaults.buttonColors(containerColor = K.Gold, contentColor = Color(0xFF1A0E00)),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                ) {
                    Text("+ Win", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        if (state.wins.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 60.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🏆", fontSize = 48.sp)
                        Text("No wins logged yet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = K.Text)
                        Text("Log a win to celebrate progress.", fontSize = 12.sp, color = K.Muted)
                    }
                }
            }
        } else {
            items(state.wins, key = { it.id }) { win ->
                WinCard(win, onDelete = { onDelete(win) })
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WinCard(win: Win, onDelete: () -> Unit) {
    val date = runCatching { LocalDate.parse(win.date).format(DateTimeFormatter.ofPattern("EEE, MMM d")) }.getOrElse { win.date }
    Surface(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = {}, onLongClick = onDelete),
        shape    = RoundedCornerShape(16.dp),
        color    = K.Card,
        border   = BorderStroke(1.dp, K.Streak.copy(0.35f)),
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = RoundedCornerShape(10.dp), color = K.Streak.copy(0.15f), modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) { Text("🏆", fontSize = 22.sp) }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(win.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = K.Text)
                if (win.description.isNotBlank()) {
                    Text(win.description, fontSize = 12.sp, color = K.Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Text(date, fontSize = 10.sp, color = K.Muted)
            }
        }
    }
}

// ── Bottom sheets ─────────────────────────────────────────────────────────

@Composable
private fun JournalSheet(
    form: JournalForm,
    isEditing: Boolean,
    onText: (String) -> Unit,
    onMood: (Int) -> Unit,
    onTags: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(if (isEditing) "Edit Entry" else "New Journal Entry", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = K.Text)

        Text("MOOD", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 2.sp), color = K.GoldDim)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            moodEmoji.forEachIndexed { i, emoji ->
                val selected = form.mood == i + 1
                Surface(
                    modifier = Modifier.size(48.dp).clickable { onMood(i + 1) },
                    shape    = RoundedCornerShape(12.dp),
                    color    = if (selected) moodColor[i].copy(0.2f) else K.Card2,
                    border   = BorderStroke(1.dp, if (selected) moodColor[i].copy(0.6f) else K.Border),
                ) {
                    Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = 22.sp) }
                }
            }
        }

        Text("ENTRY", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 2.sp), color = K.GoldDim)
        Surface(shape = RoundedCornerShape(14.dp), color = K.Card2, border = BorderStroke(1.dp, K.Border)) {
            BasicTextField(
                value           = form.text,
                onValueChange   = onText,
                modifier        = Modifier.fillMaxWidth().padding(14.dp).heightIn(min = 100.dp),
                textStyle       = TextStyle(fontSize = 14.sp, color = K.Text, lineHeight = 20.sp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                decorationBox   = { inner ->
                    if (form.text.isEmpty()) Text("What's on your mind today?", fontSize = 14.sp, color = K.Muted.copy(0.5f))
                    else inner()
                },
            )
        }

        Text("TAGS (comma-separated)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 2.sp), color = K.GoldDim)
        Surface(shape = RoundedCornerShape(14.dp), color = K.Card2, border = BorderStroke(1.dp, K.Border)) {
            BasicTextField(
                value           = form.tags,
                onValueChange   = onTags,
                modifier        = Modifier.fillMaxWidth().padding(14.dp),
                textStyle       = TextStyle(fontSize = 14.sp, color = K.Text),
                singleLine      = true,
                decorationBox   = { inner ->
                    if (form.tags.isEmpty()) Text("recovery, mindset, pr...", fontSize = 14.sp, color = K.Muted.copy(0.5f))
                    else inner()
                },
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, K.Border)) {
                Text("Cancel", color = K.Muted)
            }
            Button(
                onClick  = onSubmit,
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = K.Gold, contentColor = Color(0xFF1A0E00)),
                enabled  = form.text.isNotBlank(),
            ) { Text(if (isEditing) "Save" else "Add Entry", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun GoalSheet(
    form: GoalForm,
    isEditing: Boolean,
    onTitle: (String) -> Unit,
    onDesc: (String) -> Unit,
    onDate: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(if (isEditing) "Edit Goal" else "New Goal", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = K.Text)

        SheetField("GOAL", form.title, onTitle, "e.g. 5 strict pull-ups by June", singleLine = true)
        SheetField("DESCRIPTION (optional)", form.description, onDesc, "Why this matters...", minHeight = 60.dp)
        SheetField("TARGET DATE (optional, YYYY-MM-DD)", form.targetDate, onDate, "2026-06-30", singleLine = true)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, K.Border)) {
                Text("Cancel", color = K.Muted)
            }
            Button(
                onClick  = onSubmit,
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = K.Gold, contentColor = Color(0xFF1A0E00)),
                enabled  = form.title.isNotBlank(),
            ) { Text(if (isEditing) "Save" else "Add Goal", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun WinSheet(
    form: WinForm,
    onTitle: (String) -> Unit,
    onDesc: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Log a Win 🏆", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = K.Text)

        SheetField("WIN", form.title, onTitle, "e.g. First strict pull-up!", singleLine = true)
        SheetField("DETAILS (optional)", form.description, onDesc, "What made it special...", minHeight = 60.dp)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, K.Border)) {
                Text("Cancel", color = K.Muted)
            }
            Button(
                onClick  = onSubmit,
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = K.Streak, contentColor = Color.Black),
                enabled  = form.title.isNotBlank(),
            ) { Text("Log Win", fontWeight = FontWeight.Bold) }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 2.5.sp), color = K.GoldDim)
}

@Composable
private fun SheetField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = false,
    minHeight: Dp = 0.dp,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 2.sp), color = K.GoldDim)
        Surface(shape = RoundedCornerShape(14.dp), color = K.Card2, border = BorderStroke(1.dp, K.Border)) {
            BasicTextField(
                value           = value,
                onValueChange   = onChange,
                modifier        = Modifier.fillMaxWidth().padding(14.dp).then(if (minHeight > 0.dp) Modifier.heightIn(min = minHeight) else Modifier),
                textStyle       = TextStyle(fontSize = 14.sp, color = K.Text, lineHeight = 20.sp),
                singleLine      = singleLine,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                decorationBox   = { inner ->
                    if (value.isEmpty()) Text(placeholder, fontSize = 14.sp, color = K.Muted.copy(0.5f))
                    else inner()
                },
            )
        }
    }
}
