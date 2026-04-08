# 体育赛事管理平台 - 快速启动指南（小白专用）

## 📋 准备工作清单

在开始之前，请确保您的电脑已安装以下软件：

| 软件名称 | 版本要求 | 下载地址 | 备注 |
|---------|---------|---------|------|
| JDK | 17 或以上 | https://adoptium.net/ | 选择 LTS 版本 |
| MySQL | 8.0 | https://dev.mysql.com/downloads/installer/ | 安装时记住root密码 |
| IntelliJ IDEA | Community版（免费） | https://www.jetbrains.com/idea/download/ | 推荐使用 |

---

## 🔧 详细安装步骤

### 一、安装 JDK 17

#### Windows 系统：

1. **下载 JDK**
   - 打开浏览器，访问：https://adoptium.net/
   - 选择 **Temurin 17 (LTS)** 版本
   - 点击 **Download** 下载 `.msi` 安装包

2. **安装 JDK**
   - 双击下载的 `.msi` 文件
   - 点击 **Next** → **I Agree** → **Next**
   - 选择安装路径（建议默认：`C:\Program Files\Eclipse Adoptium\jdk-17...`）
   - 点击 **Install** 等待安装完成

3. **验证安装**
   - 按键盘 `Win + R`，输入 `cmd`，按回车
   - 在黑色窗口输入：`java -version`
   - 如果显示 `openjdk version "17.x.x"`，说明安装成功！

#### Mac 系统：

打开"终端"应用，输入以下命令：
```bash
# 安装 Homebrew（如果没有）
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# 安装 JDK 17
brew install openjdk@17

# 创建软链接
sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
```

---

### 二、安装 MySQL 数据库

#### Windows 系统：

1. **下载 MySQL**
   - 访问：https://dev.mysql.com/downloads/installer/
   - 选择 **Windows (x86, 32-bit), MSI Installer** 
   - 点击 **Download**（可以选择 No thanks, just start my download 跳过登录）

2. **安装 MySQL**
   - 双击下载的 `.msi` 文件
   - 选择 **Developer Default** → **Next**
   - 点击 **Execute** 开始下载组件
   - 等待下载完成后点击 **Next**

3. **配置 MySQL**
   - **Type and Networking**: 保持默认，点击 **Next**
   - **Authentication Method**: 选择 "Use Strong Password Encryption"，点击 **Next**
   - **Accounts and Roles**: 设置 root 密码（重要！建议设为 `123456` 方便记忆）
   - **Windows Service**: 勾选 "Start the MySQL Server at System Startup"
   - 点击 **Execute** 应用配置

4. **验证安装**
   - 按 `Win + R`，输入 `cmd`
   - 输入：`mysql -u root -p`
   - 输入你设置的密码，看到 `mysql>` 提示符就成功了！

#### Mac 系统：

```bash
# 安装 MySQL
brew install mysql

# 启动 MySQL 服务
brew services start mysql

# 安全配置（设置密码）
mysql_secure_installation
```

---

### 三、安装 IntelliJ IDEA

1. **下载 IDEA**
   - 访问：https://www.jetbrains.com/idea/download/
   - 选择 **Community** 版本（免费）
   - 点击 **Download**

2. **安装 IDEA**
   - Windows: 双击 `.exe` 文件，一路下一步
   - Mac: 拖动到 Applications 文件夹

3. **首次启动**
   - 打开 IDEA
   - 选择 **New Project** 或 **Open**
   - 接受用户协议

---

## 📥 获取项目代码

### 方法一：使用 Git 克隆（推荐）

如果您的电脑已安装 Git：

1. 打开命令行（Windows: CMD 或 PowerShell；Mac: 终端）
2. 输入以下命令：
```bash
# 进入您想存放项目的目录
cd D:\Projects  # Windows
# 或
cd ~/Documents  # Mac

# 克隆项目（这里假设您已将代码上传到 Git 仓库）
# git clone https://github.com/your-username/sports-event-management.git
```

### 方法二：手动创建项目

如果没有 Git，请按以下步骤操作：

1. **创建项目文件夹**
```
D:\Projects\sports-event-management\
```

2. **创建目录结构**
```
sports-event-management/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── sports/
        │           └── platform/
        │               ├── SportsPlatformApplication.java
        │               ├── config/
        │               ├── entity/
        │               ├── repository/
        │               ├── service/
        │               └── controller/
        └── resources/
            ├── application.yml
            ├── db/
            │   └── schema.sql
            ├── static/
            │   ├── css/
            │   └── js/
            └── templates/
                ├── layout/
                ├── auth/
                ├── dashboard/
                └── event/
```

---

## ⚙️ 配置数据库

### 步骤 1：创建数据库

1. **打开 MySQL 命令行**
   - Windows: 按 `Win + R`，输入 `cmd`
   - Mac: 打开终端

2. **登录 MySQL**
```bash
mysql -u root -p
# 输入您的密码（例如：123456）
```

3. **创建数据库**
```sql
-- 在 mysql> 提示符后输入：
CREATE DATABASE sports_platform 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 查看数据库是否创建成功
SHOW DATABASES;

-- 退出 MySQL
EXIT;
```

---

### 步骤 2：导入初始数据

**方法一：使用命令行导入**

```bash
# 在项目根目录下执行：
mysql -u root -p sports_platform < src/main/resources/db/schema.sql
```

**方法二：手动执行 SQL**

1. 登录 MySQL：
```bash
mysql -u root -p
```

2. 切换到数据库：
```sql
USE sports_platform;
```

3. 复制 `schema.sql` 文件中的内容，粘贴执行

---

## 🚀 运行项目

### 使用 IntelliJ IDEA（最简单）

#### 步骤 1：打开项目

1. 启动 IntelliJ IDEA
2. 点击 **Open** 按钮
3. 选择项目文件夹 `sports-event-management`
4. 点击 **OK**

#### 步骤 2：等待依赖下载

IDEA 会自动识别这是一个 Maven 项目，并开始下载依赖包。

- 右下角会显示进度条
- 等待进度条完成（首次可能需要 5-10 分钟）

#### 步骤 3：配置数据库连接

1. 打开文件：`src/main/resources/application.yml`

2. 找到以下内容并修改：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sports_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456  # 改成您的 MySQL 密码！
```

#### 步骤 4：运行项目

**方法一：点击运行按钮**
1. 打开文件：`src/main/java/com/sports/platform/SportsPlatformApplication.java`
2. 在类名 `SportsPlatformApplication` 上点击绿色三角形
3. 选择 **Run 'SportsPlatformApplication'**

**方法二：使用 Maven**
1. 点击 IDEA 右侧的 **Maven** 面板
2. 展开 **Plugins** → **spring-boot**
3. 双击 **spring-boot:run**

#### 步骤 5：验证运行成功

看到以下输出说明启动成功：
```
========================================
  体育赛事管理平台启动成功!
  访问地址: http://localhost:8080
  默认管理员: admin / admin123
========================================
```

---

## 🌐 访问系统

1. 打开浏览器（推荐 Chrome 或 Edge）
2. 在地址栏输入：http://localhost:8080
3. 使用默认账号登录：
   - 用户名：`admin`
   - 密码：`admin123`

---

## ❓ 常见问题解决

### 问题 1：启动报错 "Communications link failure"

**原因**：无法连接到 MySQL 数据库

**解决方法**：
1. 确认 MySQL 服务已启动
   - Windows: 按 `Win + R`，输入 `services.msc`，找到 MySQL 服务，确保状态为"正在运行"
   - Mac: 终端输入 `brew services list` 查看
2. 检查 `application.yml` 中的密码是否正确
3. 确认数据库 `sports_platform` 已创建

### 问题 2：端口 8080 被占用

**原因**：其他程序占用了 8080 端口

**解决方法**：
方法一：关闭占用端口的程序
```bash
# Windows: 查找占用端口的进程
netstat -ano | findstr :8080
# 根据显示的 PID 关闭进程

# Mac/Linux
lsof -i :8080
kill -9 <PID>
```

方法二：修改端口
在 `application.yml` 中修改：
```yaml
server:
  port: 8081  # 改成其他端口
```

### 问题 3：Maven 依赖下载失败

**原因**：网络问题或 Maven 仓库连接超时

**解决方法**：
1. 配置国内镜像源
   - 找到 Maven 的 `settings.xml` 文件（通常在 `C:\Users\您的用户名\.m2\` 目录下）
   - 添加阿里云镜像：
```xml
<mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <name>Aliyun Maven</name>
    <url>https://maven.aliyun.com/repository/public</url>
</mirror>
```

### 问题 4：登录时提示密码错误

**原因**：初始管理员账户未正确创建

**解决方法**：
重新执行 `schema.sql` 文件：
```bash
mysql -u root -p sports_platform < src/main/resources/db/schema.sql
```

---

## 📞 需要帮助？

如果按照以上步骤仍无法运行，请提供以下信息：

1. 您的操作系统版本（Windows 10/11 或 Mac）
2. 错误信息截图
3. 已完成的步骤

我会帮您逐一排查问题！

---

## 🎉 成功标志

当您看到以下界面时，恭喜您已成功运行项目！

1. 浏览器显示登录页面
2. 输入 `admin` / `admin123` 后进入仪表盘
3. 左侧显示菜单：赛事管理、运动员管理、赛程管理等

祝您使用愉快！🎊
