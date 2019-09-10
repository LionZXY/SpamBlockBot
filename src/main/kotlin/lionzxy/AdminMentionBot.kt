package lionzxy

import lionzxy.storage.Credentials
import lionzxy.storage.CredentialsEnum
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.concurrent.TimeUnit

class AdminMentionBot : TelegramLongPollingBot() {
    var lastMentionTimestamps = HashMap<Long, Long>() //chatId to timestamp
    val triggerWord = listOf("/admin")

    override fun getBotUsername() = Credentials.get(CredentialsEnum.ADMIN_MENTION_BOT_USERNAME)
    override fun getBotToken() = Credentials.get(CredentialsEnum.ADMIN_MENTION_BOT_TOKEN)
    override fun onUpdateReceived(update: Update?) {
        val msg = update?.message ?: return
        val text = msg.text ?: return

        val findWord = triggerWord.firstOrNull { text.startsWith(it) } ?: return

        println("Find word $findWord in $text")

        sendApiMethod(DeleteMessage(msg.chatId, msg.messageId))

        val mentionTimeout = TimeUnit.MINUTES.toMillis(
                Credentials.get(CredentialsEnum.ADMIN_MENTION_BOT_TIMEOUT_MINUTE).toLong())
        val diff = System.currentTimeMillis() - (lastMentionTimestamps[msg.chatId] ?: 0L)
        if (diff < mentionTimeout) {
            println("Not call mention because mentionTimeout ($mentionTimeout) larger then diff $diff")
            return
        }

        lastMentionTimestamps[msg.chatId] = System.currentTimeMillis()

        val message = SendMessage()
        message.chatId = msg.chatId.toString()
        message.text = "@StealthTech @reo7sp @LionZXY @Ansile"
        sendApiMethod(message)
    }

}