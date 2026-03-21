<template>
  <div class="reviews-page">
    <!-- ===== Hero Header ===== -->
    <section class="page-hero">
      <div class="hero-bg">
        <div class="hero-shape hero-shape-1" />
        <div class="hero-shape hero-shape-2" />
      </div>
      <div class="hero-content">
        <div class="hero-icon-circle">
          <n-icon :size="32" color="#8B5CF6"><StarOutline /></n-icon>
        </div>
        <h1 class="hero-title">我的评价</h1>
        <p class="hero-subtitle">分享您的停车体验，帮助我们改进服务</p>
      </div>
    </section>

    <div class="page-container">
      <!-- ===== Action Bar ===== -->
      <div class="action-bar">
        <span class="review-count">
          共 <strong>{{ total }}</strong> 条评价
        </span>
        <n-button type="primary" round @click="openCreateModal">
          <template #icon><n-icon><CreateOutline /></n-icon></template>
          发表评价
        </n-button>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="loading-wrapper">
        <n-spin size="large" />
      </div>

      <!-- Empty -->
      <n-empty v-else-if="reviews.length === 0" description="暂无评价记录" style="margin-top: 60px" />

      <!-- ===== Review Cards ===== -->
      <div v-else class="card-list">
        <div
          v-for="item in reviews"
          :key="item.id"
          class="review-card"
        >
          <div class="card-color-bar" :style="{ background: getRatingColor(item.rating) }" />
          <div class="card-inner">
            <div class="card-header">
              <div class="rating-section">
                <n-rate :value="item.rating" readonly size="small" />
                <span class="rating-label">{{ ratingText(item.rating) }}</span>
              </div>
              <span class="review-time">
                <n-icon :size="13" style="margin-right: 3px; vertical-align: -2px;"><TimeOutline /></n-icon>
                {{ formatTime(item.createdAt) }}
              </span>
            </div>

            <div class="review-content">
              {{ item.content || '该用户未填写评价内容' }}
            </div>

            <div v-if="item.reply" class="review-reply">
              <div class="reply-label">
                <n-icon :size="14" style="margin-right: 4px; vertical-align: -2px;"><ChatbubblesOutline /></n-icon>
                商家回复
              </div>
              <div class="reply-content">{{ item.reply }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="pagination-wrapper" v-if="total > pageSize">
        <n-pagination
          v-model:page="currentPage"
          :page-size="pageSize"
          :item-count="total"
          @update:page="fetchList"
        />
      </div>

      <!-- Create Review Modal -->
      <n-modal v-model:show="createModalVisible" preset="card" title="发表评价" style="max-width: 480px">
        <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="top">
          <n-form-item label="关联订单" path="orderId">
            <n-input
              v-model:value="formData.orderIdStr"
              placeholder="请输入订单编号"
            />
          </n-form-item>
          <n-form-item label="评分" path="rating">
            <n-rate v-model:value="formData.rating" />
          </n-form-item>
          <n-form-item label="评价内容" path="content">
            <n-input
              v-model:value="formData.content"
              type="textarea"
              placeholder="请分享您的停车体验..."
              :rows="4"
              :maxlength="500"
              show-count
            />
          </n-form-item>
        </n-form>
        <template #action>
          <n-space justify="end">
            <n-button @click="createModalVisible = false">取消</n-button>
            <n-button type="primary" :loading="submitLoading" @click="handleCreate">提交评价</n-button>
          </n-space>
        </template>
      </n-modal>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useMessage, NIcon, type FormInst } from 'naive-ui'
import dayjs from 'dayjs'
import { getMyReviews, createReview } from '@/api/review'
import {
  StarOutline,
  CreateOutline,
  TimeOutline,
  ChatbubblesOutline,
} from '@vicons/ionicons5'

const message = useMessage()

function formatTime(t: string) {
  return t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '—'
}

function ratingText(rating: number) {
  const texts = ['', '很差', '较差', '一般', '满意', '非常满意']
  return texts[rating] ?? ''
}

function getRatingColor(rating: number): string {
  if (rating >= 4) return '#2D6A4F'
  if (rating >= 3) return '#F59E0B'
  return '#EF4444'
}

// --- Data ---
const loading = ref(false)
const reviews = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10

async function fetchList() {
  loading.value = true
  try {
    const res: any = await getMyReviews({ page: currentPage.value, size: pageSize })
    reviews.value = res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } catch (e: any) {
    message.error(e.message || '获取评价列表失败')
  } finally {
    loading.value = false
  }
}

// --- Create Review ---
const createModalVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInst | null>(null)

const formData = reactive({
  orderIdStr: '',
  rating: 5,
  content: '',
})

const formRules = {
  orderIdStr: { required: true, message: '请输入订单编号', trigger: 'blur' },
  rating: { required: true, type: 'number' as const, min: 1, message: '请选择评分', trigger: 'change' },
  content: { required: true, message: '请输入评价内容', trigger: 'blur' },
}

function openCreateModal() {
  formData.orderIdStr = ''
  formData.rating = 5
  formData.content = ''
  createModalVisible.value = true
}

async function handleCreate() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  const orderId = parseInt(formData.orderIdStr, 10)
  if (isNaN(orderId) || orderId <= 0) {
    message.warning('请输入有效的订单编号')
    return
  }

  submitLoading.value = true
  try {
    await createReview({
      orderId,
      rating: formData.rating,
      content: formData.content,
    })
    message.success('评价提交成功')
    createModalVisible.value = false
    currentPage.value = 1
    fetchList()
  } catch (e: any) {
    message.error(e.message || '提交评价失败')
  } finally {
    submitLoading.value = false
  }
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
  background: linear-gradient(135deg, #f5f3ff 0%, #ede9fe 50%, #f0f7f4 100%);
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
  background: #8B5CF6;
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
  background: rgba(139, 92, 246, 0.10);
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
.reviews-page .page-container {
  max-width: 900px;
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

.review-count {
  font-size: 14px;
  color: var(--text-secondary);
}

.review-count strong {
  color: var(--text-primary);
  font-size: 18px;
}

/* ===== Review Cards ===== */
.review-card {
  display: flex;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  transition: box-shadow var(--transition-normal), transform var(--transition-normal);
}

.review-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.card-color-bar {
  width: 4px;
  flex-shrink: 0;
}

.card-inner {
  flex: 1;
  padding: 18px 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.rating-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rating-label {
  font-size: 13px;
  color: var(--accent);
  font-weight: 600;
}

.review-time {
  font-size: 12px;
  color: var(--text-tertiary);
}

.review-content {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.7;
}

.review-reply {
  margin-top: 14px;
  padding: 12px 14px;
  background: var(--bg-page);
  border-radius: var(--radius-sm);
  border-left: 3px solid var(--primary);
}

.reply-label {
  font-size: 12px;
  color: var(--primary);
  font-weight: 600;
  margin-bottom: 4px;
}

.reply-content {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .page-hero {
    padding: 32px 16px 40px;
  }

  .hero-title {
    font-size: 22px;
  }

  .reviews-page .page-container {
    padding: 24px 16px 40px;
  }
}
</style>
