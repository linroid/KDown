package com.linroid.kdown

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform