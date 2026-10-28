import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { LoginResult } from '@/types/api'

interface AuthState {
  /** 访问令牌（请求头 Authorization: Bearer <token>） */
  token: string | null
  /** 登录用户基础信息 */
  user: Pick<LoginResult, 'userId' | 'username' | 'nickname'> | null
  setAuth: (result: LoginResult) => void
  setUser: (user: Pick<LoginResult, 'userId' | 'username' | 'nickname'>) => void
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
      setAuth: (result) =>
        set({
          token: result.token,
          user: { userId: result.userId, username: result.username, nickname: result.nickname },
        }),
      setUser: (user) => set({ user }),
      clearAuth: () => set({ token: null, user: null }),
    }),
    { name: 'codeblog-auth' },
  ),
)
