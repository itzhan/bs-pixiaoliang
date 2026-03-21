<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import {
  getAnnouncements,
  createAnnouncement,
  updateAnnouncement,
  publishAnnouncement,
  deleteAnnouncement,
} from '@/api/announcement'

/* ---------- Type / Status maps ---------- */
const typeMap: Record<string, { label: string; color: string }> = {
  NOTICE: { label: '通知', color: 'blue' },
  MAINTENANCE: { label: '维护', color: 'orange' },
  PROMOTION: { label: '推广', color: 'green' },
}

const statusMap: Record<number, { label: string; color: string }> = {
  0: { label: '草稿', color: 'default' },
  1: { label: '已发布', color: 'success' },
}

/* ---------- Search ---------- */
const searchForm = reactive({
  status: undefined as number | undefined,
  keyword: '',
})

/* ---------- Table ---------- */
const loading = ref(false)
const dataSource = ref<any[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const columns = [
  { title: 'ID', dataIndex: 'id', width: 70 },
  { title: '标题', dataIndex: 'title', ellipsis: true },
  { title: '类型', dataIndex: 'announcementType', width: 100 },
  { title: '状态', dataIndex: 'status', width: 100 },
  { title: '发布时间', dataIndex: 'publishedAt', width: 170 },
  { title: '创建时间', dataIndex: 'createdAt', width: 170 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' as const },
]

async function fetchData() {
  loading.value = true
  try {
    const res = await getAnnouncements({
      page: pagination.current,
      size: pagination.pageSize,
      status: searchForm.status,
      keyword: searchForm.keyword || undefined,
    })
    dataSource.value = res.data.records
    pagination.total = res.data.total
  } catch {
    /* handled by interceptor */
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

function handleSearch() {
  pagination.current = 1
  fetchData()
}

function handleReset() {
  searchForm.status = undefined
  searchForm.keyword = ''
  pagination.current = 1
  fetchData()
}

/* ---------- Add / Edit Modal ---------- */
const modalVisible = ref(false)
const modalTitle = ref('新增公告')
const confirmLoading = ref(false)
const formRef = ref()
const editingId = ref<number | null>(null)

const formState = reactive({
  title: '',
  content: '',
  announcementType: 'NOTICE',
})

function openAdd() {
  editingId.value = null
  modalTitle.value = '新增公告'
  formState.title = ''
  formState.content = ''
  formState.announcementType = 'NOTICE'
  modalVisible.value = true
}

function openEdit(record: any) {
  editingId.value = record.id
  modalTitle.value = '编辑公告'
  formState.title = record.title
  formState.content = record.content
  formState.announcementType = record.announcementType
  modalVisible.value = true
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  confirmLoading.value = true
  try {
    if (editingId.value) {
      await updateAnnouncement(editingId.value, { ...formState })
      message.success('更新成功')
    } else {
      await createAnnouncement({ ...formState })
      message.success('创建成功')
    }
    modalVisible.value = false
    fetchData()
  } catch {
    /* handled by interceptor */
  } finally {
    confirmLoading.value = false
  }
}

/* ---------- Publish ---------- */
async function handlePublish(id: number) {
  try {
    await publishAnnouncement(id)
    message.success('发布成功')
    fetchData()
  } catch {
    /* handled by interceptor */
  }
}

/* ---------- Delete ---------- */
async function handleDelete(id: number) {
  try {
    await deleteAnnouncement(id)
    message.success('删除成功')
    fetchData()
  } catch {
    /* handled by interceptor */
  }
}

/* ---------- Helpers ---------- */
function formatDate(val: string) {
  return val ? dayjs(val).format('YYYY-MM-DD HH:mm:ss') : '-'
}

onMounted(fetchData)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>公告管理</h2>
    </div>

    <!-- Search bar -->
    <div class="table-toolbar">
      <div class="toolbar-left">
        <a-select
          v-model:value="searchForm.status"
          placeholder="状态筛选"
          allow-clear
          style="width: 140px"
        >
          <a-select-option :value="0">草稿</a-select-option>
          <a-select-option :value="1">已发布</a-select-option>
        </a-select>
        <a-input
          v-model:value="searchForm.keyword"
          placeholder="搜索标题关键词"
          allow-clear
          style="width: 200px"
          @press-enter="handleSearch"
        />
        <a-button type="primary" @click="handleSearch">
          <template #icon><SearchOutlined /></template>
          搜索
        </a-button>
        <a-button @click="handleReset">
          <template #icon><ReloadOutlined /></template>
          重置
        </a-button>
      </div>
      <div class="toolbar-right">
        <a-button type="primary" @click="openAdd">
          <template #icon><PlusOutlined /></template>
          新增公告
        </a-button>
      </div>
    </div>

    <!-- Table -->
    <a-table
      :columns="columns"
      :data-source="dataSource"
      :loading="loading"
      :pagination="{
        current: pagination.current,
        pageSize: pagination.pageSize,
        total: pagination.total,
        showSizeChanger: true,
        showTotal: (t: number) => `共 ${t} 条`,
      }"
      row-key="id"
      :scroll="{ x: 1100 }"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'announcementType'">
          <a-tag :color="typeMap[record.announcementType]?.color ?? 'default'">
            {{ typeMap[record.announcementType]?.label ?? record.announcementType }}
          </a-tag>
        </template>

        <template v-else-if="column.dataIndex === 'status'">
          <a-tag :color="statusMap[record.status]?.color ?? 'default'">
            {{ statusMap[record.status]?.label ?? record.status }}
          </a-tag>
        </template>

        <template v-else-if="column.dataIndex === 'publishedAt'">
          {{ formatDate(record.publishedAt) }}
        </template>

        <template v-else-if="column.dataIndex === 'createdAt'">
          {{ formatDate(record.createdAt) }}
        </template>

        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="openEdit(record)">编辑</a-button>
            <a-popconfirm
              v-if="record.status === 0"
              title="确定发布该公告？"
              @confirm="handlePublish(record.id)"
            >
              <a-button type="link" size="small">发布</a-button>
            </a-popconfirm>
            <a-popconfirm title="确定删除该公告？" @confirm="handleDelete(record.id)">
              <a-button type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- Add / Edit Modal -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      :confirm-loading="confirmLoading"
      @ok="handleSubmit"
      :width="600"
      destroy-on-close
    >
      <a-form ref="formRef" :model="formState" layout="vertical" style="margin-top: 16px">
        <a-form-item label="标题" name="title" :rules="[{ required: true, message: '请输入标题' }]">
          <a-input v-model:value="formState.title" placeholder="请输入公告标题" />
        </a-form-item>
        <a-form-item label="类型" name="announcementType" :rules="[{ required: true, message: '请选择类型' }]">
          <a-select v-model:value="formState.announcementType" placeholder="请选择公告类型">
            <a-select-option value="NOTICE">通知</a-select-option>
            <a-select-option value="MAINTENANCE">维护</a-select-option>
            <a-select-option value="PROMOTION">推广</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="内容" name="content" :rules="[{ required: true, message: '请输入内容' }]">
          <a-textarea v-model:value="formState.content" placeholder="请输入公告内容" :rows="6" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
