import { request } from './http'
import type { LoginParams, LoginResult, UserInfo } from '@/types/api'

/** 认证相关接口（经网关：/user/** → 用户服务 /auth/**） */
export const authApi = {
  login: (data: LoginParams) =>
    request<LoginResult>({ url: '/user/auth/login', method: 'POST', data }),

  logout: () => request<void>({ url: '/user/auth/logout', method: 'POST' }),

  me: () => request<UserInfo>({ url: '/user/auth/me' }),
}
