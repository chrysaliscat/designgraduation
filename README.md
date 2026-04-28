# 🌾 农产品销售管理系统 (AgriShop)

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://gitee.com/y_project/RuoYi-Vue/blob/master/LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.5.15-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue.js-2.6.12-blue.svg)](https://vuejs.org/)

## 📖 项目简介

本项目是一款专为**毕业设计**打造的**农产品销售管理系统**。基于流行的 [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue) 前后端分离架构进行二次开发，深度集成了农产品垂直电商的业务逻辑，包括多商户商品管理、库存预警、订单流转与静态数据可视化等核心功能。

系统由 **Web 后台管理端** 和 **后端 API 服务** 两部分组成，旨在提供一套完整的数字化农产品流通解决方案。

---

## 🛠️ 技术栈清单

项目采用了当前主流的开发技术，确保了系统的稳定性和可扩展性：

- **服务端**：Spring Boot 2.5.15, Spring Security, MyBatis, JWT, Druid, Fastjson2.
- **前端 (Web)**：Vue.js 2.6.12, Element UI, ECharts.
- **终端形态**：仅保留 PC 端 Web 界面。
- **存储与支撑**：MySQL 8.0.44, Redis 5.0.14.

> [!TIP]
> 详细的技术清单与本地环境版本，请参阅 [技术栈.md](./技术栈.md)。

---

## ✨ 核心功能模块

### 1. 后台管理系统 (Admin Panel)
- **商品运营**：支持农产品的分类管理、属性设置（SKU）、多图上传及详情编辑。
- **订单中心**：实现订单状态实时跟踪、发货管理、售后处理及财务流水对账。
- **用户体系**：包含 RBAC 权限管理（管理员、商户、普通用户）。
- **数据监控**：通过 ECharts 实现销售额、订单分布等数据的可视化分析。

---

## 🚀 快速启动

### ⚡ 基础准备
1. 准备本地 MySQL (8.0+) 环境，创建名为 `ry-vue` 的数据库。
2. 导入数据库初始化脚本：`sql/ry_vue_complete_backup_2026.sql`。
3. 确保本地 Redis (5.0+) 已启动并运行在 `6379` 端口。

### 🍃 后端启动 (Dev Mode)
建议在 PowerShell 中使用以下命令以开启**热重启**支持：
```powershell
cd ruoyi-admin
cmd /c "mvn spring-boot:run -Dspring-boot.run.profiles=druid"
```

### 🎨 前端启动 (Web)
```bash
cd ruoyi-ui
npm install
npm run dev
```

> [!IMPORTANT]
> 关于开发中的环境优化与常见问题解决，请务必阅读 [DEVELOPMENT_NOTES.md](./DEVELOPMENT_NOTES.md)。

---

## 📂 项目结构
```text
RuoYi-Vue
├── ruoyi-admin          // 后端 Web 服务入口
├── ruoyi-common         // 通用工具模块
├── ruoyi-framework      // 核心框架配置
├── ruoyi-generator      // 代码生成工具
├── ruoyi-quartz         // 定时任务处理
├── ruoyi-system         // 权限及业务系统模块
├── ruoyi-ui             // 前端 Vue 项目目录
└── sql                  // 数据库初始化脚本
```

---

## 🤝 致谢
感谢 [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue) 开源项目的底层技术支持。

---

**由 AI 助手在 2026 开发周期协助构建与维护。**
