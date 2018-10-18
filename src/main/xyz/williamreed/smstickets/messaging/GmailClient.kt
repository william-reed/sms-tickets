package xyz.williamreed.smstickets.messaging

import com.sun.mail.imap.IMAPFolder
import com.sun.mail.imap.IMAPStore
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.lang.IllegalArgumentException
import java.util.Properties
import java.util.concurrent.TimeUnit
import javax.mail.*
import javax.mail.event.MessageCountAdapter
import javax.mail.event.MessageCountEvent
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

// TODO: is this the best I can do?
// IDK what the idle timeout is on google but I haven't had luck other than this
private const val IDLE_PERIOD = 1L
private val IDLE_UNIT = TimeUnit.SECONDS

class GmailClient(private val username: String, private val password: String,
                  private val scheduler: Scheduler = Schedulers.computation()) : EmailClient {

    private val imapProperties: Properties = Properties().apply {
        this["mail.store.protocol"] = "imaps"
        this["mail.imaps.host"] = "imap.gmail.com"
        this["mail.imaps.port"] = "993"
        this["mail.imaps.timeout"] = "10000"
    }

    private val smtpProperties = Properties().apply {
        this["mail.smtp.auth"] = "true"
        this["mail.smtp.host"] = "smtp.gmail.com"
        this["mail.smtp.port"] = "465"
        this["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory";
    }

    lateinit var imapSession: Session
    lateinit var smtpSession: Session
    lateinit var store: IMAPStore
    lateinit var inbox: IMAPFolder

    lateinit var publishSubject: PublishSubject<Message>
    lateinit var idleTimer: Disposable

    override fun connect(): Observable<Message> {
        imapSession = Session.getInstance(imapProperties)
        store = imapSession.getStore("imaps") as IMAPStore
        store.connect(username, password)

        smtpSession = Session.getInstance(smtpProperties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })

        if (!store.hasCapability("IDLE")) {
            throw RuntimeException("IDLE not supported via mail server");
        }

        inbox = store.getFolder("INBOX") as IMAPFolder
        inbox.open(Folder.READ_ONLY)
        publishSubject = PublishSubject.create<Message>()

        inbox.addMessageCountListener(MessageCountHandler(publishSubject))
        // Gmail IDLE needs calls about every 10 minutes it seems
        idleTimer = Observable.interval(0, IDLE_PERIOD, IDLE_UNIT)
                .subscribeOn(scheduler)
                .subscribe {
                    inbox.idle()
                }

        return publishSubject
    }

    override fun sendMessage(recipient: String, subject: String, body: String) {
        val recipientAddress = InternetAddress(recipient)
        try {
            recipientAddress.validate()
        } catch (e: AddressException) {
            throw IllegalArgumentException("Illegal recipient email address given: '$recipient'")
        }

        val message = MimeMessage(smtpSession).apply {
            setFrom(InternetAddress(username))
            addRecipient(Message.RecipientType.TO, recipientAddress)
            setSubject(subject)
            setText(body)
        }
        // TODO: catch `MessagingException`
        Transport.send(message)
    }

    override fun disconnect() {
        inbox.close()
        publishSubject.onComplete()
        idleTimer.dispose()
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