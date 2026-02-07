package com.linroid.kdown.internal

import kotlinx.datetime.Clock

internal fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
