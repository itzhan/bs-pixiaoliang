<template>
  <div class="vehicles-page">
    <!-- ===== Hero Header ===== -->
    <section class="page-hero">
      <div class="hero-bg">
        <div class="hero-shape hero-shape-1" />
        <div class="hero-shape hero-shape-2" />
      </div>
      <div class="hero-content">
        <div class="hero-icon-circle">
          <n-icon :size="32" color="#F59E0B"><CarOutline /></n-icon>
        </div>
        <h1 class="hero-title">我的车辆</h1>
        <p class="hero-subtitle">管理您的绑定车辆信息</p>
      </div>
    </section>

    <div class="page-container">
      <!-- ===== Add Button ===== -->
      <div class="action-bar">
        <span class="vehicle-count">
          共 <strong>{{ vehicles.length }}</strong> 辆车
        </span>
        <n-button type="primary" round @click="openAddModal">
          <template #icon><n-icon><AddOutline /></n-icon></template>
          添加车辆
        </n-button>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="loading-wrapper">
        <n-spin size="large" />
      </div>

      <!-- Empty -->
      <n-empty v-else-if="vehicles.length === 0" description="暂未绑定车辆，请先添加" style="margin-top: 60px">
        <template #extra>
          <n-button type="primary" size="small" round @click="openAddModal">添加车辆</n-button>
        </template>
      </n-empty>

      <!-- ===== Vehicle Cards ===== -->
      <div v-else class="card-grid">
        <div
          v-for="item in vehicles"
          :key="item.id"
          class="vehicle-card"
        >
          <!-- Plate Header -->
          <div class="plate-header">
            <div class="plate-badge">
              <n-icon :size="16" color="#fff"><CarOutline /></n-icon>
            </div>
            <span class="plate-number">{{ item.plateNumber }}</span>
            <n-tag v-if="item.isDefault === 1" type="warning" size="small" round :bordered="false">默认</n-tag>
          </div>

          <!-- Vehicle Details -->
          <div class="vehicle-info">
            <div class="info-item" v-if="item.vehicleType">
              <n-icon :size="14" color="#94A3B8"><PricetagOutline /></n-icon>
              <span class="label">类型</span>
              <n-tag size="tiny" :bordered="false">{{ vehicleTypeLabel(item.vehicleType) }}</n-tag>
            </div>
            <div class="info-item" v-if="item.brand">
              <n-icon :size="14" color="#94A3B8"><ShieldCheckmarkOutline /></n-icon>
              <span class="label">品牌</span>
              <span class="value">{{ item.brand }}</span>
            </div>
            <div class="info-item" v-if="item.color">
              <n-icon :size="14" color="#94A3B8"><ColorPaletteOutline /></n-icon>
              <span class="label">颜色</span>
              <span class="value">{{ item.color }}</span>
            </div>
          </div>

          <!-- Actions -->
          <div class="card-actions">
            <n-button text type="primary" size="small" @click="openEditModal(item)">
              <template #icon><n-icon :size="14"><CreateOutline /></n-icon></template>
              编辑
            </n-button>
            <n-button text type="error" size="small" @click="handleDelete(item)">
              <template #icon><n-icon :size="14"><TrashOutline /></n-icon></template>
              删除
            </n-button>
          </div>
        </div>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useMessage, useDialog, NIcon, type FormInst } from 'naive-ui'
import { getMyVehicles, addVehicle, updateVehicle, deleteVehicle } from '@/api/vehicle'
import {
  CarOutline,
  AddOutline,
  PricetagOutline,
  ShieldCheckmarkOutline,
  ColorPaletteOutline,
  CreateOutline,
  TrashOutline,
} from '@vicons/ionicons5'

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
/* ===== Hero Header ===== */
.page-hero {
  position: relative;
  z-index: 0;
  padding: 40px 24px 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: linear-gradient(135deg, #fef9ef 0%, #fef3c7 50%, #f0f7f4 100%);
}

.hero-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.hero-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.12;
}

.hero-shape-1 {
  width: 280px;
  height: 280px;
  background: #F59E0B;
  top: -80px;
  right: -60px;
}

.hero-shape-2 {
  width: 180px;
  height: 180px;
  background: #2d6a4f;
  bottom: -60px;
  left: -40px;
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.hero-icon-circle {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  background: rgba(245, 158, 11, 0.10);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}

.hero-title {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.hero-subtitle {
  font-size: 15px;
  color: #94a3b8;
  margin: 0;
}

/* ===== Container ===== */
.vehicles-page .page-container {
  max-width: 960px;
  margin: 0 auto;
  padding: 32px 24px 48px;
  position: relative;
  z-index: 1;
}

/* ===== Action Bar ===== */
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.vehicle-count {
  font-size: 14px;
  color: var(--text-secondary);
}

.vehicle-count strong {
  color: var(--text-primary);
  font-size: 18px;
}

/* ===== Vehicle Cards ===== */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.vehicle-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  padding: 20px;
  transition: box-shadow var(--transition-normal), transform var(--transition-normal);
}

.vehicle-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-3px);
}

/* Plate Header */
.plate-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.plate-badge {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #2D6A4F 0%, #40916C 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.plate-number {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 2px;
}

/* Vehicle Info */
.vehicle-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
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

/* Actions */
.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--border-color);
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .card-grid {
    grid-template-columns: 1fr;
  }

  .page-hero {
    padding: 32px 16px 40px;
  }

  .hero-title {
    font-size: 22px;
  }

  .vehicles-page .page-container {
    padding: 24px 16px 40px;
  }
}
</style>
