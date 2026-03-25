<template>
  <div class="page-container">
    <div class="page-header">
      <h2>黑名单管理</h2>
    </div>

    <!-- Search Bar -->
    <div class="table-toolbar">
      <div class="toolbar-left">
        <a-select
          v-model:value="searchParams.status"
          placeholder="状态"
          allow-clear
          style="width: 130px"
        >
          <a-select-option value="">全部</a-select-option>
          <a-select-option :value="1">生效中</a-select-option>
          <a-select-option :value="0">已移除</a-select-option>
        </a-select>
        <a-input
          v-model:value="searchParams.keyword"
          placeholder="搜索车牌号 / 原因"
          allow-clear
          style="width: 240px"
          @press-enter="handleSearch"
        />
        <a-button type="primary" @click="handleSearch">搜索</a-button>
        <a-button @click="handleReset">重置</a-button>
      </div>
      <div class="toolbar-right">
        <a-button type="primary" danger @click="openAddModal">添加黑名单</a-button>
      </div>
    </div>

    <!-- Table -->
    <a-table
      :columns="columns"
      :data-source="dataList"
      :loading="loading"
      :pagination="pagination"
      row-key="id"
      :scroll="{ x: 1100 }"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="record.status === 1 ? 'red' : 'gray'">
            {{ record.status === 1 ? '生效中' : '已移除' }}
          </a-tag>
        </template>
        <template v-if="column.dataIndex === 'blockedAt'">
          {{ formatDate(record.blockedAt) }}
        </template>
        <template v-if="column.dataIndex === 'removedAt'">
          {{ formatDate(record.removedAt) }}
        </template>
        <template v-if="column.key === 'action'">
          <a-popconfirm
            v-if="record.status === 1"
            title="确定要将此车牌从黑名单中移除吗？"
            ok-text="确定"
            cancel-text="取消"
            @confirm="handleRemove(record.id)"
          >
            <a-button type="link" size="small" danger>移除</a-button>
          </a-popconfirm>
          <span v-else style="color: #999">-</span>
        </template>
      </template>
    </a-table>

    <!-- Add Modal -->
    <a-modal
      v-model:open="addModalVisible"
      title="添加黑名单"
      :confirm-loading="addLoading"
      @ok="handleAdd"
      @cancel="addModalVisible = false"
    >
      <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 17 }">
        <a-form-item label="车牌号" required>
          <a-input v-model:value="addForm.plateNumber" placeholder="请输入车牌号" />
        </a-form-item>
        <a-form-item label="原因" required>
          <a-textarea
            v-model:value="addForm.reason"
            placeholder="请输入加入黑名单的原因"
            :rows="3"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { getBlacklist, addToBlacklist, removeFromBlacklist } from '@/api/blacklist'

/* ---------- Columns ---------- */
const columns = [
  { title: '车牌号', dataIndex: 'plateNumber', width: 130 },
  { title: '原因', dataIndex: 'reason', ellipsis: true },
  { title: '状态', dataIndex: 'status', width: 100 },
  { title: '加入时间', dataIndex: 'blockedAt', width: 170 },
  { title: '移除时间', dataIndex: 'removedAt', width: 170 },
  { title: '操作员', dataIndex: 'operatorName', width: 100 },
  { title: '操作', key: 'action', width: 100, fixed: 'right' as const },
]

/* ---------- State ---------- */
const loading = ref(false)
const dataList = ref<any[]>([])
const searchParams = reactive({
  status: undefined as number | undefined,
  keyword: '',
})
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

/* Add modal */
const addModalVisible = ref(false)
const addLoading = ref(false)
const addForm = reactive({ plateNumber: '', reason: '' })

/* ---------- Helpers ---------- */
const formatDate = (val: string | null | undefined) => {
  if (!val) return '-'
  return dayjs(val).format('YYYY-MM-DD HH:mm:ss')
}

/* ---------- Data Fetching ---------- */
const fetchData = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.current,
      size: pagination.pageSize,
    }
    if (searchParams.status !== undefined) {
      params.status = searchParams.status
    }
    if (searchParams.keyword) {
      params.keyword = searchParams.keyword
    }
    const res: any = await getBlacklist(params)
    dataList.value = res.data?.records ?? []
    pagination.total = res.data?.total ?? 0
  } catch (e: any) {
    message.error(e.message || '获取黑名单列表失败')
  } finally {
    loading.value = false
  }
}

/* ---------- Search / Reset ---------- */
const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  searchParams.status = undefined
  searchParams.keyword = ''
  pagination.current = 1
  fetchData()
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

/* ---------- Add ---------- */
const openAddModal = () => {
  addForm.plateNumber = ''
  addForm.reason = ''
  addModalVisible.value = true
}

const handleAdd = async () => {
  if (!addForm.plateNumber) {
    message.warning('请输入车牌号')
    return
  }
  if (!addForm.reason) {
    message.warning('请输入原因')
    return
  }
  addLoading.value = true
  try {
    await addToBlacklist({ plateNumber: addForm.plateNumber, reason: addForm.reason })
    message.success('添加成功')
    addModalVisible.value = false
    fetchData()
  } catch (e: any) {
    message.error(e.message || '添加失败')
  } finally {
    addLoading.value = false
  }
}

/* ---------- Remove ---------- */
const handleRemove = async (id: number) => {
  try {
    await removeFromBlacklist(id)
    message.success('已从黑名单移除')
    fetchData()
  } catch (e: any) {
    message.error(e.message || '移除失败')
  }
}

/* ---------- Init ---------- */
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
/* Component-level styles handled by global.css */
</style>
