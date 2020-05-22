package io.spuyt.streamkt.mqtt

import com.google.gson.Gson

data class PingEvent(
        val deviceId: String,
        val locationId: String,
        val timestamp: Long,

        val status: String
        // TODO last eventId
){

    fun toJson():String{
        return gson.toJson(this)
    }

    companion object {
        val gson = Gson()

        fun myStatusJson(status: String): String {
            val pe: PingEvent = PingEvent(
                    "server",
                    "global",
                    System.currentTimeMillis(),
                    status
            )

            return pe.toJson()
        }
    }
}

