# FishBook

一个 IntelliJ IDEA 插件，让你在 IDE 侧边栏中阅读本地电子书，获得类似 Kindle 的阅读体验。

## 功能特性

- 在 IDE 右侧工具窗口中阅读 txt 格式电子书
- 自动记录阅读进度（书签功能），下次打开可继续阅读
- 支持翻页：首页、上一页、下一页、末页、跳转指定页
- 支持左右方向键快速翻页
- 可自定义每页显示行数

## 构建

### 环境要求

- JDK 11 或更高版本
- 无需单独安装 Gradle（项目自带 Gradle Wrapper）

### 编译插件

```bash
./gradlew buildPlugin
```

构建成功后，插件包生成在 `build/distributions/FishBook-1.3.zip`。

### 其他常用命令

```bash
# 清理构建产物
./gradlew clean

# 启动一个带有插件的 IDEA 沙箱实例（用于调试）
./gradlew runIde

# 检查插件兼容性
./gradlew verifyPlugin
```

## 安装

1. 打开 IntelliJ IDEA
2. 进入 **Settings** → **Plugins**
3. 点击齿轮图标 ⚙️ → **Install Plugin from Disk...**
4. 选择 `build/distributions/FishBook-1.3.zip`
5. 重启 IDE

## 使用方法

1. **配置书籍路径**：进入 **Settings** → **Tools** → **FishBook Config**，点击「选择」按钮选择一本 txt 电子书，可选设置每页行数
2. **打开阅读窗口**：在 IDE 右侧边栏找到 **FishBook** 工具窗口并点击打开
3. **翻页操作**：
   - 点击 `<` / `>` 按钮：上一页 / 下一页
   - 点击 `|<<` / `>>|` 按钮：首页 / 末页
   - 输入页码后点击 **Jump**：跳转到指定页
   - 在阅读区域按 **←** / **→** 方向键：快速翻页

## 兼容性

- 目标平台：IntelliJ IDEA 2019.2+（build 173.0 起）
- 适用于所有基于 IntelliJ 平台的 IDE（IDEA、WebStorm、PyCharm 等）

## 致谢

原项目来自 [jogeen/FishBook](https://github.com/jogeen/FishBook)，本仓库为自定义修改版本。
