package lionzxy.bots.technopark

import lionzxy.bots.technopark.api.TPRequestHelper
import lionzxy.bots.technopark.delegate.AdminDelegate
import lionzxy.bots.technopark.delegate.HelloMessageDelegate
import lionzxy.storage.Credentials
import lionzxy.storage.CredentialsEnum
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.groupadministration.RestrictChatMember
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.ChatPermissions
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User


class TechnoparkBot : TelegramLongPollingBot() {
    private val TP_CHAT_ID = Credentials.get(CredentialsEnum.TP_BOT_CHAT_ID).toLong()
    private val technoparkRequestHelper = TPRequestHelper().apply { start() }
    private val helloMessageDelegate = HelloMessageDelegate(this, TP_CHAT_ID, technoparkRequestHelper)
    private val adminDelegate = AdminDelegate(this, TP_CHAT_ID)

    override fun getBotUsername() = Credentials.get(CredentialsEnum.TP_BOT_USERNAME)
    override fun getBotToken() = Credentials.get(CredentialsEnum.TP_BOT_TOKEN)

    override fun onUpdateReceived(update: Update?) {
        val msg = update?.message ?: update?.channelPost ?: return
        try {
            processMessageMain(msg)
        } catch (exp: Exception) {
            log("@LionZXY У меня тут пиздец какой-то: ${exp.localizedMessage}")
            exp.printStackTrace()
        }
    }

    private fun processMessageMain(msg: Message) {
        if (helloMessageDelegate.onMessage(msg)) {
            return
        }
        if (adminDelegate.onMessage(msg)) {
            return
        }

        if (msg.newChatMembers.isNullOrEmpty()) {
            return
        }

        msg.newChatMembers.forEach {
            processMember(msg, it)
        }
    }

    private fun processMember(msg: Message, user: User) {
        switchToReadOnly(msg.chatId, user)
        if (user.userName.isNullOrEmpty()) {
            log("Пользователь ${user.id} БЕЗ НИКА присоединился в режиме READ-ONLY")
            helloMessageDelegate.writeMainWelcomeMessage()
            return
        }
        technoparkRequestHelper.requestCheckNickname(user) {
            if (it != null) {
                log("Пользователь @${user.userName} присоединился и его ник есть на портале")
                switchToReadWrite(TP_CHAT_ID, user)
            } else {
                log("Пользователь @${user.userName} присоединился в режиме READ-ONLY")
                helloMessageDelegate.writeMainWelcomeMessage()
            }
        }
    }

    public fun switchToReadOnly(chatId: Long, user: User) {
        log("Пользователь ${user.id} (@${user.userName}) переведен в read-only")
        val restrict = RestrictChatMember()
        val permission = ChatPermissions()
        permission.canSendMessages = false
        permission.canAddWebPagePreviews = false
        permission.canChangeInfo = false
        permission.canSendPolls = false
        permission.canSendOtherMessages = false
        permission.canInviteUsers = false
        permission.canPinMessages = false
        restrict.permissions = permission
        restrict.chatId = chatId.toString()
        restrict.userId = user.id
        execute(restrict)
    }

    public fun switchToReadWrite(chatId: Long, user: User) {
        log("Пользователь ${user.id} (@${user.userName}) переведен в read-write")
        val restrict = RestrictChatMember()
        val permission = ChatPermissions()
        permission.canSendMessages = true
        permission.canAddWebPagePreviews = true
        permission.canChangeInfo = true
        permission.canSendPolls = true
        permission.canSendOtherMessages = true
        permission.canInviteUsers = true
        permission.canPinMessages = true
        restrict.permissions = permission
        restrict.chatId = chatId.toString()
        restrict.userId = user.id
        execute(restrict)
    }

    public fun log(text: String) {
        val chatId = Credentials.get(CredentialsEnum.TP_BOT_CHAT_LOG)
        execute(SendMessage.builder().text(text).chatId(chatId).build())
    }
}
