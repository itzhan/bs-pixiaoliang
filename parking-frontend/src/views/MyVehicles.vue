<template>
  <div class="page-container">
    <div class="title-bar">
      <h2 class="section-title">我的车辆</h2>
      <n-button type="primary" @click="openAddModal">
        + 添加车辆
      </n-button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-wrapper">
      <n-spin size="large" />
    </div>

    <!-- Empty -->
    <n-empty v-else-if="vehicles.length === 0" description="暂未绑定车辆，请先添加" style="margin-top: 60px">
      <template #extra>
        <n-button type="primary" size="small" @click="openAddModal">添加车辆</n-button>
      </template>
    </n-empty>

    <!-- Vehicle Cards -->
    <div v-else class="card-grid">
      <n-card
        v-for="item in vehicles"
        :key="item.id"
        class="vehicle-card card-hover"
        :bordered="false"
      >
        <div class="plate-section">
          <span class="plate-number">{{ item.plateNumber }}</span>
          <n-tag v-if="item.isDefault === 1" type="primary" size="small" round>默认</n-tag>
        </div>

        <div class="vehicle-info">
          <div class="info-item" v-if="item.vehicleType">
            <span class="label">类型</span>
            <n-tag size="tiny" :bordered="false">{{ vehicleTypeLabel(item.vehicleType) }}</n-tag>
          </div>
          <div class="info-item" v-if="item.brand">
            <span class="label">品牌</span>
            <span class="value">{{ item.brand }}</span>
          </div>
          <div class="info-item" v-if="item.color">
            <span class="label">颜色</span>
            <span class="value">{{ item.color }}</span>
          </div>
        </div>

        <div class="card-actions">
          <n-button text type="primary" size="small" @click="openEditModal(item)">编辑</n-button>
          <n-button text type="error" size="small" @click="handleDelete(item)">删除</n-button>
        </div>
      </n-card>
    </div>

    <!-- Add / Edit Modal -->
    <n-modal v-model:show="formModalVisible" preset="card" :title="isEdit ? '编辑车辆' : '添加车辆'" style="max-width: 460px">
      <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="left" label-width="80">
        <n-form-item label="车牌号" path="plateNumber">
          <n-input v-model:value="formData.plateNumber" placeholder="请输入车牌号" />
        </n-form-item>
        <n-form-item label="车辆类型" path="vehicleType">
          <n-select v-model:value="formData.vehicleType" :options="vehicleTypeOptions" placeholder="请选择车辆类型" clearable />
        </n-form-item>
        <n-form-item label="品牌" path="brand">
          <n-input v-model:value="formData.brand" placeholder="请输入品牌（选填）" />
        </n-form-item>
        <n-form-item label="颜色" path="color">
          <n-input v-model:value="formData.color" placeholder="请输入颜色（选填）" />
        </n-form-item>
      </n-form>
      <template #action>
        <n-space justify="end">
          <n-button @click="formModalVisible = false">取消</n-button>
          <n-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useMessage, useDialog, type FormInst } from 'naive-ui'
import { getMyVehicles, addVehicle, updateVehicle, deleteVehicle } from '@/api/vehicle'

const message = useMessage()
const dialog = useDialog()

// --- Vehicle Type ---
const vehicleTypeOptions = [
  { label: '小型车', value: 'SMALL' },
  { label: '中型车', value: 'MEDIUM' },
  { label: '大型车', value: 'LARGE' },
  { label: '新能源', value: 'NEW_ENERGY' },
  { label: '摩托车', value: 'MOTORCYCLE' },
]

const vehicleTypeMap: Record<string, string> = {
  SMALL: '小型车',
  MEDIUM: '中型车',
  LARGE: '大型车',
  NEW_ENERGY: '新能源',
  MOTORCYCLE: '摩托车',
}

function vehicleTypeLabel(t: string) {
  return vehicleTypeMap[t] ?? t ?? '—'
}

// --- Data ---
const loading = ref(false)
const vehicles = ref<any[]>([])

async function fetchList() {
  loading.value = true
  try {
    const res: any = await getMyVehicles()
    vehicles.value = res.data ?? []
  } catch (e: any) {
    message.error(e.message || '获取车辆列表失败')
  } finally {
    loading.value = false
  }
}

// --- Form ---
const formModalVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const submitLoading = ref(false)
const formRef = ref<FormInst | null>(null)

const formData = reactive({
  plateNumber: '',
  vehicleType: null as string | null,
  brand: '',
  color: '',
})

const formRules = {
  plateNumber: { required: true, message: '请输入车牌号', trigger: 'blur' },
}

function resetForm() {
  formData.plateNumber = ''
  formData.vehicleType = null
  formData.brand = ''
  formData.color = ''
  editId.value = null
}

function openAddModal() {
  isEdit.value = false
  resetForm()
  formModalVisible.value = true
}

function openEditModal(item: any) {
  isEdit.value = true
  editId.value = item.id
  formData.plateNumber = item.plateNumber || ''
  formData.vehicleType = item.vehicleType || null
  formData.brand = item.brand || ''
  formData.color = item.color || ''
  formModalVisible.value = true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    const payload: any = {
      plateNumber: formData.plateNumber,
      vehicleType: formData.vehicleType ?? undefined,
      brand: formData.brand || undefined,
      color: formData.color || undefined,
    }

    if (isEdit.value && editId.value) {
      await updateVehicle(editId.value, payload)
      message.success('车辆信息已更新')
    } else {
      await addVehicle(payload)
      message.success('车辆添加成功')
    }
    formModalVisible.value = false
    fetchList()
  } catch (e: any) {
    message.error(e.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

// --- Delete ---
function handleDelete(item: any) {
  dialog.warning({
    title: '删除车辆',
    content: `确定要删除车牌号 ${item.plateNumber} 吗？`,
    positiveText: '确认删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteVehicle(item.id)
        message.success('删除成功')
        fetchList()
      } catch (e: any) {
        message.error(e.message || '删除失败')
      }
    },
  })
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.title-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.title-bar .section-title {
  margin-bottom: 0;
}

.loading-wrapper {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.vehicle-card {
  border-radius: var(--radius-md);
}

.plate-section {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.plate-number {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 2px;
}

.vehicle-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.info-item .label {
  color: var(--text-tertiary);
  font-size: 13px;
  min-width: 36px;
}

.info-item .value {
  color: var(--text-primary);
  font-size: 14px;
}

.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--border-color);
}

@media (max-width: 768px) {
  .card-grid {
    grid-template-columns: 1fr;
  }
}
</style>
