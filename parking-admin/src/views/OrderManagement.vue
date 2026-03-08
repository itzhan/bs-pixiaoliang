<template>
  <div class="page-container">
    <div class="page-header">
      <h2>停车订单</h2>
    </div>

    <!-- Search Bar -->
    <div class="table-toolbar">
      <div class="toolbar-left">
        <a-select
          v-model:value="searchParams.status"
          placeholder="订单状态"
          allow-clear
          style="width: 130px"
        >
          <a-select-option value="">全部</a-select-option>
          <a-select-option :value="0">停车中</a-select-option>
          <a-select-option :value="1">已完成</a-select-option>
          <a-select-option :value="2">异常</a-select-option>
        </a-select>
        <a-select
          v-model:value="searchParams.paymentStatus"
          placeholder="支付状态"
          allow-clear
          style="width: 130px"
        >
          <a-select-option value="">全部</a-select-option>
          <a-select-option :value="0">未支付</a-select-option>
          <a-select-option :value="1">已支付</a-select-option>
          <a-select-option :value="2">已退款</a-select-option>
        </a-select>
        <a-input
          v-model:value="searchParams.keyword"
          placeholder="搜索订单号 / 车牌号"
          allow-clear
          style="width: 240px"
          @press-enter="handleSearch"
        />
        <a-button type="primary" @click="handleSearch">搜索</a-button>
        <a-button @click="handleReset">重置</a-button>
      </div>
      <div class="toolbar-right">
        <a-button type="primary" @click="openEntryModal">车辆入场</a-button>
        <a-button @click="openExitModal">车辆出场</a-button>
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
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="orderStatusMap[record.status]?.color ?? 'default'">
            {{ orderStatusMap[record.status]?.label ?? '未知' }}
          </a-tag>
        </template>
        <template v-if="column.dataIndex === 'paymentStatus'">
          <a-tag :color="paymentStatusMap[record.paymentStatus]?.color ?? 'default'">
            {{ paymentStatusMap[record.paymentStatus]?.label ?? '未知' }}
          </a-tag>
        </template>
        <template v-if="column.dataIndex === 'amount'">
          ¥{{ (record.amount ?? 0).toFixed(2) }}
        </template>
        <template v-if="column.dataIndex === 'entryTime'">
          {{ formatDate(record.entryTime) }}
        </template>
        <template v-if="column.dataIndex === 'exitTime'">
          {{ formatDate(record.exitTime) }}
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="viewDetail(record)">详情</a-button>
            <a-popconfirm
              v-if="record.status !== 2"
              title="确定要将此订单标记为异常吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleMarkAbnormal(record.id)"
            >
              <a-button type="link" size="small" danger>标记异常</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- Entry Modal -->
    <a-modal
      v-model:open="entryModalVisible"
      title="车辆入场"
      :confirm-loading="entryLoading"
      @ok="handleEntry"
      @cancel="entryModalVisible = false"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="车牌号" required>
          <a-input v-model:value="entryForm.plateNumber" placeholder="请输入车牌号" />
        </a-form-item>
        <a-form-item label="车位ID" required>
          <a-input-number
            v-model:value="entryForm.spaceId"
            placeholder="请输入车位ID"
            :min="1"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Exit Modal -->
    <a-modal
      v-model:open="exitModalVisible"
      title="车辆出场"
      :confirm-loading="exitLoading"
      @ok="handleExit"
      @cancel="exitModalVisible = false"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="车牌号" required>
          <a-input v-model:value="exitForm.plateNumber" placeholder="请输入车牌号" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Detail Drawer -->
    <a-drawer
      v-model:open="detailVisible"
      title="订单详情"
      :width="500"
    >
      <a-descriptions :column="1" bordered size="small" v-if="currentRecord">
        <a-descriptions-item label="订单号">{{ currentRecord.orderNo }}</a-descriptions-item>
        <a-descriptions-item label="车牌号">{{ currentRecord.plateNumber }}</a-descriptions-item>
        <a-descriptions-item label="车位ID">{{ currentRecord.spaceId }}</a-descriptions-item>
        <a-descriptions-item label="入场时间">{{ formatDate(currentRecord.entryTime) }}</a-descriptions-item>
        <a-descriptions-item label="出场时间">{{ formatDate(currentRecord.exitTime) }}</a-descriptions-item>
        <a-descriptions-item label="时长(分钟)">{{ currentRecord.duration ?? '-' }}</a-descriptions-item>
        <a-descriptions-item label="金额">¥{{ (currentRecord.amount ?? 0).toFixed(2) }}</a-descriptions-item>
        <a-descriptions-item label="订单状态">
          <a-tag :color="orderStatusMap[currentRecord.status]?.color ?? 'default'">
            {{ orderStatusMap[currentRecord.status]?.label ?? '未知' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="支付状态">
          <a-tag :color="paymentStatusMap[currentRecord.paymentStatus]?.color ?? 'default'">
            {{ paymentStatusMap[currentRecord.paymentStatus]?.label ?? '未知' }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { getAllOrders, vehicleEntry, vehicleExit, markAbnormal } from '@/api/order'

/* ---------- Status Maps ---------- */
const orderStatusMap: Record<number, { label: string; color: string }> = {
  0: { label: '停车中', color: 'processing' },
  1: { label: '已完成', color: 'success' },
  2: { label: '异常', color: 'error' },
}

const paymentStatusMap: Record<number, { label: string; color: string }> = {
  0: { label: '未支付', color: 'orange' },
  1: { label: '已支付', color: 'green' },
  2: { label: '已退款', color: 'gray' },
}

/* ---------- Columns ---------- */
const columns = [
  { title: 'ID', dataIndex: 'id', width: 70 },
  { title: '订单号', dataIndex: 'orderNo', width: 160 },
  { title: '车牌号', dataIndex: 'plateNumber', width: 120 },
  { title: '车位ID', dataIndex: 'spaceId', width: 90 },
  { title: '入场时间', dataIndex: 'entryTime', width: 170 },
  { title: '出场时间', dataIndex: 'exitTime', width: 170 },
  { title: '时长(分钟)', dataIndex: 'duration', width: 100 },
  { title: '金额(¥)', dataIndex: 'amount', width: 100 },
  { title: '状态', dataIndex: 'status', width: 90 },
  { title: '支付状态', dataIndex: 'paymentStatus', width: 100 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' as const },
]

/* ---------- State ---------- */
const loading = ref(false)
const dataList = ref<any[]>([])
const searchParams = reactive({
  status: undefined as number | undefined,
  paymentStatus: undefined as number | undefined,
  keyword: '',
})
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

/* Entry modal */
const entryModalVisible = ref(false)
const entryLoading = ref(false)
const entryForm = reactive({ plateNumber: '', spaceId: undefined as number | undefined })

/* Exit modal */
const exitModalVisible = ref(false)
const exitLoading = ref(false)
const exitForm = reactive({ plateNumber: '' })

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
    if (searchParams.status !== undefined && searchParams.status !== '') {
      params.status = searchParams.status
    }
    if (searchParams.paymentStatus !== undefined && searchParams.paymentStatus !== '') {
      params.paymentStatus = searchParams.paymentStatus
    }
    if (searchParams.keyword) {
      params.keyword = searchParams.keyword
    }
    const res: any = await getAllOrders(params)
    dataList.value = res.data?.records ?? []
    pagination.total = res.data?.total ?? 0
  } catch (e: any) {
    message.error(e.message || '获取订单列表失败')
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
  searchParams.paymentStatus = undefined
  searchParams.keyword = ''
  pagination.current = 1
  fetchData()
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

/* ---------- Entry ---------- */
const openEntryModal = () => {
  entryForm.plateNumber = ''
  entryForm.spaceId = undefined
  entryModalVisible.value = true
}

const handleEntry = async () => {
  if (!entryForm.plateNumber) {
    message.warning('请输入车牌号')
    return
  }
  if (!entryForm.spaceId) {
    message.warning('请输入车位ID')
    return
  }
  entryLoading.value = true
  try {
    await vehicleEntry({ plateNumber: entryForm.plateNumber, spaceId: entryForm.spaceId })
    message.success('车辆入场成功')
    entryModalVisible.value = false
    fetchData()
  } catch (e: any) {
    message.error(e.message || '车辆入场失败')
  } finally {
    entryLoading.value = false
  }
}

/* ---------- Exit ---------- */
const openExitModal = () => {
  exitForm.plateNumber = ''
  exitModalVisible.value = true
}

const handleExit = async () => {
  if (!exitForm.plateNumber) {
    message.warning('请输入车牌号')
    return
  }
  exitLoading.value = true
  try {
    await vehicleExit({ plateNumber: exitForm.plateNumber })
    message.success('车辆出场成功')
    exitModalVisible.value = false
    fetchData()
  } catch (e: any) {
    message.error(e.message || '车辆出场失败')
  } finally {
    exitLoading.value = false
  }
}

/* ---------- Mark Abnormal ---------- */
const handleMarkAbnormal = async (id: number) => {
  try {
    await markAbnormal(id)
    message.success('已标记为异常')
    fetchData()
  } catch (e: any) {
    message.error(e.message || '操作失败')
  }
}

/* ---------- Detail ---------- */
const viewDetail = (record: any) => {
  currentRecord.value = record
  detailVisible.value = true
}

/* ---------- Init ---------- */
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
/* Component-level styles handled by global.css */
</style>
