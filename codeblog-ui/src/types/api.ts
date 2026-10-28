/**
 * 与后端 site.codeblog.common.core.result 对齐的通用类型
 */

/** 统一响应结构 Result<T> */
export interface ApiResult<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

/** 分页响应 PageResult<T> */
export interface PageResult<T> {
  list: T[]
  total: number
}

/** 分页请求基础参数 */
export interface PageParam {
  pageNo: number
  pageSize: number
}

/** 用户状态：0 启用，1 停用 */
export type UserStatus = 0 | 1

/** 用户信息 */
export interface UserInfo {
  id: string
  username: string
  nickname: string
  email?: string
  mobile?: string
  status: UserStatus
  createTime?: string
}

/** 登录请求参数 */
export interface LoginParams {
  username: string
  password: string
}

/** 登录响应 */
export interface LoginResult {
  token: string
  userId: string
  username: string
  nickname: string
}

/** 用户分页查询参数 */
export interface UserPageQuery extends Partial<PageParam> {
  username?: string
  nickname?: string
  status?: UserStatus
}

/** 创建用户参数 */
export interface UserCreateParams {
  username: string
  password: string
  nickname: string
  email?: string
  mobile?: string
}

/** 更新用户参数 */
export interface UserUpdateParams {
  id: string
  nickname: string
  email?: string
  mobile?: string
  status: UserStatus
}

/** 重置密码参数 */
export interface UserResetPasswordParams {
  id: string
  password: string
}
