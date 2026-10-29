import { lazy, Suspense, type ReactNode } from 'react'
import { Navigate, createBrowserRouter, useLocation } from 'react-router'
import { Spin } from 'antd'
import { useAuthStore } from '@/stores/auth'

const AdminLayout = lazy(() => import('@/layouts/AdminLayout'))
const LoginPage = lazy(() => import('@/pages/login/LoginPage'))
const DashboardPage = lazy(() => import('@/pages/dashboard/DashboardPage'))
const UserPage = lazy(() => import('@/pages/system/UserPage'))
const RolePage = lazy(() => import('@/pages/system/RolePage'))
const DictPage = lazy(() => import('@/pages/system/DictPage'))
const NotFoundPage = lazy(() => import('@/pages/NotFoundPage'))

/** 路由加载占位 */
function PageLoading() {
  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
      <Spin size="large" />
    </div>
  )
}

/** 登录守卫：未登录跳转登录页 */
function RequireAuth({ children }: { children: ReactNode }) {
  const token = useAuthStore((s) => s.token)
  const location = useLocation()
  if (!token) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }
  return <>{children}</>
}

/** 懒加载包装 */
function withSuspense(node: ReactNode) {
  return <Suspense fallback={<PageLoading />}>{node}</Suspense>
}

export const router = createBrowserRouter([
  {
    path: '/login',
    element: withSuspense(<LoginPage />),
  },
  {
    path: '/',
    element: (
      <RequireAuth>{withSuspense(<AdminLayout />)}</RequireAuth>
    ),
    children: [
      { index: true, element: <Navigate to="/dashboard" replace /> },
      {
        path: 'dashboard',
        element: withSuspense(<DashboardPage />),
      },
      {
        path: 'system/users',
        element: withSuspense(<UserPage />),
      },
      {
        path: 'system/roles',
        element: withSuspense(<RolePage />),
      },
      {
        path: 'system/dicts',
        element: withSuspense(<DictPage />),
      },
    ],
  },
  {
    path: '*',
    element: withSuspense(<NotFoundPage />),
  },
])
