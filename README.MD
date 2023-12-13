# LarkIntegrated

LarkIntegrated 是一款使用 ktor 编写的飞书集成工具，可帮助您更好的使用飞书 :)

## 功能

LarkIntegrated 仅支持一个功能：群聊消息同步

## 群聊消息同步

由于飞书的限制，LarkIntegrated 仅支持内部群与内部群的双向聊天消息同步和内部群向外部群的单向聊天消息同步。
要使用该功能，您可在应用根目录创建 `config.json`，然后按如下示例编写配置文件：

```json5
{
  "bots": {
    "message_sync_bot": { // 唯一名称
      "type": "cn.org.xaufeca.Configuration.StandardBot", // 内部群机器人
      "app_id": "XXXXXXXXXXXX",
      "app_secret": "XXXXXXXXXXXXXXX",
      "encrypt_key": "XXXXXXXXXXXXXXXXXX",
      "verification_token": "XXXXXXXXXXXXXXXXX"
    },
    "message_sync_bot_external": {
      "type": "cn.org.xaufeca.Configuration.ExternalBot", // 外部群机器人
      "webhook_url": "https://open.feishu.cn/open-apis/bot/v2/hook/XXXXXXXXXXXXXX"
    }
  },
  "abilities": {
    "message_sync": {
      "strategy": [ // 消息同步策略，可创建多个
        {
          "from": { // 从
            "chat_id": "oc_XXXXXXXXXXXXX",
            "receive_using": "message_sync_bot"
          },
          "to": { // 发送至
            "chat_id": "oc_XXXXXXXXXXXXX",
            "send_using": "message_sync_bot_external"
          }
        }
      ]
    }
  }
}
```

## License

LarkIntegrated is licensed under the [MIT License](LICENSE).