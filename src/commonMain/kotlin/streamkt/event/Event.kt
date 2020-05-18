package streamkt.event

import streamkt.util.Id
import streamkt.util.unix

data class EventMessage(

        // information for deserializers
        val eventType: String,
        val eventVersion: String,

        // content of the message
        var payloadJson: String, // Payload, encoded in JSON. Things like Journal.toJson()

        // optional event time that differs from the EventMessage creation time
        var eventTimeUnix: Long = unix()

){

    // identification
    var eventId: String = Id.nextId()
    var creationTimeUnix: Long = unix()

    // creator information
    var deviceId: String = Id.deviceId
    var locationId: String = Id.locationId
    var appVersion: String = Id.appVersion

    // convert to json
    fun toJson(pretty: Boolean = false): String {
        if (pretty) {
            return gsonPretty.toJson(this)
        }
        return gson.toJson(this)
    }

    companion object {
        private val gson = Gson()
        private val gsonPretty = GsonBuilder().setPrettyPrinting().create()

        // obtain from json
        fun fromJson(json: String): EventMessage{
            return gson.fromJson(json, EventMessage::class)
        }

    }
}

