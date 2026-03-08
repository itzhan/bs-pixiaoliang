<template>
  <div class="page-container">
    <div class="page-header">
      <h2>预约管理</h2>
    </div>

    <!-- Search Bar -->
    <div class="table-toolbar">
      <div class="toolbar-left">
        <a-select
          v-model:value="searchParams.status"
          placeholder="预约状态"
          allow-clear
          style="width: 140px"
        >
          <a-select-option value="">全部</a-select-option>
          <a-select-option :value="0">待确认</a-select-option>
          <a-select-option :value="1">已确认</a-select-option>
          <a-select-option :value="2">已取消</a-select-option>
          <a-select-option :value="3">已过期</a-select-option>
          <a-select-option :value="4">已完成</a-select-option>
        </a-select>
        <a-input
          v-model:value="searchParams.keyword"
          placeholder="搜索预约编号 / 车辆ID / 用户ID"
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
      :scroll="{ x: 1200 }"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="statusMap[record.status]?.color ?? 'default'">
            {{ statusMap[record.status]?.label ?? '未知' }}
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
        <template v-if="column.dataIndex === 'createTime'">
          {{ formatDate(record.createTime) }}
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { getAllReservations } from '@/api/reservation'

/* ---------- Status Map ---------- */
const statusMap: Record<number, { label: string; color: string }> = {
  0: { label: '待确认', color: 'blue' },
  1: { label: '已确认', color: 'green' },
  2: { label: '已取消', color: 'red' },
  3: { label: '已过期', color: 'gray' },
  4: { label: '已完成', color: 'cyan' },
}

/* ---------- Columns ---------- */
const columns = [
  { title: 'ID', dataIndex: 'id', width: 70 },
  { title: '预约编号', dataIndex: 'reservationNo', width: 160 },
  { title: '车位ID', dataIndex: 'spaceId', width: 90 },
  { title: '车辆ID', dataIndex: 'vehicleId', width: 90 },
  { title: '用户ID', dataIndex: 'userId', width: 90 },
  { title: '开始时间', dataIndex: 'startTime', width: 170 },
  { title: '结束时间', dataIndex: 'endTime', width: 170 },
  { title: '状态', dataIndex: 'status', width: 100 },
  { title: '金额', dataIndex: 'amount', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', width: 170 },
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
    if (searchParams.status !== undefined && searchParams.status !== '') {
      params.status = searchParams.status
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

/* ---------- Init ---------- */
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
/* Component-level styles handled by global.css */
</style>
