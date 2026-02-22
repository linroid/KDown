package com.linroid.ketch.config

import java.io.File

/**
 * Returns the platform-aware config directory for Ketch.
 *
 * - macOS: `~/Library/Application Support/ketch`
 * - Windows: `%APPDATA%/ketch`
 * - Linux: `$XDG_CONFIG_HOME/ketch` (falls back to `~/.config/ketch`)
 */
fun defaultConfigDir(): String {
  val os = System.getProperty("os.name", "").lowercase()
  val home = System.getProperty("user.home")
  return when {
    os.contains("mac") ->
      "$home${File.separator}Library${File.separator}" +
        "Application Support${File.separator}ketch"

    os.contains("win") -> {
      val appData = System.getenv("APPDATA")
        ?: "$home${File.separator}AppData${File.separator}Roaming"
      "$appData${File.separator}ketch"
    }

    else -> {
      val xdg = System.getenv("XDG_CONFIG_HOME")
        ?: "$home${File.separator}.config"
      "$xdg${File.separator}ketch"
    }
  }
}

/** Returns the default config file path: `<configDir>/config.toml`. */
fun defaultConfigPath(): String {
  return File(defaultConfigDir(), "config.toml").absolutePath
}

/** Returns the default database path: `<configDir>/ketch.db`. */
fun defaultDbPath(): String {
  val dir = File(defaultConfigDir())
  dir.mkdirs()
  return File(dir, "ketch.db").absolutePath
}
