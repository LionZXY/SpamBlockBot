package lionzxy.bots.utils

import org.telegram.telegrambots.bots.DefaultAbsSender
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import java.io.Serializable

fun <T : Serializable?, Method : BotApiMethod<T>?> DefaultAbsSender.execute(method: Method?): T {
    return execute(method)
}

fun DefaultAbsSender.answer(msg: Message, text: String) {
    execute(SendMessage().setChatId(msg.chatId)
            .enableMarkdown(true)
            .setText(text)
            .disableWebPagePreview()
            .setReplyToMessageId(msg.messageId))
}
