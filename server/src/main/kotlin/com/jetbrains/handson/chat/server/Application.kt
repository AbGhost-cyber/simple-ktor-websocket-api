package com.jetbrains.handson.chat.server

import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import java.util.*
import kotlin.collections.LinkedHashSet


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    install(WebSockets)
    routing {
//        webSocket("/chat") {
//            send("You are connected")
//            for (frame in incoming) {
//                frame as? Frame.Text ?: continue
//                val receivedText = frame.readText()
//                send("You said $receivedText")
//            }
//        }
        //create a thread safe collections of connections
        val connections = Collections.synchronizedSet<Connection>(LinkedHashSet())
        webSocket("/chat") {
            println("Adding user!")
            //create a users connection object
            val thisConnection = Connection(this)
            //add connection to the thread safe collections
            connections.add(thisConnection)
            if(connections.isNotEmpty()){
                connections.filter { it.name != thisConnection.name }
                        .forEach { it.session.send("${thisConnection.name} joined the chat") }
            }
            try {
                send("You are connected! There are ${connections.count()} users here, say hello to others 😁")
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    val textWithUserName = "[${thisConnection.name}]: $receivedText"
                    connections.filter { it.name != thisConnection.name }
                            .forEach { it.session.send(textWithUserName) }
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            } finally {
                //remove user on connection terminated
                println("Removing ${thisConnection.name}!")
                connections.remove(thisConnection)
            }
        }
    }
}
