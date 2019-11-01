package lionzxy.bots.iu4

import kotlinx.coroutines.runBlocking
import lionzxy.Main
import lionzxy.storage.Credentials
import lionzxy.storage.CredentialsEnum
import name.anton3.vkapi.generated.messages.methods.MessagesSend
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.*

class TGIu4Bot : TelegramLongPollingBot() {
    var previousPeerId = -1

    override fun getBotUsername() = Credentials.get(CredentialsEnum.TG_IU4_NAME)
    override fun getBotToken(): String {
        return Credentials.get(CredentialsEnum.TG_IU4_TOKEN)
    }

    override fun onUpdateReceived(update: Update?) {
        val msg = update?.message ?: return

        if (msg.chatId != Credentials.get(CredentialsEnum.TG_IU4_GROUP).toLong()) {
            return
        }
        if (msg.text.isNullOrEmpty()) {
            return
        }
        val sendMessage = MessagesSend(0, Random().nextInt())
        var text = if (previousPeerId != msg.from.id) {
            "${msg.from.firstName} ${msg.from.lastName} (${msg.from.userName})\n\n${msg.text}"
        } else {
            msg.text
        }
        sendMessage.message = text
        sendMessage.peerId = Credentials.get(CredentialsEnum.VK_IU4_CHATID).toInt()
        runBlocking {
            Main.vkIu4Bot.api.invoke(sendMessage)
        }
        previousPeerId = msg.from.id
        Main.vkIu4Bot.resetPrevMessage()
    }

    public fun resetPrevMessage() {
        previousPeerId = -1
    }
}
