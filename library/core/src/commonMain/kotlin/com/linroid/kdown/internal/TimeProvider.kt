package com.linroid.kdown.internal

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
