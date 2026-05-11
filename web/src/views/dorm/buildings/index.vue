<template>
  <el-container>
    <el-main>
      <el-card shadow="never">
        <div class="toolbar">
          <el-form :inline="true" :model="query" class="search-form">
            <el-form-item label="楼栋名称">
              <el-input
                v-model="query.name"
                placeholder="如 1号楼"
                style="width: 200px;"
                clearable
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="onSearch">搜索</el-button>
              <el-button @click="onReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div style="margin: 12px 0;">
          <el-button type="primary" @click="openAdd">新增楼栋</el-button>
        </div>

        <el-table :data="tableData" border stripe style="width: 100%;">
          <el-table-column prop="name" label="楼栋名称" min-width="180" />
          <el-table-column prop="location" label="位置" min-width="220" />

          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-popconfirm
                title="确定删除该楼栋？（若存在房间/学生关联可能失败）"
                confirm-button-text="确定"
                cancel-button-text="取消"
                @confirm="onDelete(row.id)"
              >
                <template #reference>
                  <el-button link type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>

        <div style="margin-top: 16px; display: flex; justify-content: flex-end;">
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
      >
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="90px"
          size="large"
        >
          <el-form-item label="楼栋名称" prop="name">
            <el-input v-model="form.name" placeholder="如 1号楼" />
          </el-form-item>

          <el-form-item label="位置" prop="location">
            <el-input v-model="form.location" placeholder="如 校区A-东侧" />
          </el-form-item>
        </el-form>

        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="saving" @click="onSave">
              保存
            </el-button>
          </span>
        </template>
      </el-dialog>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import request, { type Result } from '@/utils/request'
import type { DormBuilding, PageResult } from '@/api/dorm'

type BuildingForm = {
  id?: number
  name: string
  location?: string
}

const tableData = ref<DormBuilding[]>([])
const total = ref(0)

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
}

const onSearch = async () => {
  query.current = 1
  try {
    await loadPage()
  } catch (e: any) {
    ElMessage.error(e?.message || '搜索失败')
  }
}

const onReset = () => {
  query.name = ''
  query.current = 1
  void onSearch()
}

const onPageChange = (page: number) => {
  query.current = page
  void loadPage().catch((e: any) => ElMessage.error(e?.message || '分页失败'))
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
  void loadPage().catch((e: any) => ElMessage.error(e?.message || '初始化失败'))
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>

