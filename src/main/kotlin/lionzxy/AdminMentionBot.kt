package lionzxy

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

class AdminMentionBot : TelegramLongPollingBot() {
    val triggerWord = listOf("@admin", "/admin")

    override fun getBotUsername() = "tpmailru_bot"
    override fun getBotToken() = SecureConfig.ADMIN_MENTION_TOKEN
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