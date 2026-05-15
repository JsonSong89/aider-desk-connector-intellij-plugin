# Prompt Action 变更记录

本次为 AiderDesk Connector 增加了基于选中文本的 Prompt 操作能力，并补充了连接配置能力。

## 功能摘要

- 状态栏菜单新增 `Sync Workspace`，用于主动发送一次初始化消息，同步 IDEA 当前工作区文件到 AiderDesk。
- 设置页新增 `AiderDesk URL` 配置项，默认值为 `http://localhost:24337`。
- Socket.IO 连接和 REST API 请求统一使用同一个 `AiderDesk URL`，避免分别维护 host/port。
- 设置页保留 `Username` 和 `Password`，两者都非空时 REST API 请求会添加 `Authorization: Basic <base64(username:password)>`。
- 新增 `Save Prompt` 和 `Run Prompt` 操作，菜单顺序为 `Save Prompt` 在上、`Run Prompt` 在下。
- Prompt 操作会使用当前项目 `basePath` 作为 `projectDir`，使用编辑器选中文本作为 `prompt`。
- 发送 Prompt 前会通过 `/api/project/tasks?projectDir=...` 获取任务列表：
  - 优先选择 `name` 包含 `最新` 的任务；
  - 否则按 `updatedAt` 选择最新任务；
  - 最终使用该任务 `id` 作为 `taskId`。
