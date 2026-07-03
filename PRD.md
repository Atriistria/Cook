# SmartCompanion (智伴) - 跨平台 AI 智能助手

这是一个基于 **Compose Multiplatform (CMP)** 和 **Kotlin Multiplatform (KMP)** 开发的跨平台 AI 聊天客户端，支持 Android、iOS 和 Desktop (Windows/macOS/Linux)。

本项目旨在作为一个全面的练手项目，深度整合 KMP 生态中最新、最主流的跨平台技术栈。

---

## 1. 产品定位与设计目标
* **定位**：个人私有 AI 助手（用户自备 API Key）。
* **数据安全**：本地优先（Local-First），聊天记录完全存储在本地数据库中。
* **多端一致**：一套代码实现多端丝滑的 UI 交互与业务逻辑。

---

## 2. 技术栈规划
本项目将深度实践并整合以下技术：

| 模块              | 技术选型                    | 说明                                |
|:----------------|:------------------------|:----------------------------------|
| **跨平台 UI**      | `Compose Multiplatform` | 共享多端 UI 视图与动画                     |
| **依赖注入**        | `Koin`                  | 统一管理多端对象的生命周期与注入                  |
| **AI Agent 框架** | `Koog`                  | 封装 LLM 调用、流式响应、对话记忆、工具调用等核心 AI 逻辑 |
| **网络通信**        | `Ktor Client`           | 底层 HTTP 通信，与 Koog 协作处理 API 请求     |
| **本地数据库**       | `Room Multiplatform`    | 存储会话与聊天历史记录 (1对多关联)               |
| **图片加载**        | `Coil 3`                | 异步加载并缓存 AI 生成的图片                  |
| **导航/路由**       | `Navigation3`           | 多平台页面跳转与生命周期管理                    |
| **配置存储**        | `Jetpack DataStore`     | 存储 API Key、模型选择、主题等偏好设置           |

---

## 3. 核心功能需求清单 (Task List)

### 3.1 会话管理 (Session Management)
- [ ] **新建会话**：点击按钮创建一个新的聊天会话。
- [ ] **会话列表**：在侧边栏（Desktop）或抽屉菜单（Mobile）中展示历史会话。
- [ ] **会话标题自动生成**：当发送第一条消息后，自动截取或请求 AI 生成会话标题。
- [ ] **会话维护**：支持对会话进行重命名和删除操作（联动级联删除本地聊天记录）。

### 3.2 聊天空间 (Chat Space)
- [ ] **打字机效果 (Streaming)**：通过 Koog Streaming API 对接 LLM 流式响应，实现字符渐进式呈现。
- [ ] **多轮对话上下文**：利用 Koog Chat Memory 自动携带会话历史，结合 History Compression 优化长对话的 Token 消耗（需支持最大 Token/条数限制）。
- [ ] **富文本渲染 (Markdown)**：
    - [ ] 支持基本的加粗、斜体、列表渲染。
    - [ ] 支持代码块（Code Block）的高亮显示。
    - [ ] 提供一键复制展示的代码块内容。
- [ ] **AI 绘图支持**：支持输入 Prompt 调用绘图模型，并使用 Coil 3 渲染生成的图片。
- [ ] **交互控制**：回答过程中支持“中止生成”，回答完毕后支持“重新生成”。

### 3.3 系统设置 (Settings)
- [ ] **API 密钥配置**：支持 API Key 的输入、保存（加密存储可选）与脱敏显示。
- [ ] **自定义端点 (Base URL)**：支持自定义 API 域名（方便接入代理、第三方中转站或本地 Ollama）。
- [ ] **模型管理**：提供快捷模型切换（如 `gpt-4o`, `deepseek-chat`），并支持手动输入自定义模型。
- [ ] **系统提示词 (System Prompt)**：允许用户自定义全局或单次会话的 AI 人设。
- [ ] **主题管理**：支持“深色模式”、“浅色模式”及“跟随系统系统主题”。

---

## 4. 平台特有适配需求 (Platform-Specific)

### 4.1 移动端 (Android & iOS)
- [ ] **键盘与布局适配 (WindowInsets)**：软键盘弹起时，聊天列表自动向上滚动，输入框不被遮挡。
- [ ] **系统安全区适配**：适配 iOS 的刘海屏、底部 Home 条。
- [ ] **滑动交互**：支持列表滑动惯性，支持下拉加载更多历史消息。

### 4.2 桌面端 (Desktop)
- [ ] **快捷键支持**：
    - [ ] `Enter` 直接发送消息。
    - [ ] `Shift + Enter` 进行换行。
- [ ] **响应式布局**：窗口变窄时自动隐藏左侧会话列表，提供折叠/展开按钮。
- [ ] **应用窗口控制**：支持自定义窗口的最小尺寸限制。

---

## 5. 项目目录结构参考
```text
shared/
  ├── src/
  │    ├── commonMain/             # 共享核心逻辑与 UI
  │    │    ├── kotlin/
│    │    │    └── com.atride.cook/
│    │    │         ├── di/           # Koin 配置
│    │    │         ├── data/         # Room 数据库, DataStore
│    │    │         ├── ai/           # Koog Agent 配置、Chat Memory、工具定义
│    │    │         ├── ui/           # Compose 界面 (Voyager 页面)
│    │    │         └── App.kt        # 应用主入口
  │    │    └── composeResources/  # 共享图片、字体、字符资源
  │    ├── androidMain/            # Android 平台特有实现 (expect/actual)
  │    └── iosMain/                # iOS 平台特有实现 (expect/actual)
androidApp/                        # Android 壳工程 (配置清单、打包、启动 Activity)
iosApp/                            # iOS 壳工程 (配置 Xcode 项目、启动入口)
desktopApp/                        # Desktop 壳工程 (配置窗口尺寸、打包格式)