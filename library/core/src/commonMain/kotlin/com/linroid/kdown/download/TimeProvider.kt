package com.linroid.kdown.download

import kotlin.time.Clock

internal fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
