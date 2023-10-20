package cn.org.xaufeca.lark

import cn.org.xaufeca.Configuration
import cn.org.xaufeca.configuration
import com.lark.oapi.Client
import com.lark.oapi.core.utils.OKHttps
import com.lark.oapi.event.EventDispatcher
import com.lark.oapi.okhttp.MediaType
import com.lark.oapi.okhttp.Request
import com.lark.oapi.okhttp.RequestBody
import com.lark.oapi.sdk.servlet.ext.ServletAdapter
import com.lark.oapi.service.contact.v3.enums.GetUserUserIdTypeEnum
import com.lark.oapi.service.contact.v3.model.GetUserReq
import com.lark.oapi.service.im.v1.ImService
import com.lark.oapi.service.im.v1.enums.CreateMessageReceiveIdTypeEnum
import com.lark.oapi.service.im.v1.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.util.concurrent.Executors

val cachedEventDispatcher: MutableMap<String, EventDispatcher> = mutableMapOf()
val larkServletAdapter = ServletAdapter()

private val executor = Executors.newSingleThreadExecutor()

fun EventDispatcher.Builder.buildEventDispatcher(botName: String): EventDispatcher {
    return this.onP2MessageReceiveV1(object : ImService.P2MessageReceiveV1Handler() {
        override fun handle(event: P2MessageReceiveV1) {
            executor.submit {
                event.handleMessageSync(botName)
            }
        }
    }).build()
}

fun P2MessageReceiveV1.handleMessageSync(botName: String) {
    val sender = event.sender
    val message = event.message

    configuration.abilities.messageSync.strategy
        .filter { it.from.receiveUsing == botName && it.from.chatId == message.chatId }
        .forEach {
            when (val bot = configuration.bots[it.to.sendUsing]) {
                is Configuration.StandardBot -> {
                    cachedClients[it.to.sendUsing]?.im()?.message()?.create(
                        CreateMessageReq.newBuilder()
                            .receiveIdType(CreateMessageReceiveIdTypeEnum.CHAT_ID)
                            .createMessageReqBody(
                                CreateMessageReqBody.newBuilder()
                                    .receiveId(it.to.chatId)
                                    .msgType(message.messageType)
                                    .content(
                                        message.withSender(
                                            cachedClients[botName],
                                            sender.senderId.userId,
                                            it.from.chatId
                                        )
                                    )
                                    .build()
                            )
                            .build()
                    )
                }

                is Configuration.ExternalBot -> {
                    OKHttps.defaultClient.newCall(
                        Request.Builder()
                            .post(
                                RequestBody.create(
                                    MediaType.parse("application/json"),
                                    Json.encodeToString(
                                        JsonObject(
                                            mapOf(
                                                "msg_type" to JsonPrimitive("post"),
                                                "content" to JsonObject(mapOf(
                                                        "post" to Json.decodeFromString<JsonObject>(
                                                            message.withSender(
                                                                cachedClients[botName],
                                                                sender.senderId.userId,
                                                                it.from.chatId
                                                            )
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    ).also { it2 -> println(it2) }
                                )
                            )
                            .url(bot.webhookUrl)
                            .build()
                    ).execute()
                }

                null -> {}
            }
        }
}

fun EventMessage.withSender(
    client: Client?,
    senderUserID: String,
    senderChatID: String,
): String {
    var contentJson = Json.decodeFromString<JsonObject>(content)
    val messageType = if (this.messageType == "image") "img" else this.messageType

    val sender = client?.contact()?.user()?.get(
        GetUserReq.newBuilder()
            .userId(senderUserID)
            .userIdType(GetUserUserIdTypeEnum.USER_ID)
            .build()
    )?.data?.user?.let { user ->
        if (user.nickname != null) "${user.nickname}(${user.name})"
        else user.name
    } ?: senderUserID

    val group = client?.im()?.chat()?.get(
        GetChatReq.newBuilder()
            .chatId(chatId)
            .build()
    )?.data?.name ?: senderChatID

    when (messageType) {
        "post" -> {
            contentJson = JsonObject(
                JsonObject(
                    mapOf(
                        "title" to JsonPrimitive("$sender 在 $group 的聊天记录"),
                        "content" to JsonArray(contentJson["content"]?.jsonArray?.toMutableList()?.also { list ->
                            contentJson["title"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }?.let {
                                list.add(0, JsonArray(listOf(
                                    JsonObject(mapOf(
                                                    "tag" to JsonPrimitive("text"),
                                                    "text" to JsonPrimitive("原标题：${it}")))))
                                )
                            }
                        }?.let { list ->
                            list.map { it.jsonArray }
                                .map { array ->
                                    JsonArray(array.toMutableList().map {
                                        if (it.jsonObject["tag"]?.jsonPrimitive?.content == "text")
                                            JsonObject(it.jsonObject.toMutableMap().apply {
                                                remove("style")
                                            })
                                        else it
                                    })
                                }
                        } ?: emptyList())
                    )
                ).let { mapOf("zh_cn" to it) }
            )
        }

        "text", "img" -> {
            contentJson = JsonObject(
                JsonObject(
                    mapOf(
                        "title" to JsonPrimitive("$sender 在 $group 的聊天记录"),
                        "content" to JsonArray(listOf(
                            JsonArray(listOf(
                                        JsonObject(contentJson.toMutableMap().apply {
                                            put("tag", JsonPrimitive(messageType))
                                        })
                                    )
                                )
                            )
                        )
                    )
                ).let { mapOf("zh_cn" to it) }
            )
        }

        else -> {
            contentJson = JsonObject(
                JsonObject(
                    mapOf(
                        "title" to JsonPrimitive("$sender 在 $group 的聊天记录"),
                        "content" to JsonArray(listOf(
                                JsonArray(listOf(
                                        JsonObject(mapOf(
                                                "tag" to JsonPrimitive("text"),
                                                "text" to JsonPrimitive("[$messageType 消息] 升级至最新版手机飞书也看不了")
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                ).let { mapOf("zh_cn" to it) }
            )
        }
    }

    var rst = Json.encodeToString(contentJson)

    mentions?.forEach {
        rst = rst.replace(it.key, "@${it.name}")
    }

    return rst
}
