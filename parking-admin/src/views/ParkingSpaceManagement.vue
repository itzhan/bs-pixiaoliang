<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { getSpaces, createSpace, updateSpace, updateSpaceStatus, deleteSpace } from '@/api/parkingSpace'
import { getAllAreas } from '@/api/parkingArea'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.role === 'ADMIN')

/* ---------- Types ---------- */
interface AreaOption {
  id: number
  name: string
}

interface SpaceRecord {
  id: number
  spaceNumber: string
  areaId: number
  areaName: string
  spaceType: string
  status: number
  hourlyRate: number
  createdAt: string
}

/* ---------- Constants ---------- */
const spaceTypeMap: Record<string, string> = {
  STANDARD: '普通',
  VIP: 'VIP',
  CHARGING: '充电',
  DISABLED: '无障碍',
}

const spaceTypeColorMap: Record<string, string> = {
  STANDARD: 'blue',
  VIP: 'gold',
  CHARGING: 'green',
  DISABLED: 'purple',
}

const statusMap: Record<number, string> = {
  0: '空闲',
  1: '占用',
  2: '预约中',
  3: '维护中',
}

const statusColorMap: Record<number, string> = {
  0: 'green',
  1: 'red',
  2: 'orange',
  3: 'default',
}

/* ---------- Area Options ---------- */
const areaOptions = ref<AreaOption[]>([])

async function loadAreaOptions() {
  try {
    const res: any = await getAllAreas()
    areaOptions.value = res.data || []
  } catch {
    message.error('获取区域列表失败')
  }
}

function getAreaName(areaId: number): string {
  const area = areaOptions.value.find((a) => a.id === areaId)
  return area ? area.name : `区域${areaId}`
}

/* ---------- Search ---------- */
const searchParams = reactive({
  areaId: undefined as number | undefined,
  spaceType: undefined as string | undefined,
  status: undefined as number | undefined,
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
const dataSource = ref<SpaceRecord[]>([])

const columns = [
  { title: '车位号', dataIndex: 'spaceNumber', width: 120 },
  { title: '所属区域', dataIndex: 'areaId', width: 130, key: 'areaId' },
  { title: '车位类型', dataIndex: 'spaceType', width: 110, key: 'spaceType' },
  { title: '状态', dataIndex: 'status', width: 100, key: 'status' },
  { title: '单价(¥/h)', dataIndex: 'hourlyRate', width: 110, key: 'hourlyRate' },
  { title: '操作', key: 'action', width: 220, fixed: 'right' as const },
]

/* ---------- Fetch ---------- */
async function fetchData() {
  loading.value = true
  try {
    const res: any = await getSpaces({
      page: pagination.current,
      size: pagination.pageSize,
      areaId: searchParams.areaId || undefined,
      spaceType: searchParams.spaceType || undefined,
      status: searchParams.status,
    })
    dataSource.value = res.data.records
    pagination.total = res.data.total
  } catch {
    message.error('获取车位列表失败')
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
  searchParams.areaId = undefined
  searchParams.spaceType = undefined
  searchParams.status = undefined
  pagination.current = 1
  fetchData()
}

/* ---------- Add / Edit Modal ---------- */
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formData = reactive({
  id: 0,
  areaId: undefined as number | undefined,
  spaceNumber: '',
  spaceType: 'STANDARD',
  hourlyRate: 0,
})

function resetForm() {
  formData.id = 0
  formData.areaId = undefined
  formData.spaceNumber = ''
  formData.spaceType = 'STANDARD'
  formData.hourlyRate = 0
}

function handleAdd() {
  resetForm()
  isEdit.value = false
  modalVisible.value = true
}

function handleEdit(record: SpaceRecord) {
  formData.id = record.id
  formData.areaId = record.areaId
  formData.spaceNumber = record.spaceNumber || ''
  formData.spaceType = record.spaceType || 'STANDARD'
  formData.hourlyRate = record.hourlyRate || 0
  isEdit.value = true
  modalVisible.value = true
}

async function handleSubmit() {
  if (!formData.areaId) {
    message.warning('请选择所属区域')
    return
  }
  if (!formData.spaceNumber) {
    message.warning('请输入车位号')
    return
  }

  modalLoading.value = true
  try {
    const payload = {
      areaId: formData.areaId,
      spaceNumber: formData.spaceNumber,
      spaceType: formData.spaceType,
      hourlyRate: formData.hourlyRate,
    }
    if (isEdit.value) {
      await updateSpace(formData.id, payload)
      message.success('更新成功')
    } else {
      await createSpace(payload)
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

/* ---------- Status Update Modal ---------- */
const statusModalVisible = ref(false)
const statusModalLoading = ref(false)
const statusForm = reactive({
  id: 0,
  spaceNumber: '',
  status: 0,
})

function handleStatusUpdate(record: SpaceRecord) {
  statusForm.id = record.id
  statusForm.spaceNumber = record.spaceNumber
  statusForm.status = record.status
  statusModalVisible.value = true
}

async function handleStatusSubmit() {
  statusModalLoading.value = true
  try {
    await updateSpaceStatus(statusForm.id, statusForm.status)
    message.success('状态更新成功')
    statusModalVisible.value = false
    fetchData()
  } catch {
    message.error('状态更新失败')
  } finally {
    statusModalLoading.value = false
  }
}

/* ---------- Delete ---------- */
async function handleDelete(id: number) {
  try {
    await deleteSpace(id)
    message.success('删除成功')
    fetchData()
  } catch {
    message.error('删除失败')
  }
}

/* ---------- Format ---------- */
function formatCurrency(val: number) {
  return val != null ? `¥${Number(val).toFixed(2)}` : '-'
}

/* ---------- Init ---------- */
onMounted(() => {
  loadAreaOptions()
  fetchData()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>车位管理</h2>
    </div>

    <!-- Search Bar -->
    <div class="table-toolbar">
      <div class="toolbar-left">
        <a-select
          v-model:value="searchParams.areaId"
          placeholder="选择区域"
          allow-clear
          style="width: 160px"
          :options="areaOptions.map((a) => ({ label: a.name, value: a.id }))"
        />
        <a-select
          v-model:value="searchParams.spaceType"
          placeholder="车位类型"
          allow-clear
          style="width: 130px"
        >
          <a-select-option value="STANDARD">普通</a-select-option>
          <a-select-option value="VIP">VIP</a-select-option>
          <a-select-option value="CHARGING">充电</a-select-option>
          <a-select-option value="DISABLED">无障碍</a-select-option>
        </a-select>
        <a-select
          v-model:value="searchParams.status"
          placeholder="状态"
          allow-clear
          style="width: 120px"
        >
          <a-select-option :value="0">空闲</a-select-option>
          <a-select-option :value="1">占用</a-select-option>
          <a-select-option :value="2">预约中</a-select-option>
          <a-select-option :value="3">维护中</a-select-option>
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
      <div class="toolbar-right">
        <a-button v-if="isAdmin" type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新增车位
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
      :scroll="{ x: 900 }"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'areaId'">
          {{ record.areaName || getAreaName(record.areaId) }}
        </template>
        <template v-else-if="column.key === 'spaceType'">
          <a-tag :color="spaceTypeColorMap[record.spaceType] || 'default'">
            {{ spaceTypeMap[record.spaceType] || record.spaceType }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="statusColorMap[record.status] || 'default'">
            {{ statusMap[record.status] || '未知' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'hourlyRate'">
          {{ formatCurrency(record.hourlyRate) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button v-if="isAdmin" type="link" size="small" @click="handleEdit(record)">编辑</a-button>
            <a-button type="link" size="small" @click="handleStatusUpdate(record)">更新状态</a-button>
            <a-popconfirm
              v-if="isAdmin"
              title="确定删除该车位？"
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
      :title="isEdit ? '编辑车位' : '新增车位'"
      :confirm-loading="modalLoading"
      @ok="handleSubmit"
    >
      <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }" style="margin-top: 16px">
        <a-form-item label="所属区域" required>
          <a-select
            v-model:value="formData.areaId"
            placeholder="请选择区域"
            :options="areaOptions.map((a) => ({ label: a.name, value: a.id }))"
          />
        </a-form-item>
        <a-form-item label="车位号" required>
          <a-input v-model:value="formData.spaceNumber" placeholder="请输入车位号" />
        </a-form-item>
        <a-form-item label="车位类型">
          <a-select v-model:value="formData.spaceType">
            <a-select-option value="STANDARD">普通</a-select-option>
            <a-select-option value="VIP">VIP</a-select-option>
            <a-select-option value="CHARGING">充电</a-select-option>
            <a-select-option value="DISABLED">无障碍</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="单价(¥/h)">
          <a-input-number
            v-model:value="formData.hourlyRate"
            :min="0"
            :precision="2"
            :step="0.5"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Status Update Modal -->
    <a-modal
      v-model:open="statusModalVisible"
      title="更新车位状态"
      :confirm-loading="statusModalLoading"
      @ok="handleStatusSubmit"
    >
      <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }" style="margin-top: 16px">
        <a-form-item label="车位号">
          <a-input :value="statusForm.spaceNumber" disabled />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="statusForm.status">
            <a-select-option :value="0">空闲</a-select-option>
            <a-select-option :value="1">占用</a-select-option>
            <a-select-option :value="2">预约中</a-select-option>
            <a-select-option :value="3">维护中</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
