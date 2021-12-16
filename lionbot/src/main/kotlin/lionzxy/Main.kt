package lionzxy

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import lionzxy.bots.iu4.TGIu4Bot
import lionzxy.bots.iu4.VKIu4Bot
import lionzxy.bots.spam.SpamBlockBot
import lionzxy.bots.switch.SwitchBot
import lionzxy.bots.switch.SwitchIdInformationDAO
import lionzxy.bots.technopark.TechnoparkBot
import lionzxy.bots.technopark.api.TechnoparkUserDAO
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
        botsApi.registerBot(TechnoparkBot())
        return
    }
    botsApi.registerBot(SwitchBot())
    botsApi.registerBot(SpamBlockBot())
    //botsApi.registerBot(TechnoparkBot())
    //botsApi.registerBot(Main.tgIu4Bot)
    //Main.vkIu4Bot.init()
    println("All bot init!")
}

private fun connectToDB() {
    val url = Credentials.get(CredentialsEnum.SERVER_DATABASE_URL)
    val user = Credentials.get(CredentialsEnum.SERVER_DATABASE_USER)
    val password = Credentials.get(CredentialsEnum.SERVER_DATABASE_PASSWORD)
    val connectionPoolSize = 10

    val config = HikariConfig()
    config.driverClassName = "org.postgresql.Driver"
    config.jdbcUrl = url
    config.username = user
    config.password = password
    config.maximumPoolSize = connectionPoolSize

    Database.connect(HikariDataSource(config))
}


private fun initDB() {
    connectToDB()
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE // Or Connection.TRANSACTION_READ_UNCOMMITTED

    transaction {
        SchemaUtils.create(SwitchIdInformationDAO, TechnoparkUserDAO)
    }
}
