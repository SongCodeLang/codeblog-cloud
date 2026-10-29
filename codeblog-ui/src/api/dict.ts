import { request } from './http'
import type {
  DictItem,
  DictType,
  PageResult,
  UserStatus,
} from '@/types/api'

/** 字典类型保存参数（id 为空=新增；更新时 dictType 编码不可变更） */
export interface DictTypeSaveParams {
  id?: string
  dictName: string
  dictType: string
  status?: UserStatus
  remark?: string
  version?: number
}

/** 字典项保存参数（id 为空=新增；更新时 dictType/itemValue 不可变更） */
export interface DictItemSaveParams {
  id?: string
  dictType: string
  itemLabel: string
  itemValue: string
  sortOrder?: number
  status?: UserStatus
  remark?: string
  version?: number
}

/** 数据字典接口（经网关：/system/dict/**） */
export const dictApi = {
  typePage: async (query: {
    pageNo?: number
    pageSize?: number
    dictName?: string
    dictType?: string
    status?: UserStatus
  }) => {
    const res = await request<PageResult<DictType>>({
      url: '/system/dict/type/page',
      method: 'GET',
      params: query,
    })
    return { list: res.list, total: Number(res.total) }
  },

  typeCreate: (data: DictTypeSaveParams) =>
    request<string>({ url: '/system/dict/type/create', method: 'POST', data }),

  typeUpdate: (data: DictTypeSaveParams) =>
    request<void>({ url: '/system/dict/type/update', method: 'PUT', data }),

  typeRemove: (id: string) =>
    request<void>({ url: `/system/dict/type/delete/${id}`, method: 'DELETE' }),

  itemPage: async (query: {
    pageNo?: number
    pageSize?: number
    dictType?: string
    itemLabel?: string
    status?: UserStatus
  }) => {
    const res = await request<PageResult<DictItem>>({
      url: '/system/dict/item/page',
      method: 'GET',
      params: query,
    })
    return { list: res.list, total: Number(res.total) }
  },

  itemCreate: (data: DictItemSaveParams) =>
    request<string>({ url: '/system/dict/item/create', method: 'POST', data }),

  itemUpdate: (data: DictItemSaveParams) =>
    request<void>({ url: '/system/dict/item/update', method: 'PUT', data }),

  itemRemove: (id: string) =>
    request<void>({ url: `/system/dict/item/delete/${id}`, method: 'DELETE' }),

  /** 按字典类型查启用项（页面下拉数据源） */
  data: (dictType: string) =>
    request<DictItem[]>({ url: `/system/dict/data/${dictType}`, method: 'GET' }),
}
