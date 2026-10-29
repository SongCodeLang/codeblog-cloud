import { PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  App,
  Button,
  Card,
  Drawer,
  Form,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Select,
  Space,
  Switch,
  Table,
  Tag,
  Tree,
  type TablePaginationConfig,
} from 'antd'
import { useState } from 'react'
import { ApiError } from '@/api/http'
import { menuApi } from '@/api/menu'
import { roleApi } from '@/api/role'
import type { MenuTreeItem, RoleInfo, RolePageQuery, UserStatus } from '@/types/api'
import type { DataNode } from 'antd/es/tree'
import { hasPerm } from '@/utils/perm'

/** 角色查询表单值 */
interface SearchFormValues {
  roleName?: string
  roleKey?: string
  status?: UserStatus
}

/** 编辑弹窗表单值 */
interface EditorFormValues {
  roleName: string
  roleKey: string
  sortOrder?: number
  enabled?: boolean
  remark?: string
}

const ROLE_QUERY_KEY = ['system', 'roles'] as const

/** 菜单树 → antd Tree 节点 */
function toTreeData(menus: MenuTreeItem[]): DataNode[] {
  return menus.map((menu) => ({
    key: menu.id,
    title: menu.menuName + (menu.permission ? `（${menu.permission}）` : ''),
    children: menu.children.length > 0 ? toTreeData(menu.children) : undefined,
  }))
}

export default function RolePage() {
  const { message } = App.useApp()
  const queryClient = useQueryClient()

  const [query, setQuery] = useState<RolePageQuery>({ pageNo: 1, pageSize: 10 })
  const [searchForm] = Form.useForm<SearchFormValues>()

  // 编辑弹窗
  const [editorOpen, setEditorOpen] = useState(false)
  const [editing, setEditing] = useState<RoleInfo | null>(null)
  const [editorForm] = Form.useForm<EditorFormValues>()

  // 授权抽屉
  const [assignTarget, setAssignTarget] = useState<RoleInfo | null>(null)
  const [checkedMenuIds, setCheckedMenuIds] = useState<string[]>([])

  const { data, isFetching } = useQuery({
    queryKey: [...ROLE_QUERY_KEY, query],
    queryFn: () => roleApi.page(query),
  })

  const { data: menuTree } = useQuery({
    queryKey: ['system', 'menu-tree'],
    queryFn: menuApi.tree,
    enabled: assignTarget !== null,
  })

  const invalidateRoles = () => queryClient.invalidateQueries({ queryKey: ROLE_QUERY_KEY })

  const showError = (error: unknown, fallback: string) =>
    message.error(error instanceof ApiError ? error.message : fallback)

  const createMutation = useMutation({
    mutationFn: roleApi.create,
    onSuccess: () => {
      message.success('创建成功')
      setEditorOpen(false)
      invalidateRoles()
    },
    onError: (error) => showError(error, '创建失败'),
  })

  const updateMutation = useMutation({
    mutationFn: roleApi.update,
    onSuccess: () => {
      message.success('更新成功')
      setEditorOpen(false)
      invalidateRoles()
    },
    onError: (error) => showError(error, '更新失败'),
  })

  const deleteMutation = useMutation({
    mutationFn: roleApi.remove,
    onSuccess: () => {
      message.success('删除成功')
      invalidateRoles()
    },
    onError: (error) => showError(error, '删除失败'),
  })

  const assignMenusMutation = useMutation({
    mutationFn: ({ roleId, menuIds }: { roleId: string; menuIds: string[] }) =>
      roleApi.assignMenus(roleId, menuIds),
    onSuccess: () => {
      message.success('授权已保存')
      setAssignTarget(null)
    },
    onError: (error) => showError(error, '授权失败'),
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

  const openEdit = (record: RoleInfo) => {
    setEditing(record)
    editorForm.setFieldsValue({
      roleName: record.roleName,
      roleKey: record.roleKey,
      sortOrder: record.sortOrder,
      enabled: record.status === 0,
      remark: record.remark,
    })
    setEditorOpen(true)
  }

  const handleEditorSubmit = async () => {
    const values = await editorForm.validateFields()
    const status: UserStatus = values.enabled ? 0 : 1
    if (editing) {
      updateMutation.mutate({
        id: editing.id,
        roleName: values.roleName,
        roleKey: values.roleKey,
        sortOrder: values.sortOrder ?? 0,
        status,
        remark: values.remark,
        version: editing.version,
      })
    } else {
      createMutation.mutate({
        roleName: values.roleName,
        roleKey: values.roleKey,
        sortOrder: values.sortOrder ?? 0,
        status,
        remark: values.remark,
      })
    }
  }

  // 打开授权抽屉：拉取角色当前菜单绑定
  const openAssign = async (record: RoleInfo) => {
    setAssignTarget(record)
    setCheckedMenuIds([])
    try {
      const menuIds = await roleApi.menuIds(record.id)
      setCheckedMenuIds(menuIds)
    } catch (error) {
      showError(error, '查询角色菜单失败')
    }
  }

  const pagination: TablePaginationConfig = {
    current: query.pageNo,
    pageSize: query.pageSize,
    total: data?.total ?? 0,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`,
    onChange: (pageNo, pageSize) => setQuery((prev) => ({ ...prev, pageNo, pageSize })),
  }

  const columns = [
    { title: '角色名称', dataIndex: 'roleName', width: 140 },
    { title: '权限字符', dataIndex: 'roleKey', width: 140 },
    { title: '排序', dataIndex: 'sortOrder', width: 80 },
    {
      title: '状态',
      dataIndex: 'status',
      width: 90,
      render: (status: UserStatus, record: RoleInfo) =>
        status === 0 ? (
          <Tag color="success">{record.statusLabel ?? '启用'}</Tag>
        ) : (
          <Tag color="error">{record.statusLabel ?? '停用'}</Tag>
        ),
    },
    { title: '备注', dataIndex: 'remark', ellipsis: true },
    { title: '创建时间', dataIndex: 'createTime', width: 170 },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_: unknown, record: RoleInfo) => (
        <Space size={4}>
          {hasPerm('system:role:update') && record.roleKey !== 'admin' && (
            <Button type="link" size="small" onClick={() => openEdit(record)}>
              编辑
            </Button>
          )}
          {hasPerm('system:role:assign-menu') && record.roleKey !== 'admin' && (
            <Button type="link" size="small" onClick={() => openAssign(record)}>
              授权
            </Button>
          )}
          {hasPerm('system:role:delete') && record.roleKey !== 'admin' && (
            <Popconfirm
              title="确认删除该角色？"
              onConfirm={() => deleteMutation.mutate(record.id)}
            >
              <Button type="link" size="small" danger>
                删除
              </Button>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ]

  return (
    <div>
      {/* 搜索栏 */}
      <Card size="small" style={{ marginBottom: 16 }}>
        <Form form={searchForm} layout="inline" onFinish={handleSearch}>
          <Form.Item name="roleName">
            <Input placeholder="角色名称" allowClear style={{ width: 160 }} />
          </Form.Item>
          <Form.Item name="roleKey">
            <Input placeholder="权限字符" allowClear style={{ width: 160 }} />
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

      {/* 角色表格 */}
      <Card size="small">
        <div style={{ marginBottom: 12 }}>
          {hasPerm('system:role:create') && (
            <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
              新增角色
            </Button>
          )}
        </div>
        <Table<RoleInfo>
          rowKey="id"
          columns={columns}
          dataSource={data?.list ?? []}
          loading={isFetching}
          pagination={pagination}
        />
      </Card>

      {/* 新增 / 编辑弹窗 */}
      <Modal
        title={editing ? `编辑角色 - ${editing.roleName}` : '新增角色'}
        open={editorOpen}
        onOk={handleEditorSubmit}
        confirmLoading={createMutation.isPending || updateMutation.isPending}
        onCancel={() => setEditorOpen(false)}
        destroyOnHidden
      >
        <Form form={editorForm} layout="vertical" autoComplete="off" initialValues={{ sortOrder: 0, enabled: true }}>
          <Form.Item
            name="roleName"
            label="角色名称"
            rules={[{ required: true, message: '请输入角色名称' }]}
          >
            <Input placeholder="如：运营专员" maxLength={30} />
          </Form.Item>
          <Form.Item
            name="roleKey"
            label="权限字符"
            rules={[
              { required: true, message: '请输入权限字符' },
              { pattern: /^[a-zA-Z0-9_:]+$/, message: '仅支持字母、数字、下划线、冒号' },
            ]}
          >
            <Input placeholder="如：operator" maxLength={60} disabled={editing?.roleKey === 'admin'} />
          </Form.Item>
          <Form.Item name="sortOrder" label="显示顺序">
            <InputNumber min={0} style={{ width: 120 }} />
          </Form.Item>
          <Form.Item name="enabled" label="启用" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea placeholder="选填" maxLength={200} rows={2} />
          </Form.Item>
        </Form>
      </Modal>

      {/* 菜单授权抽屉 */}
      <Drawer
        title={`菜单授权 - ${assignTarget?.roleName ?? ''}`}
        width={420}
        open={assignTarget !== null}
        onClose={() => setAssignTarget(null)}
        extra={
          <Space>
            <Button onClick={() => setAssignTarget(null)}>取消</Button>
            <Button
              type="primary"
              loading={assignMenusMutation.isPending}
              onClick={() =>
                assignTarget &&
                assignMenusMutation.mutate({ roleId: assignTarget.id, menuIds: checkedMenuIds })
              }
            >
              保存
            </Button>
          </Space>
        }
      >
        <Tree
          checkable
          checkStrictly
          selectable={false}
          defaultExpandAll
          treeData={menuTree ? toTreeData(menuTree) : []}
          checkedKeys={{ checked: checkedMenuIds, halfChecked: [] }}
          onCheck={(keys) =>
            setCheckedMenuIds(((keys as { checked: React.Key[] }).checked ?? []).map(String))
          }
        />
      </Drawer>
    </div>
  )
}
