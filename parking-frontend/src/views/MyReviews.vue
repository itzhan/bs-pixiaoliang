<template>
  <div class="page-container">
    <div class="title-bar">
      <h2 class="section-title">我的评价</h2>
      <n-button type="primary" @click="openCreateModal">
        发表评价
      </n-button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-wrapper">
      <n-spin size="large" />
    </div>

    <!-- Empty -->
    <n-empty v-else-if="reviews.length === 0" description="暂无评价记录" style="margin-top: 60px" />

    <!-- Review Cards -->
    <div v-else class="card-list">
      <n-card
        v-for="item in reviews"
        :key="item.id"
        class="review-card card-hover"
        :bordered="false"
      >
        <div class="card-header">
          <n-rate :value="item.rating" readonly size="small" />
          <span class="review-time">{{ formatTime(item.createTime) }}</span>
        </div>

        <div class="review-content">
          {{ item.content || '该用户未填写评价内容' }}
        </div>

        <div v-if="item.reply" class="review-reply">
          <div class="reply-label">商家回复</div>
          <div class="reply-content">{{ item.reply }}</div>
        </div>
      </n-card>
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
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useMessage, type FormInst } from 'naive-ui'
import dayjs from 'dayjs'
import { getMyReviews, createReview } from '@/api/review'

const message = useMessage()

function formatTime(t: string) {
  return t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '—'
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

.card-list {
  display: grid;
  gap: 16px;
  margin-top: 20px;
}

.review-card {
  border-radius: var(--radius-md);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
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

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}
</style>
