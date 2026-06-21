package com.example.thebusysimulator.presentation.ui.screen

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.thebusysimulator.R
import com.example.thebusysimulator.presentation.ui.statusBarPadding
import com.example.thebusysimulator.presentation.ui.theme.GenZTheme

@Composable
fun PolicyScreen(navController: NavController) {
    val theme = getGenZTheme()
    MainContainer(navController = navController, theme = theme) {
        PolicyScreenContent(onBack = { navController.popBackStack() })
    }
}

@Composable
fun PolicyScreenContent(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            com.example.thebusysimulator.presentation.ui.component.GenZBackButton(
                onClick = onBack
            )
            Text(
                text = "PRIVACY POLICY",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = GenZTheme.colors.text
            )
        }

        // WebView
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                )
                .border(
                    width = 2.dp,
                    color = GenZTheme.colors.border,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                )
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = WebViewClient()
                        settings.javaScriptEnabled = false
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        loadUrl("https://anhngocnguyen1034.github.io/privacy-policy/")
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            )
        }
    }
}
