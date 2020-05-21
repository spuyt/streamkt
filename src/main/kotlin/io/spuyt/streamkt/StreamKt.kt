import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import io.spuyt.streamkt.EventStream
import io.spuyt.streamkt.event.EventMessage

class StreamKt {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("Welcome to StreamKt")

            val stream = EventStream()

            val event = EventMessage("BOOKMARK", "1", "{\"tags\": [\"test\"], \"url\": \"https://spuyt.io\"}")

            GlobalScope.launch {
                stream.postEvent(event)
            }

            println()
            println("event history:")
            println(stream.eventHistory)

	
        }
    }
}
