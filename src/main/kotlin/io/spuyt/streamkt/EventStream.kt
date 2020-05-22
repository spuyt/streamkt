package io.spuyt.streamkt

import io.spuyt.streamkt.event.EventMessage
import io.spuyt.streamkt.consumer.Consumer
import io.spuyt.streamkt.db.StreamDatabase
import java.lang.NullPointerException

class EventStream() {

    private val streamDatabase: StreamDatabase? = null

    var consumers = mutableListOf<Consumer>()

    val historySize: Int = 1000
    val eventHistory = mutableListOf<EventMessage>()

    fun addEventHistory(event: EventMessage){
        eventHistory.add(event)
        if(eventHistory.size > historySize){
            eventHistory.removeAt(0)
        }
    }

    init{
        println("initializing EventStream")
    }

    suspend fun postEvent(event: EventMessage) {
        if(streamDatabase == null) {
            throw NullPointerException("streamDatabase is null")
        }

        // add in (remote) database
        streamDatabase.insert(event)

        // add to local history
        addEventHistory(event)


        // TODO update consumers
    }

    companion object {
        private const val TAG = "EventStream"
    }

}
