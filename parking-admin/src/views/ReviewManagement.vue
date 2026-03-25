<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { getAllReviews, replyReview, updateReviewStatus, deleteReview } from '@/api/review'

/* ---------- Status map ---------- */
const statusMap: Record<number, { label: string; color: string }> = {
  1: { label: '显示', color: 'green' },
  0: { label: '隐藏', color: 'red' },
}

/* ---------- Search ---------- */
const searchForm = reactive({
  status: undefined as number | undefined,
})

/* ---------- Table ---------- */
const loading = ref(false)
const dataSource = ref<any[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const columns = [
  { title: '用户', dataIndex: 'userName', width: 100 },
  { title: '订单号', dataIndex: 'orderNo', width: 160 },
  { title: '评分', dataIndex: 'rating', width: 180 },
  { title: '评价内容', dataIndex: 'content', ellipsis: true },
  { title: '回复', dataIndex: 'reply', ellipsis: true },
  { title: '回复时间', dataIndex: 'replyAt', width: 170 },
  { title: '状态', dataIndex: 'status', width: 90 },
  { title: '创建时间', dataIndex: 'createdAt', width: 170 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' as const },
]

async function fetchData() {
  loading.value = true
  try {
    const res = await getAllReviews({
      page: pagination.current,
      size: pagination.pageSize,
      status: searchForm.status,
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
  pagination.current = 1
  fetchData()
}

/* ---------- Reply Modal ---------- */
const replyModalVisible = ref(false)
const replyLoading = ref(false)
const replyingId = ref<number | null>(null)
const replyContent = ref('')

function openReply(record: any) {
  replyingId.value = record.id
  replyContent.value = record.reply || ''
  replyModalVisible.value = true
}

async function handleReplySubmit() {
  if (!replyContent.value.trim()) {
    message.warning('请输入回复内容')
    return
  }
  replyLoading.value = true
  try {
    await replyReview(replyingId.value!, { reply: replyContent.value })
    message.success('回复成功')
    replyModalVisible.value = false
    fetchData()
  } catch {
    /* handled by interceptor */
  } finally {
    replyLoading.value = false
  }
}

/* ---------- Status Toggle ---------- */
async function handleToggleStatus(record: any) {
  const newStatus = record.status === 1 ? 0 : 1
  try {
    await updateReviewStatus(record.id, newStatus)
    message.success('状态更新成功')
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

/* ---------- Delete ---------- */
async function handleDeleteReview(id: number) {
  try {
    await deleteReview(id)
    message.success('删除成功')
    fetchData()
  } catch {
    message.error('删除失败')
  }
}
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>评价管理</h2>
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
          <a-select-option :value="1">显示</a-select-option>
          <a-select-option :value="0">隐藏</a-select-option>
        </a-select>
        <a-button type="primary" @click="handleSearch">
          <template #icon><SearchOutlined /></template>
          搜索
        </a-button>
        <a-button @click="handleReset">
          <template #icon><ReloadOutlined /></template>
          重置
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
      :scroll="{ x: 1200 }"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'rating'">
          <a-rate :value="record.rating" disabled allow-half />
        </template>

        <template v-else-if="column.dataIndex === 'status'">
          <a-tag :color="statusMap[record.status]?.color ?? 'default'">
            {{ statusMap[record.status]?.label ?? record.status }}
          </a-tag>
        </template>

        <template v-else-if="column.dataIndex === 'replyAt'">
          {{ formatDate(record.replyAt) }}
        </template>

        <template v-else-if="column.dataIndex === 'createdAt'">
          {{ formatDate(record.createdAt) }}
        </template>

        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="openReply(record)">回复</a-button>
            <a-popconfirm
              :title="`确定${record.status === 1 ? '隐藏' : '显示'}该评价？`"
              @confirm="handleToggleStatus(record)"
            >
              <a-button type="link" size="small">
                {{ record.status === 1 ? '隐藏' : '显示' }}
              </a-button>
            </a-popconfirm>
            <a-popconfirm
              title="确定删除该评价？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleDeleteReview(record.id)"
            >
              <a-button type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- Reply Modal -->
    <a-modal
      v-model:open="replyModalVisible"
      title="回复评价"
      :confirm-loading="replyLoading"
      @ok="handleReplySubmit"
      :width="520"
      destroy-on-close
    >
      <a-form layout="vertical" style="margin-top: 16px">
        <a-form-item label="回复内容">
          <a-textarea
            v-model:value="replyContent"
            placeholder="请输入回复内容"
            :rows="4"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
