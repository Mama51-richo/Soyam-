package com.example.soyamgebeya.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.soyamgebeya.data.model.ChatMessage
import com.example.soyamgebeya.data.model.Listing
import com.example.soyamgebeya.data.model.MarketplaceCategory
import com.example.soyamgebeya.viewmodel.AppLanguage
import com.example.soyamgebeya.viewmodel.SoyamViewModel
import java.text.SimpleDateFormat
import java.util.*

// Color Companion extensions for lowercase compatibilities
val Color.Companion.white get() = Color.White
val Color.Companion.black get() = Color.Black
val Color.Companion.gray get() = Color.Gray
val Color.Companion.lightGray get() = Color.LightGray
val Color.Companion.transparent get() = Color.Transparent

val SoyamPrimaryGreen = Color(0xFF009639) // Ethiopian Green
val SoyamYellow = Color(0xFFFFD100)        // Ethiopian Yellow
val SoyamPrimaryRed = Color(0xFFEF3340)   // Ethiopian Red
val SoyamDarkBg = Color(0xFF121417)
val SoyamCardBg = Color(0xFF1A1F26)
val OrangeAccent = Color(0xFFFF7E36)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SoyamApp(viewModel: SoyamViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedListingDetail by viewModel.selectedListingDetail.collectAsState()

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = SoyamPrimaryGreen,
            secondary = SoyamYellow,
            tertiary = SoyamPrimaryRed,
            background = SoyamDarkBg,
            surface = SoyamCardBg,
            onPrimary = Color.white,
            onSecondary = Color.black,
            onBackground = Color.white,
            onSurface = Color.white
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            color = SoyamDarkBg
        ) {
            Scaffold(
                bottomBar = {
                    if (selectedListingDetail == null) {
                        SoyamBottomBar(
                            currentTab = currentTab,
                            onTabSelected = { viewModel.currentTab.value = it },
                            viewModel = viewModel
                        )
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    AnimatedContent(
                        targetState = selectedListingDetail,
                        transitionSpec = {
                            slideInHorizontally { width -> width } with slideOutHorizontally { width -> -width }
                        },
                        label = "main_transition"
                    ) { listing ->
                        if (listing != null) {
                            ListingDetailScreen(
                                listing = listing,
                                onBack = { viewModel.selectListing(null) },
                                viewModel = viewModel
                            )
                        } else {
                            when (currentTab) {
                                "Home" -> HomeScreen(viewModel = viewModel)
                                "Marketplace" -> MarketplaceScreen(viewModel = viewModel)
                                "Sell" -> SellScreen(viewModel = viewModel)
                                "Messages" -> MessagesScreen(viewModel = viewModel)
                                "Profile" -> ProfileScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SoyamBottomBar(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    viewModel: SoyamViewModel
) {
    NavigationBar(
        containerColor = SoyamCardBg,
        tonalElevation = 6.dp,
        modifier = Modifier.testTag("soyam_bottom_navigation")
    ) {
        val navItems = listOf(
            Triple("Home", Icons.Filled.Home, "home"),
            Triple("Marketplace", Icons.Filled.Store, "marketplace"),
            Triple("Sell", Icons.Filled.AddCircle, "sell"),
            Triple("Messages", Icons.Filled.Chat, "messages"),
            Triple("Profile", Icons.Filled.Person, "profile")
        )

        navItems.forEach { (tab, icon, labelKey) ->
            val label = viewModel.getTranslation(labelKey)
            NavigationBarItem(
                selected = currentTab == tab,
                onClick = { onTabSelected(tab) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SoyamYellow,
                    unselectedIconColor = Color.lightGray,
                    selectedTextColor = SoyamYellow,
                    unselectedTextColor = Color.lightGray,
                    indicatorColor = SoyamPrimaryGreen
                ),
                modifier = Modifier.testTag("nav_tab_$tab")
            )
        }
    }
}

// ======================= HOME SCREEN =======================

@Composable
fun HomeScreen(viewModel: SoyamViewModel) {
    val query by viewModel.searchQuery.collectAsState()
    val listings by viewModel.listings.collectAsState()
    val language by viewModel.selectedLanguage.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SoyamDarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Top Title & Logo Frame
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(SoyamPrimaryGreen, SoyamYellow, SoyamPrimaryRed)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_icon_fg_1782123977312),
                            contentDescription = "Soyam Logo",
                            modifier = Modifier.size(36.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Soyam Gebeya",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.white,
                            fontFamily = FontFamily.SansSerif
                        )
                        Text(
                            text = viewModel.getTranslation("app_tagline"),
                            fontSize = 11.sp,
                            color = SoyamYellow,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Global Language Selector Toggles
                LanguagePicker(viewModel)
            }
        }

        // Modern Unified Search Bar
        item {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    viewModel.searchQuery.value = it
                    if (it.isNotEmpty()) {
                        viewModel.currentTab.value = "Marketplace"
                    }
                },
                placeholder = {
                    Text(
                        viewModel.getTranslation("search_hint"),
                        color = Color.lightGray,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = SoyamPrimaryGreen) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SoyamPrimaryGreen,
                    unfocusedBorderColor = Color.gray,
                    focusedContainerColor = SoyamCardBg,
                    unfocusedContainerColor = SoyamCardBg
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_search_input"),
                singleLine = true
            )
        }

        // Feature Banner Frame (Uses Generated Asset)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SoyamCardBg)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner_1782123964612),
                    contentDescription = "Soyam Hero Board",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Backdrop Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(SoyamPrimaryRed, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SPECIAL PROMO",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.white
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Empowering Ethiopia's Digital Economy",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.white
                    )
                    Text(
                        text = "Pay secure via Telebirr or CBEBirr escrow",
                        fontSize = 11.sp,
                        color = SoyamYellow,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Popular Categories Carousel
        item {
            Text(
                text = "Hunduu Gabaa (Explore All)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.white
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(MarketplaceCategory.values()) { cat ->
                    val displayName = if (language == AppLanguage.ENGLISH) cat.displayName else cat.amharicName
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SoyamCardBg)
                            .border(1.dp, Color.gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.selectedCategory.value = cat
                                viewModel.currentTab.value = "Marketplace"
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                getCategoryIcon(cat),
                                contentDescription = displayName,
                                tint = SoyamPrimaryGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = displayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.white
                            )
                        }
                    }
                }
            }
        }

        // Nearby Opportunities / Microtasks Simulator
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = viewModel.getTranslation("nearby"),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.white
                )
                Text(
                    text = "Addis Ababa / Bole",
                    fontSize = 11.sp,
                    color = SoyamYellow,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SoyamCardBg),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SoyamPrimaryGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.LocationOn, contentDescription = "GPS", tint = SoyamPrimaryGreen)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Mercato Delivery Support Needed",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.white
                        )
                        Text(
                            "Distance: 4.8 km • Pays 300 ETB hourly",
                            fontSize = 11.sp,
                            color = Color.gray
                        )
                    }
                    Button(
                        onClick = {
                            viewModel.sendMessageToThread(
                                threadId = "Near-1",
                                text = "Selam! I saw your Mercato delivery request nearby. I am ready to start right now."
                            )
                            viewModel.activeChatThread.value = "Near-1"
                            viewModel.currentTab.value = "Messages"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoyamPrimaryGreen),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Apply", fontSize = 11.sp, color = Color.white)
                    }
                }
            }
        }

        // Trending Listings List Grid
        item {
            Text(
                text = "${viewModel.getTranslation("trending")} 🔥",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.white
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(listings.take(3)) { listing ->
            ListingItemCard(listing = listing, viewModel = viewModel)
        }

        // Informative Platform Metrics banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(SoyamCardBg, SoyamPrimaryGreen.copy(alpha = 0.2f))
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("100K+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SoyamYellow)
                        Text("Users Active", fontSize = 10.sp, color = Color.lightGray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("500+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SoyamYellow)
                        Text("Verify Badges", fontSize = 10.sp, color = Color.lightGray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("50M+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SoyamYellow)
                        Text("ETB Circulated", fontSize = 10.sp, color = Color.lightGray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun LanguagePicker(viewModel: SoyamViewModel) {
    var showMenu by remember { mutableStateOf(false) }
    val currentLang by viewModel.selectedLanguage.collectAsState()

    Box {
        Button(
            onClick = { showMenu = true },
            colors = ButtonDefaults.buttonColors(containerColor = SoyamCardBg),
            border = BorderStroke(1.dp, SoyamYellow.copy(alpha = 0.8f)),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(Icons.Filled.Translate, contentDescription = "Language", tint = SoyamYellow, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = when (currentLang) {
                    AppLanguage.ENGLISH -> "EN"
                    AppLanguage.AMHARIC -> "አማ"
                    AppLanguage.AFAAN_OROMO -> "ORM"
                    AppLanguage.SOMALI -> "SOM"
                    AppLanguage.TIGRINYA -> "ትግ"
                },
                fontSize = 11.sp,
                color = Color.white,
                fontWeight = FontWeight.Bold
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(SoyamCardBg)
        ) {
            DropdownMenuItem(
                text = { Text("English", color = Color.white) },
                onClick = { viewModel.updateLanguage(AppLanguage.ENGLISH); showMenu = false }
            )
            DropdownMenuItem(
                text = { Text("አማርኛ (Amharic)", color = Color.white) },
                onClick = { viewModel.updateLanguage(AppLanguage.AMHARIC); showMenu = false }
            )
            DropdownMenuItem(
                text = { Text("Oromoo (Afaan Oromo)", color = Color.white) },
                onClick = { viewModel.updateLanguage(AppLanguage.AFAAN_OROMO); showMenu = false }
            )
            DropdownMenuItem(
                text = { Text("Soomaali (Somali)", color = Color.white) },
                onClick = { viewModel.updateLanguage(AppLanguage.SOMALI); showMenu = false }
            )
            DropdownMenuItem(
                text = { Text("ትግርኛ (Tigrinya)", color = Color.white) },
                onClick = { viewModel.updateLanguage(AppLanguage.TIGRINYA); showMenu = false }
            )
        }
    }
}

// ======================= MARKETPLACE SCREEN =======================

@Composable
fun MarketplaceScreen(viewModel: SoyamViewModel) {
    val listings by viewModel.listings.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val selectedCat by viewModel.selectedCategory.collectAsState()
    val reportedSet by viewModel.reportedListings.collectAsState()
    val language by viewModel.selectedLanguage.collectAsState()

    // Filter logic
    val currentSelectedCat = selectedCat
    val filteredListings = listings.filter { listing ->
        val matchesQuery = (listing.title + listing.description + listing.subcategory).lowercase().contains(query.lowercase()) ||
                (listing.titleAmharic + listing.descriptionAmharic).contains(query)
        val matchesCategory = currentSelectedCat == null || listing.category == currentSelectedCat.name
        val isNotFlagged = !reportedSet.contains(listing.id)
        matchesQuery && matchesCategory && isNotFlagged
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoyamDarkBg)
            .padding(16.dp)
    ) {
        // Toolbar with category list
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedCat?.let { if (language == AppLanguage.ENGLISH) it.displayName else it.amharicName }
                    ?: viewModel.getTranslation("marketplace"),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.white
            )

            if (selectedCat != null) {
                IconButton(onClick = { viewModel.selectedCategory.value = null }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Clear Category", tint = SoyamPrimaryRed)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Large searchable bar with instant typing
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Filter items in Ethiopia...", color = Color.lightGray, fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Filled.FilterList, contentDescription = "Filter", tint = SoyamPrimaryGreen) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SoyamPrimaryGreen,
                unfocusedBorderColor = Color.gray,
                focusedContainerColor = SoyamCardBg,
                unfocusedContainerColor = SoyamCardBg
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("marketplace_query_input"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Categories chip grids
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedCat == null,
                    onClick = { viewModel.selectedCategory.value = null },
                    label = { Text(viewModel.getTranslation("all")) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SoyamPrimaryGreen,
                        selectedLabelColor = Color.white,
                        labelColor = Color.gray
                    )
                )
            }
            items(MarketplaceCategory.values()) { cat ->
                val label = if (language == AppLanguage.ENGLISH) cat.displayName else cat.amharicName
                FilterChip(
                    selected = selectedCat == cat,
                    onClick = { viewModel.selectedCategory.value = cat },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SoyamPrimaryGreen,
                        selectedLabelColor = Color.white,
                        labelColor = Color.gray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredListings.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.ContentPasteOff, contentDescription = "No data", modifier = Modifier.size(64.dp), tint = Color.gray)
                Spacer(modifier = Modifier.height(12.dp))
                Text("No matching listings found.", color = Color.lightGray, fontSize = 14.sp)
                Text("Try adjusting keywords or selecting another category.", color = Color.gray, fontSize = 12.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredListings) { listing ->
                    ListingItemCard(listing = listing, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun ListingItemCard(listing: Listing, viewModel: SoyamViewModel) {
    val language by viewModel.selectedLanguage.collectAsState()
    val isReported = viewModel.reportedListings.collectAsState().value.contains(listing.id)

    val title = if (language == AppLanguage.ENGLISH) listing.title else listing.titleAmharic
    val desc = if (language == AppLanguage.ENGLISH) listing.description else listing.descriptionAmharic
    val loc = if (language == AppLanguage.ENGLISH) listing.location else listing.locationAmharic

    Card(
        colors = CardDefaults.cardColors(containerColor = SoyamCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.selectListing(listing) }
            .testTag("listing_card_${listing.id}"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.gray.copy(alpha = 0.2f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF323B47), Color(0xFF1E252E))
                        )
                    )
            ) {
                // Feature Category Label Background Graphic
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = getCategoryIconName(listing.category),
                            contentDescription = listing.category,
                            modifier = Modifier.size(44.dp),
                            tint = SoyamYellow.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = listing.subcategory.ifEmpty { "E-commerce" },
                            fontSize = 11.sp,
                            color = Color.lightGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Verify and Price tag badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${listing.price.toInt()} ETB",
                            color = SoyamYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    if (listing.isSellerVerified) {
                        Box(
                            modifier = Modifier
                                .background(SoyamPrimaryGreen, CircleShape)
                                .padding(4.dp)
                        ) {
                            Icon(Icons.Filled.Verified, contentDescription = "Verified Merchant", tint = Color.white, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // Text Info body
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.white,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    fontSize = 12.sp,
                    color = Color.lightGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, contentDescription = "Location", tint = SoyamPrimaryRed, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(loc, fontSize = 11.sp, color = Color.gray)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Visibility, contentDescription = "Views", tint = Color.gray, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("${listing.viewCount} views", fontSize = 11.sp, color = Color.gray)
                    }
                }
            }
        }
    }
}

// Helper icons mapping
fun getCategoryIcon(cat: MarketplaceCategory): ImageVector {
    return when (cat) {
        MarketplaceCategory.PRODUCTS -> Icons.Filled.ShoppingBag
        MarketplaceCategory.SERVICES -> Icons.Filled.Build
        MarketplaceCategory.MANUFACTURING -> Icons.Filled.Factory
        MarketplaceCategory.PROFESSIONAL_SERVICES -> Icons.Filled.Gavel
        MarketplaceCategory.CREATIVE_SERVICES -> Icons.Filled.Palette
        MarketplaceCategory.KNOWLEDGE_MARKETPLACE -> Icons.Filled.CastForEducation
        MarketplaceCategory.BUSINESS_OPPORTUNITIES -> Icons.Filled.TrendingUp
        MarketplaceCategory.JOBS -> Icons.Filled.Work
        MarketplaceCategory.PROPERTY -> Icons.Filled.Home
        MarketplaceCategory.VEHICLES -> Icons.Filled.DirectionsCar
        MarketplaceCategory.TOURISM -> Icons.Filled.Flight
        MarketplaceCategory.EDUCATION -> Icons.Filled.AutoStories
        MarketplaceCategory.GOVERNMENT_SERVICES -> Icons.Filled.AccountBalance
    }
}

fun getCategoryIconName(name: String): ImageVector {
    val matched = MarketplaceCategory.values().find { it.name == name }
    return if (matched != null) getCategoryIcon(matched) else Icons.Filled.Storefront
}

// ======================= SELLER SUB-PLATFORM & ADVISOR =======================

@Composable
fun SellScreen(viewModel: SoyamViewModel) {
    var title by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf(MarketplaceCategory.PRODUCTS) }
    var subcat by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Addis Ababa") }

    val pricingSuggestion by viewModel.pricingSuggestion.collectAsState()
    val isGenerating by viewModel.aiGenerating.collectAsState()
    val listings by viewModel.listings.collectAsState()

    var activeTab by remember { mutableStateOf("Create") } // Create or Live Store Analytics

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoyamDarkBg)
    ) {
        // Toggle view header
        TabRow(
            selectedTabIndex = if (activeTab == "Create") 0 else 1,
            containerColor = SoyamCardBg,
            contentColor = SoyamYellow
        ) {
            Tab(selected = activeTab == "Create", onClick = { activeTab = "Create" }) {
                Text(viewModel.getTranslation("add_listing"), modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Tab(selected = activeTab == "Analytics", onClick = { activeTab = "Analytics" }) {
                Text(viewModel.getTranslation("seller_tools"), modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        if (activeTab == "Create") {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "List your Enterprise or Offer on Soyam",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.white
                    )
                    Text(
                        "Be discovered across thousands of buyers in Ethiopia.",
                        fontSize = 11.sp,
                        color = Color.gray
                    )
                }

                // Core Form inputs
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Listing Title (e.g. Traditional dress)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sell_title_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoyamPrimaryGreen)
                    )
                }

                // Marketplace Category selector dropdown
                item {
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.white)
                        ) {
                            Text("Category: ${selectedCat.displayName}")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(SoyamCardBg)
                        ) {
                            MarketplaceCategory.values().forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.displayName, color = Color.white) },
                                    onClick = {
                                        selectedCat = cat
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = subcat,
                        onValueChange = { subcat = it },
                        label = { Text("Subcategory (e.g. Phones, Web Dev, Land)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoyamPrimaryGreen)
                    )
                }

                item {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Price in Birr (ETB)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sell_price_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoyamPrimaryGreen)
                    )
                }

                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Regional Location City (e.g. Adama, Bahir Dar)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoyamPrimaryGreen)
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Describe details (condition, specs, warranty)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoyamPrimaryGreen)
                    )
                }

                // AI Pricing Suggestion module Integration
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SoyamCardBg.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, SoyamPrimaryGreen.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = "AI", tint = SoyamYellow, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("AI Pricing Advisor", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.white)
                                }
                                Button(
                                    onClick = {
                                        viewModel.requestPriceCheck(title, selectedCat.name, description)
                                    },
                                    enabled = title.isNotBlank() && !isGenerating,
                                    colors = ButtonDefaults.buttonColors(containerColor = SoyamYellow),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Evaluate price", fontSize = 10.sp, color = Color.black, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (isGenerating) {
                                LinearProgressIndicator(color = SoyamPrimaryGreen, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                            }

                            pricingSuggestion?.let { suggestion ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = suggestion,
                                    fontSize = 11.sp,
                                    color = Color.lightGray,
                                    modifier = Modifier.padding(2.dp)
                                )
                            }
                        }
                    }
                }

                // Post Button
                item {
                    Button(
                        onClick = {
                            val price = priceStr.toDoubleOrNull() ?: 100.0
                            viewModel.publishListing(
                                title = title,
                                category = selectedCat,
                                price = price,
                                description = description,
                                location = location,
                                subcategory = subcat
                            )
                            // Clear inputs
                            title = ""
                            priceStr = ""
                            description = ""
                            subcat = ""
                            viewModel.currentTab.value = "Marketplace"
                        },
                        enabled = title.isNotBlank() && priceStr.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = SoyamPrimaryGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("publish_listing_button")
                    ) {
                        Text("Publish Listing to Soyam National Gabaa", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        } else {
            // Analytics and store tools
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Merchant Store details
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SoyamCardBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(SoyamPrimaryGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = viewModel.sellerStoreName.collectAsState().value.take(2).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.white
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = viewModel.sellerStoreName.collectAsState().value,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.white
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Filled.Verified, "Verified Merchant", tint = SoyamYellow, modifier = Modifier.size(14.dp))
                                    }
                                    Text(
                                        text = viewModel.sellerStoreLocation.collectAsState().value,
                                        fontSize = 11.sp,
                                        color = Color.gray
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Tagline: " + viewModel.sellerStoreTagline.collectAsState().value,
                                fontSize = 11.sp,
                                color = Color.lightGray
                            )
                        }
                    }
                }

                // Analytical Metrics Summary card
                item {
                    Text("Enterprise Commerce Dashboard", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.white)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SoyamCardBg),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Total Products Listed", fontSize = 11.sp, color = Color.gray)
                                Text("8 Listings", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SoyamPrimaryGreen)
                            }
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SoyamCardBg),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Active Leads Channel", fontSize = 11.sp, color = Color.gray)
                                Text("1,803 views", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SoyamYellow)
                            }
                        }
                    }
                }

                // AI Business Advisor recommendations inside active screen
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SoyamCardBg),
                        border = BorderStroke(1.dp, SoyamYellow.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.TipsAndUpdates, contentDescription = "Advisor", tint = SoyamYellow, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Soyam AI Business Advisor suggestions", fontWeight = FontWeight.Bold, color = Color.white, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "🚀 *Sidama Coffee Wholesale*: Sidama sun-dried coffee is trending with a 15% increase in searches this week in Addis and Bahir Dar. Consider raising prices slightly and marketing to local high-end hotels.\n\n🌐 *Bole Rentals*: 2-Bed rentals can offer an additional daily tourist options. Enable 'Bookable via CBE' for higher tourist trust levels.",
                                fontSize = 11.sp,
                                color = Color.lightGray
                            )
                        }
                    }
                }

                // Your listings list to delete
                item {
                    Text("Manage Your Storefront Listings", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.white)
                }

                items(listings.filter { it.sellerName == viewModel.sellerStoreName.value }) { listing ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SoyamCardBg)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(listing.title, color = Color.white, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("${listing.price.toInt()} ETB • ${listing.location}", color = Color.gray, fontSize = 11.sp)
                        }
                        IconButton(onClick = { viewModel.deleteListing(listing.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete Listing", tint = SoyamPrimaryRed)
                        }
                    }
                }
            }
        }
    }
}

// ======================= COMMUNICATION SYSTEM (REAL-TIME CHAT) =======================

@Composable
fun MessagesScreen(viewModel: SoyamViewModel) {
    val activeThread by viewModel.activeChatThread.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isGenerating by viewModel.aiGenerating.collectAsState()

    var messageText by remember { mutableStateOf("") }

    if (activeThread == null) {
        // Threads List Screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SoyamDarkBg)
                .padding(16.dp)
        ) {
            Text(
                "Unified Message Desk",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.white
            )
            Text(
                "Engage with merchants or contact smart AI support advisors.",
                fontSize = 12.sp,
                color = Color.gray
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Choose Bot list options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            viewModel.activeChatThread.value = "AI_ASSISTANT"
                        },
                    colors = CardDefaults.cardColors(containerColor = SoyamPrimaryGreen.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.SmartToy, contentDescription = "AI Assistant", tint = SoyamPrimaryGreen, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(viewModel.getTranslation("ai_assistant"), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.white)
                        Text("Instant support", fontSize = 9.sp, color = Color.lightGray)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            viewModel.activeChatThread.value = "AI_BUSINESS_ADVISOR"
                        },
                    colors = CardDefaults.cardColors(containerColor = SoyamPrimaryRed.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.ModelTraining, contentDescription = "Advisor Bot", tint = SoyamPrimaryRed, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(viewModel.getTranslation("business_advisor"), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.white)
                        Text("Scale your listings", fontSize = 9.sp, color = Color.lightGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Standard threads
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("Recent Seller Discussions", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.gray)
                }

                item {
                    ChatMessageThreadItem(
                        name = "Selamawit Harari Craft",
                        lastMessage = "Injera serving tray is available for bole delivery",
                        time = "10:35 AM",
                        onClick = {
                            viewModel.activeChatThread.value = "L1" // Thread id linked to listing
                        }
                    )
                }
                item {
                    ChatMessageThreadItem(
                        name = "Kidane Toyota Importer",
                        lastMessage = "We can handle the Transfer process via CBE Birr",
                        time = "Yesterday",
                        onClick = {
                            viewModel.activeChatThread.value = "L3"
                        }
                    )
                }
            }
        }
    } else {
        // Active Chat Box Screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SoyamDarkBg)
        ) {
            // Chat Box Toolbar header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SoyamCardBg)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.activeChatThread.value = null }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.white)
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (activeThread!!.startsWith("AI_")) SoyamYellow else SoyamPrimaryGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (activeThread!!.startsWith("AI_")) Icons.Filled.SmartToy else Icons.Filled.Person,
                        contentDescription = "Avatar",
                        tint = Color.black,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (activeThread == "AI_ASSISTANT") "Soyam AI Assistant"
                        else if (activeThread == "AI_BUSINESS_ADVISOR") "AI Business Advisor"
                        else if (activeThread == "L1") "Selamawit Harari Craft"
                        else if (activeThread == "L3") "Kidane Toyota Importer"
                        else "Merchant Help Support",
                        fontWeight = FontWeight.Bold,
                        color = Color.white,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (activeThread!!.startsWith("AI_")) "Powered by Gemini 3.5-Flash" else "Direct Connection Online",
                        fontSize = 10.sp,
                        color = SoyamPrimaryGreen
                    )
                }
            }

            // Message timeline
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chatMessages) { msg ->
                    BubbleChatMessage(msg)
                }

                if (isGenerating) {
                    item {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = SoyamPrimaryGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Soyam AI is typing...", fontSize = 11.sp, color = SoyamYellow, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Input panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SoyamCardBg)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Simulated Image Sharing */ }) {
                    Icon(Icons.Filled.AddPhotoAlternate, contentDescription = "Share Image", tint = SoyamYellow)
                }
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Write message here...", color = Color.gray, fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_text")
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoyamPrimaryGreen),
                    maxLines = 3,
                    singleLine = false
                )
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessageToThread(activeThread!!, messageText)
                            messageText = ""
                        }
                    },
                    modifier = Modifier.testTag("send_msg_icon_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = SoyamPrimaryGreen)
                }
            }
        }
    }
}

@Composable
fun ChatMessageThreadItem(name: String, lastMessage: String, time: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SoyamCardBg)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SoyamPrimaryGreen.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Person, contentDescription = "avatar", tint = SoyamPrimaryGreen)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(name, fontWeight = FontWeight.Bold, color = Color.white, fontSize = 13.sp)
                Text(time, fontSize = 10.sp, color = Color.gray)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(lastMessage, color = Color.lightGray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun BubbleChatMessage(message: ChatMessage) {
    val isMe = message.senderId == "USER"
    val align = if (isMe) Alignment.End else Alignment.Start
    val containerBg = if (isMe) SoyamPrimaryGreen else SoyamCardBg
    val labelColor = if (isMe) Color.white else Color.lightGray

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = align) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 0.dp,
                        bottomEnd = if (isMe) 0.dp else 16.dp
                    )
                )
                .background(containerBg)
                .padding(12.dp)
        ) {
            Column {
                if (!isMe) {
                    Text(
                        message.senderName,
                        fontWeight = FontWeight.Bold,
                        color = SoyamYellow,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                Text(message.content, color = Color.white, fontSize = 13.sp)

                // Optional Amharic translations
                if (message.contentAmharic.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.gray.copy(alpha = 0.3f))
                    Text(message.contentAmharic, color = SoyamYellow, fontSize = 12.sp)
                }
            }
        }
        Text(
            text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp)),
            fontSize = 9.sp,
            color = Color.gray,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

// ======================= LISTING DETAIL SCREEN =======================

@Composable
fun ListingDetailScreen(listing: Listing, onBack: () -> Unit, viewModel: SoyamViewModel) {
    var showCheckoutSheet by remember { mutableStateOf(false) }
    var chosenWallet by remember { mutableStateOf("TELEBIRR") }
    val isGenerating by viewModel.aiGenerating.collectAsState()
    val scamCheckResult by viewModel.fraudStatus.collectAsState()

    val language by viewModel.selectedLanguage.collectAsState()
    val title = if (language == AppLanguage.ENGLISH) listing.title else listing.titleAmharic
    val desc = if (language == AppLanguage.ENGLISH) listing.description else listing.descriptionAmharic
    val loc = if (language == AppLanguage.ENGLISH) listing.location else listing.locationAmharic

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoyamDarkBg)
    ) {
        // Appbar row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.white)
            }
            Text("Listing Details", fontWeight = FontWeight.Bold, color = Color.white, fontSize = 16.sp)
            IconButton(onClick = { viewModel.toggleReportListing(listing.id) }) {
                Icon(Icons.Filled.Flag, contentDescription = "Report", tint = SoyamPrimaryRed)
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Placeholder detail banner box
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.sweepGradient(
                                listOf(SoyamCardBg, Color(0xFF282C34), SoyamCardBg)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val icon = getCategoryIconName(listing.category)
                        Icon(icon, contentDescription = "Detail Icon", tint = SoyamYellow, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(listing.category, color = SoyamYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Title block
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.white
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Category • ${listing.subcategory}",
                            fontSize = 12.sp,
                            color = SoyamPrimaryGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(SoyamPrimaryGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${listing.price.toInt()} ETB",
                            color = SoyamYellow,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // Trust badge and scanning module
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SoyamCardBg),
                    border = BorderStroke(1.dp, SoyamPrimaryRed.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Security, contentDescription = "Shield", tint = SoyamPrimaryRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Soyam AI Fraud Scanner", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.white)
                            }
                            Button(
                                onClick = { viewModel.requestFraudScan(listing.title, listing.description) },
                                colors = ButtonDefaults.buttonColors(containerColor = SoyamPrimaryRed),
                                enabled = !isGenerating,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 1.dp)
                            ) {
                                Text("Scan Seller", fontSize = 9.sp, color = Color.white, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (isGenerating) {
                            LinearProgressIndicator(color = SoyamPrimaryRed, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                        }

                        scamCheckResult?.let { check ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(check, fontSize = 11.sp, color = Color.lightGray)
                        } ?: Text(
                            "Safe Deal Guarantee: Funds are protected until verified arrival.",
                            fontSize = 10.sp,
                            color = Color.gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Description
            item {
                Text("Description", fontWeight = FontWeight.Bold, color = Color.white, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(desc, color = Color.lightGray, fontSize = 13.sp)
            }

            // Metadata Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Location", color = Color.gray, fontSize = 11.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, "Loc", tint = SoyamPrimaryRed, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(loc, color = Color.white, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Suggested Valuation", color = Color.gray, fontSize = 11.sp)
                        Text(
                            listing.recommendedPriceRange.ifEmpty { "Dynamic ETB" },
                            color = SoyamYellow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Seller Card and button triggers
            item {
                Card(colors = CardDefaults.cardColors(containerColor = SoyamCardBg)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(listing.sellerName, fontWeight = FontWeight.Bold, color = Color.white, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                if (listing.isSellerVerified) {
                                    Icon(Icons.Filled.Verified, "Verified", tint = SoyamYellow, modifier = Modifier.size(14.dp))
                                }
                            }
                            Text("Seller Rating: ★ ${listing.sellerRating}", fontSize = 11.sp, color = SoyamYellow)
                        }

                        // Connect button trigger
                        Button(
                            onClick = {
                                viewModel.sendMessageToThread(listing.id, "Hi! I am interested in purchasing '${listing.title}'. Is it available?")
                                viewModel.activeChatThread.value = listing.id
                                viewModel.currentTab.value = "Messages"
                                viewModel.selectListing(null)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoyamPrimaryGreen)
                        ) {
                            Text("Chat with Seller", fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Checkout Trigger Footer Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SoyamCardBg)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Escrow Total Price", color = Color.gray, fontSize = 11.sp)
                Text("${listing.price.toInt()} ETB", color = Color.white, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }

            Button(
                onClick = { showCheckoutSheet = true },
                colors = ButtonDefaults.buttonColors(containerColor = SoyamYellow),
                modifier = Modifier
                    .width(180.dp)
                    .height(44.dp)
                    .testTag("buy_now_button")
            ) {
                Text(
                    text = viewModel.getTranslation("buy_now"),
                    fontWeight = FontWeight.Bold,
                    color = Color.black,
                    fontSize = 13.sp
                )
            }
        }
    }

    if (showCheckoutSheet) {
        AlertDialog(
            onDismissRequest = { showCheckoutSheet = false },
            title = { Text("Complete Purchase via Escrow", color = Color.white) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select your preferred Ethiopian Payment provider to process securely. Funds remain safe until you confirm delivery.", fontSize = 12.sp, color = Color.lightGray)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (chosenWallet == "TELEBIRR") SoyamPrimaryGreen.copy(alpha = 0.3f) else SoyamCardBg)
                            .border(1.dp, if (chosenWallet == "TELEBIRR") SoyamPrimaryGreen else Color.transparent, RoundedCornerShape(8.dp))
                            .clickable { chosenWallet = "TELEBIRR" }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = chosenWallet == "TELEBIRR", onClick = { chosenWallet = "TELEBIRR" })
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Telebirr Mobile Escrow", fontWeight = FontWeight.Bold, color = Color.white)
                            Text("Balance: ${viewModel.telebirrBalance.value} ETB", fontSize = 11.sp, color = Color.gray)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (chosenWallet == "CBEBIRR") CBEBirrColor.copy(alpha = 0.3f) else SoyamCardBg)
                            .border(1.dp, if (chosenWallet == "CBEBIRR") CBEBirrColor else Color.transparent, RoundedCornerShape(8.dp))
                            .clickable { chosenWallet = "CBEBIRR" }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = chosenWallet == "CBEBIRR", onClick = { chosenWallet = "CBEBIRR" })
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("CBE Birr (Com Bank of Ethiopia)", fontWeight = FontWeight.Bold, color = Color.white)
                            Text("Balance: ${viewModel.cbebirrBalance.value} ETB", fontSize = 11.sp, color = Color.gray)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.checkoutListing(listing, chosenWallet)
                        showCheckoutSheet = false
                        viewModel.selectListing(null)
                        viewModel.currentTab.value = "Profile" // Redirect to track order
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoyamYellow)
                ) {
                    Text("Authorize Escrow Pay", color = Color.black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutSheet = false }) {
                    Text("Cancel", color = Color.white)
                }
            },
            containerColor = SoyamCardBg
        )
    }
}

val CBEBirrColor = Color(0xFFC0932C)

// ======================= PROFILE, WALLET & ADMIN DASHBOARD =======================

@Composable
fun ProfileScreen(viewModel: SoyamViewModel) {
    val language by viewModel.selectedLanguage.collectAsState()
    val teleBalance by viewModel.telebirrBalance.collectAsState()
    val cbeBalance by viewModel.cbebirrBalance.collectAsState()
    val points by viewModel.rewardPoints.collectAsState()
    val referrals by viewModel.totalReferrals.collectAsState()

    val orders by viewModel.orders.collectAsState()

    var showTransferDialog by remember { mutableStateOf(false) }
    var transferAmount by remember { mutableStateOf("") }
    var transferRecipient by remember { mutableStateOf("") }
    var chosenWalletType by remember { mutableStateOf("TELEBIRR") }

    var isAdminActive by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SoyamDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    viewModel.getTranslation("profile"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.white
                )

                // Admin dashboard layout triggers
                Button(
                    onClick = { isAdminActive = !isAdminActive },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isAdminActive) SoyamPrimaryRed else SoyamCardBg),
                    border = BorderStroke(1.dp, Color.gray)
                ) {
                    Text(if (isAdminActive) "Close Admin" else "Admin Board", fontSize = 11.sp)
                }
            }
        }

        if (isAdminActive) {
            // ADMIN MANAGEMENT MODULE VIEW
            item {
                Text("Soyam Admin Super-Platform Dashboard", color = SoyamPrimaryRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = SoyamCardBg)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Listings Approvals", color = Color.white, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Active: 8", color = SoyamPrimaryGreen, fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Fraud/Spam flags", color = Color.white, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Reported items: ${viewModel.reportedListings.collectAsState().value.size}", color = SoyamPrimaryRed, fontSize = 11.sp)
                        }
                    }
                }
            }

            item {
                Text("Simulate Platform Security Broadcast Alert", fontSize = 12.sp, color = Color.gray)
                Button(
                    onClick = {
                        viewModel.sendMessageToThread(
                            threadId = "AI_ASSISTANT",
                            text = "[SYSTEM ALERT] Soyam platform requires all gold merchants to pass Chapa/Telebirr official identity verification guidelines before July 1st.",
                            senderId = "SYSTEM_BROADCAST"
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoyamPrimaryRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Trigger Global System Broadcast Announcement", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // STANDARD PROFILE AND DIGITAL WALLET VIEW
            item {
                // User ID Card
                Card(colors = CardDefaults.cardColors(containerColor = SoyamCardBg)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(SoyamPrimaryGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Person, "Avatar", tint = Color.white, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Juanquavious Jackson Bot II", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.white)
                            Text("Loyal Customer (Soyam Gold Level)", fontSize = 11.sp, color = SoyamYellow, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // SOYAM DIGITAL WALLETS COLLAPSE MODULE
            item {
                Text(viewModel.getTranslation("wallet"), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.white)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Telebirr Pink layout card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, SoyamPrimaryGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = SoyamCardBg)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(SoyamPrimaryGreen, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Telebirr Pay", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.white)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("${teleBalance.toInt()} ETB", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SoyamYellow)
                            Text("Escrow Active", fontSize = 9.sp, color = Color.gray)
                        }
                    }

                    // CBEBirr yellow layout card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, CBEBirrColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = SoyamCardBg)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(CBEBirrColor, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("CBEBirr Client", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.white)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("${cbeBalance.toInt()} ETB", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SoyamYellow)
                            Text("Direct Escrow", fontSize = 9.sp, color = Color.gray)
                        }
                    }
                }
            }

            // Wallet actions (transfer dialog & reward tracking)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showTransferDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SoyamPrimaryGreen),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Payment, "Trans", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(viewModel.getTranslation("transfer"), fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.topUpWallet(1000.0, "TELEBIRR")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoyamCardBg),
                        border = BorderStroke(1.dp, Color.gray),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.AccountBalanceWallet, "Top", modifier = Modifier.size(16.dp), tint = SoyamYellow)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add 1000 ETB", fontSize = 11.sp, color = Color.white)
                    }
                }
            }

            // Cashback Referral Rewards
            item {
                Card(colors = CardDefaults.cardColors(containerColor = SoyamCardBg)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏆 $points", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SoyamYellow)
                            Text(viewModel.getTranslation("points"), fontSize = 10.sp, color = Color.lightGray)
                        }
                        VerticalDivider(color = Color.gray.copy(alpha = 0.3f), modifier = Modifier.height(30.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("👥 $referrals", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SoyamYellow)
                            Text("Referral Invites", fontSize = 10.sp, color = Color.lightGray)
                        }
                    }
                }
            }

            // Active Buyer Purchase orders Tracking
            item {
                Text(viewModel.getTranslation("order_tracking"), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.white)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (orders.isEmpty()) {
                item {
                    Text("No transactions or escrow payments recorded yet.", color = Color.gray, fontSize = 12.sp)
                }
            } else {
                items(orders) { order ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SoyamCardBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    order.listingTitle,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.white,
                                    fontSize = 13.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .background(
                                            when (order.status) {
                                                "Completed" -> SoyamPrimaryGreen
                                                "Pending" -> SoyamYellow
                                                else -> SoyamPrimaryRed
                                            },
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        order.status,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.black
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Order ID: ${order.orderId} • Seller: ${order.sellerName}",
                                fontSize = 11.sp,
                                color = Color.gray
                            )
                            Text(
                                "${order.price.toInt()} ETB Paid via ${order.paymentMethod}",
                                fontSize = 11.sp,
                                color = SoyamPrimaryGreen,
                                fontWeight = FontWeight.SemiBold
                            )

                            // Complete trigger or Dispute actions simulator
                            if (order.status == "Pending") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.updateOrderStatus(order.orderId, "Completed") },
                                        colors = ButtonDefaults.buttonColors(containerColor = SoyamPrimaryGreen),
                                        contentPadding = PaddingValues(horizontal = 10.dp)
                                    ) {
                                        Text("Confirm Received", fontSize = 10.sp, color = Color.white)
                                    }
                                    Button(
                                        onClick = { viewModel.updateOrderStatus(order.orderId, "Disputed") },
                                        colors = ButtonDefaults.buttonColors(containerColor = SoyamPrimaryRed),
                                        contentPadding = PaddingValues(horizontal = 10.dp)
                                    ) {
                                        Text("Dispute", fontSize = 10.sp, color = Color.white)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTransferDialog) {
        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = { Text("Transfer Funds safely", color = Color.white) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = transferRecipient,
                        onValueChange = { transferRecipient = it },
                        label = { Text("Recipient Phone (+251...) or Merchant ID") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoyamPrimaryGreen)
                    )

                    OutlinedTextField(
                        value = transferAmount,
                        onValueChange = { transferAmount = it },
                        label = { Text("Amount in Birr (ETB)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SoyamPrimaryGreen)
                    )

                    Text("Selected wallet provider:", fontSize = 11.sp, color = Color.gray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { chosenWalletType = "TELEBIRR" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (chosenWalletType == "TELEBIRR") SoyamPrimaryGreen else SoyamCardBg
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Telebirr", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { chosenWalletType = "CBE_BIRR" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (chosenWalletType == "CBE_BIRR") CBEBirrColor else SoyamCardBg
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("CBEBirr", fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = transferAmount.toDoubleOrNull() ?: 0.0
                        if (amount > 0.0) {
                            viewModel.transferMoney(amount, transferRecipient, chosenWalletType)
                        }
                        showTransferDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoyamYellow)
                ) {
                    Text("Confirm Send", color = Color.black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransferDialog = false }) {
                    Text("Cancel", color = Color.white)
                }
            },
            containerColor = SoyamCardBg
        )
    }
}
