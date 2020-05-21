package io.spuyt.streamkt.util

// get the unix timestamp in milliseconds, need to override in platform specific libraries
actual fun unix():Long{
    return System.currentTimeMillis()
}