package io.spuyt.streamkt.event

import com.google.gson.Gson
import com.google.gson.GsonBuilder

data class EventMessage(
        // content of the message
        val eventType: String, // SALE, PAYMENT, CLOSE, etc  -> payload type
        val eventVersion: String,
        var payloadJson: String, // Payload, encoded in JSON. Things like Journal.toJson()
        // optional content
        var eventTimeUnixSec: Long = currentUnixSeconds() // for a custom event time that is different from the creation time of its message
){
    // message id
    var id: Long = 0L // use this to get the events in the order they were added to the database
    var eventId: String = ""
    var creationTimeUnixSec: Long = currentUnixSeconds()
    // creator information
    var deviceId: String = ""
    var locationId: String = ""
    var appVersion: String = ""

    fun toJson(pretty: Boolean = false): String {
        if (pretty) {
            return gsonPretty.toJson(this)
        }
        return gson.toJson(this)
    }

    companion object {
        private val gson = Gson()
        private val gsonPretty = GsonBuilder().setPrettyPrinting().create()

        fun currentUnixSeconds(): Long = System.currentTimeMillis() / 1000

        fun fromJson(json: String): EventMessage{
            return gson.fromJson(json, EventMessage::class.java)
        }

    }
}
