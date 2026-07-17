# Cook

Cook 是一个基于 Kotlin Multiplatform (KMP) + Compose Multiplatform 构建的跨平台 AI 对话助手应用，使用 [Koog Agents](https://docs.koog.ai) 框架驱动智能对话体验。

## Features

- **跨平台** — 一套代码同时运行在 Android、iOS 和 Desktop (JVM) 上
- **AI 对话** — 基于 Koog Agents 框架，支持流式响应
- **多会话管理** — 侧边栏会话列表，支持切换与新建会话
- **自适应布局** — 根据窗口/屏幕尺寸自动切换紧凑布局（抽屉式）和展开布局（侧栏 + 主区域）
- **模型切换** — 在输入栏中快速切换 AI 模型
- **现代化 UI** — 基于 Material3 + Compose Multiplatform

## Tech Stack

| 层级       | 技术选型                                     |
|----------|------------------------------------------|
| 语言       | Kotlin 2.3.10                            |
| UI 框架    | Compose Multiplatform 1.11.1 + Material3 |
| AI 引擎    | Koog Agents 1.0.0                        |
| 导航       | Navigation Compose (JetBrains) 2.9.2     |
| HTTP 客户端 | Ktor 3.3.3                               |
| 依赖注入     | Koin 4.2.2                               |
| 图片加载     | Coil 3.5.0                               |
| 序列化      | kotlinx.serialization 1.8.1              |
| 构建系统     | Gradle + Kotlin DSL                      |

## Project Structure

```
Cook/
├── androidApp/                      # Android 应用入口
│   └── src/main/kotlin/
│       └── MainActivity.kt
├── desktopApp/                      # Desktop (JVM) 应用入口
│   └── src/main/kotlin/
│       └── main.kt
├── iosApp/                          # iOS 应用入口 (Xcode project)
│   └── iosApp/
│       ├── ContentView.swift
│       ├── iOSApp.swift
│       └── Info.plist
├── shared/                          # 跨平台共享代码
│   └── src/
│       ├── commonMain/kotlin/com/atride/cook/
│       │   ├── App.kt               # 根 Composable
│       │   ├── Greeting.kt
│       │   ├── GreetingUtil.kt
│       │   ├── Platform.kt          # expect/actual 平台接口
│       │   ├── data/                # 数据层（Repository、HTTP 客户端）
│       │   ├── di/                  # 依赖注入 (Koin Module)
│       │   ├── model/               # 数据模型
│       │   ├── navigation/          # 路由定义
│       │   └── ui/                  # UI 组件与页面
│       ├── androidMain/             # Android 平台实现
│       ├── iosMain/                 # iOS 平台实现
│       ├── jvmMain/                 # JVM 平台实现
│       ├── commonTest/              # 公共测试
│       ├── androidHostTest/
│       ├── iosTest/
│       └── jvmTest/
├── gradle/
│   ├── libs.versions.toml           # 版本目录
│   └── wrapper/
├── build.gradle.kts                 # 根构建配置
├── settings.gradle.kts              # 项目设置
└── gradle.properties                # Gradle 属性
```

## Getting Started

### Prerequisites

- JDK 17+
- Android Studio（推荐最新稳定版）
- Xcode（仅 iOS 开发需要）
- Gradle（可使用项目自带的 Gradle Wrapper）

### Build & Run

**Desktop (JVM)**

```bash
# 标准运行
./gradlew :desktopApp:run

# 热重载模式
./gradlew :desktopApp:hotRun --auto
```

**Android**

```bash
./gradlew :androidApp:assembleDebug
```

或在 Android Studio 中直接选择 `androidApp` 运行配置。

**iOS**

在 Xcode 中打开 `iosApp/` 目录，选择合适的 Simulator 运行。

### Run Tests

```bash
# Desktop
./gradlew :shared:jvmTest

# Android (Host 测试)
./gradlew :shared:testAndroidHostTest

# iOS
./gradlew :shared:iosSimulatorArm64Test
```

## Contributing

欢迎贡献代码！请确保遵循以下准则：

1. Fork 本仓库并创建你的特性分支
2. 提交前确保测试通过
3. 提交 Pull Request

## License

本项目基于 [LICENSE.txt](./LICENSE.txt) 中声明的许可协议进行分发。
