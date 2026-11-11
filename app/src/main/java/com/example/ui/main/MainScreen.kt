package com.example.ui.main

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Category
import com.example.data.model.Note
import com.example.data.repository.NoteRepository
import com.example.ui.auth.performSignOut
import com.example.ui.categories.CategoriesDialog
import com.example.ui.components.ActionButton
import com.example.ui.components.Eyebrow
import com.example.ui.components.Hairline
import com.example.ui.components.NoteCard
import com.example.ui.components.NotesSkeletonLayout
import com.example.ui.components.Pill
import com.example.ui.components.SoftIconButton
import com.example.ui.components.categoryLabel
import com.example.ui.components.clickableCompat
import com.example.ui.components.toneFor
import com.example.ui.notes.NoteEditorScreen
import com.example.ui.notifications.ReminderNotifications
import com.example.ui.settings.SettingsDialog
import com.example.ui.state.NotesTab
import com.example.ui.state.UiState
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentRose
import com.example.ui.theme.XasuusTheme
import com.example.ui.viewmodel.NoteViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

/** Bottom navigation destinations. Notes is the home surface, categories the library. */
private enum class HomeNav { NOTES, CATEGORIES }

@Composable
fun MainScreen(
    currentUserId: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val repository = remember(currentUserId) { NoteRepository(context) }
    val viewModel: NoteViewModel = viewModel(
        key = currentUserId,
        factory = NoteViewModel.Factory(repository)
    )

    val notesState by viewModel.notesState.collectAsStateWithLifecycle()
    val categoriesState by viewModel.categoriesState.collectAsStateWithLifecycle()
    val filteredNotes by viewModel.filteredNotes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val operationMessage by viewModel.operationMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var isSearchExpanded by remember { mutableStateOf(false) }
    var isGridLayout by remember { mutableStateOf(true) }
    var nav by remember { mutableStateOf(HomeNav.NOTES) }

    // Dialog & screen navigation state
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var isCreatingNewNote by remember { mutableStateOf(false) }
    var showCategoriesDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val rawNotes = (notesState as? UiState.Success)?.data ?: emptyList()
    val categories = (categoriesState as? UiState.Success)?.data ?: emptyList()

    LaunchedEffect(operationMessage) {
        operationMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearOperationMessage()
        }
    }

    // Subscreen: Note Editor
    if (isCreatingNewNote || editingNote != null) {
        val targetNote = editingNote
        NoteEditorScreen(
            initialNote = targetNote,
            categories = categories,
            onBack = {
                isCreatingNewNote = false
                editingNote = null
            },
            onShare = { title, content, tags ->
                val shareText = buildString {
                    appendLine(title.ifBlank { "Xasuus" })
                    if (content.isNotBlank()) appendLine("\n$content")
                    if (tags.isNotEmpty()) appendLine("\n" + tags.joinToString(" ") { "#$it" })
                }
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, title.ifBlank { "Xasuus" })
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(sendIntent, "La wadaag xasuusta"))
            },
            onSave = { title, content, catId, tags, color, isPinned, isFavorite, isArchived, reminderAtMillis ->
                if (targetNote == null) {
                    viewModel.createNote(
                        title = title,
                        content = content,
                        categoryId = catId,
                        tags = tags,
                        color = color,
                        isPinned = isPinned,
                        isFavorite = isFavorite,
                        reminderAtMillis = reminderAtMillis,
                        onSuccess = { newId ->
                            ReminderNotifications.schedule(context, newId, title, reminderAtMillis)
                            isCreatingNewNote = false
                        }
                    )
                } else {
                    viewModel.updateNote(
                        id = targetNote.id,
                        title = title,
                        content = content,
                        categoryId = catId,
                        tags = tags,
                        color = color,
                        isPinned = isPinned,
                        isFavorite = isFavorite,
                        isArchived = isArchived,
                        reminderAtMillis = reminderAtMillis
                    ) {
                        ReminderNotifications.schedule(context, targetNote.id, title, reminderAtMillis)
                        editingNote = null
                    }
                }
            },
            onDelete = if (targetNote != null) {
                {
                    viewModel.deleteNote(targetNote.id) {
                        ReminderNotifications.cancel(context, targetNote.id)
                        editingNote = null
                    }
                }
            } else null
        )
        return
    }

    Scaffold(
        containerColor = XasuusTheme.colors.canvas,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 96.dp)
            )
        },
        floatingActionButton = {
            if (nav == HomeNav.NOTES) {
                CreateNoteFab(
                    modifier = Modifier.padding(bottom = 92.dp),
                    onClick = { isCreatingNewNote = true }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                AnimatedContent(
                    targetState = nav,
                    transitionSpec = {
                        (fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 24 })
                            .togetherWith(fadeOut(tween(150)))
                    },
                    label = "home_nav_content"
                ) { destination ->
                    when (destination) {
                        HomeNav.NOTES -> NotesHome(
                            notesState = notesState,
                            filteredNotes = filteredNotes,
                            categories = categories,
                            activeTab = activeTab,
                            selectedCategoryId = selectedCategoryId,
                            searchQuery = searchQuery,
                            isSearchExpanded = isSearchExpanded,
                            isGridLayout = isGridLayout,
                            onToggleSearch = {
                                isSearchExpanded = !isSearchExpanded
                                if (!isSearchExpanded) viewModel.setSearchQuery("")
                            },
                            onSearchChange = viewModel::setSearchQuery,
                            onToggleLayout = { isGridLayout = !isGridLayout },
                            onSelectTab = viewModel::setActiveTab,
                            onSelectCategory = viewModel::setSelectedCategory,
                            onOpenCategories = { showCategoriesDialog = true },
                            onOpenSettings = { showSettingsDialog = true },
                            onEditNote = { editingNote = it },
                            onCreateNote = { isCreatingNewNote = true },
                            onTogglePin = viewModel::togglePin,
                            onToggleFavorite = viewModel::toggleFavorite,
                            onToggleArchive = viewModel::toggleArchive,
                            onDeleteNote = { noteId ->
                                viewModel.deleteNote(noteId) {
                                    ReminderNotifications.cancel(context, noteId)
                                }
                            }
                        )

                        HomeNav.CATEGORIES -> CategoriesHome(
                            notes = rawNotes,
                            categories = categories,
                            selectedCategoryId = selectedCategoryId,
                            onSelectCategory = { id ->
                                viewModel.setSelectedCategory(id)
                                viewModel.setActiveTab(NotesTab.ALL)
                                nav = HomeNav.NOTES
                            },
                            onOpenCategories = { showCategoriesDialog = true }
                        )
                    }
                }
            }

            FloatingNavBar(
                current = nav,
                onSelect = { nav = it },
                onOpenSettings = { showSettingsDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 14.dp)
            )
        }
    }

    // Categories Dialog
    if (showCategoriesDialog) {
        CategoriesDialog(
            categories = categories,
            onDismiss = { showCategoriesDialog = false },
            onCreateCategory = { name, color, icon ->
                viewModel.createCategory(name, color, icon)
            },
            onDeleteCategory = { catId ->
                viewModel.deleteCategory(catId)
            }
        )
    }

    // Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            notes = rawNotes,
            onDismiss = { showSettingsDialog = false },
            onSignOut = {
                performSignOut(credentialManager, scope)
            }
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Notes home
// ---------------------------------------------------------------------------------------------

@Composable
private fun NotesHome(
    notesState: UiState<List<Note>>,
    filteredNotes: List<Note>,
    categories: List<Category>,
    activeTab: NotesTab,
    selectedCategoryId: String?,
    searchQuery: String,
    isSearchExpanded: Boolean,
    isGridLayout: Boolean,
    onToggleSearch: () -> Unit,
    onSearchChange: (String) -> Unit,
    onToggleLayout: () -> Unit,
    onSelectTab: (NotesTab) -> Unit,
    onSelectCategory: (String?) -> Unit,
    onOpenCategories: () -> Unit,
    onOpenSettings: () -> Unit,
    onEditNote: (Note) -> Unit,
    onCreateNote: () -> Unit,
    onTogglePin: (Note) -> Unit,
    onToggleFavorite: (Note) -> Unit,
    onToggleArchive: (Note) -> Unit,
    onDeleteNote: (String) -> Unit
) {
    val colors = XasuusTheme.colors
    val totalNotes = (notesState as? UiState.Success)?.data?.count { !it.isArchived } ?: 0
    val pinnedCount = (notesState as? UiState.Success)?.data?.count { it.isPinned && !it.isArchived } ?: 0

    Column(modifier = Modifier.fillMaxSize()) {
        // ------------------------------------------------------------------ header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = greeting(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        letterSpacing = 0.6.sp
                    ),
                    color = colors.inkFaint
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Xasuusaadayda",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 27.sp),
                    color = colors.ink
                )
            }

            SoftIconButton(
                icon = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                contentDescription = "Raadi",
                onClick = onToggleSearch,
                tint = if (isSearchExpanded) colors.accent else colors.ink
            )
            Spacer(modifier = Modifier.width(8.dp))
            SoftIconButton(
                icon = if (isGridLayout) Icons.Default.ViewAgenda else Icons.Default.GridView,
                contentDescription = "Beddel muuqaalka",
                onClick = onToggleLayout
            )
            Spacer(modifier = Modifier.width(8.dp))
            SoftIconButton(
                icon = Icons.Default.Settings,
                contentDescription = "Habaynta",
                onClick = onOpenSettings,
                modifier = Modifier.testTag("settings_button")
            )
        }

        // ------------------------------------------------------------------ search field
        AnimatedVisibility(
            visible = isSearchExpanded,
            enter = fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.96f),
            exit = fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.98f)
        ) {
            SearchField(
                query = searchQuery,
                onQueryChange = onSearchChange,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp)
            )
        }

        // ------------------------------------------------------------------ stat summary
        if (!isSearchExpanded) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatPill(
                    label = "$totalNotes xasuus",
                    icon = Icons.Default.EditNote,
                    tint = colors.accent,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    label = "$pinnedCount la xushay",
                    icon = Icons.Default.PushPin,
                    tint = AccentAmber,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    label = "${categories.size} qeyb",
                    icon = Icons.Outlined.FolderOpen,
                    tint = colors.inkMuted,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenCategories
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ------------------------------------------------------------------ segmented tabs
        SegmentedTabs(
            activeTab = activeTab,
            onSelectTab = onSelectTab,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        // ------------------------------------------------------------------ category rail
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Pill(
                text = "Dhammaan",
                selected = selectedCategoryId == null,
                onClick = { onSelectCategory(null) }
            )
            categories.forEach { cat ->
                val tone = toneFor(cat.color)
                Pill(
                    text = cat.name,
                    selected = selectedCategoryId == cat.id,
                    tone = tone,
                    onClick = {
                        onSelectCategory(if (selectedCategoryId == cat.id) null else cat.id)
                    }
                )
            }
            Pill(
                text = "Qeyb cusub",
                leadingIcon = Icons.Default.Add,
                onClick = onOpenCategories
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ------------------------------------------------------------------ content
        when (notesState) {
            is UiState.Loading -> {
                NotesSkeletonLayout(isGrid = isGridLayout, modifier = Modifier.fillMaxSize())
            }

            is UiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.errorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Xasuusaha lama soo qaadi karin",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.ink,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = (notesState as UiState.Error).message,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }

            is UiState.Success -> {
                if (filteredNotes.isEmpty()) {
                    EmptyState(
                        activeTab = activeTab,
                        hasSearch = searchQuery.isNotBlank(),
                        onCreateNote = onCreateNote
                    )
                } else {
                    val pinned = if (activeTab == NotesTab.ALL) filteredNotes.filter { it.isPinned } else emptyList()
                    val others = if (activeTab == NotesTab.ALL) filteredNotes.filter { !it.isPinned } else filteredNotes
                    val sections = pinned.isNotEmpty() && others.isNotEmpty()

                    if (isGridLayout) {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 190.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalItemSpacing = 12.dp,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (sections) {
                                item(span = StaggeredGridItemSpan.FullLine, key = "pinned_header") {
                                    SectionHeader("La xushay", pinned.size, AccentAmber, Icons.Default.PushPin)
                                }
                                items(pinned, key = { "p_${it.id}" }) { note ->
                                    NoteTile(note, onEditNote, onTogglePin, onToggleFavorite, onToggleArchive, onDeleteNote)
                                }
                                item(span = StaggeredGridItemSpan.FullLine, key = "others_header") {
                                    SectionHeader("Xasuusaha kale", others.size, colors.inkFaint, null)
                                }
                                items(others, key = { "o_${it.id}" }) { note ->
                                    NoteTile(note, onEditNote, onTogglePin, onToggleFavorite, onToggleArchive, onDeleteNote)
                                }
                            } else {
                                items(filteredNotes, key = { it.id }) { note ->
                                    NoteTile(note, onEditNote, onTogglePin, onToggleFavorite, onToggleArchive, onDeleteNote)
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 190.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (sections) {
                                item(key = "pinned_header") {
                                    SectionHeader("La xushay", pinned.size, AccentAmber, Icons.Default.PushPin)
                                }
                                items(pinned, key = { "p_${it.id}" }) { note ->
                                    NoteTile(note, onEditNote, onTogglePin, onToggleFavorite, onToggleArchive, onDeleteNote)
                                }
                                item(key = "others_header") {
                                    SectionHeader("Xasuusaha kale", others.size, colors.inkFaint, null)
                                }
                                items(others, key = { "o_${it.id}" }) { note ->
                                    NoteTile(note, onEditNote, onTogglePin, onToggleFavorite, onToggleArchive, onDeleteNote)
                                }
                            } else {
                                items(filteredNotes, key = { it.id }) { note ->
                                    NoteTile(note, onEditNote, onTogglePin, onToggleFavorite, onToggleArchive, onDeleteNote)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteTile(
    note: Note,
    onEditNote: (Note) -> Unit,
    onTogglePin: (Note) -> Unit,
    onToggleFavorite: (Note) -> Unit,
    onToggleArchive: (Note) -> Unit,
    onDeleteNote: (String) -> Unit
) {
    NoteCard(
        note = note,
        onClick = { onEditNote(note) },
        onTogglePin = { onTogglePin(note) },
        onToggleFavorite = { onToggleFavorite(note) },
        onToggleArchive = { onToggleArchive(note) },
        onDelete = { onDeleteNote(note.id) }
    )
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    tint: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(6.dp))
        }
        Eyebrow(text = title, color = tint)
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(XasuusTheme.colors.recessed)
                .padding(horizontal = 7.dp, vertical = 1.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = XasuusTheme.colors.inkMuted
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Hairline(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = XasuusTheme.colors
    val base = modifier
        .clip(RoundedCornerShape(14.dp))
        .background(colors.card)
        .border(1.dp, colors.hairline, RoundedCornerShape(14.dp))
    val clickable = if (onClick != null) base.clickableCompat(onClick) else base

    Row(
        modifier = clickable.padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
            color = colors.inkMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SegmentedTabs(
    activeTab: NotesTab,
    onSelectTab: (NotesTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = XasuusTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.recessed)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val labels = mapOf(
            NotesTab.ALL to "Dhammaan",
            NotesTab.PINNED to "La xushay",
            NotesTab.FAVORITES to "Jeceshahay",
            NotesTab.ARCHIVED to "Keydsan"
        )
        NotesTab.values().forEach { tab ->
            val selected = tab == activeTab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) colors.card else Color.Transparent)
                    .clickableCompat { onSelectTab(tab) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = labels[tab] ?: tab.label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 11.5.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (selected) colors.ink else colors.inkMuted,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = XasuusTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.card)
            .border(1.dp, colors.hairline, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = colors.inkFaint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = "Raadi cinwaan, qoraal ama tag…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkFaint
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.ink),
                cursorBrush = SolidColor(colors.accent),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_notes_input")
            )
        }
        if (query.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Tirtir raadinta",
                tint = colors.inkMuted,
                modifier = Modifier
                    .size(16.dp)
                    .clickableCompat { onQueryChange("") }
            )
        }
    }
}

@Composable
private fun EmptyState(
    activeTab: NotesTab,
    hasSearch: Boolean,
    onCreateNote: () -> Unit
) {
    val colors = XasuusTheme.colors
    val (icon, title, subtitle) = when {
        hasSearch -> Triple(
            Icons.Outlined.Search,
            "Wax natiijo ah lama helin",
            "Isku day eray kale, ama tirtir raadinta si aad dhammaan xasuusaha u aragto."
        )
        activeTab == NotesTab.PINNED -> Triple(
            Icons.Default.PushPin,
            "Weli wax xasuus ah pin kuma aadan dhejin",
            "Guji xasuus kasta oo aad jeceshahay oo ku dheji dusha sare."
        )
        activeTab == NotesTab.FAVORITES -> Triple(
            Icons.Default.Favorite,
            "Liiska jeceshahay waa madhan",
            "Xasuusaha aad jeceshahay waxay halkan ku soo ururayaan."
        )
        activeTab == NotesTab.ARCHIVED -> Triple(
            Icons.Default.Archive,
            "Keydkaagu waa madhan yahay",
            "Xasuusaha aadan hadda u baahnayn geli keydka si ay u nadiif noqoto."
        )
        else -> Triple(
            Icons.Default.NoteAdd,
            "Bilow xasuustaadaada koowaad",
            "Qor fikrad, shaqo ama wax xusuus leh — wuxuu si toos ah ugu kaydsanayaa cloud-ka."
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(colors.accentWash),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
            color = colors.ink,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = colors.inkMuted,
            textAlign = TextAlign.Center
        )
        if (!hasSearch && activeTab == NotesTab.ALL) {
            Spacer(modifier = Modifier.height(22.dp))
            ActionButton(
                text = "Qor xasuus cusub",
                icon = Icons.Default.Add,
                onClick = onCreateNote,
                container = colors.accent,
                contentColor = Color.White,
                modifier = Modifier.width(220.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Categories home ("library")
// ---------------------------------------------------------------------------------------------

@Composable
private fun CategoriesHome(
    notes: List<Note>,
    categories: List<Category>,
    selectedCategoryId: String?,
    onSelectCategory: (String) -> Unit,
    onOpenCategories: () -> Unit
) {
    val colors = XasuusTheme.colors
    val uncategorized = notes.count { it.categoryId.isBlank() || it.categoryId.equals("general", true) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Maktabaddaada",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        letterSpacing = 0.6.sp
                    ),
                    color = colors.inkFaint
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Qeybaha",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 27.sp),
                    color = colors.ink
                )
            }
            SoftIconButton(
                icon = Icons.Default.Add,
                contentDescription = "Ku dar qeyb",
                onClick = onOpenCategories,
                container = colors.inkPill,
                borderColor = Color.Transparent,
                tint = colors.onInkPill
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 190.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (categories.isEmpty() && uncategorized == 0) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = null,
                            tint = colors.inkFaint,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Weli qeyb ma aadan abuurin",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.ink
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Qeybuhu waxay xasuusaha u habeeyaan sida buugaag maktabad.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkMuted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        ActionButton(
                            text = "Abuur qeyb",
                            icon = Icons.Default.Add,
                            onClick = onOpenCategories,
                            container = colors.accent,
                            contentColor = Color.White,
                            modifier = Modifier.width(200.dp)
                        )
                    }
                }
            }

            items(categories, key = { it.id }) { cat ->
                val tone = toneFor(cat.color)
                val count = notes.count { it.categoryId.equals(cat.id, ignoreCase = true) }
                CategoryTile(
                    name = cat.name,
                    count = count,
                    tone = tone,
                    selected = selectedCategoryId == cat.id,
                    onClick = { onSelectCategory(cat.id) }
                )
            }

            if (uncategorized > 0) {
                item {
                    CategoryTile(
                        name = "Guud",
                        count = uncategorized,
                        tone = toneFor("slate"),
                        selected = selectedCategoryId == "general",
                        onClick = { onSelectCategory("general") }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryTile(
    name: String,
    count: Int,
    tone: com.example.ui.components.Tone,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = XasuusTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(tone.wash)
            .border(
                1.5.dp,
                if (selected) tone.accent else Color.Transparent,
                RoundedCornerShape(20.dp)
            )
            .clickableCompat(onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = if (colors.isDark) 0.10f else 0.72f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.FolderOpen,
                contentDescription = null,
                tint = tone.accent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (count == 1) "1 xasuus" else "$count xasuus",
                style = MaterialTheme.typography.labelSmall,
                color = tone.accent.copy(alpha = 0.85f)
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = tone.accent.copy(alpha = 0.55f),
            modifier = Modifier.size(20.dp)
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Floating chrome: FAB + pill nav bar
// ---------------------------------------------------------------------------------------------

@Composable
private fun CreateNoteFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = XasuusTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(colors.accent)
            .clickableCompat(onClick)
            .testTag("create_note_fab")
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(19.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Qoraal cusub",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
    }
}

@Composable
private fun FloatingNavBar(
    current: HomeNav,
    onSelect: (HomeNav) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = XasuusTheme.colors
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(colors.inkPill)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        NavItem(
            icon = Icons.Outlined.Home,
            active = current == HomeNav.NOTES,
            label = "Xasuus",
            onClick = { onSelect(HomeNav.NOTES) }
        )
        NavItem(
            icon = Icons.Outlined.GridView,
            active = current == HomeNav.CATEGORIES,
            label = "Qeybaha",
            onClick = { onSelect(HomeNav.CATEGORIES) }
        )
        NavItem(
            icon = Icons.Outlined.Archive,
            active = false,
            label = "Keydka",
            onClick = { onSelect(HomeNav.NOTES) }
        )
        NavItem(
            icon = Icons.Outlined.Settings,
            active = false,
            label = "Habayn",
            onClick = onOpenSettings
        )
    }
}

@Composable
private fun NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    label: String,
    onClick: () -> Unit
) {
    val colors = XasuusTheme.colors
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(if (active) colors.accent else Color.Transparent)
            .clickableCompat(onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) Color.White else colors.onInkPill.copy(alpha = 0.62f),
            modifier = Modifier.size(21.dp)
        )
    }
}

// ---------------------------------------------------------------------------------------------

private fun greeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..4 -> "Habeen wanaagsan"
        in 5..11 -> "Subax wanaagsan"
        in 12..16 -> "Duhur wanaagsan"
        in 17..21 -> "Galab wanaagsan"
        else -> "Habeen wanaagsan"
    }
}
