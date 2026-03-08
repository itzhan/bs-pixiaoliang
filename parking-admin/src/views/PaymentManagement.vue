<template>
  <div class="page-container">
    <div class="page-header">
      <h2>支付记录</h2>
    </div>

    <!-- Search Bar -->
    <div class="table-toolbar">
      <div class="toolbar-left">
        <a-select
          v-model:value="searchParams.status"
          placeholder="支付状态"
          allow-clear
          style="width: 130px"
        >
          <a-select-option value="">全部</a-select-option>
          <a-select-option :value="0">待支付</a-select-option>
          <a-select-option :value="1">支付成功</a-select-option>
          <a-select-option :value="2">已退款</a-select-option>
        </a-select>
        <a-input
          v-model:value="searchParams.keyword"
          placeholder="搜索支付单号 / 订单ID / 用户ID"
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
      :scroll="{ x: 1300 }"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'amount'">
          ¥{{ (record.amount ?? 0).toFixed(2) }}
        </template>
        <template v-if="column.dataIndex === 'paymentMethod'">
          <a-tag :color="methodMap[record.paymentMethod]?.color ?? 'default'">
            {{ methodMap[record.paymentMethod]?.label ?? record.paymentMethod }}
          </a-tag>
        </template>
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="statusMap[record.status]?.color ?? 'default'">
            {{ statusMap[record.status]?.label ?? '未知' }}
          </a-tag>
        </template>
        <template v-if="column.dataIndex === 'paymentTime'">
          {{ formatDate(record.paymentTime) }}
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { getAllPayments } from '@/api/payment'

/* ---------- Maps ---------- */
const methodMap: Record<string, { label: string; color: string }> = {
  WECHAT: { label: '微信支付', color: 'green' },
  ALIPAY: { label: '支付宝', color: 'blue' },
  CASH: { label: '现金', color: 'gray' },
  CARD: { label: '刷卡', color: 'purple' },
}

const statusMap: Record<number, { label: string; color: string }> = {
  0: { label: '待支付', color: 'orange' },
  1: { label: '支付成功', color: 'green' },
  2: { label: '已退款', color: 'red' },
}

/* ---------- Columns ---------- */
const columns = [
  { title: 'ID', dataIndex: 'id', width: 70 },
  { title: '支付单号', dataIndex: 'paymentNo', width: 170 },
  { title: '订单ID', dataIndex: 'orderId', width: 90 },
  { title: '用户ID', dataIndex: 'userId', width: 90 },
  { title: '金额(¥)', dataIndex: 'amount', width: 100 },
  { title: '支付方式', dataIndex: 'paymentMethod', width: 110 },
  { title: '状态', dataIndex: 'status', width: 100 },
  { title: '支付时间', dataIndex: 'paymentTime', width: 170 },
  { title: '备注', dataIndex: 'remark', ellipsis: true },
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
    const res: any = await getAllPayments(params)
    dataList.value = res.data?.records ?? []
    pagination.total = res.data?.total ?? 0
  } catch (e: any) {
    message.error(e.message || '获取支付记录失败')
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

/* ---------- Init ---------- */
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
/* Component-level styles handled by global.css */
</style>
