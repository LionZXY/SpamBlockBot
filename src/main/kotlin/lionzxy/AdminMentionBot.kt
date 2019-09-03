package lionzxy

import lionzxy.storage.Credentials
import lionzxy.storage.CredentialsEnum
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

class AdminMentionBot : TelegramLongPollingBot() {
    val triggerWord = listOf("@admin", "/admin")

    override fun getBotUsername() = Credentials.get(CredentialsEnum.ADMIN_MENTION_BOT_USERNAME)
    override fun getBotToken() = Credentials.get(CredentialsEnum.ADMIN_MENTION_BOT_TOKEN)
    override fun onUpdateReceived(update: Update?) {
        val msg = update?.message ?: return
        val text = msg.text ?: return

        val findWord = triggerWord.firstOrNull { text.startsWith(it) } ?: return

        val message = SendMessage()
        message.chatId = msg.chatId.toString()
        message.text = "@StealthTech @reo7sp @LionZXY @Ansile"
        sendApiMethod(message)
    }

}