package lionzxy

import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi

fun main(args: Array<String>) {
    ApiContextInitializer.init()
    val botsApi = TelegramBotsApi()
    botsApi.registerBot(SpamBlockBot())
    botsApi.registerBot(AdminMentionBot())
    println("All bot init!")
}
