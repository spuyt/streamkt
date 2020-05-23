package io.spuyt.streamkt.event

import com.google.gson.Gson
import com.google.gson.GsonBuilder

data class EventMessage(
        // message id
        var id: Long = 0L, // use this to get the events in the order they were added to the database
        var eventId: String,
        var creationTimeUnixSec: Long,
        // the time this event happened
        var eventTimeUnixSec: Long,
        // creator information
        var deviceId: String,
        var locationId: String,
        var appVersion: String,
        // to identify compatible consumers
        val eventType: String,
        val eventVersion: String,
        // content of the message
        var payloadJson: String // Payload, encoded in JSON
){
    fun toJson(pretty: Boolean = false): String {
        if (pretty) {
            return gsonPretty.toJson(this)
        }
        return gson.toJson(this)
    }

    companion object {
        private val gson = Gson()
        private val gsonPretty = GsonBuilder().setPrettyPrinting().create()

        fun fromJson(json: String): EventMessage{
            return gson.fromJson(json, EventMessage::class.java)
        }
    }
}
