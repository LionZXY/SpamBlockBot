package lionzxy.bots.technopark

import lionzxy.bots.technopark.api.TPRequestHelper
import lionzxy.storage.Credentials
import lionzxy.storage.CredentialsEnum
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators
import org.telegram.telegrambots.meta.api.methods.groupadministration.RestrictChatMember
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.*
import java.util.concurrent.TimeUnit

private val WELCOME_MESSAGE_DELAY_MS = TimeUnit.MILLISECONDS.convert(
        Credentials.get(CredentialsEnum.TP_BOT_WELCOME_DELAY_S).toLong(),
        TimeUnit.SECONDS)

class TechnoparkBot : TelegramLongPollingBot() {
    private val TP_CHAT_ID = Credentials.get(CredentialsEnum.TP_BOT_CHAT_ID).toLong()
    private var administrators: List<ChatMember>? = null
    private val technoparkRequestHelper = TPRequestHelper().apply { start() }
    private var lastWelcomeMessageTimestamp = 0L

    override fun getBotUsername() = Credentials.get(CredentialsEnum.TP_BOT_USERNAME)
    override fun getBotToken() = Credentials.get(CredentialsEnum.TP_BOT_TOKEN)

    override fun onUpdateReceived(update: Update?) {
        val msg = update?.message ?: return
        try {
            processMessageMain(msg)
        } catch (exp: Exception) {
            exp.printStackTrace()
        }
    }

    private fun processMessageMain(msg: Message) {
        if (msg.chatId != TP_CHAT_ID) {
            processOtherChat(msg)
            return
        }
        checkAndRunAdminSection(msg)

        if (msg.newChatMembers.isNullOrEmpty()) {
            return
        }

        msg.newChatMembers.forEach {
            processMember(msg, it)
        }
    }

    private fun processMember(msg: Message, user: User) {
        switchToReadOnly(msg.chatId, user.id)
        if (user.userName.isNullOrEmpty()) {
            writeWelcomeMessage()
            return
        }
        technoparkRequestHelper.requestCheckNickname(user) {
            if (it) {
                switchToReadWrite(TP_CHAT_ID, user.id)
            } else {
                writeWelcomeMessage()
            }
        }
    }

    private fun processOtherChat(msg: Message) {
        val text = msg.text ?: return

        if (text.startsWith("/invalidate") || text.startsWith("/start")) {
            technoparkRequestHelper.requestCheckNickname(msg.from) {
                if (it) {
                    switchToReadWrite(TP_CHAT_ID, msg.from.id)
                    msg.answer("Ура! Теперь ты можешь писать")
                } else {
                    msg.answer("Хм... Я до сих пор не вижу твоего ника на портале. Проверь, пожалуйста. Не забудь что он должен совпадать с ником в телеграме")
                }
            }
        }
    }

    private fun checkAndRunAdminSection(msg: Message) {
        val text = msg.text ?: return
        if (text.startsWith("/invalidate")) {
            invalidateCache()
            execute(DeleteMessage().setChatId(msg.chatId).setMessageId(msg.messageId))
            return
        }
        if (administrators == null) {
            invalidateCache()
        }

        if (administrators?.find { it.user.id == msg.from.id && (it.canRestrictMembers ?: false || it.status == "creator") } == null) {
            return
        }
        val replyMessage = msg.replyToMessage

        if (text.startsWith("/ro")) {
            if (replyMessage == null) {
                msg.answer("Перешлите сообщение пользователя которого хотите перевести в Read-only мод")
                return
            }

            if (!replyMessage.newChatMembers.isNullOrEmpty()) {
                replyMessage.newChatMembers.forEach {
                    switchToReadOnly(msg.chatId, it.id)
                    msg.answer("Пользователь с ником: `${it.userName}` переведен в read-only")
                }
                return
            }

            switchToReadOnly(msg.chatId, msg.replyToMessage.from.id)
            msg.answer("Пользователь с ником: `${msg.replyToMessage.from.userName}` переведен в read-only")
            return
        }

        if (text.startsWith("/rw")) {
            if (replyMessage == null) {
                msg.answer("Перешлите сообщение пользователя которого хотите перевести в Read-write мод")
                return
            }

            if (!replyMessage.newChatMembers.isNullOrEmpty()) {
                replyMessage.newChatMembers.forEach {
                    switchToReadWrite(msg.chatId, it.id)
                    msg.answer("Пользователь с ником: `${it.userName}` переведен в read-write")
                }
                return
            }

            switchToReadWrite(msg.chatId, replyMessage.from.id)
            msg.answer("Пользователь с ником: `${replyMessage.from.userName}` переведен в read-write")
            return
        }
    }

    private fun invalidateCache() {
        administrators = execute(GetChatAdministrators().setChatId(TP_CHAT_ID))
        println("Admin cache invalidate")
    }

    private fun Message.answer(text: String) {
        execute(SendMessage().setChatId(this.chatId)
                .enableMarkdown(true)
                .setText(text)
                .disableWebPagePreview()
                .setReplyToMessageId(this.messageId))
    }

    private fun writeWelcomeMessage() {
        val diff = (lastWelcomeMessageTimestamp + WELCOME_MESSAGE_DELAY_MS) - System.currentTimeMillis()
        if (diff > 0) {
            return
        }

        val message = SendMessage()
        message.setChatId(TP_CHAT_ID)
        message.enableMarkdown(true)
        message.disableWebPagePreview()
        message.setText("Привет! \uD83D\uDD96 Кажется, ты не указал свой ник Telegram на портале. Поэтому пока что я переведу тебя в режим read-only.\n" +
                "\n" +
                "Добавить ник в профиль можно по [ссылке](https://park.mail.ru/settings/additional_info/). После добавления ника напиши мне в личные сообщения команду `/invalidate`, чтобы я предоставил тебе возможность писать в чат.\n" +
                "\n" +
                "Если у тебя есть вопросы, их можно задать администраторам чата: [Никите](t.me/LionZXY) или [Мише](t.me/StealthTech)")

        execute(message)

        lastWelcomeMessageTimestamp = System.currentTimeMillis()
    }

    private fun switchToReadOnly(chatId: Long, userId: Int) {
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
        restrict.userId = userId
        execute(restrict)
    }

    private fun switchToReadWrite(chatId: Long, userId: Int) {
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
        restrict.userId = userId
        execute(restrict)
    }

}
