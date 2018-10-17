package xyz.williamreed.smstickets.messaging

import com.sun.mail.imap.IMAPFolder
import com.sun.mail.imap.IMAPStore
import io.reactivex.subjects.PublishSubject
import java.util.Properties
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.event.MessageCountAdapter
import javax.mail.event.MessageCountEvent
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class GmailClient(private val username: String, private val password: String) : EmailClient {
    var properties: Properties = Properties().apply {
        this["mail.store.protocol"] = "imaps";
        this["mail.imaps.host"] = "imap.gmail.com";
        this["mail.imaps.port"] = "993";
        this["mail.imaps.timeout"] = "10000";
    }
    lateinit var session: Session
    lateinit var store: IMAPStore
    lateinit var inbox: IMAPFolder
    lateinit var publishSubject: PublishSubject<Message>

    override fun connect(): PublishSubject<Message> {
        session = Session.getInstance(properties)
        store = session.getStore("imaps") as IMAPStore
        store.connect(username, password)

        if (!store.hasCapability("IDLE")) {
            throw RuntimeException("IDLE not supported via mail server");
        }

        inbox = store.getFolder("INBOX") as IMAPFolder
        publishSubject = PublishSubject.create<Message>()
        inbox.addMessageCountListener(MessageCountHandler(publishSubject))

        return publishSubject
    }

    override fun sendMessage(recipient: String, subject: String, body: String) {
        // TODO: validate email
        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(username))
            addRecipient(Message.RecipientType.TO, InternetAddress(recipient))
            setSubject(subject)
            setText(body)
        }
        // TODO: catch `MessagingException`
        Transport.send(message)
    }

    override fun disconnect() {
        publishSubject.onComplete()
        store.close()
    }

    class MessageCountHandler(private val publishSubject: PublishSubject<Message>) : MessageCountAdapter() {
        override fun messagesAdded(e: MessageCountEvent?) {
            if (e == null) return

            for (message in e.messages) {
                publishSubject.onNext(message)
            }
        }
    }
}