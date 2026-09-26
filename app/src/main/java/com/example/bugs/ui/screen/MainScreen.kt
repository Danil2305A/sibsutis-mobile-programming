package com.example.bugs.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.bugs.R
import androidx.compose.ui.res.stringArrayResource
import com.example.bugs.data.repository.rememberGameSettings
import kotlinx.coroutines.launch

@Composable
fun MainScreen() {
    val tabs = stringArrayResource(R.array.tabs)
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    val gameSettings = rememberGameSettings()

    Scaffold(
        topBar = {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = pagerState.currentPage == i,
                        onClick = { scope.launch { pagerState.animateScrollToPage(i) } },
                        text = { Text(title) }
                    )
                }
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(padding)
        ) { page ->
            when (page) {
                0 -> RegistrationFormScreen()
                1 -> SettingsScreen(gameSettings)
                2 -> RulesScreen()
                3 -> AuthorsScreen()
                4 -> GameScreen(gameSettings)
            }
        }
    }
}