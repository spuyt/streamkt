package io.spuyt.streamkt

import streamkt.consumer.Consumer
import streamkt.event.EventMessage
import streamkt.persister.Persister
import streamkt.producer.Producer

class EventStream {

    private var persister: Persister

    private var consumers = mutableMapOf<String, Consumer>()
    private var producers = mutableMapOf<String, Producer>()

    constructor(persister: Persister){
        this.persister = persister
    }

    fun postEvent(event: EventMessage){

    }

    fun addConsumer(tag: String, consumer: Consumer){
        if(consumers.containsKey(tag)){
            throw Exception("a consumer with this key already exists")
        }
        consumers.put(tag, consumer)
    }

    fun removeConsumer(tag: String){
        if(!consumers.containsKey(tag)){
            throw Exception("cannot find a consumer with this key")
        }
        consumers.remove(tag)
    }

    fun addProducer(tag: String, producer: Producer){
        if(producers.containsKey(tag)){
            throw Exception("a producer with this key already exists")
        }
        producers.put(tag, producer)
    }

    fun removeProducer(tag: String){
        if(!producers.containsKey(tag)){
            throw Exception("cannot find a producer with this key")
        }
        producers.remove(tag)
    }




}