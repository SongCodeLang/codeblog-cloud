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
  Space,
  Switch,
  Table,
  Tag,
  type TablePaginationConfig,
} from 'antd'
import { useState } from 'react'
import { ApiError } from '@/api/http'
import { dictApi, type DictItemSaveParams, type DictTypeSaveParams } from '@/api/dict'
import type { DictItem, DictType, UserStatus } from '@/types/api'
import { hasPerm } from '@/utils/perm'

/** 字典类型查询表单值 */
interface SearchFormValues {
  dictName?: string
  dictType?: string
}

/** 类型编辑表单值 */
interface TypeFormValues {
  dictName: string
  dictType: string
  enabled?: boolean
  remark?: string
}

/** 字典项编辑表单值 */
interface ItemFormValues {
  itemLabel: string
  itemValue: string
  sortOrder?: number
  enabled?: boolean
  remark?: string
}

const DICT_TYPE_QUERY_KEY = ['system', 'dict-types'] as const

export default function DictPage() {
  const { message } = App.useApp()
  const queryClient = useQueryClient()

  const [query, setQuery] = useState({ pageNo: 1, pageSize: 10 })
  const [searchForm] = Form.useForm<SearchFormValues>()

  // 类型编辑弹窗
  const [typeEditorOpen, setTypeEditorOpen] = useState(false)
  const [editingType, setEditingType] = useState<DictType | null>(null)
  const [typeForm] = Form.useForm<TypeFormValues>()

  // 字典项抽屉
  const [itemTarget, setItemTarget] = useState<DictType | null>(null)
  const [itemQuery, setItemQuery] = useState({ pageNo: 1, pageSize: 10 })
  const [itemEditorOpen, setItemEditorOpen] = useState(false)
  const [editingItem, setEditingItem] = useState<DictItem | null>(null)
  const [itemForm] = Form.useForm<ItemFormValues>()

  const showError = (error: unknown, fallback: string) => {
    message.error(error instanceof ApiError ? error.message : fallback)
  }

  const { data, isFetching } = useQuery({
    queryKey: [...DICT_TYPE_QUERY_KEY, query],
    queryFn: () => dictApi.typePage(query),
  })

  const { data: itemData, isFetching: itemFetching } = useQuery({
    queryKey: ['system', 'dict-items', itemTarget?.dictType, itemQuery],
    queryFn: () => dictApi.itemPage({ ...itemQuery, dictType: itemTarget!.dictType }),
    enabled: itemTarget !== null,
  })

  const typeSaveMutation = useMutation({
    mutationFn: async (data: DictTypeSaveParams) => {
      await (data.id ? dictApi.typeUpdate(data) : dictApi.typeCreate(data))
    },
    onSuccess: () => {
      message.success('字典类型已保存')
      setTypeEditorOpen(false)
      queryClient.invalidateQueries({ queryKey: DICT_TYPE_QUERY_KEY })
    },
    onError: (error) => showError(error, '保存失败'),
  })

  const typeRemoveMutation = useMutation({
    mutationFn: dictApi.typeRemove,
    onSuccess: () => {
      message.success('字典类型已删除')
      queryClient.invalidateQueries({ queryKey: DICT_TYPE_QUERY_KEY })
    },
    onError: (error) => showError(error, '删除失败'),
  })

  const itemSaveMutation = useMutation({
    mutationFn: async (data: DictItemSaveParams) => {
      await (data.id ? dictApi.itemUpdate(data) : dictApi.itemCreate(data))
    },
    onSuccess: () => {
      message.success('字典项已保存')
      setItemEditorOpen(false)
      queryClient.invalidateQueries({ queryKey: ['system', 'dict-items'] })
    },
    onError: (error) => showError(error, '保存失败'),
  })

  const itemRemoveMutation = useMutation({
    mutationFn: dictApi.itemRemove,
    onSuccess: () => {
      message.success('字典项已删除')
      queryClient.invalidateQueries({ queryKey: ['system', 'dict-items'] })
    },
    onError: (error) => showError(error, '删除失败'),
  })

  const openTypeCreate = () => {
    setEditingType(null)
    typeForm.resetFields()
    setTypeEditorOpen(true)
  }

  const openTypeEdit = (record: DictType) => {
    setEditingType(record)
    typeForm.setFieldsValue({
      dictName: record.dictName,
      dictType: record.dictType,
      enabled: record.status === 0,
      remark: record.remark,
    })
    setTypeEditorOpen(true)
  }

  const handleTypeSubmit = async () => {
    const values = await typeForm.validateFields()
    const status: UserStatus = values.enabled ? 0 : 1
    if (editingType) {
      typeSaveMutation.mutate({
        id: editingType.id,
        dictName: values.dictName,
        dictType: editingType.dictType,
        status,
        remark: values.remark,
        version: editingType.version,
      })
    } else {
      typeSaveMutation.mutate({
        dictName: values.dictName,
        dictType: values.dictType,
        status,
        remark: values.remark,
      })
    }
  }

  const openItemCreate = () => {
    setEditingItem(null)
    itemForm.resetFields()
    setItemEditorOpen(true)
  }

  const openItemEdit = (record: DictItem) => {
    setEditingItem(record)
    itemForm.setFieldsValue({
      itemLabel: record.itemLabel,
      itemValue: record.itemValue,
      sortOrder: record.sortOrder,
      enabled: record.status === 0,
      remark: record.remark,
    })
    setItemEditorOpen(true)
  }

  const handleItemSubmit = async () => {
    const values = await itemForm.validateFields()
    const status: UserStatus = values.enabled ? 0 : 1
    if (editingItem) {
      itemSaveMutation.mutate({
        id: editingItem.id,
        dictType: editingItem.dictType,
        itemLabel: values.itemLabel,
        itemValue: editingItem.itemValue,
        sortOrder: values.sortOrder ?? 0,
        status,
        remark: values.remark,
        version: editingItem.version,
      })
    } else {
      itemSaveMutation.mutate({
        dictType: itemTarget!.dictType,
        itemLabel: values.itemLabel,
        itemValue: values.itemValue,
        sortOrder: values.sortOrder ?? 0,
        status,
        remark: values.remark,
      })
    }
  }

  const typeColumns = [
    { title: 'ID', dataIndex: 'id', width: 70 },
    { title: '字典名称', dataIndex: 'dictName', width: 160 },
    { title: '字典类型编码', dataIndex: 'dictType', width: 180 },
    {
      title: '状态',
      dataIndex: 'status',
      width: 80,
      render: (status: UserStatus) =>
        status === 0 ? <Tag color="success">启用</Tag> : <Tag color="error">停用</Tag>,
    },
    { title: '备注', dataIndex: 'remark', ellipsis: true },
    { title: '创建时间', dataIndex: 'createTime', width: 170 },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_: unknown, record: DictType) => (
        <Space size={4}>
          {hasPerm('system:dict:query') && (
            <Button
              type="link"
              size="small"
              onClick={() => {
                setItemTarget(record)
                setItemQuery({ pageNo: 1, pageSize: 10 })
              }}
            >
              字典项
            </Button>
          )}
          {hasPerm('system:dict:update') && (
            <Button type="link" size="small" onClick={() => openTypeEdit(record)}>
              编辑
            </Button>
          )}
          {hasPerm('system:dict:delete') && (
            <Popconfirm
              title="确认删除该字典类型？"
              description="类型下有字典项时无法删除"
              onConfirm={() => typeRemoveMutation.mutate(record.id)}
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

  const itemColumns = [
    { title: 'ID', dataIndex: 'id', width: 70 },
    { title: '字典标签', dataIndex: 'itemLabel', width: 140 },
    { title: '字典值', dataIndex: 'itemValue', width: 100 },
    { title: '排序', dataIndex: 'sortOrder', width: 70 },
    {
      title: '状态',
      dataIndex: 'status',
      width: 80,
      render: (status: UserStatus) =>
        status === 0 ? <Tag color="success">启用</Tag> : <Tag color="error">停用</Tag>,
    },
    { title: '备注', dataIndex: 'remark', ellipsis: true },
    {
      title: '操作',
      key: 'action',
      width: 140,
      render: (_: unknown, record: DictItem) => (
        <Space size={4}>
          {hasPerm('system:dict:update') && (
            <Button type="link" size="small" onClick={() => openItemEdit(record)}>
              编辑
            </Button>
          )}
          {hasPerm('system:dict:delete') && (
            <Popconfirm
              title="确认删除该字典项？"
              onConfirm={() => itemRemoveMutation.mutate(record.id)}
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

  const pagination: TablePaginationConfig = {
    current: query.pageNo,
    pageSize: query.pageSize,
    total: data?.total ?? 0,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`,
    onChange: (pageNo, pageSize) => setQuery((prev) => ({ ...prev, pageNo, pageSize })),
  }

  return (
    <div>
      <Card size="small" style={{ marginBottom: 12 }}>
        <Form form={searchForm} layout="inline" onFinish={() => setQuery((prev) => ({ ...prev, pageNo: 1, ...searchForm.getFieldsValue() }))}>
          <Form.Item name="dictName" label="字典名称">
            <Input placeholder="请输入字典名称" allowClear />
          </Form.Item>
          <Form.Item name="dictType" label="类型编码">
            <Input placeholder="请输入类型编码" allowClear />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>
                查询
              </Button>
              <Button icon={<ReloadOutlined />} onClick={() => { searchForm.resetFields(); setQuery({ pageNo: 1, pageSize: 10 }) }}>
                重置
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>

      <Card size="small">
        <div style={{ marginBottom: 12 }}>
          {hasPerm('system:dict:create') && (
            <Button type="primary" icon={<PlusOutlined />} onClick={openTypeCreate}>
              新增字典类型
            </Button>
          )}
        </div>
        <Table<DictType>
          rowKey="id"
          loading={isFetching}
          columns={typeColumns}
          dataSource={data?.list ?? []}
          pagination={pagination}
        />
      </Card>

      {/* 字典类型编辑弹窗 */}
      <Modal
        title={editingType ? '编辑字典类型' : '新增字典类型'}
        open={typeEditorOpen}
        onOk={handleTypeSubmit}
        onCancel={() => setTypeEditorOpen(false)}
        confirmLoading={typeSaveMutation.isPending}
      >
        <Form form={typeForm} labelCol={{ span: 6 }}>
          <Form.Item name="dictName" label="字典名称" rules={[{ required: true, message: '请输入字典名称' }]}>
            <Input placeholder="如：通用状态" maxLength={60} />
          </Form.Item>
          <Form.Item
            name="dictType"
            label="类型编码"
            rules={[
              { required: true, message: '请输入类型编码' },
              { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '仅支持字母、数字、下划线，且以字母开头' },
            ]}
          >
            <Input placeholder="如：sys_common_status" maxLength={60} disabled={!!editingType} />
          </Form.Item>
          <Form.Item name="enabled" label="启用" valuePropName="checked" initialValue={true}>
            <Switch />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={2} maxLength={200} />
          </Form.Item>
        </Form>
      </Modal>

      {/* 字典项抽屉 */}
      <Drawer
        title={`字典项 - ${itemTarget?.dictName ?? ''}（${itemTarget?.dictType ?? ''}）`}
        open={itemTarget !== null}
        onClose={() => setItemTarget(null)}
        size="large"
        destroyOnHidden
      >
        <div style={{ marginBottom: 12 }}>
          {hasPerm('system:dict:create') && (
            <Button type="primary" icon={<PlusOutlined />} onClick={openItemCreate}>
              新增字典项
            </Button>
          )}
        </div>
        <Table<DictItem>
          rowKey="id"
          size="small"
          loading={itemFetching}
          columns={itemColumns}
          dataSource={itemData?.list ?? []}
          pagination={{
            current: itemQuery.pageNo,
            pageSize: itemQuery.pageSize,
            total: itemData?.total ?? 0,
            onChange: (pageNo, pageSize) => setItemQuery((prev) => ({ ...prev, pageNo, pageSize })),
          }}
        />
      </Drawer>

      {/* 字典项编辑弹窗 */}
      <Modal
        title={editingItem ? '编辑字典项' : '新增字典项'}
        open={itemEditorOpen}
        onOk={handleItemSubmit}
        onCancel={() => setItemEditorOpen(false)}
        confirmLoading={itemSaveMutation.isPending}
      >
        <Form form={itemForm} labelCol={{ span: 6 }}>
          <Form.Item name="itemLabel" label="字典标签" rules={[{ required: true, message: '请输入字典标签' }]}>
            <Input placeholder="展示文案" maxLength={100} />
          </Form.Item>
          <Form.Item name="itemValue" label="字典值" rules={[{ required: true, message: '请输入字典值' }]}>
            <Input placeholder="存储值" maxLength={60} disabled={!!editingItem} />
          </Form.Item>
          <Form.Item name="sortOrder" label="排序">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="enabled" label="启用" valuePropName="checked" initialValue={true}>
            <Switch />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={2} maxLength={200} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
