<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, ReloadOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { getAreas, createArea, updateArea, deleteArea } from '@/api/parkingArea'
import dayjs from 'dayjs'

/* ---------- Types ---------- */
interface AreaRecord {
  id: number
  name: string
  code: string
  description: string
  totalSpaces: number
  availableSpaces: number
  floorNumber: number
  areaType: string
  status: number
  createdAt: string
}

/* ---------- Constants ---------- */
const areaTypeMap: Record<string, string> = {
  INDOOR: '室内',
  OUTDOOR: '室外',
  UNDERGROUND: '地下',
}

const areaTypeColorMap: Record<string, string> = {
  INDOOR: 'blue',
  OUTDOOR: 'green',
  UNDERGROUND: 'orange',
}

/* ---------- Search ---------- */
const searchParams = reactive({
  keyword: '',
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
const dataSource = ref<AreaRecord[]>([])

const columns = [
  { title: 'ID', dataIndex: 'id', width: 70 },
  { title: '区域名称', dataIndex: 'name', width: 140 },
  { title: '区域编码', dataIndex: 'code', width: 120 },
  { title: '楼层', dataIndex: 'floorNumber', width: 80 },
  { title: '区域类型', dataIndex: 'areaType', width: 100, key: 'areaType' },
  { title: '总车位', dataIndex: 'totalSpaces', width: 90 },
  { title: '可用车位', dataIndex: 'availableSpaces', width: 100 },
  { title: '状态', dataIndex: 'status', width: 90, key: 'status' },
  { title: '操作', key: 'action', width: 150, fixed: 'right' as const },
]

/* ---------- Fetch ---------- */
async function fetchData() {
  loading.value = true
  try {
    const res: any = await getAreas({
      page: pagination.current,
      size: pagination.pageSize,
      keyword: searchParams.keyword || undefined,
    })
    dataSource.value = res.data.records
    pagination.total = res.data.total
  } catch {
    message.error('获取区域列表失败')
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
  pagination.current = 1
  fetchData()
}

/* ---------- Add / Edit Modal ---------- */
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const formData = reactive({
  id: 0,
  name: '',
  code: '',
  description: '',
  totalSpaces: 0,
  floorNumber: 1,
  areaType: 'INDOOR',
  status: 1,
})

function resetForm() {
  formData.id = 0
  formData.name = ''
  formData.code = ''
  formData.description = ''
  formData.totalSpaces = 0
  formData.floorNumber = 1
  formData.areaType = 'INDOOR'
  formData.status = 1
}

function handleAdd() {
  resetForm()
  isEdit.value = false
  modalVisible.value = true
}

function handleEdit(record: AreaRecord) {
  formData.id = record.id
  formData.name = record.name || ''
  formData.code = record.code || ''
  formData.description = record.description || ''
  formData.totalSpaces = record.totalSpaces || 0
  formData.floorNumber = record.floorNumber || 1
  formData.areaType = record.areaType || 'INDOOR'
  formData.status = record.status ?? 1
  isEdit.value = true
  modalVisible.value = true
}

async function handleSubmit() {
  if (!formData.name) {
    message.warning('请输入区域名称')
    return
  }
  if (!formData.code) {
    message.warning('请输入区域编码')
    return
  }

  modalLoading.value = true
  try {
    const payload = {
      name: formData.name,
      code: formData.code,
      description: formData.description,
      totalSpaces: formData.totalSpaces,
      floorNumber: formData.floorNumber,
      areaType: formData.areaType,
      status: formData.status,
    }
    if (isEdit.value) {
      await updateArea(formData.id, payload)
      message.success('更新成功')
    } else {
      await createArea(payload)
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
    await deleteArea(id)
    message.success('删除成功')
    fetchData()
  } catch {
    message.error('删除失败')
  }
}

/* ---------- Init ---------- */
onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>停车区域管理</h2>
    </div>

    <!-- Search Bar -->
    <div class="table-toolbar">
      <div class="toolbar-left">
        <a-input
          v-model:value="searchParams.keyword"
          placeholder="搜索区域名称/编码"
          allow-clear
          style="width: 220px"
          @press-enter="handleSearch"
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
          新增区域
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
        <template v-if="column.key === 'areaType'">
          <a-tag :color="areaTypeColorMap[record.areaType] || 'default'">
            {{ areaTypeMap[record.areaType] || record.areaType }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? '启用' : '停用' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
            <a-popconfirm
              title="确定删除该区域？"
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
      :title="isEdit ? '编辑区域' : '新增区域'"
      :confirm-loading="modalLoading"
      @ok="handleSubmit"
    >
      <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }" style="margin-top: 16px">
        <a-form-item label="区域名称" required>
          <a-input v-model:value="formData.name" placeholder="请输入区域名称" />
        </a-form-item>
        <a-form-item label="区域编码" required>
          <a-input v-model:value="formData.code" placeholder="请输入区域编码" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="formData.description" placeholder="请输入描述" :rows="3" />
        </a-form-item>
        <a-form-item label="总车位数">
          <a-input-number v-model:value="formData.totalSpaces" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="楼层">
          <a-input-number v-model:value="formData.floorNumber" style="width: 100%" />
        </a-form-item>
        <a-form-item label="区域类型">
          <a-select v-model:value="formData.areaType">
            <a-select-option value="INDOOR">室内</a-select-option>
            <a-select-option value="OUTDOOR">室外</a-select-option>
            <a-select-option value="UNDERGROUND">地下</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">停用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
