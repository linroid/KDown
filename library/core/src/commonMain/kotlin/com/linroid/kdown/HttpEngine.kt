package com.linroid.kdown

import com.linroid.kdown.model.ServerInfo

interface HttpEngine {
  suspend fun head(url: String): ServerInfo

  suspend fun download(
    url: String,
    range: LongRange?,
    onData: suspend (ByteArray) -> Unit
  )

  fun close()
}
