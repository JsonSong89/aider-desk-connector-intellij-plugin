 
标题： fileClosed listener throws IllegalArgumentException: 'other' has different root + endless "Cannot send message,
socket not connected" log storm when AiderDesk server is not running

环境：
- IntelliJ IDEA 2026.1.3 (Build #IU-261.25134.95)
- JDK: 25.0.3 (JetBrains JBR OpenJDK 64-Bit Server VM)
- OS: Windows 10 (build 19045)
- Plugin: AiderDesk Connector v0.6.1 (com.hotovo.plugins.aider-desk-connector)

问题描述：

当 AiderDesk 桌面服务端（默认 http://localhost:24337）未运行时，插件会产生两类问题：

Bug A — fileClosed 监听器抛 IllegalArgumentException

执行 Close All Editors But Active（或关闭编辑器）时，在 EDT 上抛出：

 ```
   java.lang.IllegalArgumentException: 'other' has different root
       at java.base/sun.nio.fs.WindowsPath.relativize(WindowsPath.java:418)
       at java.base/sun.nio.fs.WindowsPath.relativize(WindowsPath.java:42)
       at com.intellij.platform.core.nio.fs.MultiRoutingFsPath.relativize(MultiRoutingFsPath.java:140)
       at com.intellij.platform.core.nio.fs.MultiRoutingFsPath.relativize(MultiRoutingFsPath.java:21)
       at
 com.hotovo.plugins.aiderdesk.AiderDeskConnector$startProjectConnector$fileEditorListener$1.fileClosed(AiderDeskConnect
 or.kt:256)
       at com.intellij.util.messages.impl.MessageBusImplKt.invokeMethod(MessageBusImpl.kt:831)
       ... (MessageBus -> EditorWindow.closeAllExcept -> CloseAllEditorsButActiveAction)
 ```

IDEA 直接将该异常标记为 Plugin to blame: AiderDesk Connector version: 0.6.1。
根因：AiderDeskConnector.kt:256 对被关闭文件的 Path 调用 relativize(other)，但二者不在同一文件系统根（Windows 多盘符 /
UNC / MultiRoutingFs 虚拟路径时即触发）。

Bug B — 断连后无限刷屏 + 持续 CPU 占用

服务端未启动时，每个已打开项目每隔几秒就打印一次：

 ```
   WARN #com.hotovo.plugins.aiderdesk.AiderDeskConnector - Cannot send message, socket not connected for project
 <name>. Status: DISCONNECTED
 ```

没有退避（backoff）机制，日志风暴；同时 EDT 出现长达 10 秒的写动作阻塞（Cannot execute background write action in 10
seconds），系统明显卡顿。

复现步骤：
1. 不启动 AiderDesk 服务端。
2. 在 IDEA 打开任意项目 → 日志立即出现 Attempting to connect ... DISCONNECTED。
3. 执行 Close All Editors But Active → 抛 Bug A 异常。
4. 观察日志：Bug B 的 WARN 持续刷屏。

建议修复：
- Bug A：fileClosed 里 relativize 前先校验同根（或 try/catch IllegalArgumentException），并在断连状态下跳过同步逻辑。
- Bug B：发送前检查连接状态；断连后用指数退避重连，避免空转和日志风暴。
