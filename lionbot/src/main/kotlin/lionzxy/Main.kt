package lionzxy

import lionzxy.bots.admin.AdminMentionBot
import lionzxy.bots.iu4.TGIu4Bot
import lionzxy.bots.iu4.VKIu4Bot
import lionzxy.bots.spam.SpamBlockBot
import lionzxy.bots.switch.SwitchBot
import lionzxy.bots.switch.SwitchIdInformationDAO
import lionzxy.storage.Credentials
import lionzxy.storage.CredentialsEnum
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi
import java.sql.Connection

object Main {
    val tgIu4Bot = TGIu4Bot()
    val vkIu4Bot = VKIu4Bot()
}

fun main(args: Array<String>) {
    ApiContextInitializer.init()
    val isDebug = Credentials.get(CredentialsEnum.ENVIRONMENT).equals("DEBUG", true)
    val botsApi = TelegramBotsApi()
    initDB()
    if (isDebug) {
        botsApi.registerBot(SwitchBot())
        return
    }
    botsApi.registerBot(SwitchBot())
    botsApi.registerBot(SpamBlockBot())
    botsApi.registerBot(AdminMentionBot())
    botsApi.registerBot(Main.tgIu4Bot)
    Main.vkIu4Bot.init()
    println("All bot init!")
}

private fun initDB() {
    Database.connect("jdbc:sqlite:local.db", driver = "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE // Or Connection.TRANSACTION_READ_UNCOMMITTED

    transaction {
        SchemaUtils.create(SwitchIdInformationDAO)
    }
}
