import axios, { AxiosError, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { useAuthStore } from '@/stores/auth'
import type { ApiResult } from '@/types/api'

/** 业务错误（后端 Result.code 非 200） */
export class ApiError extends Error {
  code: number

  constructor(code: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.code = code
  }
}

const UNAUTHORIZED_CODES = [401]

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '',
  timeout: 15000,
})

// 请求拦截：自动携带 token
http.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/**
 * 响应拦截：解包 Result<T>（运行时返回 data 本体，类型上对齐拦截器签名）；
 * 统一处理 401（网关鉴权失败 / 登录过期）。
 */
http.interceptors.response.use(
  (response): AxiosResponse => {
    const result = response.data as ApiResult<unknown>
    // 非 Result 结构（如文件流）原样返回
    if (result === null || typeof result !== 'object' || typeof result.code !== 'number') {
      return response
    }
    if (result.code === 200) {
      return result.data as unknown as AxiosResponse
    }
    if (UNAUTHORIZED_CODES.includes(result.code)) {
      handleUnauthorized()
      return Promise.reject(new ApiError(result.code, '账号未登录或登录已过期')) as never
    }
    return Promise.reject(new ApiError(result.code, result.message || '操作失败')) as never
  },
  (error: AxiosError<ApiResult<unknown>>) => {
    const status = error.response?.status
    const bizMessage = error.response?.data?.message
    if (status === 401) {
      handleUnauthorized()
      return Promise.reject(new ApiError(401, '账号未登录或登录已过期'))
    }
    return Promise.reject(
      new ApiError(status ?? -1, bizMessage || error.message || '网络异常，请稍后重试'),
    )
  },
)

function handleUnauthorized() {
  useAuthStore.getState().clearAuth()
  // 避免在登录页死循环跳转
  if (!window.location.pathname.startsWith('/login')) {
    window.location.assign('/login')
  }
}

/**
 * 发起请求并直接返回 Result.data（业务失败抛 ApiError）
 */
export async function request<T>(config: AxiosRequestConfig): Promise<T> {
  // 拦截器已在运行时解包为 data，此处对齐类型
  return (await http.request(config)) as unknown as T
}

export default http
