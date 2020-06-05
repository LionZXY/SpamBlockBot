package lionzxy.bots.technopark.delegate

import lionzxy.bots.technopark.TechnoparkBot
import lionzxy.bots.technopark.api.TPRequestHelper
import lionzxy.bots.utils.IMessageDelegate
import lionzxy.bots.utils.answer
import lionzxy.storage.Credentials
import lionzxy.storage.CredentialsEnum
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import java.util.concurrent.TimeUnit

private val WELCOME_MESSAGE_DELAY_MS = TimeUnit.MILLISECONDS.convert(
        Credentials.get(CredentialsEnum.TP_BOT_WELCOME_DELAY_S).toLong(),
        TimeUnit.SECONDS)

class HelloMessageDelegate(val bot: TechnoparkBot,
                           val chatId: Long,
                           val technoparkRequestHelper: TPRequestHelper) : IMessageDelegate {
    private var lastWelcomeMessageTimestamp = 0L
    private var lastMessageFromBot = false

    override fun onMessage(msg: Message): Boolean {
        if (msg.chatId == chatId) {
            if (msg.newChatMembers.isNullOrEmpty()) {
                lastMessageFromBot = false
            }
            return false
        }
        return processOtherChat(msg)
    }

    private fun processOtherChat(msg: Message): Boolean {
        val text = msg.text ?: return false

        val errorText = if (text.startsWith("/start")) {
            "Привет! \uD83D\uDD96 В чате @tpmailru можно писать только пользователям, у которых указан ник Telegram на портале. Для всех остальных устанавливается режим read-only.\n" +
                    "\n" +
                    "Добавить ник в профиль можно по [ссылке](https://park.mail.ru/settings/additional_info/). После добавления ника напиши мне в личные сообщения команду /invalidate, чтобы я предоставил тебе возможность писать в чат.\n" +
                    "\n" +
                    "Если у тебя есть вопросы, их можно задать администраторам чата: [Мише](t.me/StealthTech) или [Никите](t.me/LionZXY)"
        } else if (text.startsWith("/invalidate")) {
            "Хм... Я до сих пор не вижу твоего ника на портале. Проверь, пожалуйста. Не забудь что он должен совпадать с ником в телеграме"
        } else {
            return false
        }

        if (msg.from.userName.isNullOrEmpty()) {
            bot.log("Пользователь ${msg.from.id} БЕЗ НИКА попытался валидироваться в ЛС и не получилось")
            bot.answer(msg, errorText)
            return true
        }

        technoparkRequestHelper.requestCheckNickname(msg.from) {
            if (it != null) {
                bot.log("Пользователь @${msg.from.userName} успешно валидировался в ЛС")
                bot.switchToReadWrite(chatId, msg.from)
                bot.answer(msg, "Ура! Теперь ты можешь писать")
            } else {
                bot.log("Пользователь @${msg.from.userName} попытался валидироваться в ЛС и не получилось")
                bot.answer(msg, errorText)
            }
        }
        return true
    }

    public fun writeMainWelcomeMessage() {
        val diff = (lastWelcomeMessageTimestamp + WELCOME_MESSAGE_DELAY_MS) - System.currentTimeMillis()
        if (diff > 0 || lastMessageFromBot) {
            return
        }

        val message = SendMessage()
        message.setChatId(chatId)
        message.enableMarkdown(true)
        message.disableWebPagePreview()
        message.setText("Привет! Чтобы писать в чат, [подтверди ник](https://t.me/${bot.botUsername}?start).")
        bot.execute(message)
        lastMessageFromBot = true

        lastWelcomeMessageTimestamp = System.currentTimeMillis()
    }
}
