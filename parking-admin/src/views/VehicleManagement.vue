<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { getVehicles, deleteVehicle } from '@/api/vehicle'
import dayjs from 'dayjs'

/* ---------- Types ---------- */
interface VehicleRecord {
  id: number
  plateNumber: string
  vehicleType: string
  brand: string
  color: string
  userId: number
  isDefault: number
  createdAt: string
}

/* ---------- Constants ---------- */
const vehicleTypeMap: Record<string, string> = {
  SEDAN: '普通轿车',
  SUV: 'SUV',
  TRUCK: '货车',
  MOTORCYCLE: '摩托车',
  ELECTRIC: '新能源',
}

/* ---------- Search ---------- */
const searchParams = reactive({
  keyword: '',
  userId: undefined as number | undefined,
})

/* ---------- Pagination ---------- */
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

/* ---------- Table ---------- */
const loading = ref(false)
const dataSource = ref<VehicleRecord[]>([])

const columns = [
  { title: '车牌号', dataIndex: 'plateNumber', width: 130 },
  { title: '车辆类型', dataIndex: 'vehicleType', width: 120, key: 'vehicleType' },
  { title: '品牌', dataIndex: 'brand', width: 120 },
  { title: '颜色', dataIndex: 'color', width: 90 },
  { title: '所属用户', dataIndex: 'userName', width: 110 },
  { title: '默认车辆', dataIndex: 'isDefault', width: 100, key: 'isDefault' },
  { title: '创建时间', dataIndex: 'createdAt', width: 180, key: 'createdAt' },
  { title: '操作', key: 'action', width: 100, fixed: 'right' as const },
]

/* ---------- Fetch ---------- */
async function fetchData() {
  loading.value = true
  try {
    const res: any = await getVehicles({
      page: pagination.current,
      size: pagination.pageSize,
      keyword: searchParams.keyword || undefined,
      userId: searchParams.userId || undefined,
    })
    dataSource.value = res.data.records
    pagination.total = res.data.total
  } catch {
    message.error('获取车辆列表失败')
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
  searchParams.keyword = ''
  searchParams.userId = undefined
  pagination.current = 1
  fetchData()
}

/* ---------- Delete ---------- */
async function handleDelete(id: number) {
  try {
    await deleteVehicle(id)
    message.success('删除成功')
    fetchData()
  } catch {
    message.error('删除失败')
  }
}

/* ---------- Format ---------- */
function formatDate(val: string) {
  return val ? dayjs(val).format('YYYY-MM-DD HH:mm:ss') : '-'
}

/* ---------- Init ---------- */
onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>车辆管理</h2>
    </div>

    <!-- Search Bar -->
    <div class="table-toolbar">
      <div class="toolbar-left">
        <a-input
          v-model:value="searchParams.keyword"
          placeholder="搜索车牌号/品牌"
          allow-clear
          style="width: 200px"
          @press-enter="handleSearch"
        />
        <a-input-number
          v-model:value="searchParams.userId"
          placeholder="用户ID"
          :min="1"
          style="width: 130px"
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
    </div>

    <!-- Table -->
    <a-table
      :columns="columns"
      :data-source="dataSource"
      :loading="loading"
      :pagination="pagination"
      row-key="id"
      :scroll="{ x: 1000 }"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'vehicleType'">
          <a-tag color="blue">
            {{ vehicleTypeMap[record.vehicleType] || record.vehicleType }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'isDefault'">
          <a-tag :color="record.isDefault === 1 ? 'green' : 'default'">
            {{ record.isDefault === 1 ? '是' : '否' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'createdAt'">
          {{ formatDate(record.createdAt) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-popconfirm
            title="确定删除该车辆？"
            ok-text="确定"
            cancel-text="取消"
            @confirm="handleDelete(record.id)"
          >
            <a-button type="link" danger size="small">删除</a-button>
          </a-popconfirm>
        </template>
      </template>
    </a-table>
  </div>
</template>
