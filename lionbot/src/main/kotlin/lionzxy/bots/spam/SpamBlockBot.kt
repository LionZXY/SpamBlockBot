package lionzxy.bots.spam

import lionzxy.storage.Credentials
import lionzxy.storage.CredentialsEnum
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import java.io.PrintWriter
import java.io.StringWriter


private val USERID_ERRORREPORT = Credentials.get(CredentialsEnum.USERID_ERRORREPORT)

class SpamBlockBot : TelegramLongPollingBot() {
    override fun getBotUsername() = Credentials.get(CredentialsEnum.SPAM_BOT_USERNAME)

    override fun getBotToken() = Credentials.get(CredentialsEnum.SPAM_BOT_TOKEN)

    override fun onUpdateReceived(upd: Update?) {
        try {
            SpamBlocker.processUpd(upd, this)
        } catch (e: Exception) {
            execute(SendMessage(USERID_ERRORREPORT, "Писец, насяйника"))
            val stackTrace = StringWriter().apply { e.printStackTrace(PrintWriter(this)) }.toString()
            e.printStackTrace()
            execute(SendMessage(USERID_ERRORREPORT, "$e\n$stackTrace"))
            execute(SendMessage(USERID_ERRORREPORT, "$upd"))
        }
    }

}
