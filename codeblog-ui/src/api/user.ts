import { request } from './http'
import type {
  PageResult,
  UserCreateParams,
  UserPageQuery,
  UserResetPasswordParams,
  UserUpdateParams,
  UserInfo,
} from '@/types/api'

/** 用户管理接口（经网关：/system/user/** → 系统管理服务 /user/**） */
export const userApi = {
  page: async (query: UserPageQuery) => {
    const res = await request<PageResult<UserInfo>>({
      url: '/system/user/page',
      method: 'GET',
      params: query,
    })
    // 后端 Long 全局序列化为字符串，此处归一化为 number
    return { list: res.list, total: Number(res.total) }
  },

  get: (id: string) => request<UserInfo>({ url: `/system/user/get/${id}`, method: 'GET' }),

  create: (data: UserCreateParams) =>
    request<string>({ url: '/system/user/create', method: 'POST', data }),

  update: (data: UserUpdateParams) =>
    request<void>({ url: '/system/user/update', method: 'PUT', data }),

  remove: (id: string) => request<void>({ url: `/system/user/delete/${id}`, method: 'DELETE' }),

  resetPassword: (data: UserResetPasswordParams) =>
    request<void>({ url: '/system/user/reset-password', method: 'PUT', data }),

  roleIds: (userId: string) =>
    request<string[]>({ url: `/system/user/role-ids/${userId}`, method: 'GET' }),

  assignRoles: (userId: string, roleIds: string[]) =>
    request<void>({ url: `/system/user/assign-roles/${userId}`, method: 'PUT', data: roleIds }),
}
