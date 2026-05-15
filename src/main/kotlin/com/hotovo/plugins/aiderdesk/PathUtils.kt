package com.hotovo.plugins.aiderdesk

object PathUtils {
    /**
     * Normalize project directory path for the current OS.
     * On Windows, converts forward slashes to backslashes.
     * On Unix-like systems, keeps the path as-is.
     */
    fun normalizeProjectDir(projectDir: String): String {
        val isWindows = System.getProperty("os.name").lowercase().contains("win")
        return if (isWindows) {
            projectDir.replace("/", "\\")
        } else {
            projectDir
        }
    }

    /**
     * Normalize file path for the current OS.
     * On Windows, converts forward slashes to backslashes.
     * On Unix-like systems, keeps the path as-is.
     */
    fun normalizeFilePath(path: String): String {
        val isWindows = System.getProperty("os.name").lowercase().contains("win")
        return if (isWindows) {
            path.replace("/", "\\")
        } else {
            path
        }
    }
}
