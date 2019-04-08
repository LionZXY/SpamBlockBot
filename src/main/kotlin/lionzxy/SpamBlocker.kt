package lionzxy

import lionzxy.SecureConfig.USERID_ERRORREPORT
import lionzxy.SecureConfig.USERID_LOG
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

val NICKNAME_REGEXP = "(^|\\s)/[a-zA-Z@\\d_]{1,255}|(^|\\s|\\()@[a-zA-Z\\d_]{1,32}|(^|\\s|\\()#[\\w.]+|(^|\\s)\\$[A-Z]{3,8}([ ,.]|$)".toRegex() //From https://github.com/DrKLO/Telegram/blob/e397bd9afdfd9315bf099f78a903f8754d297d7a/TMessagesProj/src/main/java/org/telegram/messenger/MessageObject.java#L2837
val DENIED_NICKNAME = listOf("hoimsufa235", "kladov_oper", "klds_bot24", "restorantumen",
        "restoransamara", "restoranoren", "tribogabot", "lab05bot", "st_smr", "restorantlt", "BitCapitan",
        "durman001_bot", "NEWSinfinityshop_biz", "nurtime1", "udnewbot", "prtekb", "prtufablag",
        "prtorenorsk", "is_op", "oper_iso24", "pharmstd_chat",
        "bgd_pharmstd", "msk_pharmstd", "krk_pharmstd", "rzn_pharmstd").map { it.toLowerCase() }.toHashSet()

object SpamBlocker {
    public fun processUpd(upd: Update?, bot: SpamBlockBot) {
        if (upd == null) {
            return
        }

        var isSpam = false

        if (upd.message.hasText() && containsSpam(upd.message.text)) {
            isSpam = true
        }

        if (upd.message.caption != null && containsSpam(upd.message.caption)) {
            isSpam = true
        }

        if (upd.message.captionEntities != null) {
            var spamCaption = upd.message.captionEntities.find { containsSpam(it?.text) || containsSpam(it?.url) }
            if (spamCaption != null) {
                isSpam = true
            }
        }

        if (!isSpam) {
            return
        }

        val deleteMessageMethod = DeleteMessage().setChatId(upd.message.chatId).setMessageId(upd.message.messageId)
        val notifyDeleteMessage = SendMessage().setChatId(USERID_LOG).setText("Удалено сообщение: \n\n ```${upd.message}```").enableMarkdown(true)

        try {
            bot.execute(deleteMessageMethod)
            println("Удалено сообщение: ${upd.message}")
            bot.execute(notifyDeleteMessage)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
            val notifyErrorMessage = SendMessage().setChatId(USERID_ERRORREPORT).setText("Ошибка удаления сообщения: ${e.localizedMessage} в чате c id ${upd.message.chatId}: ${upd.message}")
            bot.execute(notifyErrorMessage)
        }
    }

    private fun containsSpam(text: String?): Boolean {
        return text != null && (text.contains("t.me/") || text.contains("t.cn/")
                || containsHydraSpam(text))
    }

    private fun containsHydraSpam(text: String): Boolean {
        return NICKNAME_REGEXP.findAll(text)
                .toList()
                .map { it.value }
                .map { it.substring(it.indexOf('@') + 1).toLowerCase() }
                .find { DENIED_NICKNAME.contains(it) } != null
    }
}