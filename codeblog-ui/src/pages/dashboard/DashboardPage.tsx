import { Card, Col, Row, Statistic, Typography } from 'antd'
import {
  CloudServerOutlined,
  DatabaseOutlined,
  SafetyCertificateOutlined,
  TeamOutlined,
} from '@ant-design/icons'
import { useQuery } from '@tanstack/react-query'
import { userApi } from '@/api/user'
import { useAuthStore } from '@/stores/auth'

export default function DashboardPage() {
  const user = useAuthStore((s) => s.user)

  const { data: userPage } = useQuery({
    queryKey: ['dashboard', 'user-stats'],
    queryFn: () => userApi.page({ pageNo: 1, pageSize: 1 }),
  })

  return (
    <div>
      <Typography.Title level={4} style={{ marginTop: 0 }}>
        你好，{user?.nickname || user?.username || '管理员'}，欢迎回来！
      </Typography.Title>
      <Typography.Paragraph type="secondary">
        CodeBlog Cloud 微服务平台工作台，当前展示平台基础概览。
      </Typography.Paragraph>

      <Row gutter={16}>
        <Col xs={24} sm={12} lg={6}>
          <Card variant="borderless">
            <Statistic
              title="注册用户"
              value={userPage?.total ?? '-'}
              prefix={<TeamOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card variant="borderless">
            <Statistic title="微服务节点" value={2} prefix={<CloudServerOutlined />} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card variant="borderless">
            <Statistic title="数据源" value={1} prefix={<DatabaseOutlined />} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card variant="borderless">
            <Statistic title="鉴权中心" value="Sa-Token" prefix={<SafetyCertificateOutlined />} />
          </Card>
        </Col>
      </Row>
    </div>
  )
}
