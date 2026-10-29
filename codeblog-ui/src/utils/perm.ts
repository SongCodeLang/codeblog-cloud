import { useAuthStore } from '@/stores/auth'

/**
 * 判断当前登录用户是否拥有指定权限。
 * permissions 含通配 "*"（admin）时恒为 true；未传或为空数组时仅放行无权限要求的场景。
 */
export function hasPerm(permission?: string): boolean {
  if (!permission) {
    return true
  }
  const { permissions } = useAuthStore.getState()
  return permissions.includes('*') || permissions.includes(permission)
}

/** 全部权限满足才为 true */
export function hasAllPerms(...perms: string[]): boolean {
  return perms.every((p) => hasPerm(p))
}

/** 任一权限满足即 true */
export function hasAnyPerm(...perms: string[]): boolean {
  return perms.some((p) => hasPerm(p))
}
