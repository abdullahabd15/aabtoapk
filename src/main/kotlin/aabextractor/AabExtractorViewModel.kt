package aabextractor

import androidx.compose.runtime.Immutable
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import utils.Preferences
import utils.ResultEnum
import utils.ZipUtils
import java.awt.Desktop
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class AabExtractorViewModel {
    private val documentsPath = System.getProperty("user.home") + "\\Documents\\"
    private val _uiState = MutableStateFlow(AabExtractorUiState())
    val uiState = _uiState.asStateFlow()

    companion object {
        const val BUNDLE_TOOL_FILE = "BUNDLE_TOOL_FILE"
        const val JAVA_EXECUTABLE_FILE = "JAVA_EXECUTABLE_FILE"
    }

    fun savePref(key: String, value: String) = Preferences.put(key, value)

    fun getPref(key: String): String? = Preferences.get(key)

    fun openDocumentDirectory() = try {
        val desktop = Desktop.getDesktop()
        desktop.open(File(documentsPath))
    } catch (e: Exception) {
        e.printStackTrace()
    }

    fun dismissResultDialog() = CoroutineScope(context = Dispatchers.Default).launch {
        _uiState.update {
            it.copy(showResultDialog = false)
        }
    }

    fun dismissLoadingDialog() = CoroutineScope(context = Dispatchers.Default).launch {
        _uiState.update {
            it.copy(showLoadingDialog = false)
        }
    }

    fun convertAabToApk(
        aabFile: PlatformFile?,
        jksFile: PlatformFile?,
        keyStorePassword: String,
        keyPassword: String,
        keyAlias: String,
    ) = CoroutineScope(context = Dispatchers.Default).launch {
        try {
            _uiState.update {
                it.copy(resultState = ResultEnum.LOADING.name, showLoadingDialog = true)
            }
            val javaFilePath = getPref(JAVA_EXECUTABLE_FILE)?.replace("\\", "\\\\")
            val bundleToolFilePath = getPref(BUNDLE_TOOL_FILE)?.replace("\\", "\\\\")
            val aabFilePath = aabFile?.file?.absolutePath?.replace("\\", "\\\\")
            val jksFilePath = jksFile?.file?.absolutePath?.replace("\\", "\\\\")
            val apksResultFile = File("${documentsPath}result.apks")
            val apkFile = File("${documentsPath}universal.apk")
            val tocFile = File("${documentsPath}toc.pb")
            val apkName = "${aabFile?.file?.nameWithoutExtension ?: "result"}.apk"

            val command = listOf(
                "$javaFilePath",
                "-jar",
                "$bundleToolFilePath",
                "build-apks",
                "--bundle=$aabFilePath",
                "--ks=$jksFilePath",
                "--ks-pass=pass:$keyStorePassword",
                "--ks-key-alias=$keyAlias",
                "--key-pass=pass:$keyPassword",
                "--output=${apksResultFile.path}",
                "--mode=universal"
            )

            val processBuilder = ProcessBuilder(command)
            val process = processBuilder.start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))

            val outputResult = StringBuilder()
            val errorResult = StringBuilder()

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                outputResult.append(line).append("\n")
            }

            while (errorReader.readLine().also { line = it } != null) {
                errorResult.append(line).append("\n")
            }
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                ZipUtils.unzip(
                    zipFilePath = apksResultFile,
                    destDirectory = documentsPath
                )
                val apkTempFile = File("$documentsPath$apkName")
                if (apkTempFile.exists()) {
                    if (apkTempFile.delete()) {
                        apkFile.renameTo(File("${documentsPath}$apkName"))
                    }
                } else {
                    apkFile.renameTo(File("${documentsPath}$apkName"))
                }
                tocFile.delete()
                apksResultFile.delete()
                _uiState.update {
                    it.copy(
                        resultState = ResultEnum.SUCCESS.name,
                        apkName = apkName,
                        showResultDialog = true,
                        showLoadingDialog = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        resultState = ResultEnum.FAIL.name,
                        errorMessage = errorResult.toString(),
                        showResultDialog = true,
                        showLoadingDialog = false
                    )
                }
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    resultState = ResultEnum.FAIL.name,
                    errorMessage = e.message.orEmpty(),
                    showResultDialog = true,
                    showLoadingDialog = false
                )
            }
        }
    }
}

@Immutable
data class AabExtractorUiState(
    val resultState: String = ResultEnum.INIT.name,
    val apkName: String? = null,
    val errorMessage: String? = null,
    val showResultDialog: Boolean = false,
    val showLoadingDialog: Boolean = false
)