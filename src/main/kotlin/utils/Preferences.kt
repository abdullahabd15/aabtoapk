package utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

object Preferences {
    private val properties = Properties()
    private val preferencePath = File(System.getProperty("user.home") + "\\AppData\\Local\\aabtoapk\\")
    private val file = File(preferencePath.path, "preferences.properties")

    init {
        if (preferencePath.exists().not()) {
            preferencePath.mkdir()
        }
        if (file.exists()) {
            FileInputStream(file).use { properties.load(it) }
        }
    }

    fun put(key: String, value: String) {
        properties[key] = value
        FileOutputStream(file).use { properties.store(it, null) }
    }

    fun get(key: String, defaultValue: String? = null): String? {
        return properties.getProperty(key, defaultValue)
    }
}