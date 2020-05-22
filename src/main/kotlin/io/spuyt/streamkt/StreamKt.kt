import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import io.spuyt.streamkt.EventStream
import io.spuyt.streamkt.event.EventMessage
import io.spuyt.streamkt.mqtt.MqttProvider
import kotlinx.coroutines.delay

class StreamKt {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("Welcome to StreamKt")

            // coroutine for the event stream
            GlobalScope.launch {
                // init event stream
                val stream = EventStream()

                // init mqtt
                MqttProvider("tcp://spuyt.io:1883")

                // send first event
                val event = EventMessage("SERVER_STATUS", "1", "{\"status\": \"launched\"}")
                try {
                    stream.postEvent(event)
                }catch(e: java.lang.Exception) {
                    println("could not post event into the stream: ${e}")
                }

                // log the status every couple op seconds
                while(true) {
                    try {
                        println()
                        println("event history:")
                        println(stream.eventHistory)
                        Thread.sleep(10 * 1000)
                    } catch (e: Exception) {
                        println("EXCEPTION IN MAIN EVENT STREAM COROUTINE")
                        println(e)
                    }
                }
            }

            // keep running
            while (true) {
                try {
                    Thread.sleep(10 * 1000)
                } catch (e: Exception) {
                    println("EXCEPTION IN MAIN THREAD")
                    println(e)
                }
            }

        }
    }
}
