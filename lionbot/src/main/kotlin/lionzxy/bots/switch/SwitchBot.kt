package lionzxy.bots.switch

import lionzxy.storage.Credentials
import lionzxy.storage.CredentialsEnum
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import kotlin.math.abs
import kotlin.random.Random

class SwitchBot : TelegramLongPollingBot() {
    private val switchIdRegex = "SW-\\d{4}-\\d{4}-\\d{4}".toRegex()
    private val requestIdRegex = "\\/swApply(\\d+)@$botUsername".toRegex()
    private val requestMap = HashMap<Int, SwitchIdRowData>()

    override fun getBotUsername() = Credentials.get(CredentialsEnum.NINTENDO_NAME)

    override fun getBotToken() = Credentials.get(CredentialsEnum.NINTENDO_TOKEN)

    override fun onUpdateReceived(update: Update?) {
        if (update == null) {
            return
        }

        val msg = update.message ?: update.editedMessage ?: return
        val text = msg.text ?: return

        if (findSW(msg, text)) {
            return
        }
        if (findSwApply(msg, text)) {
            return
        }
        getSw(msg, text)
    }

    private fun getSw(msg: Message, text: String) {
        val toSend = SendMessage()
        toSend.replyToMessageId = msg.messageId
        toSend.chatId = msg.chatId.toString()

        var toOutput: String? = null
        if (text.startsWith("/getAll", true)) {
            transaction {
                toOutput = SwitchIdInformation.all().map {
                    val userNickName = if (it.nickname.isNullOrBlank().not()) " (@${it.nickname})" else ""
                    "${it.firstName} ${it.lastName} $userNickName: ${it.sw}"
                }.joinToString("\n")
            }
        } else if (text.startsWith("/get", true)) {
            val userGet = msg.replyToMessage
            if (userGet == null) {
                toOutput = "Не найдено reply-сообщение"
            } else {
                transaction {
                    toOutput = SwitchIdInformation.findById(userGet.from.id)?.sw ?: "Не найдено"
                }
            }
        } else if (msg.chatId == msg.from.id.toLong()) {
            val userGet = msg.forwardFrom
            if (userGet == null) {
                toOutput = "Перешли мне любое сообщение"
            } else {
                transaction {
                    toOutput = SwitchIdInformation.findById(userGet.id)?.sw ?: "Не найдено"
                }
            }
        }
        if (toOutput == null) {
            return
        }
        toSend.text = toOutput
        sendApiMethod(toSend)
    }

    private fun findSW(msg: Message, text: String): Boolean {
        val matchResult = switchIdRegex.find(text) ?: return false
        val sw = matchResult.value
        val user = msg.forwardFrom ?: msg.from ?: return false
        val requestId = generateFreeRandomId()
        val info = SwitchIdRowData(user.id, user.firstName, user.lastName, user.userName, sw)
        requestMap[requestId] = info

        val toSend = SendMessage()
        toSend.text = "Если вы хотите добавить `$sw` для ${user.userName
                ?: user.firstName}, введите /swApply$requestId@${botUsername.replace("_", "\\_")}"
        toSend.replyToMessageId = msg.messageId
        toSend.chatId = msg.chatId.toString()
        toSend.enableMarkdown(true)
        sendApiMethod(toSend)
        return true
    }

    private fun findSwApply(msg: Message, text: String): Boolean {
        val toSend = SendMessage()
        toSend.replyToMessageId = msg.messageId
        toSend.chatId = msg.chatId.toString()
        val matchResult = requestIdRegex.find(text) ?: return false
        val id = matchResult.groups.find { it?.value?.toIntOrNull() != null }?.value?.toInt() ?: return false
        if (!requestMap.containsKey(id)) {
            toSend.text = "Не нашел запрос с id $id"
            sendApiMethod(toSend)
            return true
        }
        val user = requestMap[id]!!
        transaction {
            SwitchIdInformationDAO.deleteWhere { SwitchIdInformationDAO.id eq user.id }
            SwitchIdInformationDAO.insert {
                it[SwitchIdInformationDAO.id] = EntityID(user.id, SwitchIdInformationDAO)
                it[firstName] = user.firstName
                it[lastName] = user.lastName
                it[nickname] = user.nickname
                it[sw] = user.sw
            }
        }

        toSend.text = "Ок"
        sendApiMethod(toSend)
        return true
    }

    private fun generateFreeRandomId(): Int {
        var id = abs(Random.nextInt())
        while (requestMap.containsKey(id)) {
            id = abs(Random.nextInt())
        }
        return id
    }
}
