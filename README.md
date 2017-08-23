# WechatScripts

scripts for weixin actions

Support send message to Enterprise and Service account.

Setup config.properties(Java) and Config.py(Python) first.

__WeChatPython__ - python verson

Known issues:

- __It said Subscribe account is supported, but in fact, WeChat not provide message template feature for Subscribe account.__
- Both the get tags and get taged user id API are access time limited. So it should reduce the API access time, not at every time when message sending  of a service account. A idea is to record user id in a file, very x hours refesh it\(like [how java version does](WeChatJava/src/Messager.java)\).

__WeChatJava__ - java verson

__pages__ - some static page with some features