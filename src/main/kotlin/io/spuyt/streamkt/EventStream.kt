package io.spuyt.streamkt

import io.spuyt.streamkt.event.EventMessage
import io.spuyt.streamkt.consumer.Consumer
import io.spuyt.streamkt.db.StreamDatabase
import io.spuyt.streamkt.log.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.lang.Exception

class EventStream(
        val streamDatabase: StreamDatabase
) {
    private var cursor: Long = 0L

    val eventChannel = Channel<EventMessage>(0)

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
        Log.i(TAG, "initializing EventStream")

        startProcessLoop()
    }

    fun startProcessLoop() {
        Log.i(TAG, "starting process loop")

        GlobalScope.launch {

            cursor = streamDatabase.getCurrentCursor()
            Log.i(TAG, "current cursor is ${cursor}")

            while (true) {
                // wait till we get another event
                val event = eventChannel.receive()
                Log.i(Consumer.TAG, "cursorChange received")

                try {
                    // add in (remote) database
                    streamDatabase.insert(event)

                    // add to local history
                    addEventHistory(event)

                    // notify consumers
                    for (c in consumers) {
                        c.notifyStreamEvent(cursor)
                    }
                }catch (e: Exception) {
                    Log.e(TAG, "error posting event", e)
                }
            }
        }
    }

    fun postEvent(event: EventMessage) {
        Log.i(TAG, "posting event")
        eventChannel.offer(event)

    }

    companion object {
        private const val TAG = "EventStream"
    }

}
