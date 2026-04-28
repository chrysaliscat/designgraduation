# 农产品销售平台 CI/CD 部署说明

本文档适用于当前仓库的 RuoYi-Vue 结构：后端为根目录 Maven 多模块项目，启动模块为 `ruoyi-admin`；前端为 Vue CLI 项目，目录为 `ruoyi-ui`。

## 1. 整体部署架构

访问链路：

```text
用户 -> http://服务器IP:自定义端口 -> OpenResty 静态网站 -> Vue -> /api 反向代理 -> Spring Boot 8080 -> MySQL
```

GitHub Actions 在 `main` 分支 push 后自动执行。当前仓库远端主分支为 `master`，workflow 已同时兼容 `master`：

```text
构建后端 jar -> 构建前端 dist -> 生成 package.tar.gz -> 上传到服务器 -> 执行 /opt/agriculture/deploy.sh -> 重启 1Panel Java 容器
```

## 2. 1Panel 中需要提前准备

请先在 1Panel 中准备：

- OpenResty
- MySQL
- Java 运行环境
- 静态网站目录 `/www/wwwroot/agriculture`
- 一个运行 Spring Boot jar 的 Java 容器，容器名需要和 `/opt/agriculture/deploy.sh` 中的 `CONTAINER_NAME` 保持一致
- 腾讯云未备案时不要使用公网 `80` / `443`，建议用 `18081` 作为网站访问端口

## 3. 服务器目录

在服务器执行：

```bash
mkdir -p /opt/agriculture/backend
mkdir -p /opt/agriculture/release
mkdir -p /opt/agriculture/backup
mkdir -p /opt/agriculture/config
mkdir -p /www/wwwroot/agriculture
```

## 4. 上传部署脚本

首次部署前，将仓库中的脚本放到服务器：

```bash
cp scripts/deploy.sh /opt/agriculture/deploy.sh
chmod +x /opt/agriculture/deploy.sh
```

如果是在本地电脑上传到服务器，可使用：

```bash
scp scripts/deploy.sh your-user@your-server.example.com:/opt/agriculture/deploy.sh
ssh your-user@your-server.example.com "chmod +x /opt/agriculture/deploy.sh"
```

然后在服务器编辑：

```bash
vi /opt/agriculture/deploy.sh
```

把 `CONTAINER_NAME="agriculture-backend"` 改成 1Panel 中真实的后端 Java 容器名。

## 5. 生产配置文件

仓库根目录提供了 `application-prod.example.yml` 示例。真实配置不要提交到 GitHub，请放在服务器：

```bash
cp application-prod.example.yml /opt/agriculture/config/application-prod.yml
vi /opt/agriculture/config/application-prod.yml
```

需要修改 MySQL 数据库名、用户名、密码、Redis、上传目录、token secret 等。

## 6. GitHub Secrets

在 GitHub 仓库中进入 `Settings -> Secrets and variables -> Actions -> New repository secret`，配置：

- `SERVER_HOST`：服务器 IP 或域名，例如 `your-server.example.com`
- `SERVER_PORT`：SSH 端口，通常为 `22`
- `SERVER_USER`：SSH 用户，例如 `root`
- `SERVER_KEY`：SSH 私钥内容

不要把服务器密码、数据库密码、SSH 私钥写入仓库。

## 7. SSH 密钥生成

在本地生成部署密钥：

```bash
ssh-keygen -t ed25519 -C "deploy-agriculture"
```

把公钥追加到服务器：

```bash
ssh-copy-id -i ~/.ssh/id_ed25519.pub your-user@your-server.example.com
```

如果没有 `ssh-copy-id`，可手动把公钥内容追加到服务器的 `~/.ssh/authorized_keys`。

把私钥 `~/.ssh/id_ed25519` 的完整内容配置到 GitHub Secret `SERVER_KEY`。

## 8. 1Panel Java 运行环境

1Panel Java 应用启动命令示例：

```bash
java -jar /opt/agriculture/backend/app.jar --spring.profiles.active=prod --spring.config.additional-location=/opt/agriculture/config/application-prod.yml
```

后端端口使用 `8080`，只给 OpenResty 反向代理使用，不建议在腾讯云安全组放行公网访问。

## 9. OpenResty 配置

未备案服务器建议监听非 `80` / `443` 端口。当前服务器上 `18080` 已被 Java 后端映射占用，因此前端网站使用 `18081`：

```nginx
listen 18081;
```

Vue history 模式刷新 404 处理：

```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

接口反向代理：

```nginx
location /api/ {
    proxy_pass http://127.0.0.1:18080/api/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

如果后端接口没有 `/api` 前缀，应改成：

```nginx
location /api/ {
    proxy_pass http://127.0.0.1:18080/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

当前 RuoYi-Vue 的生产前端若仍使用默认 `/prod-api`，也需要加入：

```nginx
location /prod-api/ {
    proxy_pass http://127.0.0.1:18080/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

腾讯云安全组只需要放行：

```text
18081
```

不要放行 `80` / `443`。后端 `18080` / `8080` 也不需要对公网放行。

## 10. 前端生产环境变量

当前项目是 Vue CLI，示例文件为：

```text
ruoyi-ui/.env.production.example
```

内容：

```env
VUE_APP_BASE_API=/api
```

如果后续改成 Vite，变量名通常应改为：

```env
VITE_APP_BASE_API=/api
```

## 11. 常见错误排查

### SSH 连接失败

检查 `SERVER_HOST`、`SERVER_PORT`、`SERVER_USER`、`SERVER_KEY` 是否正确；确认服务器安全组放行 SSH 端口；确认公钥已写入 `~/.ssh/authorized_keys`。

### Maven 打包失败

当前项目 `pom.xml` 使用 Java 1.8，Actions 已配置 Java 8。若依赖下载失败，检查 Maven 仓库网络；若编译错误，先在本地执行 `mvn clean package -DskipTests`。

### npm 打包失败

当前项目是 Vue CLI，Actions 使用 `npm ci` 和 `npm run build:prod`。若依赖锁文件不一致，先本地执行 `npm install` 更新 `ruoyi-ui/package-lock.json` 后再提交。

### jar 未找到

当前 Actions 从 `ruoyi-admin/target` 查找 jar。如果修改了后端模块名，需要同步修改 `.github/workflows/deploy.yml` 中的 `BACKEND_MODULE`。

### dist 未找到

确认 `ruoyi-ui/package.json` 中存在 `build:prod`，并且构建输出目录为 `ruoyi-ui/dist`。

### Docker 容器名错误

在服务器执行 `docker ps` 查看真实容器名，并修改 `/opt/agriculture/deploy.sh` 中的 `CONTAINER_NAME`。

### 前端部署后还是旧页面

清理浏览器缓存；确认 OpenResty 网站根目录是 `/www/wwwroot/agriculture`；确认 Actions 成功上传并执行了部署脚本。

### 接口 404

检查前端 `VUE_APP_BASE_API` 是否为 `/api`；检查 OpenResty `location /api/` 是否匹配后端真实接口路径。如果后端没有 `/api` 前缀，使用 `proxy_pass http://127.0.0.1:8080/;`。

### 数据库连接失败

检查 `/opt/agriculture/config/application-prod.yml` 中数据库地址、端口、库名、用户名和密码；确认 MySQL 用户允许从 Java 容器访问；确认 1Panel MySQL 服务正在运行。
