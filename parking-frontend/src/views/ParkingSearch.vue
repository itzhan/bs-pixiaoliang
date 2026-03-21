<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  NButton,
  NCard,
  NSelect,
  NTag,
  NModal,
  NForm,
  NFormItem,
  NDatePicker,
  NIcon,
  NSpin,
  NEmpty,
  useMessage,
  useDialog,
} from 'naive-ui'
import type { FormInst } from 'naive-ui'
import { SearchOutline } from '@vicons/ionicons5'
import { getAllAreas, getAvailableSpaces } from '@/api/parking'
import { getMyVehicles } from '@/api/vehicle'
import { createReservation } from '@/api/reservation'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

/* ---------- Types ---------- */
interface ParkingAreaItem {
  id: number
  name: string
  code: string
  description: string
  totalSpaces: number
  availableSpaces: number
  floorNumber: number
  areaType: string
}

interface ParkingSpaceItem {
  id: number
  areaId: number
  spaceNumber: string
  spaceType: string
  status: number
  hourlyRate: number
}

interface VehicleItem {
  id: number
  plateNumber: string
  vehicleType: string
  brand: string
  color: string
}

/* ---------- Composables ---------- */
const router = useRouter()
const route = useRoute()
const message = useMessage()
const dialog = useDialog()
const userStore = useUserStore()

/* ---------- State ---------- */
const areas = ref<ParkingAreaItem[]>([])
const spaces = ref<ParkingSpaceItem[]>([])
const vehicles = ref<VehicleItem[]>([])

const areasLoading = ref(false)
const spacesLoading = ref(false)
const reserveLoading = ref(false)

// Filter
const selectedAreaId = ref<number | null>(null)
const selectedSpaceType = ref<string | null>(null)

// Reservation modal
const showReservationModal = ref(false)
const selectedSpace = ref<ParkingSpaceItem | null>(null)
const reserveFormRef = ref<FormInst | null>(null)
const reserveForm = reactive({
  vehicleId: null as number | null,
  startTime: null as number | null,
  endTime: null as number | null,
})

/* ---------- Computed ---------- */
const areaOptions = computed(() =>
  areas.value.map((a) => ({ label: `${a.name} (${a.code})`, value: a.id })),
)

const spaceTypeOptions = [
  { label: '标准', value: 'STANDARD' },
  { label: 'VIP', value: 'VIP' },
  { label: '残疾人', value: 'DISABLED' },
  { label: '充电', value: 'CHARGING' },
]

const vehicleOptions = computed(() =>
  vehicles.value.map((v) => ({
    label: `${v.plateNumber} (${[v.brand, v.color].filter(Boolean).join(' ')})`,
    value: v.id,
  })),
)

const estimatedCost = computed(() => {
  if (!reserveForm.startTime || !reserveForm.endTime || !selectedSpace.value) return 0
  const hours = (reserveForm.endTime - reserveForm.startTime) / (1000 * 60 * 60)
  if (hours <= 0) return 0
  return Math.round(hours * Number(selectedSpace.value.hourlyRate) * 100) / 100
})

/* ---------- Label maps ---------- */
const spaceTypeLabel: Record<string, string> = {
  STANDARD: '标准',
  VIP: 'VIP',
  DISABLED: '残疾人',
  CHARGING: '充电',
}

const spaceTypeTagType: Record<string, 'default' | 'warning' | 'info' | 'success'> = {
  STANDARD: 'default',
  VIP: 'warning',
  DISABLED: 'info',
  CHARGING: 'success',
}

const areaTypeLabel: Record<string, string> = {
  INDOOR: '室内',
  OUTDOOR: '室外',
}

/* ---------- Validation ---------- */
const reserveRules = {
  vehicleId: {
    required: true,
    type: 'number' as const,
    message: '请选择车辆',
    trigger: 'change',
  },
  startTime: {
    required: true,
    type: 'number' as const,
    message: '请选择开始时间',
    trigger: 'change',
  },
  endTime: {
    required: true,
    type: 'number' as const,
    message: '请选择结束时间',
    trigger: 'change',
  },
}

/* ---------- Date restrictions ---------- */
function disablePastDate(ts: number) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return ts < today.getTime()
}

/* ---------- Data loading ---------- */
async function loadAreas() {
  areasLoading.value = true
  try {
    const res: any = await getAllAreas()
    areas.value = res.data || []
  } catch {
    /* handled by interceptor */
  } finally {
    areasLoading.value = false
  }
}

async function loadSpaces() {
  spacesLoading.value = true
  try {
    const params: { areaId?: number; spaceType?: string } = {}
    if (selectedAreaId.value) params.areaId = selectedAreaId.value
    if (selectedSpaceType.value) params.spaceType = selectedSpaceType.value
    const res: any = await getAvailableSpaces(params)
    spaces.value = res.data || []
  } catch {
    /* handled by interceptor */
  } finally {
    spacesLoading.value = false
  }
}

async function loadMyVehicles() {
  try {
    const res: any = await getMyVehicles()
    vehicles.value = res.data || []
  } catch {
    /* handled by interceptor */
  }
}

/* ---------- Actions ---------- */
function handleSearch() {
  loadSpaces()
}

function selectArea(areaId: number) {
  selectedAreaId.value = areaId
  loadSpaces()
}

function getAreaName(areaId: number): string {
  const area = areas.value.find((a) => a.id === areaId)
  return area ? area.name : ''
}

function handleReserve(space: ParkingSpaceItem) {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录')
    router.push(`/login?redirect=${route.fullPath}`)
    return
  }
  selectedSpace.value = space
  reserveForm.vehicleId = null
  reserveForm.startTime = null
  reserveForm.endTime = null
  showReservationModal.value = true
  loadMyVehicles()
}

async function handleSubmitReservation() {
  try {
    await reserveFormRef.value?.validate()
  } catch {
    return
  }

  if (
    !selectedSpace.value ||
    !reserveForm.startTime ||
    !reserveForm.endTime ||
    !reserveForm.vehicleId
  ) {
    return
  }

  if (reserveForm.endTime <= reserveForm.startTime) {
    message.error('结束时间必须晚于开始时间')
    return
  }

  // 二次确认
  const vehicle = vehicles.value.find((v) => v.id === reserveForm.vehicleId)
  const costStr = estimatedCost.value > 0 ? `¥${estimatedCost.value.toFixed(2)}` : '待计算'
  dialog.warning({
    title: '确认预约信息',
    content: () =>
      `车位：${selectedSpace.value!.spaceNumber}\n` +
      `车辆：${vehicle?.plateNumber || ''}\n` +
      `时间：${dayjs(reserveForm.startTime).format('MM-DD HH:mm')} ~ ${dayjs(reserveForm.endTime).format('MM-DD HH:mm')}\n` +
      `预估费用：${costStr}`,
    positiveText: '确认预约',
    negativeText: '再想想',
    onPositiveClick: doReserve,
  })
}

async function doReserve() {
  reserveLoading.value = true
  try {
    await createReservation({
      vehicleId: reserveForm.vehicleId!,
      spaceId: selectedSpace.value!.id,
      startTime: dayjs(reserveForm.startTime).format('YYYY-MM-DD HH:mm:ss'),
      endTime: dayjs(reserveForm.endTime).format('YYYY-MM-DD HH:mm:ss'),
    })
    message.success('预约成功！')
    showReservationModal.value = false
    loadAreas()
    loadSpaces()
  } catch {
    /* handled by interceptor */
  } finally {
    reserveLoading.value = false
  }
}

/* ---------- Init ---------- */
onMounted(() => {
  loadAreas()
  loadSpaces()
})
</script>

<template>
  <div class="parking-search-page">
    <div class="container">
      <!-- Page Header -->
      <div class="page-header">
        <h1 class="page-title">车位查询</h1>
        <p class="page-subtitle">实时查看停车位状态，在线预约心仪车位</p>
      </div>

      <!-- Filter Bar -->
      <div class="filter-bar">
        <n-select
          v-model:value="selectedAreaId"
          :options="areaOptions"
          placeholder="选择区域"
          clearable
          style="width: 200px"
        />
        <n-select
          v-model:value="selectedSpaceType"
          :options="spaceTypeOptions"
          placeholder="车位类型"
          clearable
          style="width: 160px"
        />
        <n-button type="primary" @click="handleSearch">
          <template #icon>
            <n-icon><SearchOutline /></n-icon>
          </template>
          搜索
        </n-button>
      </div>

      <!-- Area Overview Cards -->
      <div class="section-label">区域概览</div>
      <n-spin :show="areasLoading">
        <div class="area-grid">
          <n-card
            v-for="area in areas"
            :key="area.id"
            hoverable
            :class="['area-card', { selected: selectedAreaId === area.id }]"
            @click="selectArea(area.id)"
          >
            <div class="area-card-header">
              <span class="area-name">{{ area.name }}</span>
              <n-tag size="small" :bordered="false">
                {{ areaTypeLabel[area.areaType] || area.areaType }}
              </n-tag>
            </div>
            <div class="area-card-stats">
              <span class="area-available">{{ area.availableSpaces }}</span>
              <span class="area-total">/ {{ area.totalSpaces }} 车位</span>
            </div>
            <div class="area-card-floor">
              {{ area.floorNumber || '' }}
            </div>
          </n-card>
        </div>
        <n-empty
          v-if="!areasLoading && areas.length === 0"
          description="暂无停车区域"
        />
      </n-spin>

      <!-- Available Spaces -->
      <div class="section-label" style="margin-top: 40px">可用车位</div>
      <n-spin :show="spacesLoading">
        <div class="spaces-grid">
          <n-card
            v-for="space in spaces"
            :key="space.id"
            hoverable
            class="space-card"
          >
            <div class="space-header">
              <span class="space-number">{{ space.spaceNumber }}</span>
              <n-tag
                :type="spaceTypeTagType[space.spaceType] || 'default'"
                size="small"
              >
                {{ spaceTypeLabel[space.spaceType] || space.spaceType }}
              </n-tag>
            </div>
            <div class="space-area">{{ getAreaName(space.areaId) }}</div>
            <div class="space-price">
              ¥{{ space.hourlyRate }}<span class="price-unit">/小时</span>
            </div>
            <div class="space-footer">
              <n-tag type="success" size="small" round>空闲</n-tag>
              <n-button
                type="primary"
                size="small"
                @click="handleReserve(space)"
              >
                预约
              </n-button>
            </div>
          </n-card>
        </div>
        <n-empty
          v-if="!spacesLoading && spaces.length === 0"
          description="暂无可用车位"
        />
      </n-spin>
    </div>

    <!-- Reservation Modal -->
    <n-modal
      v-model:show="showReservationModal"
      preset="card"
      title="预约车位"
      :style="{ width: '480px' }"
      :mask-closable="false"
    >
      <div v-if="selectedSpace" class="reserve-space-info">
        <span>
          车位：<strong>{{ selectedSpace.spaceNumber }}</strong>
        </span>
        <span class="reserve-price">¥{{ selectedSpace.hourlyRate }}/小时</span>
      </div>

      <n-form
        ref="reserveFormRef"
        :model="reserveForm"
        :rules="reserveRules"
        label-placement="left"
        label-width="80"
      >
        <n-form-item label="选择车辆" path="vehicleId">
          <n-select
            v-model:value="reserveForm.vehicleId"
            :options="vehicleOptions"
            placeholder="请选择车辆"
          />
        </n-form-item>
        <n-form-item label="开始时间" path="startTime">
          <n-date-picker
            v-model:value="reserveForm.startTime"
            type="datetime"
            placeholder="请选择开始时间"
            :is-date-disabled="disablePastDate"
            style="width: 100%"
          />
        </n-form-item>
        <n-form-item label="结束时间" path="endTime">
          <n-date-picker
            v-model:value="reserveForm.endTime"
            type="datetime"
            placeholder="请选择结束时间"
            :is-date-disabled="disablePastDate"
            style="width: 100%"
          />
        </n-form-item>
      </n-form>

      <div v-if="estimatedCost > 0" class="estimated-cost">
        预估费用：<span class="cost-value">¥{{ estimatedCost.toFixed(2) }}</span>
      </div>

      <div class="modal-footer">
        <n-button @click="showReservationModal = false">取消</n-button>
        <n-button
          type="primary"
          :loading="reserveLoading"
          @click="handleSubmitReservation"
        >
          确认预约
        </n-button>
      </div>
    </n-modal>
  </div>
</template>

<style scoped>
.parking-search-page {
  padding: 32px 0 64px;
  background: #f8f9fa;
  min-height: calc(100vh - 64px);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.page-header {
  margin-bottom: 32px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.page-subtitle {
  font-size: 15px;
  color: #94a3b8;
}

/* ========== Filter ========== */
.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 32px;
  flex-wrap: wrap;
}

/* ========== Section Label ========== */
.section-label {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 16px;
}

/* ========== Area Cards ========== */
.area-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.area-card {
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid transparent;
}

.area-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.area-card.selected {
  border-color: #2d6a4f;
}

.area-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.area-name {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}

.area-card-stats {
  margin-bottom: 4px;
}

.area-available {
  font-size: 28px;
  font-weight: 700;
  color: #2d6a4f;
}

.area-total {
  font-size: 14px;
  color: #94a3b8;
  margin-left: 4px;
}

.area-card-floor {
  font-size: 13px;
  color: #94a3b8;
}

/* ========== Space Cards ========== */
.spaces-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.space-card {
  border-radius: 12px;
  transition: all 0.2s;
}

.space-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.space-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.space-number {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.space-area {
  font-size: 13px;
  color: #94a3b8;
  margin-bottom: 8px;
}

.space-price {
  font-size: 20px;
  font-weight: 700;
  color: #e07a5f;
  margin-bottom: 12px;
}

.price-unit {
  font-size: 13px;
  font-weight: 400;
  color: #94a3b8;
}

.space-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* ========== Reservation Modal ========== */
.reserve-space-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 14px;
  color: #1e293b;
}

.reserve-price {
  font-weight: 600;
  color: #e07a5f;
}

.estimated-cost {
  padding: 12px 16px;
  background: #fff7ed;
  border-radius: 8px;
  font-size: 15px;
  color: #1e293b;
  margin-bottom: 16px;
}

.cost-value {
  font-size: 20px;
  font-weight: 700;
  color: #e07a5f;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}

/* ========== Responsive ========== */
@media (max-width: 900px) {
  .area-grid,
  .spaces-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 540px) {
  .area-grid,
  .spaces-grid {
    grid-template-columns: 1fr;
  }
}
</style>
