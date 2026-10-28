import { PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  App,
  Button,
  Card,
  Form,
  Input,
  Modal,
  Popconfirm,
  Select,
  Space,
  Table,
  Tag,
  type TablePaginationConfig,
} from 'antd'
import { useEffect, useState } from 'react'
import { userApi } from '@/api/user'
import { ApiError } from '@/api/http'
import type { UserInfo, UserPageQuery, UserStatus } from '@/types/api'

/** 用户查询表单值 */
interface SearchFormValues {
  username?: string
  nickname?: string
  status?: UserStatus
}

/** 编辑弹窗表单值 */
interface EditorFormValues {
  username?: string
  password?: string
  nickname: string
  email?: string
  mobile?: string
  status: UserStatus
}

const USER_QUERY_KEY = ['system', 'users'] as const

export default function UserPage() {
  const { message } = App.useApp()
  const queryClient = useQueryClient()

  const [query, setQuery] = useState<UserPageQuery>({ pageNo: 1, pageSize: 10 })
  const [searchForm] = Form.useForm<SearchFormValues>()

  // 编辑弹窗状态：null 关闭；mode= create / edit
  const [editorOpen, setEditorOpen] = useState(false)
  const [editing, setEditing] = useState<UserInfo | null>(null)
  const [editorForm] = Form.useForm<EditorFormValues>()

  // 重置密码弹窗
  const [resetTarget, setResetTarget] = useState<UserInfo | null>(null)
  const [resetForm] = Form.useForm<{ password: string }>()

  const { data, isFetching } = useQuery({
    queryKey: [...USER_QUERY_KEY, query],
    queryFn: () => userApi.page(query),
  })

  const invalidateUsers = () => queryClient.invalidateQueries({ queryKey: USER_QUERY_KEY })

  const showError = (error: unknown, fallback: string) =>
    message.error(error instanceof ApiError ? error.message : fallback)

  const createMutation = useMutation({
    mutationFn: userApi.create,
    onSuccess: () => {
      message.success('创建成功')
      setEditorOpen(false)
      invalidateUsers()
    },
    onError: (error) => showError(error, '创建失败'),
  })

  const updateMutation = useMutation({
    mutationFn: userApi.update,
    onSuccess: () => {
      message.success('更新成功')
      setEditorOpen(false)
      invalidateUsers()
    },
    onError: (error) => showError(error, '更新失败'),
  })

  const deleteMutation = useMutation({
    mutationFn: userApi.remove,
    onSuccess: () => {
      message.success('删除成功')
      invalidateUsers()
    },
    onError: (error) => showError(error, '删除失败'),
  })

  const resetPasswordMutation = useMutation({
    mutationFn: userApi.resetPassword,
    onSuccess: () => {
      message.success('密码已重置')
      setResetTarget(null)
    },
    onError: (error) => showError(error, '重置失败'),
  })

  const handleSearch = () => {
    const values = searchForm.getFieldsValue()
    setQuery((prev) => ({ ...prev, pageNo: 1, ...values }))
  }

  const handleReset = () => {
    searchForm.resetFields()
    handleSearch()
  }

  const openCreate = () => {
    setEditing(null)
    editorForm.resetFields()
    setEditorOpen(true)
  }

  const openEdit = (record: UserInfo) => {
    setEditing(record)
    editorForm.setFieldsValue({
      nickname: record.nickname,
      email: record.email,
      mobile: record.mobile,
      status: record.status,
    })
    setEditorOpen(true)
  }

  const handleEditorSubmit = async () => {
    const values = await editorForm.validateFields()
    if (editing) {
      updateMutation.mutate({ ...values, id: editing.id })
    } else {
      createMutation.mutate({
        username: values.username!,
        password: values.password!,
        nickname: values.nickname,
        email: values.email,
        mobile: values.mobile,
      })
    }
  }

  const handleResetSubmit = async () => {
    const values = await resetForm.validateFields()
    if (resetTarget) {
      resetPasswordMutation.mutate({ id: resetTarget.id, password: values.password })
    }
  }

  // 关闭重置弹窗时清空表单
  useEffect(() => {
    if (!resetTarget) {
      resetForm.resetFields()
    }
  }, [resetTarget, resetForm])

  const pagination: TablePaginationConfig = {
    current: query.pageNo,
    pageSize: query.pageSize,
    total: data?.total ?? 0,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`,
    onChange: (pageNo, pageSize) => setQuery((prev) => ({ ...prev, pageNo, pageSize })),
  }

  const columns = [
    { title: '用户名', dataIndex: 'username', width: 140 },
    { title: '昵称', dataIndex: 'nickname', width: 140 },
    { title: '邮箱', dataIndex: 'email', ellipsis: true },
    { title: '手机号', dataIndex: 'mobile', width: 130 },
    {
      title: '状态',
      dataIndex: 'status',
      width: 90,
      render: (status: UserStatus) =>
        status === 0 ? <Tag color="success">启用</Tag> : <Tag color="error">停用</Tag>,
    },
    { title: '创建时间', dataIndex: 'createTime', width: 170 },
    {
      title: '操作',
      key: 'action',
      width: 220,
      render: (_: unknown, record: UserInfo) => (
        <Space size={4}>
          <Button type="link" size="small" onClick={() => openEdit(record)}>
            编辑
          </Button>
          <Button type="link" size="small" onClick={() => setResetTarget(record)}>
            重置密码
          </Button>
          <Popconfirm
            title="确认删除该用户？"
            description={`将删除用户「${record.nickname}」，删除后不可恢复`}
            onConfirm={() => deleteMutation.mutate(record.id)}
          >
            <Button type="link" size="small" danger>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <div>
      {/* 搜索栏 */}
      <Card size="small" style={{ marginBottom: 16 }}>
        <Form form={searchForm} layout="inline" onFinish={handleSearch}>
          <Form.Item name="username">
            <Input placeholder="用户名" allowClear style={{ width: 160 }} />
          </Form.Item>
          <Form.Item name="nickname">
            <Input placeholder="昵称" allowClear style={{ width: 160 }} />
          </Form.Item>
          <Form.Item name="status">
            <Select
              placeholder="状态"
              allowClear
              style={{ width: 110 }}
              options={[
                { value: 0, label: '启用' },
                { value: 1, label: '停用' },
              ]}
            />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>
                查询
              </Button>
              <Button icon={<ReloadOutlined />} onClick={handleReset}>
                重置
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>

      {/* 用户表格 */}
      <Card size="small">
        <div style={{ marginBottom: 12 }}>
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            新增用户
          </Button>
        </div>
        <Table<UserInfo>
          rowKey="id"
          columns={columns}
          dataSource={data?.list ?? []}
          loading={isFetching}
          pagination={pagination}
        />
      </Card>

      {/* 新增 / 编辑弹窗 */}
      <Modal
        title={editing ? `编辑用户 - ${editing.username}` : '新增用户'}
        open={editorOpen}
        onOk={handleEditorSubmit}
        confirmLoading={createMutation.isPending || updateMutation.isPending}
        onCancel={() => setEditorOpen(false)}
        destroyOnHidden
      >
        <Form form={editorForm} layout="vertical" autoComplete="off">
          {!editing && (
            <>
              <Form.Item
                name="username"
                label="用户名"
                rules={[
                  { required: true, message: '请输入用户名' },
                  { min: 3, max: 30, message: '长度需在 3~30 位之间' },
                  {
                    pattern: /^[a-zA-Z0-9_]+$/,
                    message: '仅支持字母、数字、下划线',
                  },
                ]}
              >
                <Input placeholder="登录账号" />
              </Form.Item>
              <Form.Item
                name="password"
                label="初始密码"
                rules={[
                  { required: true, message: '请输入初始密码' },
                  { min: 6, max: 32, message: '密码长度需在 6~32 位之间' },
                ]}
              >
                <Input.Password placeholder="6~32 位" />
              </Form.Item>
            </>
          )}
          <Form.Item
            name="nickname"
            label="昵称"
            rules={[{ required: true, message: '请输入昵称' }]}
          >
            <Input placeholder="展示昵称" maxLength={30} />
          </Form.Item>
          <Form.Item
            name="email"
            label="邮箱"
            rules={[{ type: 'email', message: '邮箱格式不正确' }]}
          >
            <Input placeholder="user@example.com" maxLength={100} />
          </Form.Item>
          <Form.Item
            name="mobile"
            label="手机号"
            rules={[{ pattern: /^$|^1[3-9]\d{9}$/, message: '手机号格式不正确' }]}
          >
            <Input placeholder="选填" maxLength={11} />
          </Form.Item>
          {editing && (
            <Form.Item
              name="status"
              label="状态"
              rules={[{ required: true, message: '请选择状态' }]}
            >
              <Select
                options={[
                  { value: 0, label: '启用' },
                  { value: 1, label: '停用' },
                ]}
              />
            </Form.Item>
          )}
        </Form>
      </Modal>

      {/* 重置密码弹窗 */}
      <Modal
        title={`重置密码 - ${resetTarget?.username ?? ''}`}
        open={resetTarget !== null}
        onOk={handleResetSubmit}
        confirmLoading={resetPasswordMutation.isPending}
        onCancel={() => setResetTarget(null)}
        destroyOnHidden
      >
        <Form form={resetForm} layout="vertical" autoComplete="off">
          <Form.Item
            name="password"
            label="新密码"
            rules={[
              { required: true, message: '请输入新密码' },
              { min: 6, max: 32, message: '密码长度需在 6~32 位之间' },
            ]}
          >
            <Input.Password placeholder="6~32 位" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
