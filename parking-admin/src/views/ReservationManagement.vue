<template>
  <div class="page-container">
    <div class="page-header">
      <h2>预约管理</h2>
    </div>

    <!-- Search Bar -->
    <div class="table-toolbar">
      <div class="toolbar-left">
        <a-input
          v-model:value="searchParams.keyword"
          placeholder="搜索预约编号"
          allow-clear
          style="width: 280px"
          @press-enter="handleSearch"
        />
        <a-button type="primary" @click="handleSearch">搜索</a-button>
        <a-button @click="handleReset">重置</a-button>
      </div>
    </div>

    <!-- Table -->
    <a-table
      :columns="columns"
      :data-source="dataList"
      :loading="loading"
      :pagination="pagination"
      row-key="id"
      :scroll="{ x: 1400 }"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'paymentStatus'">
          <a-tag :color="paymentStatusMap[record.paymentStatus]?.color ?? 'default'">
            {{ paymentStatusMap[record.paymentStatus]?.label ?? '未知' }}
          </a-tag>
        </template>
        <template v-if="column.dataIndex === 'amount'">
          ¥{{ (record.amount ?? 0).toFixed(2) }}
        </template>
        <template v-if="column.dataIndex === 'startTime'">
          {{ formatDate(record.startTime) }}
        </template>
        <template v-if="column.dataIndex === 'endTime'">
          {{ formatDate(record.endTime) }}
        </template>
        <template v-if="column.dataIndex === 'createdAt'">
          {{ formatDate(record.createdAt) }}
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="viewDetail(record)">详情</a-button>
            <a-popconfirm
              title="确定删除该预约？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- Detail Drawer -->
    <a-drawer
      v-model:open="detailVisible"
      title="预约详情"
      :width="500"
    >
      <a-descriptions :column="1" bordered size="small" v-if="currentRecord">
        <a-descriptions-item label="预约编号">{{ currentRecord.reservationNo }}</a-descriptions-item>
        <a-descriptions-item label="用户">{{ currentRecord.userName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="车牌号">{{ currentRecord.plateNumber || '-' }}</a-descriptions-item>
        <a-descriptions-item label="车位号">{{ currentRecord.spaceNumber || '-' }}</a-descriptions-item>
        <a-descriptions-item label="区域">{{ currentRecord.areaName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="开始时间">{{ formatDate(currentRecord.startTime) }}</a-descriptions-item>
        <a-descriptions-item label="结束时间">{{ formatDate(currentRecord.endTime) }}</a-descriptions-item>
        <a-descriptions-item label="金额">¥{{ (currentRecord.amount ?? 0).toFixed(2) }}</a-descriptions-item>
        <a-descriptions-item label="支付状态">
          <a-tag :color="paymentStatusMap[currentRecord.paymentStatus]?.color ?? 'default'">
            {{ paymentStatusMap[currentRecord.paymentStatus]?.label ?? '未知' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ formatDate(currentRecord.createdAt) }}</a-descriptions-item>
      </a-descriptions>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { getAllReservations, deleteReservation } from '@/api/reservation'

/* ---------- Status Maps ---------- */
const paymentStatusMap: Record<number, { label: string; color: string }> = {
  0: { label: '待支付', color: 'orange' },
  1: { label: '已支付', color: 'green' },
}

/* ---------- Columns ---------- */
const columns = [
  { title: '预约编号', dataIndex: 'reservationNo', width: 160 },
  { title: '用户', dataIndex: 'userName', width: 100 },
  { title: '车牌号', dataIndex: 'plateNumber', width: 120 },
  { title: '车位号', dataIndex: 'spaceNumber', width: 100 },
  { title: '开始时间', dataIndex: 'startTime', width: 170 },
  { title: '结束时间', dataIndex: 'endTime', width: 170 },
  { title: '支付状态', dataIndex: 'paymentStatus', width: 100 },
  { title: '金额', dataIndex: 'amount', width: 100 },
  { title: '创建时间', dataIndex: 'createdAt', width: 170 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' as const },
]

/* ---------- State ---------- */
const loading = ref(false)
const dataList = ref<any[]>([])
const searchParams = reactive({
  keyword: '',
})
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

/* Detail drawer */
const detailVisible = ref(false)
const currentRecord = ref<any>(null)

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
    if (searchParams.keyword) {
      params.keyword = searchParams.keyword
    }
    const res: any = await getAllReservations(params)
    dataList.value = res.data?.records ?? []
    pagination.total = res.data?.total ?? 0
  } catch (e: any) {
    message.error(e.message || '获取预约列表失败')
  } finally {
    loading.value = false
  }
}

/* ---------- Handlers ---------- */
const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  searchParams.keyword = ''
  pagination.current = 1
  fetchData()
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

/* ---------- Detail ---------- */
const viewDetail = (record: any) => {
  currentRecord.value = record
  detailVisible.value = true
}

/* ---------- Delete ---------- */
const handleDelete = async (id: number) => {
  try {
    await deleteReservation(id)
    message.success('删除成功')
    fetchData()
  } catch (e: any) {
    message.error(e.message || '删除失败')
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
