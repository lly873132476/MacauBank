// API服务
interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

// 类型定义
export interface AccountBalanceResponse {
  currencyCode: string;
  balance: number;
  availableBalance: number;
  frozenAmount: number;
  totalIncome: number;
  totalOutcome: number;
  updateTime: string;
}

export interface AccountResponse {
  id: number;
  userNo: string;
  accountNo: string;
  cardNumber: string;
  accountCategory: string;
  accountType: string;
  status: string;
  riskLevel: string;
  openBranchCode: string;
  openBranchName: string;
  createTime: string;
  balances: AccountBalanceResponse[];
}

export interface AssetSummaryResponse {
  totalMopValue: number;
  accounts: AccountResponse[];
}

// 兼容旧代码的类型定义 (保留或逐步替换)
interface AccountSummary extends AssetSummaryResponse {}
interface AccountInfo extends AccountResponse {
  // 增加一些兼容旧逻辑的字段
  accountId?: number;
  accountName?: string;
  currencyCode?: string;
  balance?: number;
}

export interface TransactionFlowResponse {
  id: number;
  txnId: string;
  currencyCode: string;
  direction: 'D' | 'C';
  amount: number;
  balance: number;
  bizType: string;
  bizDesc: string;
  opponentInfo: Record<string, any>;
  transTime: string;
}

export interface TransactionFlowRequest {
  accountNo?: string;
  currencyCode?: string;
  startDate?: string;
  endDate?: string;
  direction?: 'D' | 'C';
  bizType?: string;
  page?: number;
  pageSize?: number;
}

interface PageResult<T> {
  total: number;
  list: T[];
}

export interface PayeeResponse {
  id: string;
  payeeName: string;
  accountNo: string;
  bankCode: string;
  bankName: string;
  currencyCode: string;
  aliasName: string;
  lastTransTime?: string;
  totalTransCount?: number;
}

export interface AddPayeeRequest {
  payeeName: string;
  accountNo: string;
  bankCode: string;
  bankName: string;
  currencyCode?: string;
  aliasName?: string;
}

export interface UpdatePayeeRequest {
  id: string;
  payeeName?: string;
  accountNo?: string;
  bankCode?: string;
  bankName?: string;
  currencyCode?: string;
  aliasName?: string;
}

interface IPage<T> {
  total: number;
  records: T[];
}

interface ApiTransactionRecord extends TransactionFlowResponse {}

interface ExchangeResult {
  transactionId: string;
  fromAmount: number;
  toAmount: number;
}

interface TransferVerifyResult {
  fee: number;
  estimatedTime: string;
}

interface Payee {
  id: string;
  name: string;
  account: string;
  currency: string;
}

interface Recipient {
  id: string;
  name: string;
  account: string;
  currency: string;
}

interface RedPacketResult {
  redPacketId: string;
  link: string;
}

export interface ExchangeRateReferenceResponse {
  currencyPair: string;
  baseCurrency: string;
  targetCurrency: string;
  buyRate: number;
  sellRate: number;
  changePercent: string;
  updateTime: string;
}

interface UserProfile {
  // 兼容旧字段
  id?: string;
  userId?: string;
  userName?: string;
  name?: string;
  nickname?: string;
  avatar?: string;
  mobile?: string;
  idCard?: string;
  status?: number;
  hasTransactionPassword?: boolean;
  createTime?: Date;

  // 后端实际返回字段
  realNameCn?: string;
  realNameEn?: string;
  kycLevel?: number; // KYC等级
  kycLevelDesc?: string;
  kycStatus?: number;
  idCardNo?: string;
  gender?: number;
  nationality?: string;
}

export interface MessageResponse {
  messageId: string;
  type: number; // 1-TRANSACTION, 2-SECURITY, 3-SYSTEM
  title: string;
  content: string;
  isRead: number; // 0-NO, 1-YES
  createTime: string;
}

interface Message {
  id: string;
  title: string;
  content: string;
  time: string;
  read: boolean;
}

interface TransferData {
  fromAccountId: string;
  toAccountId: string;
  amount: number;
  receiverName?: string;
  receiverAccount?: string;
  purpose?: string;
}

interface TransferRequest {
  fromAccountNo: string;
  toAccountNo: string;
  toAccountName?: string;
  amount: number;
  currencyCode: string;
  transactionPassword: string;
  remark?: string;
  idempotentKey: string;
  transferType?: 'INTERNAL' | 'CROSS_BORDER'; // 转账类型: INTERNAL-行内转账, CROSS_BORDER-跨境转账
  swiftCode?: string;
  toBankCode?: string;
}

interface TransferResult {
  transactionId: number;
  fromAccountId: number;
  toAccountNumber: string;
  amount: number;
  currencyCode: string;
  fee: number;
  status: string;
  transferType: string;
  txnId: string;
  createTime: string;
}

interface TransferRecord {
  id: number;
  txnId: string; // 交易流水号
  payerAccountId: number; // 付款账户ID
  payeeAccountNo: string; // 收款账户号
  amount: number; // 金额
  currencyCode: string; // 货币代码
  fee: number; // 手续费
  status: string; // 状态
  transferType: string; // 转账类型
  transferChannel: string; // 转账通道
  createTime: string; // 创建时间
}

export interface UserCertificationRequest {
  realNameCn: string;
  realNameEn: string;
  idCardType: number; // 1-澳门身份证 2-回乡证 3-护照 4-蓝卡
  idCardNo: string;
  idCardExpiry: string; // Date string
  idCardIssueCountry: string;
  idCardIssueOrg: string;
  idCardImgFront: string;
  idCardImgBack: string;
  gender: number; // 1:男 2:女
  birthday: string; // Date string
  nationality: string;
  occupation: string;
  employmentStatus: number; // 1-受雇 2-自雇 3-待业 4-退休 5-学生
  taxId?: string;
  addressRegion: string;
  addressDetail: string;
}

// API基础URL - 通过网关访问
const API_BASE_URL = 'http://localhost:8080';

// 延迟函数（用于模拟API延迟）
const delay = (ms: number): Promise<void> => {
  return new Promise(resolve => setTimeout(resolve, ms));
};

// 获取Token
const getToken = (): string => {
  return localStorage.getItem('token') || '';
};

// 处理未授权/Token失效
const handleUnauthorized = () => {
  console.log('[API Debug] Handling unauthorized access. Clearing token and redirecting.');
  localStorage.removeItem('token');
  // Hash 模式下，检查 hash 路径
  if (!window.location.hash.includes('auth')) {
     // 路由表中登录页是 /auth
     window.location.href = '/#/auth';
  }
};

// 通用请求方法
const request = async <T>(url: string, options: RequestInit = {}): Promise<ApiResponse<T>> => {
  const token = getToken();
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': token } : {}),
    ...options.headers
  };

  try {
    const response = await fetch(`${API_BASE_URL}${url}`, {
      ...options,
      headers
    });

    // 优先处理 HTTP 401 未授权状态
    if (response.status === 401) {
      handleUnauthorized();
      return {
        code: 401,
        message: '未授权或登录已过期',
        data: null as T
      };
    }

    const result = await response.json();
    
    // DEBUG: 打印接口返回结果，排查拦截失败原因
    console.log(`[API Debug] ${url} result:`, result);

    // 全局业务错误拦截 (Token失效等)
    // 使用 == 兼容字符串类型的 code (例如 "102002")
    if (result.code == 102002 || result.code == '102002') {
      console.log('[API Debug] Detecting 102002, triggering redirect...');
      handleUnauthorized();
    }

    return result;
  } catch (error) {
    const errorMessage = error instanceof Error ? error.message : '网络请求失败';
    return {
      code: 500,
      message: errorMessage,
      data: null as T
    };
  }
};

// 认证相关API
export const authApi = {
  // 执行注册
  async registerExecute(data: {
    userName?: string;
    password: string;
    mobilePrefix: string;
    mobile: string;
    verifyCode: string;
  }): Promise<ApiResponse<{ 
    userId: string; 
    accountId: string; 
    userName: string; 
    token?: string; 
    userNo?: string; 
    name?: string; 
  }>> {
    return request('/auth/register', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  // 登录
  async login(data: { 
    userName: string; 
    password?: string; 
    loginType?: string; 
    verifyCode?: string; 
  }): Promise<ApiResponse<{
    token: string;
    userId: string;
    userName: string;
    name: string;
    userNo?: string;
  }>> {
    return request('/auth/login', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  // 登出
  async logout(): Promise<ApiResponse<null>> {
    return request('/auth/logout', {
      method: 'POST'
    });
  },

  // 获取用户信息
  async getUserInfo(): Promise<ApiResponse<UserProfile>> {
    return request('/user/profile/me', {
      method: 'GET'
    });
  },

  // 验证Token
  async verifyToken(): Promise<ApiResponse<boolean>> {
    return request('/auth/token/verify', {
      method: 'GET'
    });
  },

  // 修改登录密码
  async updatePassword(data: { oldPassword: string; newPassword: string }): Promise<ApiResponse<null>> {
    return request('/auth/password/update', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  // 设置/修改交易密码
  async updateTransactionPassword(data: { password: string }): Promise<ApiResponse<null>> {
    return request('/auth/transPwd/update', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  }
};

// 账户相关API
export const accountApi = {
  // 资产总览
  async getSummary(): Promise<ApiResponse<AssetSummaryResponse>> {
    return request('/account/asset/summary', {
      method: 'GET'
    });
  },

  // 账户列表
  async getAccountList(): Promise<ApiResponse<AccountResponse[]>> {
    return request('/account/list', {
      method: 'GET'
    });
  },

  // 交易流水
  async getTransactionRecords(data: TransactionFlowRequest): Promise<ApiResponse<IPage<TransactionFlowResponse>>> {
    return request('/account/bill/list', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  // 账户详情
  async getAccountDetail(accountId: string): Promise<ApiResponse<AccountInfo>> {
    return request(`/account/${accountId}`, {
      method: 'GET'
    });
  }
};

// 转账相关API
export const transferApi = {
  // 快速转账预校验
  async verifyTransfer(data: TransferRequest): Promise<ApiResponse<TransferVerifyResult>> {
    return request('/transfer/verify', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  // 执行快速转账
  async executeTransfer(data: TransferRequest): Promise<ApiResponse<TransferResult>> {
    return request('/transfer/submit', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  // 跨境汇款预校验
  async verifyCrossBorderTransfer(data: TransferRequest): Promise<ApiResponse<TransferVerifyResult>> {
    return request('/transfer/crossborder/verify', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  // 查询转账记录
  async getTransferRecords(payerAccountId?: string, payeeAccountNumber?: string,
                          currencyCode?: string, status?: string,
                          page: number = 1, pageSize: number = 20): Promise<ApiResponse<TransferRecord[]>> {
    const params = new URLSearchParams();
    if (payerAccountId) params.append('payerAccountId', payerAccountId);
    if (payeeAccountNumber) params.append('payeeAccountNumber', payeeAccountNumber);
    // 后端 Controller 暂不支持 currencyCode 和 status 过滤，仅保留支持的参数
    // if (currencyCode) params.append('currencyCode', currencyCode);
    // if (status) params.append('status', status);
    params.append('page', page.toString());
    params.append('pageSize', pageSize.toString());

    return request(`/transfer/list?${params.toString()}`, {
      method: 'GET'
    });
  },

  // 获取转账记录详情
  async getTransferRecordById(id: string): Promise<ApiResponse<TransferRecord>> {
    return request(`/transfer/records/${id}`, {
      method: 'GET'
    });
  },

  // 常用收款人列表
  async getRecipientList(): Promise<ApiResponse<Recipient[]>> {
    return request('/transfer/payee/list', {
      method: 'GET'
    });
  },

  // 创建红包/AA收款
  async createRedPacket(data: { amount: number; count: number; message: string }): Promise<ApiResponse<RedPacketResult>> {
    return request('/transfer/redpacket/create', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  }
};

// 收款人管理 API
export const payeeApi = {
  async getPage(current: number = 1, size: number = 20): Promise<ApiResponse<IPage<PayeeResponse>>> {
    return request(`/transfer/payee/page?current=${current}&size=${size}`, {
      method: 'GET'
    });
  },

  async add(data: AddPayeeRequest): Promise<ApiResponse<void>> {
    return request('/transfer/payee/add', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  async update(data: UpdatePayeeRequest): Promise<ApiResponse<void>> {
    return request('/transfer/payee/update', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  async delete(id: string): Promise<ApiResponse<void>> {
    return request(`/transfer/payee/${id}`, {
      method: 'DELETE'
    });
  }
};

// 货币相关API
export const currencyApi = {
  // 获取参考汇率列表 (首页看板)
  async getReferenceList(): Promise<ApiResponse<ExchangeRateReferenceResponse[]>> {
    return request('/currency/reference/list', {
      method: 'GET'
    });
  },

  // 获取具体汇率
  async getRate(from: string, to: string): Promise<ApiResponse<number>> {
    return request(`/currency/rate?from=${from}&to=${to}`, {
      method: 'GET'
    });
  },

  // 执行外币兑换
  async exchangeCurrency(data: { fromAccountId: string; toAccountId: string; amount: number }): Promise<ApiResponse<ExchangeResult>> {
    return request('/currency/exchange', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  }
};

// 用户相关API
export const userApi = {
  // 上传文件
  async uploadFile(file: File): Promise<ApiResponse<string>> {
    const formData = new FormData();
    formData.append('file', file);
    
    const token = getToken();
    try {
      const response = await fetch(`${API_BASE_URL}/user/file/upload`, {
        method: 'POST',
        headers: {
          ...(token ? { 'Authorization': token } : {})
        },
        body: formData
      });
      
      if (response.status === 401) {
        handleUnauthorized();
        return { code: 401, message: '未授权', data: '' };
      }

      const result = await response.json();
      if (result.code === 102002) {
        handleUnauthorized();
      }
      return result;
    } catch (error) {
      return {
        code: 500,
        message: '文件上传失败',
        data: ''
      };
    }
  },

  // 提交KYC开户申请 (实名认证)
  async submitKyc(data: UserCertificationRequest): Promise<ApiResponse<null>> {
    return request('/user/certify', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  // 获取用户信息 (复用 authApi 的逻辑，或直接调用 user-service)
  async getProfile(): Promise<ApiResponse<UserProfile>> {
    return request('/user/profile/me', {
      method: 'GET'
    });
  }
};

// 消息中心API
export const messageApi = {
  // 分页获取消息列表
  async getMessagePage(page: number = 1, pageSize: number = 20): Promise<ApiResponse<IPage<MessageResponse>>> {
    return request(`/message/page?page=${page}&pageSize=${pageSize}`, {
      method: 'GET'
    });
  },

  // 标记已读
  async markAsRead(messageId: string): Promise<ApiResponse<void>> {
    return request(`/message/read?messageId=${messageId}`, {
      method: 'POST'
    });
  },

  // 获取未读消息数量
  async getUnreadCount(): Promise<ApiResponse<number>> {
    return request('/message/unread/count', {
      method: 'GET'
    });
  }
};

// ==========================================
// Forex (外汇) 相关 API
// ==========================================

export enum ForexDirectionEnum {
  BUY = 'BUY',
  SELL = 'SELL'
}

export enum ForexTradeStatusEnum {
  PROCESSING = 0,
  SUCCESS = 1,
  FAIL = 2,
  REFUNDED = 3
}

export interface ForexExchangeRequest {
  requestId: string;      // 请求唯一ID (幂等键)
  pairCode: string;       // e.g. "HKD_MOP"
  direction: ForexDirectionEnum;
  sellCurrency: string;
  sellAmount: number;
  buyCurrency: string;
  accountNo: string;
}

export interface ForexExchangeResponse {
  txnId: string;
  dealRate: number;
  sellCurrency: string;
  sellAmount: number;
  buyCurrency: string;
  buyAmount: number;
  status: ForexTradeStatusEnum;
  transTime: string;
}

export const forexApi = {
  // 外币兑换
  async exchange(data: ForexExchangeRequest): Promise<ApiResponse<ForexExchangeResponse>> {
    return request('/forex/exchange', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  }
};