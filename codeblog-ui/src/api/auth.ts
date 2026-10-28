import { request } from './http'
import type { LoginParams, LoginResult, UserInfo } from '@/types/api'

/** 认证相关接口（经网关：/system/** → 系统管理服务 /auth/**） */
export const authApi = {
  login: (data: LoginParams) =>
    request<LoginResult>({ url: '/system/auth/login', method: 'POST', data }),

  logout: () => request<void>({ url: '/system/auth/logout', method: 'POST' }),

  me: () => request<UserInfo>({ url: '/system/auth/me' }),
}
