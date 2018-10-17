package xyz.williamreed.smstickets.messaging

import io.reactivex.Observable
import javax.mail.Message

interface EmailClient {
    /**
     * Connect to the mail server and return an observable of incoming messages
     */
    fun connect(): Observable<Message>
    fun sendMessage(recipient: String, subject: String, body: String)
    fun disconnect()
}