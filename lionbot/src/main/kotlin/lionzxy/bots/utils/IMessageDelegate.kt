package lionzxy.bots.utils

import org.telegram.telegrambots.meta.api.objects.Message

interface IMessageDelegate {
    fun onMessage(msg: Message): Boolean
}
