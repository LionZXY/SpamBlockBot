package lionzxy.storage

import java.io.File
import java.util.*

enum class CredentialsEnum {
    SPAM_BOT_TOKEN,
    USERID_ERRORREPORT,
    USERID_LOG,
    SPAM_BOT_USERNAME,
    SPAM_BOT_REMEMBER_LIMIT,
    TG_IU4_TOKEN,
    TG_IU4_NAME,
    TG_IU4_GROUP,
    VK_IU4_TOKEN,
    VK_IU4_ID,
    VK_IU4_CHATID,
    TP_BOT_TOKEN,
    TP_BOT_USERNAME,
    TP_BOT_CHAT_ID,
    TP_BOT_TPTOKEN,
    TP_BOT_WELCOME_DELAY_S,
    TP_BOT_CHAT_LOG,
    ENVIRONMENT,
    NINTENDO_NAME,
    NINTENDO_TOKEN
}

object Credentials {
    private const val CRED_PROPS_PATH = "cred.properties"
    private val properties = Properties()

    init {
        load()
    }

    public fun get(name: CredentialsEnum): String {
        return properties.getProperty(name.name)
    }

    private fun load() {
        val props = File(CRED_PROPS_PATH)
        try {
            props.inputStream().use {
                properties.load(it)
            }

        } catch (e: Exception) {
            println("Error while load bot properties (${props.absolutePath})")
            e.printStackTrace()
        }
    }
}
