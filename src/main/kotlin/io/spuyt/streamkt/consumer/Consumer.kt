package io.spuyt.streamkt.consumer

import io.spuyt.streamkt.db.StreamDatabase
import io.spuyt.streamkt.event.EventMessage
import io.spuyt.streamkt.log.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicBoolean

open class Consumer {

    val consumerId: String
    var isRunning: Boolean = false

    private val streamDatabase: StreamDatabase

    var cursor: Long = 0L
        set(newCursor) {
            field = newCursor
            saveState()
        }

    var streamCursor: Long
    var isProcessing: AtomicBoolean = AtomicBoolean(false)

    val cursorChange = Channel<Unit>(0)

    var usesSelection: Boolean = false
    var eventTypes: List<String> = listOf<String>()
    var eventVersions: List<String> = listOf<String>()

    // variables for the update loop
    var batchSize = 25
    var processDelay = 1000L // add some processing delay so we dont DDOS the network if we are very far behind
    var retryDelay = 10*1000L // when processing faild, try again after some delay

    constructor(consumerId: String, db: StreamDatabase, streamCursor: Long) {
        Log.e(TAG, "creating consumer: ${consumerId}")
        this.consumerId = consumerId
        this.streamDatabase = db
        this.streamCursor = streamCursor
    }

    constructor(consumerId: String, db: StreamDatabase, streamCursor: Long, eventTypes: List<String>, eventVersions: List<String>)
            :this(consumerId, db, streamCursor) {

        usesSelection = true
        this.eventTypes = eventTypes
        this.eventVersions = eventVersions
    }

    init {
        Log.e(TAG, "initializing consumer")
        if(!isRunning) {
            isRunning = true
            startUpdateLoop() // process the events for this consumer every 'intervalMs'
        }else {
            Log.e(TAG, "want to start consumer update loop but was already running")
        }
    }

    fun startUpdateLoop() {
        Log.w(TAG, "startUpdateLoop")
        GlobalScope.launch {

            loadState() // loads it's last cursor position

            while (true) {
                if(streamCursor > cursor && isProcessing.compareAndSet(false, true)) {
                    try { // catch any error ever because we dont want this coroutine to die for whatever reason
                        // keep processing while we can process the entire batch size of events
                        var processedN = batchSize
                        while (processedN == batchSize) {
                            processedN = processNext(batchSize)
                            Log.w(TAG, "processed ${processedN} events")
                            // if we need to process multiple batches at once, delay the next batch a bit not to DDOS the network
                            if(processedN == batchSize) {
                                delay(processDelay)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "error in update loop", e)
                    }

                }
                isProcessing.set(false)

                // check if we still need to do more processing
                if(streamCursor > cursor) {
                    delay(retryDelay)
                }else {
                    // if we are already up-to-date, wait for cursor change
                    cursorChange.receive()
                    Log.i(TAG, "cursorChange received")
                }
            }
        }
    }

    fun notifyStreamEvent(newStreamCursor: Long) {
        Log.i(TAG, "got new stream cursor: ${newStreamCursor}")
        streamCursor = newStreamCursor
        // notify the update coroutine of a cursor change
        cursorChange.offer(Unit)
    }

    // main processing function used
    // dont call this function directly, it is used automatically
    open suspend fun process(eventMessage: EventMessage):Boolean{
        return true
    }

    // manual process next batch function
    suspend fun processNext(batchSize: Int = 1): Int {
        val events: List<EventMessage>
        if(!usesSelection) { // all events
            events = streamDatabase.getAfter(cursor, batchSize)
        }else { // only events of specific types with specific versions
            events = streamDatabase.getAfterSelection(cursor, eventTypes, eventVersions, batchSize)
        }
        var processed: Int = 0
        for(event in events) {
            val nextCursor = event.id
            var success = false
            try {
                success = process(event)
            }catch(e: Exception) { // in case someone created a malfunctioning process function
                Log.e(TAG, "exception in process function", e)
                success = false
            }
            if(!success) { // stop if not successful for whatever reason
                return processed
            }
            processed++
            cursor = nextCursor // should also save the cursor
        }
        return processed
    }

    // save (cursor position) function
    fun saveState(){
        val state = ConsumerState(consumerId, cursor)
        runBlocking {
            streamDatabase.saveConsumerState(state)
        }
    }

    // load (cursor position) function
    fun loadState(){
        runBlocking {
            val state: ConsumerState = streamDatabase.getConsumerState(consumerId)
            state.cursor.let { cursor = state.cursor }
            Log.w(TAG, "loaded cursor: ${cursor}")
        }
    }

    companion object {
        open val TAG = "Consumer"
    }
}


