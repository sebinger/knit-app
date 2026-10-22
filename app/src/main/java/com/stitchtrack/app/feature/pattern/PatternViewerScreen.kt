package com.stitchtrack.app.feature.pattern

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stitchtrack.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternViewerScreen(
    onBack: () -> Unit,
    viewModel: PatternViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pattern = uiState.pattern
    val page = pattern?.lastPage ?: 1
    var pageCount by remember(pattern?.uri) { mutableIntStateOf(Int.MAX_VALUE) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = pattern?.fileName ?: stringResource(R.string.pattern_page_placeholder))
                        Text(
                            text = stringResource(R.string.pattern_page, page),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.semantics { contentDescription = "Close pattern" },
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = null)
                    }
                },
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                IconButton(
                    onClick = { viewModel.onPageSelected(page - 1) },
                    enabled = page > 1 && pattern != null,
                    modifier = Modifier.semantics { contentDescription = "Previous page" },
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                }
                IconButton(
                    onClick = { viewModel.onPageSelected(page + 1) },
                    enabled = pattern != null && page < pageCount,
                    modifier = Modifier.semantics { contentDescription = "Next page" },
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            if (pattern == null) {
                Text(text = stringResource(R.string.pattern_page_placeholder), style = MaterialTheme.typography.titleLarge)
            } else {
                PatternPage(uri = pattern.uri, page = page, onPageCount = { count ->
                    pageCount = count
                    if (page > count) viewModel.onPageSelected(count)
                })
            }
        }
    }
    uiState.error?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::onErrorDismissed,
            title = { Text(text = stringResource(R.string.pattern_error_title)) },
            text = { Text(text = message) },
            confirmButton = {
                TextButton(onClick = viewModel::onErrorDismissed) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun PatternPage(
    uri: String,
    page: Int,
    onPageCount: (Int) -> Unit,
) {
    val context = LocalContext.current
    var bitmap by remember(uri, page) { mutableStateOf<Bitmap?>(null) }
    var pageCount by remember(uri) { mutableIntStateOf(1) }
    LaunchedEffect(uri, page) {
        val rendered = withContext(Dispatchers.IO) { renderPage(context, uri, page) }
        pageCount = rendered.pageCount
        bitmap = rendered.bitmap
        onPageCount(rendered.pageCount)
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = stringResource(R.string.pattern_page, page),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
    } else {
        Text(text = stringResource(R.string.pattern_page_placeholder), style = MaterialTheme.typography.titleLarge)
    }
}

private data class RenderedPage(val bitmap: Bitmap?, val pageCount: Int)

private fun renderPage(context: android.content.Context, uri: String, page: Int): RenderedPage {
    return runCatching {
        context.contentResolver.openFileDescriptor(Uri.parse(uri), "r")?.use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                val safeCount = renderer.pageCount.coerceAtLeast(1)
                val index = (page - 1).coerceIn(0, safeCount - 1)
                renderer.openPage(index).use { pdfPage ->
                    val bitmap = Bitmap.createBitmap(pdfPage.width, pdfPage.height, Bitmap.Config.ARGB_8888)
                    pdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    RenderedPage(bitmap, safeCount)
                }
            }
        }
    }.getOrNull() ?: RenderedPage(null, 1)
}
