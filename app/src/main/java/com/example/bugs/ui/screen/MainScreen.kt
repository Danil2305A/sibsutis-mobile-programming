package com.example.bugs.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bugs.R
import com.example.bugs.data.db.entity.PlayerEntity
import com.example.bugs.data.repository.RepositoryProvider
import com.example.bugs.data.repository.rememberGameSettings
import kotlinx.coroutines.launch

@Composable
fun MainScreen() {
    val tabIcons = listOf(
        R.drawable.auth,
        R.drawable.settings,
        R.drawable.rules,
        R.drawable.authors,
        R.drawable.game,
        R.drawable.leaderboard
    )
    val tabTitles = stringArrayResource(R.array.tabs)
    val pagerState = rememberPagerState(pageCount = { tabIcons.size })
    val scope = rememberCoroutineScope()
    val currentPlayer = remember { mutableStateOf<PlayerEntity?>(null) }

    val gameSettings = rememberGameSettings()

////    очистка БД (таблица рекордов)
//    val context = LocalContext.current
//    val repository = remember { RepositoryProvider.get(context) }
//    LaunchedEffect(Unit) {
//        repository.clearScores()
//    }

    Scaffold(
        topBar = {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabIcons.forEachIndexed { i, iconRes ->
                    Tab(
                        selected = pagerState.currentPage == i,
                        onClick = { scope.launch { pagerState.animateScrollToPage(i) } },
                        icon = {
                            Icon(
                                painter = painterResource(iconRes),
                                contentDescription = null,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    )
                }
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { page ->
            when (page) {
                0 -> TabContentWithTitle(tabTitles[0]) { RegistrationFormScreen(currentPlayer, gameSettings) }
                1 -> TabContentWithTitle(tabTitles[1]) { SettingsScreen(gameSettings) }
                2 -> TabContentWithTitle(tabTitles[2]) { RulesScreen() }
                3 -> TabContentWithTitle(tabTitles[3]) { AuthorsScreen() }
                4 -> GameScreen(gameSettings, currentPlayer)
                5 -> TabContentWithTitle(tabTitles[4]) { LeaderboardScreen() }
            }
        }
    }
}

@Composable
private fun TabContentWithTitle(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}