import { LockOutlined, UserOutlined } from '@ant-design/icons'
import { App, Button, Card, Form, Input } from 'antd'
import { useNavigate } from 'react-router'
import { authApi } from '@/api/auth'
import { ApiError } from '@/api/http'
import { useAuthStore } from '@/stores/auth'
import type { LoginParams } from '@/types/api'

export default function LoginPage() {
  const navigate = useNavigate()
  const { message } = App.useApp()
  const setAuth = useAuthStore((s) => s.setAuth)
  const [form] = Form.useForm<LoginParams>()

  const handleSubmit = async (values: LoginParams) => {
    try {
      const result = await authApi.login(values)
      setAuth(result)
      message.success(`欢迎回来，${result.nickname}`)
      navigate('/dashboard', { replace: true })
    } catch (error) {
      message.error(error instanceof ApiError ? error.message : '登录失败，请稍后重试')
    }
  }

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'linear-gradient(135deg, #f5f7fa 0%, #e4ebf5 100%)',
      }}
    >
      <Card style={{ width: 400, boxShadow: '0 4px 24px rgba(0, 0, 0, 0.08)' }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <h2 style={{ margin: '8px 0 4px' }}>CodeBlog Cloud</h2>
          <p style={{ color: '#999', margin: 0 }}>微服务管理平台</p>
        </div>
        <Form form={form} layout="vertical" onFinish={handleSubmit} autoComplete="off">
          <Form.Item
            name="username"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input prefix={<UserOutlined />} placeholder="用户名" size="large" />
          </Form.Item>
          <Form.Item
            name="password"
            rules={[
              { required: true, message: '请输入密码' },
              { min: 6, max: 32, message: '密码长度需在 6~32 位之间' },
            ]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="密码" size="large" />
          </Form.Item>
          <Form.Item style={{ marginBottom: 0 }}>
            <Button type="primary" htmlType="submit" block size="large">
              登 录
            </Button>
          </Form.Item>
        </Form>
        <p style={{ textAlign: 'center', color: '#bbb', marginTop: 16, marginBottom: 0, fontSize: 12 }}>
          默认账号：admin / admin123
        </p>
      </Card>
    </div>
  )
}
