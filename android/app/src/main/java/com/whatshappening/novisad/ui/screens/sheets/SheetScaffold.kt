@file:OptIn(ExperimentalMaterial3Api::class)

package com.whatshappening.novisad.ui.screens.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.whatshappening.novisad.ui.theme.LocalCatppuccin

// ── SheetScaffold — shared chrome for all bottom sheets ───────────────────────

/**
 * Shared chrome used by [FilterSheet], [DateSheet], and [CategorySheet].
 *
 * Structure:
 *  - M3 [ModalBottomSheet] with 28dp top-corner radius
 *  - Custom pill drag handle
 *  - Pinned header: [title] + optional [titleTrailing] slot
 *  - Vertically-scrollable [content] column
 *  - Pinned bottom action bar ([primaryAction]) with a fade gradient so content
 *    scrolls under it cleanly
 */
@Composable
fun SheetScaffold(
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    title: String,
    titleTrailing: (@Composable () -> Unit)? = null,
    primaryAction: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = { DragHandle() },
    ) {
        Box(Modifier.fillMaxWidth()) {
            // Scrollable content with bottom padding for the pinned action bar
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 100.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 22.dp, end = 22.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(title, style = MaterialTheme.typography.displaySmall)
                    titleTrailing?.invoke()
                }
                Spacer(Modifier.height(18.dp))
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp),
                    content = content,
                )
            }

            // Pinned action bar with gradient fade
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background,
                            )
                        )
                    )
                    .padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 24.dp),
            ) { primaryAction() }
        }
    }
}

// ── DragHandle ────────────────────────────────────────────────────────────────

@Composable
private fun DragHandle() {
    Box(
        modifier = Modifier
            .padding(top = 12.dp, bottom = 4.dp)
            .size(width = 44.dp, height = 4.dp)
            .background(LocalCatppuccin.current.surface1, RoundedCornerShape(2.dp)),
    )
}
