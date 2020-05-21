package io.spuyt.streamkt

import io.spuyt.streamkt.consumer.ConsumerState
import io.spuyt.streamkt.event.EventMessage

interface StreamDatabase {

    @Throws(Exception::class)
    suspend fun insert(event: EventMessage): Long

    @Throws(Exception::class)
    suspend fun getAfter(cursor: Long, batchSize: Int): List<EventMessage>

    @Throws(Exception::class)
    suspend fun getAfterSelection(cursor: Long, eventTypes: List<String>, eventVersions: List<String>, batchSize: Int): List<EventMessage>

    @Throws(Exception::class)
    suspend fun getConsumerState(consumerId: String): ConsumerState

    @Throws(Exception::class)
    suspend fun saveConsumerState(consumerState: ConsumerState)
}