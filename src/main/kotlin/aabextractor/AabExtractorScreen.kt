package aabextractor

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import io.github.vinceglb.filekit.compose.PickerResultLauncher
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformFile
import utils.ResultEnum

@Composable
fun AabExtractorScreen(
    viewModel: AabExtractorViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    AabExtractorUi(
        modifier = modifier,
        uiState = uiState,
        onConvertClick = viewModel::convertAabToApk,
        getSavedJavaFile = {
            viewModel.getPref(AabExtractorViewModel.JAVA_EXECUTABLE_FILE)
        },
        getSavedBundleToolFile = {
            viewModel.getPref(AabExtractorViewModel.BUNDLE_TOOL_FILE)
        },
        saveJavaFilePath = {
            viewModel.savePref(AabExtractorViewModel.JAVA_EXECUTABLE_FILE, it)
        },
        saveBundleToolFilePath = {
            viewModel.savePref(AabExtractorViewModel.BUNDLE_TOOL_FILE, it)
        },
        openResult = viewModel::openDocumentDirectory,
        dismissLoadingDialog = viewModel::dismissLoadingDialog,
        dismissResultDialog = viewModel::dismissResultDialog
    )
}

@Composable
private fun AabExtractorUi(
    uiState: AabExtractorUiState,
    onConvertClick: (
        PlatformFile?,
        PlatformFile?,
        String,
        String,
        String,
    ) -> Unit,
    getSavedJavaFile: () -> String?,
    getSavedBundleToolFile: () -> String?,
    saveJavaFilePath: (String) -> Unit,
    saveBundleToolFilePath: (String) -> Unit,
    openResult: () -> Unit,
    dismissLoadingDialog: () -> Unit,
    dismissResultDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val keyStorePasswordFocusRequester = remember { FocusRequester() }
    val keyPasswordFocusRequester = remember { FocusRequester() }
    val keyAliasFocusRequester = remember { FocusRequester() }
    var showMessageDialog by remember { mutableStateOf(false) }
    var messageDialog by remember { mutableStateOf("") }
    var javaFile by remember { mutableStateOf(getSavedJavaFile()) }
    var bundleToolFile by remember { mutableStateOf(getSavedBundleToolFile()) }
    var aabFile by remember { mutableStateOf<PlatformFile?>(null) }
    var jksFile by remember { mutableStateOf<PlatformFile?>(null) }
    var keyStorePassword by remember { mutableStateOf("") }
    var keyPassword by remember { mutableStateOf("") }
    var keyAlias by remember { mutableStateOf("") }
    var keyStorePasswordVisible by remember { mutableStateOf(false) }
    var keyPasswordVisible by remember { mutableStateOf(false) }
    val javaPickerLauncher = rememberFilePickerLauncher(
        type = PickerType.File(extensions = listOf("exe")),
        mode = PickerMode.Single,
        title = "Pick Java Executable file path"
    ) { file ->
        file?.let {
            javaFile = it.file.absolutePath
            saveJavaFilePath(it.file.absolutePath)
        }
    }
    val bundleToolPickerLauncher = rememberFilePickerLauncher(
        type = PickerType.File(extensions = listOf("jar")),
        mode = PickerMode.Single,
        title = "Pick BundleTool file path"
    ) { file ->
        file?.let {
            bundleToolFile = it.file.absolutePath
            saveBundleToolFilePath(it.file.absolutePath)
        }
    }
    val aabPickerLauncher = rememberFilePickerLauncher(
        type = PickerType.File(extensions = listOf("aab")),
        mode = PickerMode.Single,
        title = "Pick AAB file",
    ) { file ->
        aabFile = file
    }
    val jksPickerLauncher = rememberFilePickerLauncher(
        type = PickerType.File(extensions = listOf("jks")),
        mode = PickerMode.Single,
        title = "Pick JKS file",
    ) { file ->
        jksFile = file
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(state = scrollState)
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                FilePickerField(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Java Executable file:",
                    file = javaFile,
                    textButton = "${if (javaFile == null) "Select" else "Change"} File",
                    filePickerLauncher = javaPickerLauncher
                )
                FilePickerField(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Bundletool file:",
                    file = bundleToolFile,
                    textButton = "${if (bundleToolFile == null) "Select" else "Change"} File",
                    filePickerLauncher = bundleToolPickerLauncher
                )
                FilePickerField(
                    modifier = Modifier.fillMaxWidth(),
                    title = "AAB file:",
                    file = aabFile?.name,
                    textButton = "Select File",
                    filePickerLauncher = aabPickerLauncher
                )
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilePickerField(
                        modifier = Modifier.weight(1f),
                        title = "JKS file:",
                        file = jksFile?.name,
                        textButton = "Select File",
                        filePickerLauncher = jksPickerLauncher
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    TextField(
                        title = "Keystore Password",
                        value = keyStorePassword,
                        onValueChange = {
                            keyStorePassword = it
                        },
                        modifier = Modifier.weight(1f),
                        visualTransformation = if (keyStorePasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        focusRequester = keyStorePasswordFocusRequester,
                        isPassword = true,
                        passwordVisible = keyStorePasswordVisible,
                        onPasswordVisibilityChanged = {
                            keyStorePasswordVisible = it
                        }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        title = "Key Alias:",
                        value = keyAlias,
                        onValueChange = {
                            keyAlias = it
                        },
                        modifier = Modifier.weight(1f),
                        focusRequester = keyAliasFocusRequester,
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    TextField(
                        title = "Key Password:",
                        value = keyPassword,
                        onValueChange = {
                            keyPassword = it
                        },
                        modifier = Modifier.weight(1f),
                        visualTransformation = if (keyPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        focusRequester = keyPasswordFocusRequester,
                        isPassword = true,
                        passwordVisible = keyPasswordVisible,
                        onPasswordVisibilityChanged = {
                            keyPasswordVisible = it
                        }
                    )
                }
                Spacer(modifier = Modifier.height(36.dp))
                Button(
                    shape = RoundedCornerShape(50),
                    onClick = {
                        if (javaFile == null) {
                            showMessageDialog = true
                            messageDialog = "Please pick Java Executable File!"
                            return@Button
                        }
                        if (bundleToolFile == null) {
                            showMessageDialog = true
                            messageDialog = "Please pick BundleTool File!"
                            return@Button
                        }
                        if (aabFile == null) {
                            showMessageDialog = true
                            messageDialog = "Please pick AAB File!"
                            return@Button
                        }
                        if (jksFile == null) {
                            showMessageDialog = true
                            messageDialog = "Please pick JKS File!"
                            return@Button
                        }
                        if (keyStorePassword.isBlank()) {
                            showMessageDialog = true
                            messageDialog = "Please fill Keystore Password!"
                            return@Button
                        }
                        if (keyPassword.isBlank()) {
                            showMessageDialog = true
                            messageDialog = "Please fill Key Password!"
                            return@Button
                        }
                        if (keyAlias.isBlank()) {
                            showMessageDialog = true
                            messageDialog = "Please fill Key Alias!"
                            return@Button
                        }
                        onConvertClick(aabFile, jksFile, keyStorePassword, keyPassword, keyAlias)
                    }
                ) {
                    Text("Convert AAB to APK", modifier = Modifier.padding(horizontal = 48.dp, vertical = 16.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (showMessageDialog) {
                AlertDialog(
                    shape = RoundedCornerShape(12),
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(messageDialog)
                        }
                    },
                    onDismissRequest = {
                        showMessageDialog = false
                    },
                    buttons = {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = {
                                    showMessageDialog = false
                                }
                            ) {
                                Text("OK")
                            }
                        }
                    }
                )
            }

            if (uiState.showResultDialog) {
                AlertDialog(
                    modifier = Modifier.sizeIn(maxHeight = 350.dp),
                    shape = RoundedCornerShape(12),
                    text = {
                        if (uiState.resultState == ResultEnum.SUCCESS.name) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(uiState.apkName.orEmpty())
                                Spacer(modifier = Modifier.width(12.dp))
                                TextButton(onClick = {
                                    openResult()
                                    dismissResultDialog()
                                }) {
                                    Text("Open")
                                }
                            }
                        } else {
                            Text(uiState.errorMessage.orEmpty(), modifier = Modifier.fillMaxHeight())
                        }
                    },
                    onDismissRequest = dismissResultDialog,
                    buttons = {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = {
                                    dismissResultDialog()
                                }
                            ) {
                                Text("Close")
                            }
                        }
                    }
                )
            }

            if (uiState.showLoadingDialog) {
                AlertDialog(
                    shape = RoundedCornerShape(12),
                    onDismissRequest = dismissLoadingDialog,
                    properties = DialogProperties(dismissOnClickOutside = false),
                    buttons = {},
                    title = {
                        Column(
                            modifier = Modifier.width(124.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.width(124.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("Please Wait...")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun TextField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    focusRequester: FocusRequester = FocusRequester(),
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChanged: (Boolean) -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = modifier
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = {
                if (!it.contains('\t')) {
                    onValueChange(it)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, shape = RoundedCornerShape(50), color = Color.Gray)
                .focusRequester(focusRequester)
                .onKeyEvent {
                    when {
                        it.awtEventOrNull?.keyChar == '\t' -> {
                            focusManager.moveFocus(FocusDirection.Next)
                            true
                        }

                        else -> false
                    }
                },
            shape = RoundedCornerShape(50),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            visualTransformation = visualTransformation,
            trailingIcon = {
                if (isPassword) {
                    val image = if (passwordVisible) {
                        Icons.Default.Visibility
                    } else {
                        Icons.Default.VisibilityOff
                    }
                    IconButton(
                        modifier = Modifier.focusable(enabled = false).pointerHoverIcon(icon = PointerIcon.Hand),
                        onClick = {
                            onPasswordVisibilityChanged(!passwordVisible)
                        }
                    ) {
                        Icon(
                            imageVector = image,
                            contentDescription = null,
                            modifier = Modifier.focusable(enabled = false)
                        )
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun FilePickerField(
    title: String,
    file: String?,
    textButton: String,
    filePickerLauncher: PickerResultLauncher,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = file.orEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .border(width = 1.dp, shape = RoundedCornerShape(50), color = Color.Gray)
                    .padding(16.dp),
            )
            Button(
                modifier = Modifier
                    .padding(start = 12.dp),
                shape = RoundedCornerShape(50),
                onClick = {
                    filePickerLauncher.launch()
                }
            ) {
                Text(textButton, modifier = Modifier.padding(vertical = 12.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
@Preview
private fun AabExtractorPreview() {
    AabExtractorUi(
        uiState = AabExtractorUiState(),
        onConvertClick = { _, _, _, _, _ -> },
        getSavedJavaFile = { "" },
        getSavedBundleToolFile = { "" },
        saveJavaFilePath = {},
        saveBundleToolFilePath = {},
        openResult = {},
        dismissLoadingDialog = {},
        dismissResultDialog = {},
    )
}