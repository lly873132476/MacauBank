# 🏦 MacauBank - 金融级微服务架构示例项目

<div align="center">

**基于 DDD 的分布式银行核心系统**

[![JDK](https://img.shields.io/badge/JDK-17-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Dubbo](https://img.shields.io/badge/Dubbo-3.3.0-blue.svg)](https://dubbo.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

</div>

---

## 📖 项目简介

MacauBank 是一个基于 **领域驱动设计（DDD）** 的金融级微服务架构示例项目，实现了银行核心业务系统的关键功能，包括账户管理、转账交易、外汇兑换等。

项目采用主流的微服务技术栈，严格遵循 DDD 分层架构，实现了分布式事务、状态机驱动、复式记账等金融级特性，可作为学习微服务架构和金融业务的参考项目。

### 🎯 核心特性

- ✅ **DDD 四层架构**：严格的 Interfaces、Application、Domain、Infrastructure 分层，Repository 接口隔离
- ✅ **状态机驱动**：基于责任链模式的业务流程编排（10 个 Handler）
- ✅ **策略模式**：3 种转账策略（行内/跨行/跨境），符合开闭原则
- ✅ **分布式事务**：从 Seata AT 演进到 TCC + 状态机的最终一致性方案
- ✅ **冲正/退款**：完整的逆向交易流程，复用状态机架构
- ✅ **审计日志**：AOP + SpEL 表达式，异步存储，满足监管合规
- ✅ **Sentinel 限流熔断**：核心接口配置熔断降级策略
- ✅ **复式记账**：符合金融行业规范的会计分录设计
- ✅ **三层幂等性防御**：Redis Token + 数据库唯一索引 + 业务幂等
- ✅ **资损告警**：幂等校验发现金额不一致时触发告警
- ✅ **HMAC 防篡改**：余额数据完整性校验
- ✅ **高并发处理**：乐观锁 + 分布式锁的组合方案
- ✅ **Docker Compose 部署**：一键启动完整的微服务环境

---

## 🏗️ 技术架构

### 技术栈

| 类别 | 技术 | 版本 | 说明 |
|------|------|------|------|
| **核心框架** | Spring Boot | 3.2.4 | 微服务基础框架 |
| **RPC 框架** | Apache Dubbo | 3.3.0 | 服务间通信 |
| **ORM 框架** | MyBatis-Plus | 3.5.5 | 数据持久化 |
| **数据库** | MySQL | 8.0.33 | 关系型数据库 |
| **缓存** | Redis | 7.2 | 分布式缓存 |
| **消息队列** | RocketMQ | 5.3.0 | 异步消息 |
| **注册中心** | Nacos | 2.5.2 | 服务注册与配置中心 |
| **分布式事务** | Seata | 2.5.0 | 分布式事务管理 |
| **链路追踪** | SkyWalking | 9.4.0 | APM 监控 |
| **限流熔断** | Sentinel | 1.8.6 | 流量控制 |
| **分布式锁** | Redisson | 3.27.2 | 分布式锁实现 |
| **容器化** | Docker Compose | - | 容器编排 |

### 系统架构

```
┌─────────────────────────────────────────────────────────┐
│                     Gateway Layer                        │
│                    (API Gateway)                         │
└─────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
┌───────▼────────┐  ┌──────▼───────┐  ┌───────▼────────┐
│ Auth Service   │  │Transfer Service│  │ Forex Service  │
│ (认证授权)      │  │  (转账引擎)     │  │  (外汇交易)     │
└────────────────┘  └────────────────┘  └────────────────┘
        │                   │                   │
        └───────────────────┼───────────────────┘
                            │
                    ┌───────▼────────┐
                    │ Account Service │
                    │   (账户核心)     │
                    └────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
┌───────▼────────┐  ┌──────▼───────┐  ┌───────▼────────┐
│     MySQL      │  │    Redis     │  │   RocketMQ     │
└────────────────┘  └──────────────┘  └────────────────┘
```

### DDD 分层架构

```
bank-backend/
├── account-service/          # 账户核心服务
│   ├── interfaces/           # 接口层（HTTP、RPC）
│   ├── application/          # 应用层（流程编排）
│   ├── domain/              # 领域层（核心业务逻辑）
│   └── infrastructure/      # 基础设施层（数据持久化）
├── transfer-service/        # 转账引擎服务
│   ├── interfaces/
│   ├── application/
│   ├── domain/
│   │   ├── pipeline/       # 状态机 Handler 链
│   │   └── service/        # 领域服务
│   └── infrastructure/
├── forex-service/           # 外汇交易服务
├── auth-service/            # 认证授权服务
├── user-service/            # 用户管理服务
└── message-service/         # 消息通知服务
```

---

## 💎 核心设计

### 1. 状态机驱动的转账流程

采用责任链模式实现状态机，每个 Handler 负责一个业务阶段：

```
转账流程：
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ 冻结资金     │ -> │ 发送风控MQ   │ -> │ 扣款        │
│FreezeFund   │    │SendRiskMq   │    │DeductMoney  │
└─────────────┘    └─────────────┘    └─────────────┘
       │                                      │
       v                                      v
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ 解冻        │ <- │ 入账        │ <- │ 通知SWIFT   │
│Unfreeze     │    │CreditPayee  │    │NotifySwift  │
└─────────────┘    └─────────────┘    └─────────────┘
```

**优势**：
- 职责单一，易于维护
- 可扩展，新增转账类型只需配置 Handler 链
- 符合开闭原则（OCP）

### 1.1 冲正/退款流程（逆向交易）

复用状态机架构实现完整的逆向流程：

```
冲正流程：
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ 反向入账     │ -> │ 反向扣款     │ -> │ 反向手续费   │
│ReverseCredit│    │ReverseDeduct│    │ReverseFee   │
└─────────────┘    └─────────────┘    └─────────────┘

状态机驱动：
SUCCESS/FAILED → REVERSING → REVERSED
```

**设计亮点**：
- 策略模式：`getReversalTransition()` 由各策略自定义逆向路径
- 架构对称：正向/逆向使用同一套状态机引擎
- 幂等保护：冲正请求同样有幂等校验

### 2. 分布式事务演进

**Seata AT（CP 模式）→ TCC + 状态机（AP 模式）**

| 方案 | 一致性 | 性能 | 适用场景 |
|------|--------|------|---------|
| Seata AT | 强一致性 | TPS ~10 | 短链路、低并发 |
| TCC + 状态机 | 最终一致性 | TPS ~1000+ | 长链路、高并发 |

**演进收益**：
- TPS 从 10 提升到 1000+（100 倍）
- 数据库锁持有时间：3s → 20ms
- 通过 MQ 重试 + 幂等性保证最终一致性

### 3. 复式记账

符合金融行业规范的会计分录设计：

```sql
-- 转账 1000 MOP：A → B
-- 账户 A：借方（D）1000
INSERT INTO account_sub_ledger (account_no, cd_flag, amount, balance, voucher_no)
VALUES ('A', 'D', 1000, 9000, 'V20260102001');

-- 账户 B：贷方（C）1000
INSERT INTO account_sub_ledger (account_no, cd_flag, amount, balance, voucher_no)
VALUES ('B', 'C', 1000, 11000, 'V20260102001');

-- 检查：SUM(借方) = SUM(贷方) ✓
```

**业务价值**：
- 自动平衡检查（借贷不平 = 有 Bug）
- 审计追溯（完整的会计分录）
- 对账简化（日终检查借贷平衡）

### 4. 三层幂等性防御

```
请求
  │
  ├─> 【第一层】Redis Token（setIfAbsent）
  │   - TTL: 5秒
  │   - 拦截 99% 重复请求
  │
  ├─> 【第二层】业务逻辑
  │   - 状态机执行
  │   - 数据库操作
  │
  └─> 【第三层】数据库唯一索引
      - idempotent_key UNIQUE
      - 最后的保底防线
```

**设计理念**：
- Redis：廉价的快速失败（性能优化）
- 数据库：昂贵的最后保底（数据一致性）

### 5. HMAC 防篡改

```java
// 余额变动时计算 MAC 码
String macCode = HmacUtils.hmacSha256Hex(
    SECRET_KEY, 
    balance + "|" + version + "|" + accountNo
);

// 读取时校验 MAC 码
if (!macCode.equals(storedMacCode)) {
    throw new SecurityException("余额数据被篡改");
}
```

**安全价值**：
- 防止 DBA 或黑客篡改余额数据
- 金融级的数据完整性保护

### 6. 高并发处理

**乐观锁 + 分布式锁**

```sql
-- 乐观锁（常规场景）
UPDATE account_balance
SET balance = balance - #{amount},
    version = version + 1
WHERE account_no = #{accountNo}
  AND version = #{oldVersion}
  AND available_balance >= #{amount};
```

```java
// 分布式锁（热点账户）
RLock lock = redissonClient.getLock(lockKey);
try {
    lock.tryLock(3, 10, TimeUnit.SECONDS);
    // 业务逻辑
} finally {
    lock.unlock();
}
```

**组合使用**：
- 常规场景：乐观锁（性能好）
- 热点账户：分布式锁（保证安全）

---

## 🚀 快速启动

### 环境要求

- JDK 17+
- Maven 3.8+
- Docker & Docker Compose
- 至少 4GB 可用内存

### 一键启动

```bash
# 1. 克隆项目
git clone https://github.com/yourusername/MacauBank.git
cd MacauBank/bank-backend

# 2. 启动中间件（MySQL、Redis、RocketMQ、Nacos、Seata 等）
cd docker
docker-compose up -d

# 3. 等待中间件启动完成（约 2 分钟）
docker-compose ps

# 4. 编译项目
cd ..
mvn clean package -DskipTests

# 5. 启动微服务
# 按顺序启动：account-service -> transfer-service -> forex-service
cd account-service
mvn spring-boot:run

# 新开终端
cd transfer-service
mvn spring-boot:run

# 新开终端
cd forex-service
mvn spring-boot:run
```

### 验证服务

```bash
# 检查 Nacos 服务注册
open http://localhost:8848/nacos
# 用户名/密码：nacos/nacos

# 检查 SkyWalking 监控
open http://localhost:8080

# 测试转账接口
curl -X POST http://localhost:8082/transfer/submit \
  -H "Content-Type: application/json" \
  -d '{
    "payerAccountNo": "ACC001",
    "payeeAccountNo": "ACC002",
    "amount": 1000,
    "currency": "MOP",
    "requestId": "REQ20260102001"
  }'
```

---

## 📊 数据库设计

### 核心表结构

#### account_balance（余额表）

```sql
CREATE TABLE account_balance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_no VARCHAR(32) NOT NULL UNIQUE COMMENT '账号',
    balance DECIMAL(18,2) NOT NULL COMMENT '余额',
    available_balance DECIMAL(18,2) NOT NULL COMMENT '可用余额',
    frozen_balance DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '冻结余额',
    currency VARCHAR(3) NOT NULL DEFAULT 'MOP' COMMENT '币种',
    version INT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    mac_code VARCHAR(64) COMMENT 'HMAC校验码',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='账户余额表';
```

#### account_sub_ledger（分户账表）

```sql
CREATE TABLE account_sub_ledger (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_no VARCHAR(32) NOT NULL COMMENT '账号',
    cd_flag CHAR(1) NOT NULL COMMENT '借贷标志：D-借方，C-贷方',
    amount DECIMAL(18,2) NOT NULL COMMENT '发生额',
    balance DECIMAL(18,2) NOT NULL COMMENT '余额快照',
    voucher_no VARCHAR(32) NOT NULL COMMENT '会计凭证号',
    biz_no VARCHAR(64) NOT NULL COMMENT '业务流水号',
    biz_type VARCHAR(16) NOT NULL COMMENT '业务类型',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_account_no (account_no),
    INDEX idx_voucher_no (voucher_no),
    INDEX idx_biz_no (biz_no)
) COMMENT='账户分户账表（复式记账）';
```

#### transfer_order（转账订单表）

```sql
CREATE TABLE transfer_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
    payer_account_no VARCHAR(32) NOT NULL COMMENT '付款账号',
    payee_account_no VARCHAR(32) NOT NULL COMMENT '收款账号',
    amount DECIMAL(18,2) NOT NULL COMMENT '金额',
    currency VARCHAR(3) NOT NULL COMMENT '币种',
    status VARCHAR(16) NOT NULL COMMENT '状态',
    current_phase VARCHAR(32) COMMENT '当前阶段',
    idempotent_key VARCHAR(64) NOT NULL UNIQUE COMMENT '幂等键',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_payer (payer_account_no),
    INDEX idx_payee (payee_account_no),
    INDEX idx_status (status)
) COMMENT='转账订单表';
```

---

## 🧪 测试

### 单元测试

```bash
# 运行所有单元测试
mvn test

# 运行指定服务的测试
cd account-service
mvn test
```

### 集成测试

```bash
# 使用 Postman 导入测试集合
docs/postman/MacauBank.postman_collection.json
```

---

## 📈 性能指标

| 指标 | 数值 | 说明 |
|------|------|------|
| **转账 TPS** | 1000+ | 使用 TCC + 状态机方案 |
| **查询 QPS** | 5000+ | Redis 缓存加速 |
| **平均响应时间** | < 100ms | 正常业务场景 |
| **P99 响应时间** | < 500ms | 99% 请求 |
| **数据库锁持有时间** | < 20ms | 乐观锁方案 |

---

## 📝 项目文档

- [架构设计文档](docs/architecture.md)
- [API 接口文档](docs/api.md)
- [数据库设计文档](docs/database.md)
- [部署运维文档](docs/deployment.md)

---

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

### 开发规范

1. **代码规范**：遵循阿里巴巴 Java 开发手册
2. **提交规范**：使用 Conventional Commits 格式
3. **分支策略**：Git Flow 工作流
4. **测试覆盖率**：核心业务逻辑 > 80%

---

## 📄 License

本项目采用 [MIT License](LICENSE) 开源协议。

---

## 🙏 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Apache Dubbo](https://dubbo.apache.org/)
- [Seata](https://seata.io/)
- [RocketMQ](https://rocketmq.apache.org/)
- [Nacos](https://nacos.io/)
- [SkyWalking](https://skywalking.apache.org/)

---

<div align="center">

**如果这个项目对您有帮助，请给个 ⭐️ Star 支持一下！**

Made with ❤️ by MacauBank Team

</div>
