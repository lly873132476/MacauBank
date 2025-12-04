// API服务
interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

// 类型定义
interface AccountSummary {
  totalMopValue: number;
  accounts: AccountInfo[];
}

interface AccountInfo {
  accountId: number;
  accountName?: string;
  currencyCode: string;
  balance: number;
  mopValue?: number;
  status?: number;
  // 兼容字段
  id?: number;
  currency?: string;
  name?: string;
}

interface TransactionRecord {
  transactionId: number;
  accountId?: number;
  type?: string;
  amount: number;
  currencyCode: string;
  mopValue: number;
  description: string;
  relatedAccount?: string;
  transactionTime: string;
  // 兼容字段
  id?: number;
  date?: string;
  currency?: string;
}

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

interface UserProfile {
  id: string;
  name: string;
  nickname: string;
  avatar: string;
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
  fromAccountId: number;
  toAccount: string;
  amount: number;
  currencyCode: string;
  transactionPassword: string;
  remark?: string;
  idempotentKey: string;
  transferType?: 'NORMAL' | 'CROSS_BORDER'; // 转账类型: NORMAL-普通转账, CROSS_BORDER-跨境转账
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
  remark: string;
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

    const result = await response.json();
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
    username: string;
    password: string;
    name: string;
    mobile: string;
    idCard: string;
    initialCurrency: string;
  }): Promise<ApiResponse<{ userId: string; accountId: string; username: string }>> {
    return request('/auth/register/execute', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  // 登录
  async login(data: { username: string; password: string }): Promise<ApiResponse<{ 
    token: string;
    userId: string;
    username: string;
    name: string;
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
  async getUserInfo(): Promise<ApiResponse<{
    userId: string;
    username: string;
    name: string;
    mobile: string;
    idCard: string;
    status: number;
    hasTransactionPassword: boolean;
    createTime: Date;
  }>> {
    return request('/auth/user/info', {
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
    return request('/auth/security/transactionPwd/update', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  }
};

// 账户相关API
export const accountApi = {
  // 资产总览
  async getSummary(): Promise<ApiResponse<AccountSummary>> {
    return request('/account/summary', {
      method: 'GET'
    });
  },

  // 账户列表
  async getAccountList(): Promise<ApiResponse<AccountInfo[]>> {
    return request('/account/list', {
      method: 'GET'
    });
  },

  // 交易流水
  async getTransactionRecords(accountId: string, currencyCode?: string, type?: string, page?: number, pageSize?: number): Promise<ApiResponse<TransactionRecord[]>> {
    const params = new URLSearchParams();
    params.append('accountId', accountId);
    if (currencyCode) params.append('currencyCode', currencyCode);
    if (type) params.append('type', type);
    if (page) params.append('page', page.toString());
    if (pageSize) params.append('pageSize', pageSize.toString());
    
    return request(`/account/transactions/records?${params.toString()}`, {
      method: 'GET'
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
    return request('/transfer/execute', {
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
    if (currencyCode) params.append('currencyCode', currencyCode);
    if (status) params.append('status', status);
    params.append('page', page.toString());
    params.append('pageSize', pageSize.toString());
    
    return request(`/transfer/records?${params.toString()}`, {
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

// 模拟货币相关API
export const currencyApi = {
  // 获取汇率
  async getRate(from: string, to: string): Promise<ApiResponse<number>> {
    await delay(50);
    const rates: Record<string, number> = {
      'MOP-HKD': 0.98,
      'MOP-CNY': 0.85,
      'HKD-MOP': 1.02,
      'HKD-CNY': 0.87,
      'CNY-MOP': 1.18,
      'CNY-HKD': 1.15
    };
    return {
      code: 200,
      message: '成功',
      data: rates[`${from}-${to}`] || 1
    };
  },

  // 执行外币兑换
  async exchangeCurrency(data: { fromAccountId: string; toAccountId: string; amount: number }): Promise<ApiResponse<ExchangeResult>> {
    await delay(50);
    return {
      code: 200,
      message: '兑换成功',
      data: {
        transactionId: 'ex_12345',
        fromAmount: data.amount,
        toAmount: data.amount * 0.98 // 模拟汇率
      }
    };
  }
};

// 模拟用户相关API
export const userApi = {
  // 获取用户信息
  async getProfile(): Promise<ApiResponse<UserProfile>> {
    await delay(50);
    return {
      code: 200,
      message: '成功',
      data: {
        id: 'user_12345',
        name: '王小明',
        nickname: '小明',
        avatar: 'https://example.com/avatar.jpg'
      }
    };
  },

  // 更新用户信息
  async updateProfile(data: Partial<UserProfile>): Promise<ApiResponse<UserProfile>> {
    await delay(50);
    // 模拟返回完整的用户信息对象
    const updatedProfile: UserProfile = {
      id: 'user_12345',
      name: data.name || '王小明',
      nickname: data.nickname || '小明',
      avatar: data.avatar || 'https://example.com/avatar.jpg'
    };
    return {
      code: 200,
      message: '更新成功',
      data: updatedProfile
    };
  }
};

// 模拟消息相关API
export const messageApi = {
  // 获取消息列表
  async getMessages(type: 'system' | 'transaction'): Promise<ApiResponse<Message[]>> {
    await delay(50);
    if (type === 'system') {
      return {
        code: 200,
        message: '成功',
        data: [
          { id: 'msg_001', title: '系统维护通知', content: '系统将于今晚00:00-02:00进行维护', time: '2023-05-15 18:00', read: false },
          { id: 'msg_002', title: '新功能上线', content: '新增外币兑换功能', time: '2023-05-10 10:00', read: false }
        ]
      };
    } else {
      return {
        code: 200,
        message: '成功',
        data: [
          { id: 'tx_msg_001', title: '转账成功', content: '您已成功向张三转账1000.00 MOP', time: '2023-05-15 14:30', read: false },
          { id: 'tx_msg_002', title: '收到转账', content: '李四向您转账500.00 HKD', time: '2023-05-14 12:15', read: false }
        ]
      };
    }
  }
};