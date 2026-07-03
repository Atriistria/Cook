package com.atride.cook

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform