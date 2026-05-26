<template>
  <div class="page-wrap">
    <div class="page-header">
      <div class="page-title">
        <el-icon :size="22"><OfficeBuilding /></el-icon>
        <span>楼栋管理</span>
      </div>
      <el-button type="primary" :icon="Plus" @click="openAdd">新增楼栋</el-button>
    </div>

    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="楼栋名称">
          <el-input
            v-model="query.name"
            placeholder="请输入楼栋名称"
            style="width: 220px;"
            clearable
            :prefix-icon="Search"
            @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
          <el-button :icon="RefreshRight" @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%;"
        empty-text="暂无楼栋数据"
        :header-cell-style="{ background: '#fafafa', color: '#303133', fontWeight: 600 }"
      >
        <el-table-column prop="name" label="楼栋名称" min-width="200">
          <template #default="{ row }">
            <div class="cell-building">
              <el-icon color="#409eff"><OfficeBuilding /></el-icon>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" min-width="260">
          <template #default="{ row }">
            <span class="cell-location">{{ row.location || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm
              title="确定删除该楼栋？关联的房间/学生可能失败"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="onDelete(row.id)"
            >
              <template #reference>
                <el-button link type="danger" :icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :total="total"
          :page-size="query.size"
          :current-page="query.current"
          @current-change="onPageChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="90px"
        size="large"
      >
        <el-form-item label="楼栋名称" prop="name">
          <el-input v-model="form.name" placeholder="如 1号楼" :prefix-icon="OfficeBuilding" />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="form.location" placeholder="如 校区A-东侧" :prefix-icon="Location" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" :icon="Check" @click="onSave">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import {
  OfficeBuilding, Search, RefreshRight,
  Plus, Edit, Delete, Location, Check
} from '@element-plus/icons-vue'
import request, { type Result } from '@/utils/request'
import type { DormBuilding, PageResult } from '@/api/dorm'

type BuildingForm = {
  id?: number
  name: string
  location?: string
}

const tableData = ref<DormBuilding[]>([])
const total = ref(0)
const loading = ref(false)

const query = reactive({
  current: 1,
  size: 10,
  name: '' as string
})

const dialogVisible = ref(false)
const saving = ref(false)
const dialogTitle = computed(() => (form.value.id ? '编辑楼栋' : '新增楼栋'))

const formRef = ref<FormInstance | null>(null)
const form = ref<BuildingForm>({
  id: undefined,
  name: '',
  location: ''
})

const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入楼栋名称', trigger: 'blur' }]
})

const loadPage = async () => {
  loading.value = true
  try {
    const res = await request.get<Result<PageResult<DormBuilding>>>('/dorm/buildings/page', {
      params: {
        current: query.current,
        size: query.size,
        name: query.name || undefined
      }
    })
    const body = res.data
    if (body.code !== 0) throw new Error(body.msg || '加载失败')
    tableData.value = body.data.records || []
    total.value = body.data.total || 0
  } catch (e: any) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const onSearch = async () => {
  query.current = 1
  await loadPage()
}

const onReset = () => {
  query.name = ''
  query.current = 1
  void loadPage()
}

const onPageChange = (page: number) => {
  query.current = page
  void loadPage()
}

const openAdd = () => {
  form.value = { id: undefined, name: '', location: '' }
  dialogVisible.value = true
}

const openEdit = (row: DormBuilding) => {
  form.value = { id: row.id, name: row.name, location: row.location || '' }
  dialogVisible.value = true
}

const onDelete = async (id: number) => {
  try {
    const res = await request.delete<Result<null>>(`/dorm/buildings/${id}`)
    if (res.data.code !== 0) throw new Error(res.data.msg || '删除失败')
    ElMessage.success('删除成功')
    await loadPage()
  } catch (e: any) {
    ElMessage.error(e?.message || '删除失败')
  }
}

const onSave = () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      saving.value = true
      const payload = {
        name: form.value.name.trim(),
        location: form.value.location?.trim()
      }
      if (form.value.id) {
        const res = await request.put<Result<null>>(`/dorm/buildings/${form.value.id}`, payload)
        if (res.data.code !== 0) throw new Error(res.data.msg || '更新失败')
        ElMessage.success('更新成功')
      } else {
        const res = await request.post<Result<null>>('/dorm/buildings', payload)
        if (res.data.code !== 0) throw new Error(res.data.msg || '新增失败')
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      await loadPage()
    } catch (e: any) {
      ElMessage.error(e?.message || '保存失败')
    } finally {
      saving.value = false
    }
  })
}

onMounted(() => {
  void loadPage()
})
</script>

<style scoped>
.page-wrap {
  max-width: 1200px;
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

.table-card {
  position: relative;
}

.cell-building {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cell-location {
  color: #606266;
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
