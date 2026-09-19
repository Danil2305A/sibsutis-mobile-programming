package com.example.bugs.tab


import android.text.Html
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.bugs.R

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun RulesScreen() {
    val ctx = LocalContext.current
    val html = remember {
        runCatching {
            ctx.resources.openRawResource(R.raw.rules)
                .bufferedReader().use { it.readText() }
        }.getOrDefault("<h3>Правила недоступны :(</h3>")
    }
    val spanned = remember(html) {
        Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
    }

    AndroidView(
        factory = { c -> TextView(c).apply {
            setText(spanned)
            setPadding(32, 32, 32, 32)
        } },
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    )

}