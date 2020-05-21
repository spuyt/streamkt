package io.spuyt.streamkt.consumer

data class ConsumerState(
        val consumerId: String,
        val cursor: Long
) {
}