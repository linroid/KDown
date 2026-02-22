package com.linroid.ketch.config

import java.io.File

/** Default TOML config template for new installations. */
const val DEFAULT_CONFIG_CONTENT = """# Ketch Server Configuration
# Lines starting with # are comments.

# name = "My Ketch"

[server]
host = "0.0.0.0"
port = 8642
# apiToken = "my-secret"
# corsAllowedHosts = ["http://localhost:3000"]

[download]
# defaultDirectory = "~/Downloads"
# speedLimit = "10m"  # "unlimited", "10m" (MB/s), "500k" (KB/s), or raw bytes
maxConnections = 4
retryCount = 3
retryDelayMs = 1000
progressUpdateIntervalMs = 200
segmentSaveIntervalMs = 5000
bufferSize = 8192

[download.queueConfig]
maxConcurrentDownloads = 3
maxConnectionsPerHost = 4
autoStart = true
"""

/** Writes a default config file to [path]. */
fun generateConfig(path: String) {
  val file = File(path)
  file.parentFile?.mkdirs()
  file.writeText(DEFAULT_CONFIG_CONTENT)
}
