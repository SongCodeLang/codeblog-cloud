import { request } from './http'
import type {
  PageResult,
  RoleCreateParams,
  RoleInfo,
  RolePageQuery,
  RoleUpdateParams,
} from '@/types/api'

/** 角色管理接口（经网关：/system/role/** → 系统管理服务 /role/**） */
export const roleApi = {
  page: async (query: RolePageQuery) => {
    const res = await request<PageResult<RoleInfo>>({
      url: '/system/role/page',
      method: 'GET',
      params: query,
    })
    return { list: res.list, total: Number(res.total) }
  },

  list: () => request<RoleInfo[]>({ url: '/system/role/list', method: 'GET' }),

  create: (data: RoleCreateParams) =>
    request<string>({ url: '/system/role/create', method: 'POST', data }),

  update: (data: RoleUpdateParams) =>
    request<void>({ url: '/system/role/update', method: 'PUT', data }),

  remove: (id: string) => request<void>({ url: `/system/role/delete/${id}`, method: 'DELETE' }),

  menuIds: (roleId: string) =>
    request<string[]>({ url: `/system/role/menu-ids/${roleId}`, method: 'GET' }),

  assignMenus: (roleId: string, menuIds: string[]) =>
    request<void>({ url: `/system/role/assign-menus/${roleId}`, method: 'PUT', data: menuIds }),
}
