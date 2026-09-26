package com.example.bugs.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.dp
import com.example.bugs.R
import com.example.bugs.data.repository.rememberGameSettings
import kotlinx.coroutines.launch

@Composable
fun MainScreen() {
    val tabIcons = listOf(
        R.drawable.auth,
        R.drawable.settings,
        R.drawable.rules,
        R.drawable.authors,
        R.drawable.game
    )
    val pagerState = rememberPagerState(pageCount = { tabIcons.size })
    val scope = rememberCoroutineScope()

    val gameSettings = rememberGameSettings()

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