package lionzxy

import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

const val BOT_USERNAME = "simply_spamblocker_bot"
const val BOT_TOKEN = SecureConfig.BOT_TOKEN
const val USERID_ERRORREPORT = 142752811L
const val USERID_LOG = 114892191L

fun main(args: Array<String>) {
    ApiContextInitializer.init()
    val botsApi = TelegramBotsApi()
    botsApi.registerBot(SpamBlockBot())
    println("All bot init!")
}

class SpamBlockBot : TelegramLongPollingBot() {
    override fun getBotUsername() = BOT_USERNAME

    override fun getBotToken() = BOT_TOKEN

    override fun onUpdateReceived(upd: Update?) {
        if (upd == null) {
            return
        }

        var isSpam = false

        if (upd.message.hasText() && containsSpam(upd.message.text)) {
            isSpam = true
        }

        if (upd.message.caption != null && containsSpam(upd.message.caption)) {
            isSpam = true
        }

        if (upd.message.captionEntities != null) {
            var spamCaption = upd.message.captionEntities.find { containsSpam(it.text) || containsSpam(it.url) }
            if (spamCaption != null) {
                isSpam = true
            }
        }

        if (!isSpam) {
            return
        }

        val deleteMessageMethod = DeleteMessage().setChatId(upd.message.chatId).setMessageId(upd.message.messageId)
        val notifyDeleteMessage = SendMessage().setChatId(USERID_LOG).setText("Удалено сообщение: \n\n ```${upd.message}```").enableMarkdown(true)

        try {
            execute(deleteMessageMethod)
            println("Удалено сообщение: ${upd.message}")
            execute(notifyDeleteMessage)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
            val notifyErrorMessage = SendMessage().setChatId(USERID_ERRORREPORT).setText("Ошибка удаления сообщения: ${e.localizedMessage} в чате c id ${upd.message.chatId}: ${upd.message}")
            execute(notifyErrorMessage)
        }
    }

    private fun containsSpam(text: String): Boolean {
        return text.contains("t.me/") || text.contains("t.cn/")
    }
}
