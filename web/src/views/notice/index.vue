<template>
  <div class="notice-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>公告管理</span>
          <el-button type="primary" @click="handleAdd">添加公告</el-button>
        </div>
      </template>
      
      <el-table :data="noticeList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="content" label="内容" :show-overflow-tooltip="true" />
        <el-table-column prop="publishTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑公告对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑公告' : '添加公告'"
      width="500px"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="form.content"
            type="textarea"
            placeholder="请输入公告内容"
            :rows="4"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
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
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentId = ref(0)
const form = ref<Form>({
  title: '',
  content: ''
})

// 获取公告列表
const getNoticeList = async () => {
  try {
    const response = await noticeApi.getNoticeList()
    noticeList.value = response.data.data
  } catch (error) {
    ElMessage.error('获取公告列表失败')
  }
}

// 添加公告
const handleAdd = () => {
  isEdit.value = false
  currentId.value = 0
  form.value = {
    title: '',
    content: ''
  }
  dialogVisible.value = true
}

// 编辑公告
const handleEdit = (row: Notice) => {
  isEdit.value = true
  currentId.value = row.id
  form.value = {
    title: row.title,
    content: row.content
  }
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  try {
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
  } catch (error) {
    ElMessage.error(isEdit.value ? '编辑公告失败' : '添加公告失败')
  }
}

// 删除公告
const handleDelete = async (id: number) => {
  try {
    const response = await noticeApi.deleteNotice(id)
    if (response.data.code !== 0) throw new Error(response.data.msg || '删除失败')
    ElMessage.success('删除公告成功')
    getNoticeList()
  } catch (error) {
    ElMessage.error('删除公告失败')
  }
}

// 初始化
onMounted(() => {
  getNoticeList()
})
</script>

<style scoped>
.notice-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  width: 100%;
  display: flex;
  justify-content: flex-end;
}
</style>
