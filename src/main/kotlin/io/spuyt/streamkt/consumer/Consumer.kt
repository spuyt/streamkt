package io.spuyt.streamkt.consumer

import io.spuyt.streamkt.StreamDatabase
import io.spuyt.streamkt.event.EventMessage


open class Consumer(private val consumerId: String) {

    val streamDatabase: StreamDatabase? = null
    var cursor:Long = 0L
        set(newCursor) {
            field = newCursor
            // saveState() // TODO make into coroutine
        }

    var usesSelection: Boolean = false
    var eventTypes: List<String> = listOf<String>()
    var eventVersions: List<String> = listOf<String>()

    constructor(consumerId: String, eventTypes: List<String>, eventVersions: List<String>) : this(consumerId) {
        usesSelection = true
        this.eventTypes = eventTypes
        this.eventVersions = eventVersions
    }

    init {
        //loadState() // loads it's last cursor position TODO make into coroutine
    }

    // main processing function used
    // dont call this function directly, it is used automatically
    open fun process(eventMessage: EventMessage):Boolean{
        return true
    }

    // manual process next batch function
    suspend fun processNext(batchSize: Int = 1): Int {
        val events: List<EventMessage>
        if(!usesSelection) { // all events
            events = streamDatabase!!.getAfter(cursor, batchSize)
        }else { // only events of specific types with specific versions
            events = streamDatabase!!.getAfterSelection(cursor, eventTypes, eventVersions, batchSize)
        }
        var processed: Int = 0
        for(event in events) {
            val nextCursor = event.id
            val success = process(event)
            if(!success) { // stop if not successful for whatever reason
                return processed
            }
            cursor = nextCursor // should also save the cursor
        }
        return processed
    }

    // save (cursor position) function
    suspend fun saveState(){
        val state = ConsumerState(consumerId, cursor)
        streamDatabase!!.saveConsumerState(state)
    }

    // load (cursor position) function
    suspend fun loadState(){
        val state: ConsumerState = streamDatabase!!.getConsumerState(consumerId)
        this.cursor = state.cursor
    }

}
