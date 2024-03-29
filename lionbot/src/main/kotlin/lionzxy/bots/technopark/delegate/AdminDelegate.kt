package lionzxy.bots.technopark.delegate

import lionzxy.bots.technopark.TechnoparkBot
import lionzxy.bots.utils.IMessageDelegate
import lionzxy.bots.utils.answer
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember

class AdminDelegate(
    private val bot: TechnoparkBot,
    private val chatId: Long
) : IMessageDelegate {
    private var administrators: List<ChatMember>? = null

    override fun onMessage(msg: Message): Boolean {
        val text = msg.text ?: return false
        if (msg.chatId != chatId) {
            return false
        }

        if (text.startsWith("/admin")) {
            invalidateCache()
            bot.execute(DeleteMessage.builder().chatId(msg.chatId.toString()).messageId(msg.messageId).build())
            val adminText = "@" + administrators?.filter { !it.user.isBot }?.joinToString(" @") { it.user.userName }
            bot.execute(
                SendMessage.builder().chatId(msg.chatId.toString())
                    .text(adminText)
                    .disableWebPagePreview(true)
                    .build()
            )
            return true
        }

        if (text.startsWith("/invalidate")) {
            invalidateCache()
            bot.execute(
                DeleteMessage.builder()
                    .chatId(msg.chatId.toString())
                    .messageId(msg.messageId)
                    .build()
            )
            return true
        }

        if (!msg.from.isAdmin()) {
            return false
        }

        val replyMessage = msg.replyToMessage

        if (text.startsWith("/ro")) {
            if (replyMessage == null) {
                bot.answer(msg, "Перешлите сообщение пользователя которого хотите перевести в Read-only мод")
                return true
            }

            if (!replyMessage.newChatMembers.isNullOrEmpty()) {
                replyMessage.newChatMembers.forEach {
                    bot.switchToReadOnly(msg.chatId, it)
                    bot.answer(msg, "Пользователь с ником: `${it.userName}` переведен в read-only")
                }
                return true
            }

            bot.switchToReadOnly(msg.chatId, msg.replyToMessage.from)
            bot.answer(msg, "Пользователь с ником: `${msg.replyToMessage.from.userName}` переведен в read-only")
            return true
        }

        if (text.startsWith("/rw")) {
            if (replyMessage == null) {
                bot.answer(msg, "Перешлите сообщение пользователя которого хотите перевести в Read-write мод")
                return true
            }

            if (!replyMessage.newChatMembers.isNullOrEmpty()) {
                replyMessage.newChatMembers.forEach {
                    bot.switchToReadWrite(msg.chatId, it)
                    bot.answer(msg, "Пользователь с ником: `${it.userName}` переведен в read-write")
                }
                return true
            }

            bot.switchToReadWrite(msg.chatId, replyMessage.from)
            bot.answer(msg, "Пользователь с ником: `${replyMessage.from.userName}` переведен в read-write")
            return true
        }
        return false
    }

    private fun User.isAdmin(): Boolean {
        if (administrators == null) {
            invalidateCache()
        }

        //if (administrators?.find { it.user.id == this.id && (it ?: false || it.status == "creator") } == null) {
        return false
        //}
        //return true
    }

    private fun invalidateCache() {
        administrators = bot.execute(GetChatAdministrators.builder().chatId(chatId.toString()).build())
        println("Admin cache invalidate")
    }
}
