package com.linroid.kdown.api

actual fun Destination.isFile(): Boolean =
  !isName() && !isDirectory()

actual fun Destination.isDirectory(): Boolean =
  value.endsWith('/') || value.endsWith('\\')

actual fun Destination.isName(): Boolean =
  !value.contains('/') && !value.contains('\\')
