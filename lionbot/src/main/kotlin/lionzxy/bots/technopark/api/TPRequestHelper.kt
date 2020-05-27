package lionzxy.bots.technopark.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import lionzxy.storage.Credentials
import lionzxy.storage.CredentialsEnum
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.transaction
import org.telegram.telegrambots.meta.api.objects.User
import java.net.URLEncoder
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

private const val RPS = 1
typealias CheckNicknameListener = (result: Boolean) -> Unit

public class TPRequestHelper : Thread() {
    private val pendingMessage: BlockingQueue<Pair<User, CheckNicknameListener>> = LinkedBlockingQueue()
    private val DELAY = TimeUnit.MILLISECONDS.convert(1, TimeUnit.SECONDS) / RPS
    private val client = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }
    private var lastTimeStamp = 0L

    override fun run() {
        var message: Pair<User, CheckNicknameListener>? = pendingMessage.take()
        while (message != null) {
            message.second.invoke(internalCheckNickname(message.first))
            message = pendingMessage.poll()
        }
    }

    private fun internalCheckNickname(user: User): Boolean {
        val diff = (lastTimeStamp + DELAY) - System.currentTimeMillis()
        if (diff > 0) {
            sleep(diff)
            return internalCheckNickname(user)
        }
        var tpUsers: List<TechnoparkUser>? = null
        runBlocking {
            try {
                tpUsers = client.get<List<TechnoparkUser>>("https://park.mail.ru/api/check_telegram/?key=${Credentials.get(CredentialsEnum.TP_BOT_TPTOKEN)}" +
                        "&nick=@${URLEncoder.encode(user.userName, "UTF-8")}")
            } catch (ignore: ClientRequestException) {

            }
        }
        lastTimeStamp = System.currentTimeMillis()
        if (tpUsers.isNullOrEmpty()) {
            return false
        }

        tpUsers?.forEach {
            addToInternalDatabase(user.id, user.userName, it)
        }

        return tpUsers?.find { it.isAccessAllowed ?: false } != null
    }

    private fun addToInternalDatabase(tgUserId: Int, nickname: String, tpUser: TechnoparkUser) {
        transaction {
            TechnoparkUserDAO.insertIgnore {
                it[TechnoparkUserDAO.id] = EntityID(tpUser.id, TechnoparkUserDAO)
                it[tgId] = tgUserId
                it[tgUsername] = nickname
                it[username] = tpUser.username
                it[profileLink] = tpUser.profileLink
                it[project] = tpUser.project
                it[isAccessAllowed] = tpUser.isAccessAllowed ?: false
            }
        }
    }

    public fun requestCheckNickname(user: User, listener: CheckNicknameListener) {
        if (pendingMessage.size > 5) {
            println("WARNING! Queue size is ${pendingMessage.size}")
        }

        pendingMessage.put(user to listener)
    }
}
