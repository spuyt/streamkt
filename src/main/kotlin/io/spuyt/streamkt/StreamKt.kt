import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import io.spuyt.streamkt.EventStream
import io.spuyt.streamkt.db.mysql.MySql
import io.spuyt.streamkt.event.EventGen
import io.spuyt.streamkt.event.EventMessage
import io.spuyt.streamkt.mqtt.MqttProvider
import kotlin.system.exitProcess

class StreamKt {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("Welcome to StreamKt")

            // coroutine for the event stream
            GlobalScope.launch {

                // init mqtt
                MqttProvider.init("tcp://spuyt.io:1883")

                // init the database connection
                try {
                    MySql.connectDebug()
                }catch(e: Exception) {
                    println(e)
                    exitProcess(1) // terminate if we cannot connect to the database
                }

                // init event stream
                val stream = EventStream(MySql)

                // initialize the Event Generator
                EventGen.init("server-1", "spuyt")
                EventGen.appVersion = "server-alpha-1" // TODO use Gradle to generate

                println("starting benchmarks")
                // create some events for as a benchmark
                var start = System.currentTimeMillis()
                for(i in 0..9) {
                    try {
                        val event: EventMessage = EventGen.createEvent("SERVER_BENCHMARK", "1", "{\"status\": \"benchmark create ${i}\"}")
                        //println(event.toJson(true))
                    } catch (e: java.lang.Exception) {
                        println("could not post event into the stream: ${e}")
                    }
                }
                var end = System.currentTimeMillis()
                println("time per created event in ms: ${(end-start)/10}")

                // send some test events
                start = System.currentTimeMillis()
                for(i in 0..9) {
                    try {
                        val event: EventMessage = EventGen.createEvent("SERVER_BENCHMARK", "1", "{\"status\": \"benchmark post event ${i}\"}")
                        //println(event.toJson(true))
                        stream.postEvent(event)
                    } catch (e: java.lang.Exception) {
                        println("could not post event into the stream: ${e}")
                    }
                }
                end = System.currentTimeMillis()
                println("time per added event in ms: ${(end-start)/10}")

                // log the status every couple op seconds
                while(true) {
                    try {
                        println()
                        println("in memory local event history length: ${stream.eventHistory.size}")
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
