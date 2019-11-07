package lionzxy.bots.spam

import lionzxy.storage.Credentials
import lionzxy.storage.CredentialsEnum
import lionzxy.storage.ListWithLimit
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

val NICKNAME_REGEXP = "(^|\\s)/[a-zA-Z@\\d_]{1,255}|(^|\\s|\\()@[a-zA-Z\\d_]{1,32}|(^|\\s|\\()#[\\w.]+|(^|\\s)\\$[A-Z]{3,8}([ ,.]|$)".toRegex() //From https://github.com/DrKLO/Telegram/blob/e397bd9afdfd9315bf099f78a903f8754d297d7a/TMessagesProj/src/main/java/org/telegram/messenger/MessageObject.java#L2837
val DENIED_NICKNAME = listOf("hoimsufa235", "kladov_oper", "klds_bot24", "restorantumen",
        "restoransamara", "restoranoren", "tribogabot", "lab05bot", "st_smr", "restorantlt", "BitCapitan",
        "durman001_bot", "NEWSinfinity","nshop_biz", "nurtime1", "udnewbot", "prtekb", "prtufablag",
        "prtorenorsk", "is_op", "oper_iso24", "pharmstd_chat",
        "bgd_pharmstd", "msk_pharmstd", "krk_pharmstd", "rzn_pharmstd", "MDKLAD3", "Fiksik86", "cannabioz_cosmos",
        "BestModerator", "qymyz07", "Oraku1", "dayhydra", "Hydrar4ever", "hydraday", "insideman_insider_bot").map { it.toLowerCase() }.toHashSet()

val DENIED_URL = listOf("bit.ly", "t.me", "t.cn", "tinyurl.com", "autofleet.bz", "xinvst.com", "ton-gram.info",
        "cutt.ly", "invst.xyz", "invest-777.site", "pro-invest.website", "insideman.tech")

object SpamBlocker {
    val chatMap = HashMap<String, ListWithLimit<Message>>()

    public fun processUpd(upd: Update?, bot: SpamBlockBot) {
        if (upd == null) {
            return
        }

        if (upd.message != null) {
            processMessage(upd.message, bot)
        }

        if (upd.editedMessage != null) {
            processMessage(upd.editedMessage, bot)
        }
    }

    private fun processMessage(msg: Message, bot: SpamBlockBot) {
        var isSpam = false

        if (msg.hasText() && containsSpam(msg.text)) {
            isSpam = true
        }

        if (msg.caption != null && containsSpam(msg.caption)) {
            isSpam = true
        }

        if (msg.captionEntities != null) {
            val spamCaption = msg.captionEntities.find { containsSpam(it?.text) || containsSpam(it?.url) }
            if (spamCaption != null) {
                isSpam = true
            }
        }

        if (!isSpam) {
            var updList = chatMap[msg.chatId.toString()]
            if (updList == null) {
                updList = ListWithLimit(Credentials.get(CredentialsEnum.SPAM_BOT_REMEMBER_LIMIT).toInt())
                chatMap[msg.chatId.toString()] = updList
            }
            updList.add(msg)
            return
        }

        onFindSpam(msg, bot)
    }

    private fun onFindSpam(msg: Message, bot: SpamBlockBot) {
        val notifyDeleteMessage = SendMessage().setChatId(Credentials.get(CredentialsEnum.USERID_LOG)).setText("Удалено сообщение: \n\n ```${msg}```").enableMarkdown(true)
        val updList = chatMap[msg.chatId.toString()]

        val messageToDeleted = updList?.filter { it.from.id == msg.from.id }?.map { it.messageId }?.toHashSet()
                ?: HashSet()
        messageToDeleted.add(msg.messageId)

        try {
            messageToDeleted.forEach {
                deleteMessage(msg.chatId, it, bot)
            }

            bot.execute(notifyDeleteMessage)
        } catch (e: Exception) {
            e.printStackTrace()
            val notifyErrorMessage = SendMessage().setChatId(Credentials.get(CredentialsEnum.USERID_ERRORREPORT)).setText("Ошибка удаления сообщения (${e.message}) в чате c id ${msg.chatId}: $msg")
            bot.execute(notifyErrorMessage)
        }
    }

    private fun deleteMessage(chatId: Long, messageId: Int, bot: SpamBlockBot) {
        val deleteMessageMethod = DeleteMessage().setChatId(chatId).setMessageId(messageId)

        try {
            bot.execute(deleteMessageMethod)
            println("Удалено сообщение: $messageId в $chatId")
        } catch (e: TelegramApiException) {
            e.printStackTrace()
            val notifyErrorMessage = SendMessage().setChatId(Credentials.get(CredentialsEnum.USERID_ERRORREPORT)).setText("Ошибка удаления сообщения: ${e.localizedMessage} в чате c id $chatId: $messageId")
            bot.execute(notifyErrorMessage)
        }
    }

    private fun containsSpam(text: String?): Boolean {
        if (text == null) {
            return false
        }
        return DENIED_URL.find { text.contains(it, true) } != null
                || containsHydraSpam(text) || text.contains("графитчик", true)
    }

    private fun containsHydraSpam(text: String): Boolean {
        return NICKNAME_REGEXP.findAll(text)
                .toList()
                .map { it.value }
                .map { it.substring(it.indexOf('@') + 1).toLowerCase() }
                .find { DENIED_NICKNAME.contains(it) } != null
    }
}
