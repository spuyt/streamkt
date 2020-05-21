package io.spuyt.streamkt.util

// get the unix timestamp in milliseconds, need to override in platform specific libraries
expect fun unix():Long

object Id {

    var deviceId: String = ""
    var locationId: String = ""
    var appVersion: String = ""

    fun nextId():String{
       // TODO implementation
        return ""
    }
}
