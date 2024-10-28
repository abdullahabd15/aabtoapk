import aabextractor.AabExtractorScreen
import aabextractor.AabExtractorViewModel
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

@Composable
fun App(
    aabExtractorViewModel: AabExtractorViewModel
) {
    MaterialTheme {
        AabExtractorScreen(viewModel = aabExtractorViewModel)
    }
}

fun main() {
    val aabExtractorViewModel = AabExtractorViewModel()
    application {
        val windowState = rememberWindowState(
            size = DpSize(1000.dp, 700.dp)
        )
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "AAB to APK",
        ) {
            App(
                aabExtractorViewModel = aabExtractorViewModel
            )
        }
    }
}