package cn.org.xaufeca

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlin.io.path.Path
import kotlin.io.path.inputStream


lateinit var configuration: Configuration

@OptIn(ExperimentalSerializationApi::class)
fun loadConfiguration() {
    configuration = Path("config.json").inputStream().use { Json.decodeFromStream<Configuration>(it) }
}

@Serializable
data class Configuration(
    @SerialName("abilities")
    val abilities: Abilities,
    @SerialName("bots")
    val bots: Map<String, Bot>
) {
    @Serializable
    data class Abilities(
        @SerialName("message_sync")
        val messageSync: MessageSync
    ) {
        @Serializable
        data class MessageSync(
            @SerialName("strategy")
            val strategy: List<Strategy>
        ) {
            @Serializable
            data class Strategy(
                @SerialName("from")
                val from: From,
                @SerialName("to")
                val to: To
            ) {
                @Serializable
                data class From(
                    @SerialName("chat_id")
                    val chatId: String,
                    @SerialName("receive_using")
                    val receiveUsing: String
                )

                @Serializable
                data class To(
                    @SerialName("chat_id")
                    val chatId: String,
                    @SerialName("send_using")
                    val sendUsing: String
                )
            }
        }
    }

    @Serializable
    sealed interface Bot

    @Serializable
    data class StandardBot(
        @SerialName("app_id")
        val appId: String,
        @SerialName("app_secret")
        val appSecret: String,
        @SerialName("encrypt_key")
        val encryptKey: String? = null,
        @SerialName("verification_token")
        val verificationToken: String? = null
    ) : Bot

    @Serializable
    data class ExternalBot(
        @SerialName("webhook_url")
        val webhookUrl: String
    ) : Bot

}