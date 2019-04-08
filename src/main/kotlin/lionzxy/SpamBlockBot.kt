package lionzxy

import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

const val BOT_USERNAME = "simply_spamblocker_bot"
const val BOT_TOKEN = SecureConfig.BOT_TOKEN

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
        try {
            SpamBlocker.processUpd(upd, this)
        } catch (e: Exception) {
            execute(SendMessage(SecureConfig.USERID_ERRORREPORT, "Писец, насяйника"))
            execute(SendMessage(SecureConfig.USERID_ERRORREPORT, "$e"))
            execute(SendMessage(SecureConfig.USERID_ERRORREPORT, "$upd"))
        }
    }

}
