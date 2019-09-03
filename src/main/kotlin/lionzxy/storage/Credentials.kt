package lionzxy.storage

import java.io.File
import java.util.*

enum class CredentialsEnum {
    SPAM_BOT_TOKEN,
    USERID_ERRORREPORT,
    USERID_LOG,
    SPAM_BOT_USERNAME,
    ADMIN_MENTION_BOT_TOKEN,
    ADMIN_MENTION_BOT_USERNAME,
    SPAM_BOT_REMEMBER_LIMIT
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