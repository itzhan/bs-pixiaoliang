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
          placeholder="搜索支付单号"
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
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="statusMap[record.status]?.color ?? 'default'">
            {{ statusMap[record.status]?.label ?? '未知' }}
          </a-tag>
        </template>
        <template v-if="column.dataIndex === 'paidAt'">
          {{ formatDate(record.paidAt) }}
        </template>
        <template v-if="column.key === 'action'">
          <a-popconfirm
            title="确定删除该支付记录？"
            ok-text="确定"
            cancel-text="取消"
            @confirm="handleDelete(record.id)"
          >
            <a-button type="link" size="small" danger>删除</a-button>
          </a-popconfirm>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { getAllPayments, deletePayment } from '@/api/payment'

/* ---------- Maps ---------- */
const statusMap: Record<number, { label: string; color: string }> = {
  0: { label: '待支付', color: 'orange' },
  1: { label: '支付成功', color: 'green' },
  2: { label: '已退款', color: 'red' },
}

/* ---------- Columns ---------- */
const columns = [
  { title: '支付单号', dataIndex: 'paymentNo', width: 170 },
  { title: '订单号', dataIndex: 'orderNo', width: 160 },
  { title: '用户', dataIndex: 'userName', width: 100 },
  { title: '金额(¥)', dataIndex: 'amount', width: 100 },
  { title: '状态', dataIndex: 'status', width: 100 },
  { title: '支付时间', dataIndex: 'paidAt', width: 170 },
  { title: '备注', dataIndex: 'remark', ellipsis: true },
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

/* ---------- Delete ---------- */
const handleDelete = async (id: number) => {
  try {
    await deletePayment(id)
    message.success('删除成功')
    fetchData()
  } catch (e: any) {
    message.error(e.message || '删除失败')
  }
}
</script>

<style scoped>
/* Component-level styles handled by global.css */
</style>
