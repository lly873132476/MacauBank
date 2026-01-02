# 前后端接口对接说明

## 已完成的接口对接

### 1. auth-service 认证服务

所有接口已对接完成，通过网关访问：`http://localhost:8080/auth`

#### 已实现的接口：

| 接口名称 | 前端方法 | 后端接口 | 状态 |
|---------|---------|---------|------|
| 注册 | `authApi.registerExecute()` | `POST /auth/register` | ✅ |
| 登录 | `authApi.login()` | `POST /auth/login` | ✅ |
| 登出 | `authApi.logout()` | `POST /auth/logout` | ✅ |
| 获取用户信息 | `authApi.getUserInfo()` | `GET /user/profile/me` | ✅ |
| 验证Token | `authApi.verifyToken()` | `GET /auth/token/verify` | ✅ |
| 修改登录密码 | `authApi.updatePassword()` | `POST /auth/password/update` | ✅ |
| 修改交易密码 | `authApi.updateTransactionPassword()` | `POST /auth/security/transactionPwd/update` | ✅ |

#### Store已实现的方法：

| Store方法 | 说明 | 状态 |
|----------|------|------|
| `login()` | 登录并保存用户信息 | ✅ |
| `logout()` | 登出并清除本地状态 | ✅ |
| `fetchUserInfo()` | 获取用户详细信息 | ✅ |
| `verifyToken()` | 验证Token有效性 | ✅ |
| `updatePassword()` | 修改登录密码 | ✅ |
| `updateTransactionPassword()` | 修改交易密码 | ✅ |

#### 已对接的页面：

| 页面 | 文件 | 功能 | 状态 |
|------|------|------|------|
| 认证页面 | `AuthView.vue` | 登录、注册 | ✅ |
| 修改密码 | `ProfilePasswordView.vue` | 修改登录密码 | ✅ |
| 交易密码 | `ProfileTransactionPasswordView.vue` | 设置/修改交易密码 | ✅ |

## 主要修改内容

### 1. API服务层 (`src/services/api.ts`)

- ✅ 移除所有mock数据
- ✅ 实现真实HTTP请求
- ✅ 添加统一请求方法 `request()`
- ✅ 自动携带Token
- ✅ 对接所有auth-service接口
- ✅ 添加logout、getUserInfo、verifyToken接口

### 2. 状态管理 (`src/stores/auth.ts`)

- ✅ 登录时保存完整用户信息（userId、username、name）
- ✅ 实现真实的logout API调用
- ✅ 添加fetchUserInfo方法
- ✅ 添加verifyToken方法
- ✅ Token失效时自动清除本地状态

### 3. 注册页面 (`src/views/AuthView.vue`)

- ✅ 修改注册表单字段：
  - 移除email字段
  - 添加username字段（第一步）
  - 将idNumber改为idCard
  - 添加initialCurrency字段（默认MOP）
- ✅ 实现注册API调用
- ✅ 注册成功后自动登录
- ✅ 添加密码一致性验证

### 4. 修复的问题

- ✅ 修复`ProfileTransactionPasswordView.vue`中的方法名：`setTransactionPassword` → `updateTransactionPassword`
- ✅ 统一字段命名：idNumber → idCard

## 数据结构对比

### 注册请求

**前端发送：**
```typescript
{
  username: string;      // 用户名
  password: string;      // 密码
  name: string;          // 姓名
  mobile: string;        // 手机号
  idCard: string;        // 身份证号
  initialCurrency: string; // 初始货币（默认MOP）
}
```

**后端接收（RegisterRequest）：**
```java
{
  username: String;
  password: String;
  name: String;
  mobile: String;
  idCard: String;
  initialCurrency: String;
}
```

✅ **完全匹配**

### 登录响应

**后端返回（LoginResponseDTO）：**
```java
{
  token: String;
  userId: String;
  username: String;
  name: String;
}
```

**前端接收：**
```typescript
{
  token: string;
  userId: string;
  username: string;
  name: string;
}
```

✅ **完全匹配**

## 网络配置

- **API基础URL**: `http://localhost:8080`
- **网关端口**: 8080
- **认证服务**: `http://localhost:8080/auth/**`
- **跨域配置**: 已在网关配置CORS

## Token管理

1. **存储位置**: `localStorage`
2. **存储Key**: `token`
3. **请求头**: `Authorization: {token}`
4. **有效期**: 2小时
5. **失效处理**: 自动清除本地状态并重定向登录

## 测试建议

### 1. 注册流程测试
1. 访问注册页面
2. 填写用户名、手机号、密码
3. 下一步填写姓名、身份证号
4. 提交注册
5. 验证是否自动登录

### 2. 登录流程测试
1. 输入用户名和密码
2. 点击登录
3. 验证Token是否保存到localStorage
4. 验证是否跳转到首页

### 3. 修改密码测试
1. 进入修改密码页面
2. 输入旧密码和新密码
3. 提交
4. 验证是否成功

### 4. 设置交易密码测试
1. 进入交易密码设置页面
2. 输入6位数字密码
3. 提交
4. 验证是否成功

## 注意事项

1. **确保后端服务已启动**：
   - Nacos (8848)
   - Gateway (8080)
   - Auth Service (8081)

2. **检查网络连接**：
   - 确保前端能访问 `http://localhost:8080`
   - 检查浏览器控制台的网络请求

3. **Token验证**：
   - 需要Token的接口会自动从localStorage获取
   - Token失效后会自动清除状态

4. **错误处理**：
   - 所有API调用都包含错误处理
   - 网络错误会返回500错误码

## 下一步工作

- [ ] 对接account-service（账户服务）
- [ ] 对接transfer-service（转账服务）
- [ ] 对接currency-service（汇率服务）
- [ ] 对接message-service（消息服务）
- [ ] 对接user-service（用户服务）
