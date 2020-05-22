package io.spuyt.streamkt.consumer

data class ConsumerState(
        val consumerId: String,
        val cursor: Long
) {
    val id: Long = 0L // used by the database
}