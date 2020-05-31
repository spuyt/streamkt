import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import io.spuyt.streamkt.EventStream
import io.spuyt.streamkt.db.mysql.MySql
import io.spuyt.streamkt.event.EventGen
import io.spuyt.streamkt.event.EventMessage
import io.spuyt.streamkt.log.Log
import io.spuyt.streamkt.mqtt.MqttProvider
import kotlin.system.exitProcess

class StreamKt {

    companion object {
        const val TAG = "StreamKt"

        @JvmStatic
        fun main(args: Array<String>) {
            Log.i(TAG, "Welcome to StreamKt")

            // coroutine for the event stream
            GlobalScope.launch {

                // init the database connection
                try {
                    //MySql.connectDebug()
                    // url, port, username, password
                    MySql.connectPostgreSQL(args[0], args[1], args[2])
                }catch(e: Exception) {
                    Log.e(TAG, "cannot connect to PostgreSQL", e)
                    exitProcess(1) // terminate if we cannot connect to the database
                }
                Log.i(TAG, "connected to PostgreSQL")

                // init event stream
                val stream = EventStream(MySql)
                Log.i(TAG, "initialized event stream")

                // init mqtt
                MqttProvider.init("tcp://spuyt.io:1883", stream)
                Log.i(TAG, "initialized mqtt")


                // initialize the Event Generator
                EventGen.init("server-1", "spuyt")
                EventGen.appVersion = "server-alpha-1" // TODO use Gradle to generate

                Log.i(TAG, "initialized event gen")

                Log.i(TAG, "starting benchmarks")

                // send some test events
                var start = System.currentTimeMillis()
                for(i in 0..9) {
                    try {
                        val event: EventMessage = EventGen.createEvent("SERVER_BENCHMARK", "1", "{\"status\": \"benchmark post event ${i}\"}")
                        stream.postEvent(event)
                    } catch (e: java.lang.Exception) {
                        Log.e(TAG, "could not post event into the stream", e)
                    }
                }
                var end = System.currentTimeMillis()
                Log.i(TAG, "time per added event in ms: ${(end-start)/10}")

                // log the status every couple op seconds
                while(true) {
                    try {
                        Log.i(TAG, "in memory local event history length: ${stream.eventHistory.size}")
                        Thread.sleep(10 * 1000)
                    } catch (e: Exception) {
                        Log.e(TAG, "EXCEPTION IN MAIN EVENT STREAM COROUTINE", e)
                    }
                }
            }

            // keep running
            while (true) {
                try {
                    Thread.sleep(10 * 1000)
                } catch (e: Exception) {
                    Log.e(TAG, "EXCEPTION IN MAIN THREAD", e)
                }
            }

        }
    }
}
