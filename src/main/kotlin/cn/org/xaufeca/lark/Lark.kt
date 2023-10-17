package cn.org.xaufeca.lark

import cn.org.xaufeca.Configuration
import cn.org.xaufeca.configuration
import com.lark.oapi.Client
import com.lark.oapi.event.EventDispatcher

val cachedClients: MutableMap<String, Client> = mutableMapOf()

fun loadLark() {
    cachedClients += configuration.bots
        .filter { it.value is Configuration.StandardBot }
        .mapValues { it.value as Configuration.StandardBot }
        .mapValues { Client.newBuilder(it.value.appId, it.value.appSecret).build() }
    cachedEventDispatcher += configuration.bots
        .filter { it.value is Configuration.StandardBot }
        .mapValues { it.value as Configuration.StandardBot }
        .mapValues {
            EventDispatcher.newBuilder(it.value.verificationToken ?: "", it.value.encryptKey ?: "")
                .buildEventDispatcher(it.key)
        }
}
