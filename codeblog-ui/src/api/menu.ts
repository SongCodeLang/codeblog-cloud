import { request } from './http'
import type { MenuTreeItem } from '@/types/api'

/** 菜单接口（经网关：/system/menu/** → 系统管理服务 /menu/**） */
export const menuApi = {
  tree: () => request<MenuTreeItem[]>({ url: '/system/menu/tree', method: 'GET' }),
}
