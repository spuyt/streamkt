package io.spuyt.streamkt.mqtt

data class Device(
        val deviceId: String,
        val locationId: String,
        var lastOnlineUnix: Long,
        var lastEventId: String = "",
        @Transient val historySize: Int = 600 // one hour if status is pinged every 10 seconds
) {
    private val pingEvents = mutableListOf<PingEvent>()

    fun addPingEvent(pingEvent: PingEvent){
        pingEvents.add(pingEvent)
        if(pingEvents.size > historySize){
            pingEvents.removeAt(0)
        }
    }

    fun getPingEvents(): MutableList<PingEvent> {
        return pingEvents
    }

}