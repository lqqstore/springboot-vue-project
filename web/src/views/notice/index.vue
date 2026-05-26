<template>
  <div class="page-wrap">
    <div class="page-header">
      <div class="page-title">
        <el-icon :size="22"><Notification /></el-icon>
        <span>公告管理</span>
      </div>
      <el-button type="primary" :icon="Plus" @click="handleAdd">添加公告</el-button>
    </div>

    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchQuery" class="search-form">
        <el-form-item label="标题">
          <el-input
            v-model="searchQuery.title"
            placeholder="搜索公告标题"
            style="width: 220px;"
            clearable
            :prefix-icon="Search"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="currentPage = 1">搜索</el-button>
          <el-button :icon="RefreshRight" @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table
        v-loading="loading"
        :data="paginatedList"
        border
        stripe
        style="width: 100%;"
        empty-text="暂无公告数据"
        :header-cell-style="{ background: '#fafafa', color: '#303133', fontWeight: 600 }"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <div class="cell-title">
              <el-icon color="#409eff"><Reading /></el-icon>
              <span>{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="cell-content">{{ row.content }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="180" align="center">
          <template #default="{ row }">
            <div class="cell-time">
              <el-icon color="#909399"><Clock /></el-icon>
              <span>{{ row.publishTime }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm
              title="确定删除该公告？"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button size="small" type="danger" :icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          background
          layout="total, sizes, prev, pager, next"
          :total="displayList.length"
          :page-sizes="[10, 20, 50]"
        />
      </div>
    </el-card>

    <!-- 添加/编辑公告对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑公告' : '添加公告'"
      width="550px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" :prefix-icon="Edit" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            placeholder="请输入公告内容"
            :rows="5"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" :icon="Check" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  Notification, Plus, Search, RefreshRight, Edit, Delete, Check,
  Reading, Clock
} from '@element-plus/icons-vue'
import { noticeApi } from '@/api/notice'

interface Notice {
  id: number
  title: string
  content: string
  publishTime: string
}

interface Form {
  title: string
  content: string
}

const noticeList = ref<Notice[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const currentId = ref(0)
const formRef = ref<FormInstance | null>(null)

const currentPage = ref(1)
const pageSize = ref(10)

const searchQuery = reactive({
  title: ''
})

const form = ref<Form>({
  title: '',
  content: ''
})

const formRules: FormRules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在2-100个字符之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' },
    { min: 2, max: 2000, message: '内容长度在2-2000个字符之间', trigger: 'blur' }
  ]
}

const displayList = computed(() => {
  if (searchQuery.title) {
    return noticeList.value.filter(n => n.title.includes(searchQuery.title))
  }
  return noticeList.value
})

const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return displayList.value.slice(start, start + pageSize.value)
})

const getNoticeList = async () => {
  loading.value = true
  try {
    const response = await noticeApi.getNoticeList()
    noticeList.value = response.data.data
  } catch (error) {
    ElMessage.error('获取公告列表失败')
  } finally {
    loading.value = false
  }
}

const onReset = () => {
  searchQuery.title = ''
  currentPage.value = 1
}

const handleAdd = () => {
  isEdit.value = false
  currentId.value = 0
  form.value = {
    title: '',
    content: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row: Notice) => {
  isEdit.value = true
  currentId.value = row.id
  form.value = {
    title: row.title,
    content: row.content
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      saving.value = true
      if (isEdit.value) {
        const response = await noticeApi.updateNotice(currentId.value, form.value)
        if (response.data.code !== 0) throw new Error(response.data.msg || '编辑失败')
        ElMessage.success('编辑公告成功')
      } else {
        const response = await noticeApi.addNotice(form.value)
        if (response.data.code !== 0) throw new Error(response.data.msg || '添加失败')
        ElMessage.success('添加公告成功')
      }
      dialogVisible.value = false
      getNoticeList()
    } catch (error: any) {
      ElMessage.error(error?.message || (isEdit.value ? '编辑公告失败' : '添加公告失败'))
    } finally {
      saving.value = false
    }
  })
}

const handleDelete = async (id: number) => {
  try {
    const response = await noticeApi.deleteNotice(id)
    if (response.data.code !== 0) throw new Error(response.data.msg || '删除失败')
    ElMessage.success('删除公告成功')
    getNoticeList()
  } catch (error: any) {
    ElMessage.error(error?.message || '删除公告失败')
  }
}

onMounted(() => {
  getNoticeList()
})
</script>

<style scoped>
.page-wrap {
  max-width: 1300px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.search-card {
  margin-bottom: 16px;
}

.cell-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cell-content {
  color: #606266;
}

.cell-time {
  display: flex;
  align-items: center;
  gap: 6px;
  justify-content: center;
  color: #909399;
  font-size: 13px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
