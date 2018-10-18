import io.reactivex.schedulers.Schedulers
import xyz.williamreed.smstickets.messaging.GmailClient
import java.lang.Thread.sleep

fun main(args: Array<String>) {
    val emailClient = GmailClient(System.getenv("EMAIL_USER"), System.getenv("EMAIL_PASS"))
    val messageObservable = emailClient.connect()

//    emailClient.sendMessage("test@gmail.com", "test", "body text")

    messageObservable.observeOn(Schedulers.computation())
            .subscribe({
                println("Subject: ${it.subject}\nBody: ${it.content}")
            }, ::println)

    sleep(10000000)
}