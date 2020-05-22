package io.spuyt.streamkt.mqtt

import com.google.gson.Gson
import com.google.gson.JsonParseException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.*

class MqttProvider(uri: String, statusIntervalSec: Long = 2) {
    private var mqttClient: IMqttAsyncClient
    private lateinit var connectCallback: IMqttActionListener

    private var devices: MutableMap<String, Device> = mutableMapOf<String, Device>()

    var isConnected: Boolean = false
    var isConnecting: Boolean = false
    var statusIntervalSec: Long = 10L

    private val locationPath = "eventstream/#"
    private val devicePath = "eventstream/global/server"
    private val statusPath = "eventstream/global/status"

    private val QOS_EXACTLY_ONCE: Int = 2 // There are 3 QoS levels in MQTT: At most once (0) At least once (1) Exactly once (2).

    private val gson = Gson()

    init {
        println("mqtt: creating MqttClientProvider")

        mqttClient = MqttAsyncClient(uri, "server", null)

        //val options = MqttConnectOptions()
        //mqttClient.connect(options)
        connect()

        receiveMessages()

        // put in my own status without going through mqtt
        val pingStr = PingEvent.myStatusJson("connecting")
        val ping= gson.fromJson(pingStr, PingEvent::class.java)
        val deviceId: String = ping.deviceId
        devices.get(deviceId)?.let {
            it.lastOnlineUnix = ping.timestamp
            it.addPingEvent(ping)
        } ?:{
            val device = Device(deviceId, ping.locationId, ping.timestamp)
            device.addPingEvent(ping)
            devices.put(deviceId, device)
        }()
        publish(statusPath, pingStr)

        // start loop to send status updates
        this.statusIntervalSec = statusIntervalSec
        statusLoop()
    }

    fun connect(){
        isConnecting = true
        try {
            // set the callback methods
            connectCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken)                        {
                    println("mqtt: connected")
                    // start receiving messages
                    // Give your callback on connection established here
                    subscribe(locationPath)
                    publish(statusPath, PingEvent.myStatusJson("joining"))
                    isConnected = true
                    isConnecting = false
                }
                override fun onFailure(asyncActionToken: IMqttToken, e: Throwable) {
                    //connectionStatus = false
                    println("mqtt: failed to connect")
                    println(e)
                    isConnecting = false
                }
            }
            // connect to the mqtt broker
            println("mqtt: trying to connect...")
            val options = MqttConnectOptions()
            mqttClient.connect(options, connectCallback)
        } catch (e: MqttException) {
            // Give your callback on connection failure here
            println("mqtt: exception")
            println(e)
            isConnecting = false
        }
    }

    fun statusLoop(){
        GlobalScope.launch {
            while (true) {
                try { // catch any error ever because we dont want this coroutine to die for whatever reason
                    if(!isConnected) {
                        println("mqtt: sending status update, currently connected: ${isConnected}, currently connecting: ${isConnecting}")
                    }
                    if (isConnected) { // publish status if connected
                        publish(statusPath, PingEvent.myStatusJson("online"))
                    } else if(!isConnecting){ // try to reconnect if not already trying to connect
                        connect()
                    }
                } catch(e: Exception){
                    println("mqtt: error in MQTT status loop")
                    println(e)
                }

                //Thread.sleep(intervalSec * 1000)
                delay(statusIntervalSec * 1000)
            }
        }
    }

    fun subscribe(topic: String) {
        try {
            mqttClient.subscribe(topic, QOS_EXACTLY_ONCE)
        } catch (e: MqttException) {
            // Give your subscription failure callback here
            println("mqtt: could not subscribe to topic: ${topic}")
            println(e)
        }
    }

    fun unSubscribe(topic: String) {
        try {
            mqttClient.unsubscribe(topic)
        } catch (e: MqttException) {
            // Give your callback on failure here
            println("mqtt: exception")
            println(e)
        }
    }

    fun receiveMessages() {
        mqttClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable) {
                isConnected = false
                // Give your callback on failure here
                println("mqtt: connection lost - ${cause}")
            }
            override fun messageArrived(topic: String, message: MqttMessage) {
                try {
                    val data = String(message.payload, charset("UTF-8"))
                    // data is the desired received message
                    // Give your callback on message received here
                    println("mqtt: received message on topic: ${topic}\nmessage : ${data}")

                    // decide what to do with the message
                    if(topic == statusPath){
                        addStatus(data)
                    }

                } catch (e: Exception) {
                    // Give your callback on error here
                    println("mqtt: error receiving message")
                    println(e)
                }
            }
            override fun deliveryComplete(token: IMqttDeliveryToken) {
                // Acknowledgement on delivery complete
                println("mqtt: message delivered")
            }

        })
    }

    fun addStatus(msg: String){
        try{
            val ping= gson.fromJson(msg, PingEvent::class.java)
            val deviceId: String = ping.deviceId
            devices.get(deviceId)?.let {
                it.lastOnlineUnix = ping.timestamp
                it.addPingEvent(ping)
            } ?:{
                val device = Device(deviceId, ping.locationId, ping.timestamp)
                device.addPingEvent(ping)
                devices.put(deviceId, device)
            }()
        } catch (e: JsonParseException){
            println("mqtt: could not parse status")
            println(e)
        }
    }

    fun publishToLocation(json: String, retain: Boolean = false) {
        publish(devicePath, json, retain)
    }

    fun publish(topic: String, data: String, retain: Boolean = false) {
        var encodedPayload : ByteArray
        try {
            encodedPayload = data.toByteArray(charset("UTF-8"))
            val message = MqttMessage(encodedPayload)
            message.qos = QOS_EXACTLY_ONCE
            message.isRetained = retain
            mqttClient.publish(topic, message)
            println("mqtt: message sent")
        } catch (e: Exception) {
            // Give Callback on error here
            println("mqtt: exception publishing message on topic: ${topic}")
            println(e)
        } catch (e: MqttException) {
            // Give Callback on error here
            println("mqtt: error publishing message")
            println(e)
        }
    }

    fun disconnect() {
        try {
            mqttClient.disconnect()
        } catch (e: MqttException) {
            // Give Callback on error here
            println("mqtt: error disconnecting")
            println(e)
        }
    }


}