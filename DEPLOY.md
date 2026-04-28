# ☁️ 云端部署指南 (Cloud Deployment Guide)

您希望老师可以通过 **网页链接** 直接访问您的项目。这里有两种主流方案：

## 方案 A：云服务器部署 (推荐 - 专业)
**适合**：长期运行、稳定、速度快。需要购买一台服务器 (VPS)。

### 1. 准备工作
-   **购买服务器**: 阿里云/腾讯云 ECS (推荐 CentOS 7.x)。
-   **安装 Docker**: 在服务器执行 `yum install -y docker` 和 `docker-compose`。
-   **导出数据**: 在您本地 Navicat 中，右键数据库 -> 转储 SQL 文件 -> 保存为 `init.sql`。
-   **上传代码**: 将整个 `RuoYi-Vue` 文件夹上传到服务器（或使用 Git Clone）。

### 2. 准备数据库
将刚才导出的 `init.sql` 放入项目目录下的 `docker/mysql/init/` 文件夹中 (如果没有请新建此文件夹)。
```bash
mkdir -p docker/mysql/init
cp init.sql docker/mysql/init/
```

### 3. 一键启动
在服务器的 `RuoYi-Vue` 根目录下执行：
```bash
# 这一步会自动编译前端和后端，并启动数据库
docker-compose up -d --build
```

### 4. 访问
打开浏览器输入: `http://您的服务器公网IP` 即可。

---

## 方案 B：本地内网穿透 (省钱 - 临时)
**适合**：临时演示，不需要买服务器。电脑必须开着。

### 1. 下载工具
可以使用 `cpolar` 或 `ngrok` (以 cpolar 为例)。

### 2. 启动本地项目
确保您本地的 `localhost:80` (Nginx) 或 `localhost:8080` (后端) 正在运行。
*如果已经在本地开发环境跑起来了，直接下一步。*

### 3. 创建隧道
安装 cpolar 后执行：
```bash
cpolar http 80
```
它会生成一个类似 `http://xyz.cpolar.io` 的公网链接。

### 4. 发送链接
把这个链接发给老师即可。

---

## 🐳 Docker 配置说明
为了方便您使用方案 A，我已经为您生成了全套 Docker 配置文件：
1.  `docker-compose.yml`: 一键编排所有服务。
2.  `docker/backend/Dockerfile`: 后端打包配置。
3.  `docker/frontend/Dockerfile`: 前端编译 + Nginx 配置。
4.  `docker/frontend/nginx.conf`: 反向代理规则。

## 🖥️ 服务器规格建议 (Server Specs)

运行本全栈项目 (Java + Vue + MySQL + Redis) 的建议配置：

| 配置项 | 最低配 (勉强运行) | 推荐配 (流畅演示) | 说明 |
| :--- | :--- | :--- | :--- |
| **CPU** | 1核 (1 vCPU) | **2核 (2 vCPU)** | Java 启动和编译比较吃 CPU，1核会很慢。 |
| **内存 (RAM)** | 2GB | **4GB** | Spring Boot + MySQL + Redis 组合至少需要 2.5GB 才能稳定，2GB 容易 OOM (内存溢出) 导致服务挂掉。 |
| **带宽** | 1Mbps | **3Mbps+** | 1Mbps 下载图片会很慢，演示体验不好。尽量选 3M 以上。 |
| **系统** | CentOS 7.6 / Ubuntu 20.04 | CentOS 7.9 | 建议使用 Linux 系统，Docker 支持最好。 |

**💡 省钱小贴士**：
- 如果您是学生，阿里云/腾讯云通常有 "学生机" 或 "轻量应用服务器" (Lighthouse)，大约 ¥30-¥50/月 就能买到 [2核 4G] 的配置，非常划算。
- 尽量选择 "轻量应用服务器"，比标准 ECS 便宜很多，性能足够演示用。

## 🌍 海外免备案 VPS 推荐 (Overseas Options)

如果您希望**无需备案**且价格便宜，推荐以下国外服务商 (支持支付宝/微信支付)：

| 服务商 | 推荐配置 | 价格估算 | 优点 | 缺点 |
| :--- | :--- | :--- | :--- | :--- |
| **RackNerd** | 2核 3G/4G | **~$30/年** | **极其便宜** (约 ¥220/年)，支持支付宝 | 晚高峰偶尔网络波动，适合演示用 |
| **CloudCone** | 2核 4G | **~$35/年** | 价格便宜，支持支付宝，按小时计费可随时删机 | 洛杉矶机房，国内访问延迟稍高 |
| **Vultr** | 2核 4G | $20/月 | 性能极其稳定，按小时计费 (演示完就删只需几块钱) | 单价较贵，适合短期突击演示 |
| **DigitalOcean** | 2核 4G | $24/月 | 全球大厂，界面友好，文档丰富 | 价格较贵 |

**💡 购买建议**：
1.  **省钱首选**: RackNerd 或 CloudCone 的**年付特惠套餐** (搜索 "RackNerd New Year Special" 等关键词)。
2.  **临时突击**: 注册 Vultr 或 DigitalOcean，**按小时付费**。演示前开机，演示完直接删除 (Destroy)，总花费可能不到 ¥10 元。
3.  **支付方式**: RackNerd 和 CloudCone 大多支持 **Alipay (支付宝)**，非常方便。

## 🆓 "白嫖" 方案 (Zero Cost Options)

如果您完全不想花钱，可以尝试以下终极方案：

### 1. 甲骨文云永久免费 (Oracle Cloud Always Free) - **推荐尝试**
-   **配置**: 4核 (ARM) 24G 内存。**完全免费，永久。**
-   **难点**: 注册非常困难 (俗称"玄学注册")，必须有**外币信用卡 (Visa/MasterCard)**，且国内 IP 注册成功率极低。
-   **建议**: 如果您有外币卡，值得一试。这是目前全网唯一的"真·免费"高性能服务器。

### 2. GitHub 学生包 (GitHub Student Developer Pack)
-   **福利**: 验证学生身份后，赠送 **DigitalOcean $200 美金** 额度 (有效期1年) + **Microsoft Azure $100 额度**。
-   **操作**: 去 GitHub Education 官网申请，需要验证 Edu 邮箱或学生证。
-   **价值**: 相当于免费使用 1 年的高配服务器。

### 3. 本地内网穿透 (Local Tunnel) - **最稳妥的保底**
-   **原理**: 您的电脑就是服务器。使用 `cpolar` 把您本地的 `localhost:80` 映射到公网。
-   **优点**: **完全免费**，无需重新部署，数据直接在本地改。
-   **操作**:
    1.  去 cpolar 官网注册账号。
    2.  下载客户端并登录。
    3.  执行 `cpolar http 80`。
    4.  复制生成的链接发给老师。
-   **注意**: 演示期间，**您的电脑 absolute 不能关机 / 不能休眠**。
