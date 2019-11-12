package lionzxy.bots.iu4

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import lionzxy.Main
import lionzxy.storage.Credentials
import lionzxy.storage.CredentialsEnum
import name.anton3.vkapi.client.GroupClient
import name.anton3.vkapi.client.ktorClientFactory
import name.anton3.vkapi.generated.messages.methods.MessagesSend
import name.anton3.vkapi.generated.messages.objects.MessageAttachment
import name.anton3.vkapi.generated.users.methods.UsersGet
import name.anton3.vkapi.methods.callback.MessageNew
import name.anton3.vkapi.methods.longpoll.groupLongPollEvents
import name.anton3.vkapi.tokens.GroupToken
import name.anton3.vkapi.vktypes.VkLang
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import java.util.*


val clientFactory = ktorClientFactory(HttpClient(OkHttp))
val groupToken = GroupToken(Credentials.get(CredentialsEnum.VK_IU4_TOKEN))

typealias VKMessage = name.anton3.vkapi.generated.messages.objects.Message

class VKIu4Bot {
    val nameCache = HashMap<Int, String>()
    var previousPeerId = -1
    lateinit var api: GroupClient

    init {
        runBlocking {
            api = clientFactory.group(groupToken)
        }
    }

    fun init() {
        GlobalScope.launch {
            val groupChannel = groupLongPollEvents(api, Integer.valueOf(Credentials.get(CredentialsEnum.VK_IU4_ID)), 8)

            groupChannel.consumeEach {
                try {
                    if (it is MessageNew) {
                        runBlocking {
                            onNewMessage(it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private suspend fun onNewMessage(message: MessageNew) {
        val msg = message.attachment

        if (msg.text.isNotEmpty() && msg.text.startsWith("/chatid")) {
            val sendMessage = MessagesSend(null, Random().nextInt(), msg.peerId)
            sendMessage.message = msg.peerId.toString()
            api.invoke(sendMessage)
            return
        }

        if (msg.peerId != Credentials.get(CredentialsEnum.VK_IU4_CHATID).toInt()) {
            println("Try access from ${msg.peerId}")
            return
        }

        resendToTelegram(msg)
    }

    private suspend fun resendToTelegram(msg: VKMessage) {
        val fromId = msg.fromId
        val tgMessage = SendMessage()
        tgMessage.chatId = Credentials.get(CredentialsEnum.TG_IU4_GROUP)
        tgMessage.disableWebPagePreview()
        var text = if (msg.fromId != previousPeerId) {
            "*${getUserName(fromId)}* ([${fromId}](https://vk.com/id${fromId}))\n\n${msg.text}"
        } else {
            msg.text
        }
        tgMessage.enableMarkdown(true)

        msg.attachments?.let { attach ->
            if (attach.isNotEmpty()) {
                text += "\nВложения: " + attach.map { it.toKey() }.joinToString(", ")
            }
        }

        tgMessage.text = text

        println("Try send: $tgMessage")

        Main.tgIu4Bot.execute(tgMessage)
        previousPeerId = fromId
        Main.tgIu4Bot.resetPrevMessage()
    }

    private suspend fun getUserName(authorId: Int): String {
        if (nameCache.containsKey(authorId)) {
            return nameCache[authorId]!!
        }

        try {
            val usersGet = UsersGet(listOf(authorId.toString()))
            usersGet.lang = VkLang.RU

            val user = api.invoke(usersGet).firstOrNull() ?: return "Unknown name"

            val fullName = "${user.firstName ?: ""} ${user.lastName ?: ""}"
            nameCache[authorId] = fullName
            return fullName
        } catch (e: Exception) {
            e.printStackTrace()
            return "Unknown name"
        }
    }

    public fun resetPrevMessage() {
        previousPeerId = -1
    }
}

fun MessageAttachment.toKey(): String {
    val type = when (type) {
        "photo" -> "картинка"
        "video" -> "видео"
        "audio" -> "музычка"
        "doc" -> "ботва"
        else -> "неизвестное"
    }
    return "_${type}_"
}
