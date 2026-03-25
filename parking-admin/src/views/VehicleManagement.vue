<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { getVehicles, addVehicle, updateVehicle, deleteVehicle } from '@/api/vehicle'
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

/* ---------- Add / Edit Modal ---------- */
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formData = reactive({
  id: 0,
  plateNumber: '',
  vehicleType: 'SEDAN',
  brand: '',
  color: '',
})

function resetForm() {
  formData.id = 0
  formData.plateNumber = ''
  formData.vehicleType = 'SEDAN'
  formData.brand = ''
  formData.color = ''
}

function handleAdd() {
  resetForm()
  isEdit.value = false
  modalVisible.value = true
}

function handleEdit(record: VehicleRecord) {
  formData.id = record.id
  formData.plateNumber = record.plateNumber || ''
  formData.vehicleType = record.vehicleType || 'SEDAN'
  formData.brand = record.brand || ''
  formData.color = record.color || ''
  isEdit.value = true
  modalVisible.value = true
}

async function handleSubmit() {
  if (!formData.plateNumber) { message.warning('请输入车牌号'); return }
  modalLoading.value = true
  try {
    const payload = {
      plateNumber: formData.plateNumber,
      vehicleType: formData.vehicleType,
      brand: formData.brand,
      color: formData.color,
    }
    if (isEdit.value) {
      await updateVehicle(formData.id, payload)
      message.success('更新成功')
    } else {
      await addVehicle(payload)
      message.success('新增成功')
    }
    modalVisible.value = false
    fetchData()
  } catch {
    message.error(isEdit.value ? '更新失败' : '新增失败')
  } finally {
    modalLoading.value = false
  }
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
      <div class="toolbar-right">
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新增车辆
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
          <a-space>
            <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
            <a-popconfirm
              title="确定删除该车辆？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" danger size="small">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- Add / Edit Modal -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑车辆' : '新增车辆'"
      :confirm-loading="modalLoading"
      @ok="handleSubmit"
    >
      <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }" style="margin-top: 16px">
        <a-form-item label="车牌号" required>
          <a-input v-model:value="formData.plateNumber" placeholder="请输入车牌号" />
        </a-form-item>
        <a-form-item label="车辆类型">
          <a-select v-model:value="formData.vehicleType">
            <a-select-option value="SEDAN">普通轿车</a-select-option>
            <a-select-option value="SUV">SUV</a-select-option>
            <a-select-option value="TRUCK">货车</a-select-option>
            <a-select-option value="MOTORCYCLE">摩托车</a-select-option>
            <a-select-option value="ELECTRIC">新能源</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="品牌">
          <a-input v-model:value="formData.brand" placeholder="请输入品牌" />
        </a-form-item>
        <a-form-item label="颜色">
          <a-input v-model:value="formData.color" placeholder="请输入颜色" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
