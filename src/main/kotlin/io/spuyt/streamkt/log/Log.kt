package io.spuyt.streamkt.log

object Log {
    fun d(tag: String, msg: String) {
        println("${tag}: ${msg}")
    }
    fun i(tag: String, msg: String) {
        println("${tag}: ${msg}")
    }
    fun v(tag: String, msg: String) {
        println("${tag}: ${msg}")
    }
    fun w(tag: String, msg: String) {
        println("${tag}: ${msg}")
    }
    fun e(tag: String, msg: String, e: Throwable? = null) {
        println("${tag}: ${msg}")
        e?.let{println(e)}
    }
    fun a(tag: String, msg: String) {
        println("${tag}: ${msg}")
    }
}