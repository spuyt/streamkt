package io.spuyt.streamkt.event

import io.spuyt.streamkt.Guid

object EventGen{
    var appVersion: String? = null // TODO load from build settings
    lateinit var deviceId: String
    lateinit var locationId: String

    fun init(deviceId: String, locationId: String) {
        this.deviceId = deviceId
        this.locationId = locationId
    }

    fun createEvent (
            eventType: String,
            eventVersion: String,
            payloadJson: String,
            eventTimeUnixSec: Long = System.currentTimeMillis() / 1000L // default to now
    ): EventMessage {
        return EventMessage(
                eventId = Guid.nextGUID(),
                creationTimeUnixSec = System.currentTimeMillis() / 1000L,
                eventTimeUnixSec = eventTimeUnixSec,
                deviceId = deviceId!!,
                locationId = locationId!!,
                appVersion = appVersion!!,
                eventType = eventType,
                eventVersion = eventVersion,
                payloadJson = payloadJson
        )
    }
}
