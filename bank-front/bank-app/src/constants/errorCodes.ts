/**
 * 错误码常量
 * 
 * 错误码规则：6位数字
 * 格式：应用码(2位) + 错误类型(2位) + 错误序号(2位)
 * 
 * 应用码分配：10-99
 * 10 - auth-service      (认证服务)
 * 20 - account-service   (账户服务)
 * 30 - transfer-service  (转账服务)
 * 40 - currency-service  (货币服务)
 * 50 - user-service      (用户服务)
 * 60 - message-service   (消息服务)
 * 99 - common            (通用/网关)
 * 
 * 错误类型：10-99
 * 10 - 系统错误
 * 20 - 认证错误
 * 30 - 业务错误
 * 40 - 数据错误
 * 50 - 网络错误（预留）
 * 
 * 错误序号：01-99
 */

// ==================== 通用错误码 ====================
export const SUCCESS = 200
export const FAIL = 991001
export const PARAM_ERROR = 991002

// ==================== Auth服务 - 认证错误 (1020xx) ====================
export const UNAUTHORIZED = 102001
export const TOKEN_INVALID = 102002
export const TOKEN_EXPIRED = 102003
export const PERMISSION_DENIED = 102004
export const ACCOUNT_DISABLED = 102005

// ==================== Auth服务 - 业务错误 (1030xx) ====================
export const USER_NOT_FOUND = 103001
export const USER_ALREADY_EXISTS = 103002
export const PASSWORD_ERROR = 103003

// ==================== 错误码判断工具函数 ====================

/**
 * 判断是否为认证错误
 * 认证错误格式：xx20xx（错误类型为20）
 */
export const isAuthError = (code: number): boolean => {
  // 获取错误类型（中间2位）
  const errorType = Math.floor(code / 100) % 100
  return errorType === 20
}

/**
 * 获取应用码（前2位）
 */
export const getAppCode = (code: number): number => {
  return Math.floor(code / 10000)
}

/**
 * 获取错误类型（中间2位）
 */
export const getErrorType = (code: number): number => {
  return Math.floor(code / 100) % 100
}

/**
 * 获取错误序号（后2位）
 */
export const getErrorNumber = (code: number): number => {
  return code % 100
}

/**
 * 错误类型枚举
 */
export enum ErrorType {
  SYSTEM = 10,   // 系统错误
  AUTH = 20,     // 认证错误
  BUSINESS = 30, // 业务错误
  DATA = 40,     // 数据错误
  NETWORK = 50   // 网络错误
}

/**
 * 应用码枚举
 */
export enum AppCode {
  AUTH = 10,
  ACCOUNT = 20,
  TRANSFER = 30,
  CURRENCY = 40,
  USER = 50,
  MESSAGE = 60,
  COMMON = 99
}
