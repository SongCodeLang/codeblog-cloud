import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { LoginResult } from '@/types/api'

interface AuthState {
  /** 访问令牌（请求头 Authorization: Bearer <token>） */
  token: string | null
  /** 登录用户基础信息 */
  user: Pick<LoginResult, 'userId' | 'username' | 'nickname'> | null
  /** 权限标识集合（admin 为 ['*']） */
  permissions: string[]
  setAuth: (result: LoginResult) => void
  setUser: (user: Pick<LoginResult, 'userId' | 'username' | 'nickname'>) => void
  setPermissions: (permissions: string[]) => void
  clearAuth: () => void
}

/**
 * 登录态（zustand + localStorage 持久化）
 */
export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      user: null,
      permissions: [],
      setAuth: (result) =>
        set({
          token: result.token,
          user: { userId: result.userId, username: result.username, nickname: result.nickname },
          permissions: result.permissions ?? [],
        }),
      setUser: (user) => set({ user }),
      setPermissions: (permissions) => set({ permissions }),
      clearAuth: () => set({ token: null, user: null, permissions: [] }),
    }),
    { name: 'codeblog-auth' },
  ),
)
