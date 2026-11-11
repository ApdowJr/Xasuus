package com.example.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.ui.components.NoteCard
import com.example.ui.components.NotesSkeletonLayout
import com.example.ui.notes.NoteEditorScreen
import com.example.ui.settings.SettingsDialog
import com.example.ui.state.NotesTab
import com.example.ui.state.UiState
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentRose
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.VibrantViolet
import com.example.ui.viewmodel.NoteViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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

    // Dialog & Screen navigation state
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
            onSave = { title, content, catId, tags, color, isPinned, isFavorite, isArchived ->
                if (targetNote == null) {
                    viewModel.createNote(
                        title = title,
                        content = content,
                        categoryId = catId,
                        tags = tags,
                        color = color,
                        isPinned = isPinned,
                        isFavorite = isFavorite
                    ) {
                        isCreatingNewNote = false
                    }
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
                        isArchived = isArchived
                    ) {
                        editingNote = null
                    }
                }
            },
            onDelete = if (targetNote != null) {
                {
                    viewModel.deleteNote(targetNote.id) {
                        editingNote = null
                    }
                }
            } else null
        )
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchExpanded) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Raadi cinwaan, qoraal ama tag...") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_notes_input"),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Tirtir raadinta")
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricPurple,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ElectricPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "MyNotes",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Xasuus-Qor Casri ah",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isSearchExpanded = !isSearchExpanded
                        if (!isSearchExpanded) viewModel.setSearchQuery("")
                    }) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Raadi",
                            tint = if (isSearchExpanded) ElectricPurple else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { isGridLayout = !isGridLayout }) {
                        Icon(
                            imageVector = if (isGridLayout) Icons.Default.ViewAgenda else Icons.Default.GridView,
                            contentDescription = "Badal Layout",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showCategoriesDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Qeybaha",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Habaynta",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { isCreatingNewNote = true },
                containerColor = ElectricPurple,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Qoraal Cusub", fontWeight = FontWeight.SemiBold) },
                modifier = Modifier.testTag("create_note_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs Row
            TabRow(
                selectedTabIndex = activeTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = ElectricPurple,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeTab.ordinal]),
                        color = ElectricPurple
                    )
                }
            ) {
                NotesTab.values().forEach { tab ->
                    Tab(
                        selected = activeTab == tab,
                        onClick = { viewModel.setActiveTab(tab) },
                        text = {
                            Text(
                                text = tab.label,
                                fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            // Categories Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedCategoryId == null,
                    onClick = { viewModel.setSelectedCategory(null) },
                    label = { Text("Dhammaan") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricPurple,
                        selectedLabelColor = Color.White
                    )
                )

                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategoryId == cat.id,
                        onClick = {
                            viewModel.setSelectedCategory(if (selectedCategoryId == cat.id) null else cat.id)
                        },
                        label = { Text(cat.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricPurple,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                FilterChip(
                    selected = false,
                    onClick = { showCategoriesDialog = true },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Qeyb Cusub")
                        }
                    }
                )
            }

            // Main Content Area
            when (notesState) {
                is UiState.Loading -> {
                    NotesSkeletonLayout(
                        isGrid = isGridLayout,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    )
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = (notesState as UiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                is UiState.Success -> {
                    if (filteredNotes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp)
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(ElectricPurple.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (activeTab) {
                                            NotesTab.ALL -> Icons.Default.NoteAdd
                                            NotesTab.PINNED -> Icons.Default.PushPin
                                            NotesTab.FAVORITES -> Icons.Default.Favorite
                                            NotesTab.ARCHIVED -> Icons.Default.Archive
                                        },
                                        contentDescription = null,
                                        tint = ElectricPurple,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = when (activeTab) {
                                        NotesTab.ALL -> if (searchQuery.isNotEmpty()) "Wax xasuus ah oo raadinta ku habboon lama helin" else "Weli wax xasuus ah ma qorin"
                                        NotesTab.PINNED -> "Weli wax xasuus ah pin kuma aadan dhejin"
                                        NotesTab.FAVORITES -> "Weli wax xasuus ah kuma darin jecleysiga"
                                        NotesTab.ARCHIVED -> "Weli wax xasuus ah kuma jiraan keydka"
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "Isku day eray kale ama tirtir raadinta" else "Guji 'Qoraal Cusub' hoose si aad u bilowdo qorista xasuustaadii koowaad!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        val pinnedNotes = remember(filteredNotes, activeTab) {
                            if (activeTab == NotesTab.ALL) filteredNotes.filter { it.isPinned } else emptyList()
                        }
                        val otherNotes = remember(filteredNotes, activeTab) {
                            if (activeTab == NotesTab.ALL) filteredNotes.filter { !it.isPinned } else filteredNotes
                        }
                        val showSections = activeTab == NotesTab.ALL && pinnedNotes.isNotEmpty() && otherNotes.isNotEmpty()

                        if (isGridLayout) {
                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Fixed(2),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalItemSpacing = 12.dp,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                            ) {
                                if (showSections) {
                                    item(span = StaggeredGridItemSpan.FullLine, key = "pinned_section_header") {
                                        NotesSectionHeader(
                                            title = "DUSHA LA DHIGAY (PINNED)",
                                            count = pinnedNotes.size,
                                            isPinned = true
                                        )
                                    }
                                    items(pinnedNotes, key = { "pinned_${it.id}" }) { note ->
                                        NoteCard(
                                            note = note,
                                            onClick = { editingNote = note },
                                            onTogglePin = { viewModel.togglePin(note) },
                                            onToggleFavorite = { viewModel.toggleFavorite(note) },
                                            onToggleArchive = { viewModel.toggleArchive(note) },
                                            onDelete = { viewModel.deleteNote(note.id) }
                                        )
                                    }
                                    item(span = StaggeredGridItemSpan.FullLine, key = "other_section_header") {
                                        NotesSectionHeader(
                                            title = "XASUUSAHA KALE",
                                            count = otherNotes.size,
                                            isPinned = false
                                        )
                                    }
                                    items(otherNotes, key = { "other_${it.id}" }) { note ->
                                        NoteCard(
                                            note = note,
                                            onClick = { editingNote = note },
                                            onTogglePin = { viewModel.togglePin(note) },
                                            onToggleFavorite = { viewModel.toggleFavorite(note) },
                                            onToggleArchive = { viewModel.toggleArchive(note) },
                                            onDelete = { viewModel.deleteNote(note.id) }
                                        )
                                    }
                                } else {
                                    items(filteredNotes, key = { it.id }) { note ->
                                        NoteCard(
                                            note = note,
                                            onClick = { editingNote = note },
                                            onTogglePin = { viewModel.togglePin(note) },
                                            onToggleFavorite = { viewModel.toggleFavorite(note) },
                                            onToggleArchive = { viewModel.toggleArchive(note) },
                                            onDelete = { viewModel.deleteNote(note.id) }
                                        )
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                            ) {
                                if (showSections) {
                                    item(key = "pinned_section_header") {
                                        NotesSectionHeader(
                                            title = "DUSHA LA DHIGAY (PINNED)",
                                            count = pinnedNotes.size,
                                            isPinned = true
                                        )
                                    }
                                    items(pinnedNotes, key = { "pinned_${it.id}" }) { note ->
                                        NoteCard(
                                            note = note,
                                            onClick = { editingNote = note },
                                            onTogglePin = { viewModel.togglePin(note) },
                                            onToggleFavorite = { viewModel.toggleFavorite(note) },
                                            onToggleArchive = { viewModel.toggleArchive(note) },
                                            onDelete = { viewModel.deleteNote(note.id) }
                                        )
                                    }
                                    item(key = "other_section_header") {
                                        NotesSectionHeader(
                                            title = "XASUUSAHA KALE",
                                            count = otherNotes.size,
                                            isPinned = false
                                        )
                                    }
                                    items(otherNotes, key = { "other_${it.id}" }) { note ->
                                        NoteCard(
                                            note = note,
                                            onClick = { editingNote = note },
                                            onTogglePin = { viewModel.togglePin(note) },
                                            onToggleFavorite = { viewModel.toggleFavorite(note) },
                                            onToggleArchive = { viewModel.toggleArchive(note) },
                                            onDelete = { viewModel.deleteNote(note.id) }
                                        )
                                    }
                                } else {
                                    items(filteredNotes, key = { it.id }) { note ->
                                        NoteCard(
                                            note = note,
                                            onClick = { editingNote = note },
                                            onTogglePin = { viewModel.togglePin(note) },
                                            onToggleFavorite = { viewModel.toggleFavorite(note) },
                                            onToggleArchive = { viewModel.toggleArchive(note) },
                                            onDelete = { viewModel.deleteNote(note.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
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

@Composable
fun NotesSectionHeader(
    title: String,
    count: Int,
    isPinned: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isPinned) {
            Icon(
                imageVector = Icons.Filled.PushPin,
                contentDescription = null,
                tint = AccentAmber,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = if (isPinned) AccentAmber else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(
                    if (isPinned) AccentAmber.copy(alpha = 0.18f)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isPinned) AccentAmber else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
