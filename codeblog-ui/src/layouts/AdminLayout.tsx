import {
  DashboardOutlined,
  LogoutOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons'
import { App, Avatar, Breadcrumb, Dropdown, Layout, Menu, theme } from 'antd'
import { useEffect, useMemo, useState } from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router'
import { authApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { hasPerm } from '@/utils/perm'

const { Header, Sider, Content, Footer } = Layout

/** 侧边菜单项定义（permission 为空表示登录即可见） */
const MENU_ITEMS = [
  { key: '/dashboard', icon: <DashboardOutlined />, label: '工作台' },
  {
    key: '/system',
    icon: <TeamOutlined />,
    label: '系统管理',
    children: [
      { key: '/system/users', label: '用户管理', permission: 'system:user:list' },
      { key: '/system/roles', label: '角色管理', permission: 'system:role:list' },
      { key: '/system/dicts', label: '数据字典', permission: 'system:dict:list' },
    ],
  },
]

/** 面包屑标题映射 */
const BREADCRUMB_MAP: Record<string, string> = {
  '/dashboard': '工作台',
  '/system': '系统管理',
  '/system/users': '用户管理',
  '/system/roles': '角色管理',
  '/system/dicts': '数据字典',
}

export default function AdminLayout() {
  const [collapsed, setCollapsed] = useState(false)
  const location = useLocation()
  const navigate = useNavigate()
  const { message } = App.useApp()
  const { user, clearAuth, setUser, setPermissions } = useAuthStore()
  const { token: themeToken } = theme.useToken()

  // 每次进入后台刷新用户信息与权限集合（角色调整后刷新页面即生效）
  useEffect(() => {
    authApi
      .me()
      .then((info) => {
        setUser({ userId: info.id, username: info.username, nickname: info.nickname })
        setPermissions(info.permissions ?? [])
      })
      .catch(() => {
        // 401 已由拦截器统一处理，此处静默
      })
  }, [setUser, setPermissions])

  // 按权限过滤侧栏：子项全部无权限时隐藏整个目录
  const menuItems = useMemo(() => {
    return MENU_ITEMS.map((item) => {
      if (!('children' in item) || !item.children) {
        return item
      }
      const children = item.children.filter((child) => hasPerm(child.permission))
      if (children.length === 0) {
        return null
      }
      return { ...item, children }
    }).filter(Boolean)
  }, [])

  const selectedKeys = [location.pathname]
  const breadcrumbPaths = location.pathname.split('/').filter(Boolean)
  const breadcrumbItems = [{ title: '首页' }].concat(
    breadcrumbPaths.map((seg, i) => {
      const path = '/' + breadcrumbPaths.slice(0, i + 1).join('/')
      return { title: BREADCRUMB_MAP[path] ?? seg }
    }),
  )

  const handleLogout = async () => {
    try {
      await authApi.logout()
    } catch {
      // 登出接口失败不阻塞本地登出
    }
    clearAuth()
    message.success('已退出登录')
    navigate('/login', { replace: true })
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        collapsible
        collapsed={collapsed}
        onCollapse={setCollapsed}
        theme="light"
        style={{ borderRight: `1px solid ${themeToken.colorBorderSecondary}` }}
      >
        <div
          style={{
            height: 48,
            margin: 12,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontWeight: 700,
            fontSize: collapsed ? 14 : 16,
            color: themeToken.colorPrimary,
          }}
        >
          CodeBlog Cloud
        </div>
        <Menu
          mode="inline"
          selectedKeys={selectedKeys}
          defaultOpenKeys={['/system']}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Header
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            background: themeToken.colorBgContainer,
            borderBottom: `1px solid ${themeToken.colorBorderSecondary}`,
            paddingInline: 24,
          }}
        >
          <Breadcrumb items={breadcrumbItems} />
          <Dropdown
            menu={{
              items: [
                {
                  key: 'logout',
                  icon: <LogoutOutlined />,
                  label: '退出登录',
                  onClick: handleLogout,
                },
              ],
            }}
          >
            <span style={{ display: 'inline-flex', alignItems: 'center', gap: 8, cursor: 'pointer' }}>
              <Avatar size="small" icon={<UserOutlined />} />
              {user?.nickname || user?.username || '未登录'}
            </span>
          </Dropdown>
        </Header>
        <Content style={{ margin: 16 }}>
          <div
            style={{
              padding: 24,
              minHeight: 360,
              background: themeToken.colorBgContainer,
              borderRadius: themeToken.borderRadiusLG,
            }}
          >
            <Outlet />
          </div>
        </Content>
        <Footer style={{ textAlign: 'center', color: themeToken.colorTextDescription }}>
          CodeBlog Cloud · 管理后台
        </Footer>
      </Layout>
    </Layout>
  )
}
