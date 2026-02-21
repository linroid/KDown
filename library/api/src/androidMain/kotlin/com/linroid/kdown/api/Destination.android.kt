package com.linroid.kdown.api

import android.provider.DocumentsContract
import androidx.core.net.toUri

actual fun Destination.isFile(): Boolean =
  !isName() && !isDirectory()

actual fun Destination.isDirectory(): Boolean {
  val uri = value.toUri()
  if (uri.scheme == "content") {
    return DocumentsContract.isTreeUri(uri)
  }
  return value.endsWith('/') || value.endsWith('\\')
}

actual fun Destination.isName(): Boolean {
  val uri = value.toUri()
  if (uri.scheme != null) return false
  return !value.contains('/') && !value.contains('\\')
}
