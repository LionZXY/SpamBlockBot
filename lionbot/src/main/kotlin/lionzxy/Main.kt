package lionzxy

import lionzxy.bots.admin.AdminMentionBot
import lionzxy.bots.iu4.TGIu4Bot
import lionzxy.bots.iu4.VKIu4Bot
import lionzxy.bots.spam.SpamBlockBot
import lionzxy.storage.Credentials
import lionzxy.storage.CredentialsEnum
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi

object Main {
    val tgIu4Bot = TGIu4Bot()
    val vkIu4Bot = VKIu4Bot()
}

fun main(args: Array<String>) {
    ApiContextInitializer.init()
    val isDebug = Credentials.get(CredentialsEnum.ENVIRONMENT).equals("DEBUG", true)
    val botsApi = TelegramBotsApi()
    if (isDebug) {
        Main.vkIu4Bot.init()
        botsApi.registerBot(Main.tgIu4Bot)
        return
    }
    botsApi.registerBot(SpamBlockBot())
    botsApi.registerBot(AdminMentionBot())
    botsApi.registerBot(Main.tgIu4Bot)
    Main.vkIu4Bot.init()
    println("All bot init!")
}
