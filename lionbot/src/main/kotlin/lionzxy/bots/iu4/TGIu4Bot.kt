package lionzxy.bots.iu4

import kotlinx.coroutines.runBlocking
import lionzxy.Main
import lionzxy.storage.Credentials
import lionzxy.storage.CredentialsEnum
import name.anton3.vkapi.generated.messages.methods.MessagesSend
import name.anton3.vkapi.generated.photos.methods.PhotosGetMessagesUploadServer
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.objects.File
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.PhotoSize
import org.telegram.telegrambots.meta.api.objects.Update
import java.net.URL
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
        /*if (!msg.photo.isNullOrEmpty()) {
            onPhoto()
        }*/
        onSimpleMessage(msg)
    }

    private suspend fun onPhoto(attachs: List<PhotoSize>) {
        val sendMessage = MessagesSend(0, Random().nextInt())

        val result = attachs.map { file -> GetFile().apply { fileId = file.fileId } }.map {
            sendApiMethod(it)
        }


    }

    private suspend fun uploadPhoto(tgFile: File, photoSize: PhotoSize) {
        val messageUploadServer = Main.vkIu4Bot.api.invoke(PhotosGetMessagesUploadServer())
        val bytes = URL(tgFile.getFileUrl(botToken)).openStream().use {
            it.readBytes()
        }
        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", "${tgFile.filePath}.jpg",
                        bytes.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0))
                .build()
    }

    private fun onSimpleMessage(msg: Message) {
        val sendMessage = MessagesSend(0, Random().nextInt())
        var text = if (previousPeerId != msg.from.id) {
            "${msg.from.firstName ?: ""} ${msg.from.lastName ?: ""} (${msg.from.userName})\n\n${msg.text}"
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
