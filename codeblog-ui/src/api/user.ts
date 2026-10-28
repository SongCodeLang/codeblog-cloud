import { request } from './http'
import type {
  PageResult,
  UserCreateParams,
  UserPageQuery,
  UserResetPasswordParams,
  UserUpdateParams,
  UserInfo,
} from '@/types/api'

/** 用户管理接口（经网关：/user/user/** → 用户服务 /user/**） */
export const userApi = {
  page: async (query: UserPageQuery) => {
    const res = await request<PageResult<UserInfo>>({
      url: '/user/user/page',
      method: 'GET',
      params: query,
    })
    // 后端 Long 全局序列化为字符串，此处归一化为 number
    return { list: res.list, total: Number(res.total) }
  },

  get: (id: string) => request<UserInfo>({ url: `/user/user/get/${id}`, method: 'GET' }),

  create: (data: UserCreateParams) =>
    request<string>({ url: '/user/user/create', method: 'POST', data }),

  update: (data: UserUpdateParams) =>
    request<void>({ url: '/user/user/update', method: 'PUT', data }),

  remove: (id: string) => request<void>({ url: `/user/user/delete/${id}`, method: 'DELETE' }),

  resetPassword: (data: UserResetPasswordParams) =>
    request<void>({ url: '/user/user/reset-password', method: 'PUT', data }),
}
