# 🎓 农产品销售平台 - 开发与启动手册

本手册汇集了毕设开发阶段的关键配置与注意事项，确保您在本地能够稳定、高效地运行系统。

---

## 🚀 启动指令 (最佳实践)

为了支持**代码热重载 (DevTools)**，建议在开发阶段使用 Maven 启动，而不是直接运行 Jar 包。

### 1. 后端 (Spring Boot)
在终端（尤其是 PowerShell）中，请使用以下格式启动以避免路径解析错误：
```powershell
# 进入子模块目录
cd ruoyi-admin
# 使用 CMD 兼容模式启动，激活 druid 数据源
cmd /c "mvn spring-boot:run -Dspring-boot.run.profiles=druid"
```
> [!TIP]
> **热重启原理**：修改代码后，AI 助手会执行 `mvn compile`，项目将在 3-5 秒内自动完成应用上下文重载。

### 2. 前端 (Vue.js)
```powershell
cd ruoyi-ui
npm run dev
```
> [!NOTE]
> 默认端口为 `80`。若被占用，将自动回落到 **`81`**。

---

## 🛠️ 环境依赖 (核心参数)

| 组件 | 配置要求 | 本地参数 |
| :--- | :--- | :--- |
| **Java** | JDK 1.8 (必需) | `java version "1.8.0_481"` |
| **Maven**| 3.x | `Apache Maven 3.9.12` |
| **MySQL**| 5.7 / 8.0 | `localhost:3306`, 库名: `ry-vue` |
| **Redis**| 3.0+ | `localhost:6379`, 无密码 |

---

## 🔐 账户与权限
- **管理端入口**：`http://localhost:81`
- **默认管理员**：请按本地初始化数据或服务器实际配置为准，公开仓库不要记录真实账号密码。
- **后台接口文档**：`http://localhost:8080/swagger-ui/index.html`

---

## ⚠️ 常见问题排查 (Troubleshooting)

### 1. 启动报错 `LifecyclePhaseNotFoundException`
*   **原因**：PowerShell 对冒号 `:` 解析不当，或未进入子模块目录。
*   **对策**：使用上述 `cmd /c` 包装指令，并在 `ruoyi-admin` 目录下执行。

### 2. 数据库连接异常
*   **检查**：MySQL 密码在 `ruoyi-admin/src/main/resources/application-druid.yml` 中通过环境变量配置，公开仓库不要写入真实密码。

### 3. Redis 未启动
*   **现象**：登录时验证码无法生成或控制台报错。
*   **对策**：确保 `redis-server` 进程已开启。

---

## 👨‍💻 AI 协同开发指南
- **功能开发**：直接告诉 AI “添加XX模块”，AI 会利用 Maven 热重启环境快速部署。
- **UI 调整**：直接描述样式修改需求，网页会实时刷新（Hot Reload）。
--最后同步时间:
2026-04-02
04:35--
