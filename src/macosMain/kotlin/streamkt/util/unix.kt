package streamkt.util

actual fun unix(): Long {
    return java.lang.System.currentTimeMillis()
}