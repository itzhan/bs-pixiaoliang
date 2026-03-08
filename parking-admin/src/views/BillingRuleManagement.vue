<template>
  <div class="page-container">
    <div class="page-header">
      <h2>计费规则</h2>
    </div>

    <!-- Toolbar -->
    <div class="table-toolbar">
      <div class="toolbar-left">
        <a-button type="primary" @click="openAddModal">新增规则</a-button>
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
        <template v-if="column.dataIndex === 'spaceType'">
          <a-tag :color="spaceTypeMap[record.spaceType]?.color ?? 'default'">
            {{ spaceTypeMap[record.spaceType]?.label ?? record.spaceType }}
          </a-tag>
        </template>
        <template v-if="column.dataIndex === 'hourlyRate'">
          ¥{{ (record.hourlyRate ?? 0).toFixed(2) }}
        </template>
        <template v-if="column.dataIndex === 'dailyCap'">
          ¥{{ (record.dailyCap ?? 0).toFixed(2) }}
        </template>
        <template v-if="column.dataIndex === 'isActive'">
          <a-tag :color="record.isActive ? 'green' : 'red'">
            {{ record.isActive ? '启用' : '禁用' }}
          </a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="openEditModal(record)">编辑</a-button>
            <a-popconfirm
              title="确定要删除此计费规则吗？"
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

    <!-- Add / Edit Modal -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑规则' : '新增规则'"
      :confirm-loading="modalLoading"
      @ok="handleSubmit"
      @cancel="modalVisible = false"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="规则名称" required>
          <a-input v-model:value="formData.name" placeholder="请输入规则名称" />
        </a-form-item>
        <a-form-item label="车位类型" required>
          <a-select v-model:value="formData.spaceType" placeholder="请选择车位类型">
            <a-select-option value="NORMAL">普通车位</a-select-option>
            <a-select-option value="VIP">VIP车位</a-select-option>
            <a-select-option value="DISABLED">无障碍车位</a-select-option>
            <a-select-option value="CHARGING">充电车位</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="每小时费率" required>
          <a-input-number
            v-model:value="formData.hourlyRate"
            :min="0"
            :precision="2"
            placeholder="每小时费率（元）"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="每日上限" required>
          <a-input-number
            v-model:value="formData.dailyCap"
            :min="0"
            :precision="2"
            placeholder="每日收费上限（元）"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="免费分钟数">
          <a-input-number
            v-model:value="formData.freeMinutes"
            :min="0"
            :precision="0"
            placeholder="免费停车分钟数"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group v-model:value="formData.isActive">
            <a-radio :value="true">启用</a-radio>
            <a-radio :value="false">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="说明">
          <a-textarea
            v-model:value="formData.description"
            placeholder="请输入规则说明"
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
import { getRules, createRule, updateRule, deleteRule } from '@/api/billingRule'

/* ---------- Space Type Map ---------- */
const spaceTypeMap: Record<string, { label: string; color: string }> = {
  NORMAL: { label: '普通车位', color: 'blue' },
  VIP: { label: 'VIP车位', color: 'gold' },
  DISABLED: { label: '无障碍车位', color: 'purple' },
  CHARGING: { label: '充电车位', color: 'green' },
}

/* ---------- Columns ---------- */
const columns = [
  { title: 'ID', dataIndex: 'id', width: 70 },
  { title: '规则名称', dataIndex: 'name', width: 160 },
  { title: '车位类型', dataIndex: 'spaceType', width: 120 },
  { title: '每小时费率(¥)', dataIndex: 'hourlyRate', width: 130 },
  { title: '每日上限(¥)', dataIndex: 'dailyCap', width: 120 },
  { title: '免费分钟数', dataIndex: 'freeMinutes', width: 110 },
  { title: '状态', dataIndex: 'isActive', width: 90 },
  { title: '说明', dataIndex: 'description', ellipsis: true },
  { title: '操作', key: 'action', width: 140, fixed: 'right' as const },
]

/* ---------- State ---------- */
const loading = ref(false)
const dataList = ref<any[]>([])
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

/* Modal */
const modalVisible = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const editId = ref<number>(0)

const getDefaultForm = () => ({
  name: '',
  spaceType: undefined as string | undefined,
  hourlyRate: undefined as number | undefined,
  dailyCap: undefined as number | undefined,
  freeMinutes: 0,
  isActive: true,
  description: '',
})
const formData = reactive(getDefaultForm())

/* ---------- Data Fetching ---------- */
const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getRules({ page: pagination.current, size: pagination.pageSize })
    dataList.value = res.data?.records ?? []
    pagination.total = res.data?.total ?? 0
  } catch (e: any) {
    message.error(e.message || '获取计费规则失败')
  } finally {
    loading.value = false
  }
}

/* ---------- Pagination ---------- */
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

/* ---------- Add / Edit ---------- */
const resetForm = () => {
  Object.assign(formData, getDefaultForm())
}

const openAddModal = () => {
  isEdit.value = false
  resetForm()
  modalVisible.value = true
}

const openEditModal = (record: any) => {
  isEdit.value = true
  editId.value = record.id
  Object.assign(formData, {
    name: record.name,
    spaceType: record.spaceType,
    hourlyRate: record.hourlyRate,
    dailyCap: record.dailyCap,
    freeMinutes: record.freeMinutes ?? 0,
    isActive: record.isActive ?? true,
    description: record.description ?? '',
  })
  modalVisible.value = true
}

const handleSubmit = async () => {
  if (!formData.name) {
    message.warning('请输入规则名称')
    return
  }
  if (!formData.spaceType) {
    message.warning('请选择车位类型')
    return
  }
  if (formData.hourlyRate === undefined || formData.hourlyRate === null) {
    message.warning('请输入每小时费率')
    return
  }
  if (formData.dailyCap === undefined || formData.dailyCap === null) {
    message.warning('请输入每日上限')
    return
  }
  modalLoading.value = true
  try {
    const payload = { ...formData }
    if (isEdit.value) {
      await updateRule(editId.value, payload)
      message.success('更新成功')
    } else {
      await createRule(payload)
      message.success('新增成功')
    }
    modalVisible.value = false
    fetchData()
  } catch (e: any) {
    message.error(e.message || '操作失败')
  } finally {
    modalLoading.value = false
  }
}

/* ---------- Delete ---------- */
const handleDelete = async (id: number) => {
  try {
    await deleteRule(id)
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
