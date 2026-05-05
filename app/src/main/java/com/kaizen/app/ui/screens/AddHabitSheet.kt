package com.kaizen.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.kaizen.app.data.*
import com.kaizen.app.ui.*
import com.kaizen.app.ui.theme.*

private val PRESET_HABITS = mapOf(
    HabitCategory.FLEXIBILITY to listOf("Morning stretch (10 min)","Hip flexor stretch","Hamstring stretch","Thoracic mobility","Neck & shoulder release","Full body stretch"),
    HabitCategory.YOGA        to listOf("Sun salutation","Yin yoga (20 min)","Yoga nidra","Vinyasa flow","Restorative yoga","Breathwork"),
    HabitCategory.HEALTH      to listOf("Drink 8 glasses water","No screens 1hr before bed","Cold shower","Take supplements","8 hours sleep"),
    HabitCategory.FITNESS     to listOf("10k steps","Active recovery walk","Jump rope (10 min)","Foam roll"),
    HabitCategory.MINDFULNESS to listOf("Morning meditation","Gratitude journal","Evening reflection","No phone after 9pm"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitSheet(
    form: AddHabitForm,
    isEditing: Boolean = false,
    onNameChange: (String) -> Unit,
    onCategoryChange: (HabitCategory) -> Unit,
    onSlotChange: (TimeSlot) -> Unit,
    onSubmit: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = K.Card2, contentColor = K.Text) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(if (isEditing) "Edit Habit" else "New Habit", style = MaterialTheme.typography.headlineMedium)
                if (isEditing && onDelete != null) {
                    TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFF87171))) {
                        Text("Delete", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Category
            Text("CATEGORY", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp), color = K.Muted)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(HabitCategory.values()) { cat ->
                    val selected = form.category == cat
                    val color    = cat.color()
                    Surface(modifier = Modifier.clickable { onCategoryChange(cat) }, shape = RoundedCornerShape(100.dp),
                        color = if (selected) color.copy(0.20f) else K.Bg, border = BorderStroke(1.dp, if (selected) color else K.Border)) {
                        Text("${cat.emoji} ${cat.label}", modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (selected) color else K.Muted)
                    }
                }
            }

            // Name
            Text("NAME", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp), color = K.Muted)
            OutlinedTextField(
                value = form.name, onValueChange = onNameChange,
                placeholder   = { Text("e.g. Morning stretch", color = K.Muted) },
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                singleLine    = true,
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = K.Gold, unfocusedBorderColor = K.Border,
                    focusedTextColor = K.Text, unfocusedTextColor = K.Text, cursorColor = K.Gold),
            )

            // Presets
            val presets = PRESET_HABITS[form.category]
            if (!presets.isNullOrEmpty() && !isEditing) {
                Text("QUICK ADD", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp), color = K.Muted)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(presets) { preset ->
                        val catColor = form.category.color()
                        Surface(modifier = Modifier.clickable { onNameChange(preset) }, shape = RoundedCornerShape(8.dp),
                            color = catColor.copy(0.08f), border = BorderStroke(1.dp, catColor.copy(0.25f))) {
                            Text(preset, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 11.sp, color = catColor.copy(0.9f))
                        }
                    }
                }
            }

            // Time slot
            Text("TIME OF DAY", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp), color = K.Muted)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimeSlot.values().forEach { slot ->
                    val selected  = form.slot == slot
                    val slotColor = when (slot) {
                        TimeSlot.MORNING -> K.Morning; TimeSlot.EVENING -> K.Night; TimeSlot.ANYTIME -> Color(0xFFAAAACC)
                    }
                    Surface(modifier = Modifier.weight(1f).clickable { onSlotChange(slot) }, shape = RoundedCornerShape(12.dp),
                        color = if (selected) slotColor.copy(0.18f) else K.Bg, border = BorderStroke(1.dp, if (selected) slotColor else K.Border)) {
                        Column(modifier = Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(slot.emoji, fontSize = 18.sp)
                            Spacer(Modifier.height(2.dp))
                            Text(slot.label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selected) slotColor else K.Muted)
                        }
                    }
                }
            }

            // Submit
            Button(
                onClick = onSubmit, modifier = Modifier.fillMaxWidth().height(54.dp),
                shape   = RoundedCornerShape(14.dp), enabled = form.name.isNotBlank(),
                colors  = ButtonDefaults.buttonColors(containerColor = K.Gold, contentColor = Color(0xFF1A0E00),
                    disabledContainerColor = K.Gold.copy(0.35f), disabledContentColor = Color(0xFF1A0E00).copy(0.5f)),
            ) {
                Text(if (isEditing) "Save Changes" else "Create Habit", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}
