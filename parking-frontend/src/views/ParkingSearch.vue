<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
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
  NSteps,
  NStep,
  useMessage,
} from 'naive-ui'
import { SearchOutline, TimeOutline, CheckmarkCircleOutline } from '@vicons/ionicons5'
import { getAllAreas, getAvailableSpaces } from '@/api/parking'
import { getMyVehicles } from '@/api/vehicle'
import { createReservation, getBookedSlots } from '@/api/reservation'
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

interface TimeSlot {
  startHour: number
  label: string
  startTime: string
  endTime: string
  booked: boolean
}

/* ---------- Composables ---------- */
const router = useRouter()
const route = useRoute()
const message = useMessage()
const userStore = useUserStore()

/* ---------- State ---------- */
const areas = ref<ParkingAreaItem[]>([])
const spaces = ref<ParkingSpaceItem[]>([])
const vehicles = ref<VehicleItem[]>([])

const areasLoading = ref(false)
const spacesLoading = ref(false)
const reserveLoading = ref(false)
const slotsLoading = ref(false)

// Filter
const selectedAreaId = ref<number | null>(null)
const selectedSpaceType = ref<string | null>(null)

// Reservation modal — multi-step
const showReservationModal = ref(false)
const selectedSpace = ref<ParkingSpaceItem | null>(null)
const currentStep = ref(0) // 0: 选日期&车辆, 1: 选时间段, 2: 确认&支付

const reserveForm = reactive({
  vehicleId: null as number | null,
  selectedDate: null as number | null, // timestamp from NDatePicker
})

// Time slots
const timeSlots = ref<TimeSlot[]>([])
const selectedSlotIndices = ref<number[]>([])

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

// 已选时间段的起止
const selectedTimeRange = computed(() => {
  if (selectedSlotIndices.value.length === 0) return null
  const sorted = [...selectedSlotIndices.value].sort((a, b) => a - b)
  const first = timeSlots.value[sorted[0]]
  const last = timeSlots.value[sorted[sorted.length - 1]]
  return { start: first.startTime, end: last.endTime, startLabel: first.label.split('-')[0], endLabel: last.label.split('-')[1] }
})

// 预估费用
const estimatedCost = computed(() => {
  if (!selectedTimeRange.value || !selectedSpace.value) return 0
  const hours = selectedSlotIndices.value.length
  return Math.round(hours * Number(selectedSpace.value.hourlyRate) * 100) / 100
})

// 选中的车辆信息
const selectedVehicle = computed(() => {
  return vehicles.value.find((v) => v.id === reserveForm.vehicleId)
})

// 选中日期的格式化
const selectedDateStr = computed(() => {
  if (!reserveForm.selectedDate) return ''
  return dayjs(reserveForm.selectedDate).format('YYYY-MM-DD')
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

/* ---------- Time Slots ---------- */
async function loadTimeSlots() {
  if (!selectedSpace.value || !reserveForm.selectedDate) return
  slotsLoading.value = true
  try {
    const dateStr = dayjs(reserveForm.selectedDate).format('YYYY-MM-DD')
    const res: any = await getBookedSlots(selectedSpace.value.id, dateStr)
    const bookedRanges: { start: string; end: string }[] = res.data || []

    // 生成全天时间段 (08:00 ~ 22:00, 每小时一个)
    const slots: TimeSlot[] = []
    for (let h = 8; h < 22; h++) {
      const startH = String(h).padStart(2, '0')
      const endH = String(h + 1).padStart(2, '0')
      const slotStart = `${startH}:00`
      const slotEnd = `${endH}:00`

      // 检查是否与已预约时段重叠
      const isBooked = bookedRanges.some((range) => {
        return slotStart < range.end && slotEnd > range.start
      })

      // 过去的时间段也标记为不可用（仅当日）
      const isToday = dayjs(reserveForm.selectedDate).isSame(dayjs(), 'day')
      const isPast = isToday && h < dayjs().hour()

      slots.push({
        startHour: h,
        label: `${slotStart} - ${slotEnd}`,
        startTime: `${dateStr} ${slotStart}:00`,
        endTime: `${dateStr} ${slotEnd}:00`,
        booked: isBooked || isPast,
      })
    }
    timeSlots.value = slots
    selectedSlotIndices.value = []
  } catch {
    /* handled by interceptor */
  } finally {
    slotsLoading.value = false
  }
}

function toggleSlot(index: number) {
  const slot = timeSlots.value[index]
  if (slot.booked) return

  const idx = selectedSlotIndices.value.indexOf(index)
  if (idx !== -1) {
    selectedSlotIndices.value.splice(idx, 1)
  } else {
    // 确保连续选择
    if (selectedSlotIndices.value.length === 0) {
      selectedSlotIndices.value.push(index)
    } else {
      const sorted = [...selectedSlotIndices.value].sort((a, b) => a - b)
      const min = sorted[0]
      const max = sorted[sorted.length - 1]

      if (index === min - 1 || index === max + 1) {
        // 相邻 - 可以添加
        selectedSlotIndices.value.push(index)
      } else if (index >= min && index <= max) {
        // 在范围内，取消后缩小范围
        selectedSlotIndices.value.splice(idx, 1)
      } else {
        // 不相邻，重新选择
        selectedSlotIndices.value = [index]
      }
    }
  }
}

function isSlotSelected(index: number) {
  return selectedSlotIndices.value.includes(index)
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
  reserveForm.selectedDate = null
  currentStep.value = 0
  timeSlots.value = []
  selectedSlotIndices.value = []
  showReservationModal.value = true
  loadMyVehicles()
}

function goToStep1() {
  // 验证 Step 0
  if (!reserveForm.vehicleId) {
    message.warning('请选择车辆')
    return
  }
  if (!reserveForm.selectedDate) {
    message.warning('请选择停车日期')
    return
  }
  currentStep.value = 1
  loadTimeSlots()
}

function goToStep2() {
  if (selectedSlotIndices.value.length === 0) {
    message.warning('请至少选择一个时间段')
    return
  }
  currentStep.value = 2
}

function goBack(step: number) {
  currentStep.value = step
}

async function handleSubmit(payNow: boolean) {
  if (!selectedSpace.value || !selectedTimeRange.value || !reserveForm.vehicleId) return

  reserveLoading.value = true
  try {
    await createReservation({
      vehicleId: reserveForm.vehicleId,
      spaceId: selectedSpace.value.id,
      startTime: selectedTimeRange.value.start,
      endTime: selectedTimeRange.value.end,
      payNow,
    })
    message.success(payNow ? '预约成功，已完成支付！' : '预约成功，请稍后完成支付！')
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

    <!-- ========== Reservation Modal (Multi-Step) ========== -->
    <n-modal
      v-model:show="showReservationModal"
      preset="card"
      title="预约车位"
      :style="{ width: '560px' }"
      :mask-closable="false"
    >
      <!-- Space Info Banner -->
      <div v-if="selectedSpace" class="reserve-space-info">
        <span>
          车位：<strong>{{ selectedSpace.spaceNumber }}</strong>
          <span style="margin-left: 8px; color: #94a3b8;">{{ getAreaName(selectedSpace.areaId) }}</span>
        </span>
        <span class="reserve-price">¥{{ selectedSpace.hourlyRate }}/小时</span>
      </div>

      <!-- Steps indicator -->
      <n-steps :current="currentStep + 1" size="small" style="margin-bottom: 24px;">
        <n-step title="选择日期" />
        <n-step title="选择时段" />
        <n-step title="确认预约" />
      </n-steps>

      <!-- ===== Step 0: 选日期 & 车辆 ===== -->
      <div v-if="currentStep === 0" class="step-content">
        <n-form label-placement="left" label-width="80">
          <n-form-item label="选择车辆">
            <n-select
              v-model:value="reserveForm.vehicleId"
              :options="vehicleOptions"
              placeholder="请选择车辆"
            />
          </n-form-item>
          <n-form-item label="停车日期">
            <n-date-picker
              v-model:value="reserveForm.selectedDate"
              type="date"
              placeholder="请选择停车日期"
              :is-date-disabled="disablePastDate"
              style="width: 100%"
            />
          </n-form-item>
        </n-form>
        <div class="modal-footer">
          <n-button @click="showReservationModal = false">取消</n-button>
          <n-button type="primary" @click="goToStep1">下一步</n-button>
        </div>
      </div>

      <!-- ===== Step 1: 选择时间段 ===== -->
      <div v-if="currentStep === 1" class="step-content">
        <div class="step-hint">
          <n-icon :size="16" color="#3B82F6" style="vertical-align: -3px; margin-right: 4px;"><TimeOutline /></n-icon>
          {{ selectedDateStr }} 可用时间段（点击选择连续时段）
        </div>
        <n-spin :show="slotsLoading">
          <div class="slot-grid">
            <div
              v-for="(slot, idx) in timeSlots"
              :key="idx"
              :class="['slot-item', { booked: slot.booked, selected: isSlotSelected(idx) }]"
              @click="toggleSlot(idx)"
            >
              <span class="slot-label">{{ slot.label }}</span>
              <span v-if="slot.booked" class="slot-tag">已预约</span>
              <n-icon v-if="isSlotSelected(idx)" :size="16" color="#2D6A4F" class="slot-check">
                <CheckmarkCircleOutline />
              </n-icon>
            </div>
          </div>
        </n-spin>
        <div v-if="selectedTimeRange" class="selected-range-info">
          已选：<strong>{{ selectedTimeRange.startLabel }} - {{ selectedTimeRange.endLabel }}</strong>
          （共 {{ selectedSlotIndices.length }} 小时，预估 ¥{{ estimatedCost.toFixed(2) }}）
        </div>
        <div class="modal-footer">
          <n-button @click="goBack(0)">上一步</n-button>
          <n-button type="primary" @click="goToStep2" :disabled="selectedSlotIndices.length === 0">下一步</n-button>
        </div>
      </div>

      <!-- ===== Step 2: 确认 & 支付 ===== -->
      <div v-if="currentStep === 2" class="step-content">
        <div class="confirm-card">
          <div class="confirm-row">
            <span class="confirm-label">车位</span>
            <span class="confirm-value">{{ selectedSpace?.spaceNumber }} · {{ getAreaName(selectedSpace?.areaId || 0) }}</span>
          </div>
          <div class="confirm-row">
            <span class="confirm-label">车辆</span>
            <span class="confirm-value">{{ selectedVehicle?.plateNumber }} {{ [selectedVehicle?.brand, selectedVehicle?.color].filter(Boolean).join(' ') }}</span>
          </div>
          <div class="confirm-row">
            <span class="confirm-label">日期</span>
            <span class="confirm-value">{{ selectedDateStr }}</span>
          </div>
          <div class="confirm-row">
            <span class="confirm-label">时段</span>
            <span class="confirm-value">{{ selectedTimeRange?.startLabel }} - {{ selectedTimeRange?.endLabel }}（{{ selectedSlotIndices.length }}小时）</span>
          </div>
          <div class="confirm-row amount-row">
            <span class="confirm-label">金额</span>
            <span class="confirm-value price-text-large">¥{{ estimatedCost.toFixed(2) }}</span>
          </div>
        </div>
        <div class="modal-footer pay-footer">
          <n-button @click="goBack(1)">上一步</n-button>
          <div class="pay-buttons">
            <n-button
              :loading="reserveLoading"
              @click="handleSubmit(false)"
              size="medium"
            >
              稍后支付
            </n-button>
            <n-button
              type="primary"
              :loading="reserveLoading"
              @click="handleSubmit(true)"
              size="medium"
            >
              立即支付
            </n-button>
          </div>
        </div>
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

/* Step content */
.step-content {
  min-height: 200px;
}

.step-hint {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 16px;
  font-weight: 500;
}

/* ===== Time Slot Grid ===== */
.slot-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 16px;
}

.slot-item {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 4px;
  padding: 14px 10px;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
  user-select: none;
}

.slot-item:hover:not(.booked) {
  border-color: #2d6a4f;
  background: rgba(45, 106, 79, 0.03);
}

.slot-item.selected {
  border-color: #2d6a4f;
  background: rgba(45, 106, 79, 0.08);
}

.slot-item.booked {
  background: #f1f5f9;
  border-color: #e2e8f0;
  cursor: not-allowed;
  opacity: 0.6;
}

.slot-label {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.booked .slot-label {
  color: #94a3b8;
}

.slot-tag {
  font-size: 11px;
  color: #ef4444;
  font-weight: 500;
}

.slot-check {
  position: absolute;
  top: 6px;
  right: 6px;
}

.selected-range-info {
  padding: 12px 16px;
  background: #fff7ed;
  border-radius: 8px;
  font-size: 14px;
  color: #1e293b;
  margin-bottom: 16px;
}

/* ===== Confirm Card ===== */
.confirm-card {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.confirm-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.confirm-label {
  font-size: 14px;
  color: #94a3b8;
  min-width: 48px;
}

.confirm-value {
  font-size: 14px;
  color: #1e293b;
  font-weight: 500;
  text-align: right;
}

.amount-row {
  padding-top: 14px;
  border-top: 1px dashed #e2e8f0;
}

.price-text-large {
  font-size: 22px;
  font-weight: 700;
  color: #e07a5f;
}

/* ===== Footer ===== */
.modal-footer {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 8px;
}

.pay-footer {
  align-items: center;
}

.pay-buttons {
  display: flex;
  gap: 10px;
}

/* ========== Responsive ========== */
@media (max-width: 900px) {
  .area-grid,
  .spaces-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .slot-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 540px) {
  .area-grid,
  .spaces-grid {
    grid-template-columns: 1fr;
  }

  .slot-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
