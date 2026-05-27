<template>
  <div class="page-wrap">
    <div class="page-header">
      <div class="page-title">
        <el-icon :size="22"><House /></el-icon>
        <span>房间管理</span>
      </div>
      <el-button type="primary" :icon="Plus" @click="openAdd">新增房间</el-button>
    </div>

    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="楼栋">
          <el-select
            v-model="query.buildingId"
            placeholder="全部楼栋"
            style="width: 180px;"
            clearable
          >
            <el-option
              v-for="b in buildings"
              :key="b.id"
              :label="b.name"
              :value="b.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="房间号">
          <el-input
            v-model="query.roomNumber"
            placeholder="如 301"
            style="width: 180px;"
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
        empty-text="暂无房间数据"
      >
        <el-table-column prop="roomNumber" label="房间号" width="120" />
        <el-table-column prop="buildingName" label="所属楼栋" width="180">
          <template #default="{ row }">
            <div class="cell-building">
              <el-icon color="#409eff"><OfficeBuilding /></el-icon>
              <span>{{ row.buildingName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="capacity" label="容量" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info" effect="plain" round>{{ row.capacity }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentCount" label="当前入住" width="120" align="center">
          <template #default="{ row }">
            <div class="cell-count">
              <el-progress
                :percentage="row.capacity ? Math.round(row.currentCount / row.capacity * 100) : 0"
                :stroke-width="8"
                :color="row.currentCount >= row.capacity ? 'var(--app-color-danger)' : row.currentCount / row.capacity > 0.8 ? 'var(--app-color-warning)' : 'var(--app-color-success)'"
                :show-text="false"
                style="width: 80px;"
              />
              <span class="count-text" :class="{ 'count-full': row.currentCount >= row.capacity }">
                {{ row.currentCount }}/{{ row.capacity }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="当日值日生" width="140">
          <template #default="{ row }">
            <span v-if="row.dutyStudentName" class="cell-duty">
              <el-icon color="#e6a23c"><StarFilled /></el-icon>
              {{ row.dutyStudentName }}
            </span>
            <span v-else class="cell-duty-empty">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="success" :icon="User" @click="handleViewStudents(row)">查看学生</el-button>
            <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm
              title="确定删除该房间？"
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
        label-width="100px"
        size="large"
      >
        <el-form-item label="楼栋" prop="buildingId">
          <el-select v-model="form.buildingId" placeholder="请选择楼栋" style="width: 100%;">
            <el-option
              v-for="b in buildings"
              :key="b.id"
              :label="b.name"
              :value="b.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="房间号" prop="roomNumber">
          <el-input v-model="form.roomNumber" placeholder="如 301" :prefix-icon="House" />
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="当前入住数" prop="currentCount">
          <el-input-number v-model="form.currentCount" :min="0" style="width: 100%;" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" :icon="Check" @click="onSave">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看入住学生对话框 -->
    <el-dialog
      v-model="studentDialogVisible"
      :title="`${currentRoomNumber} - 入住学生`"
      width="650px"
      destroy-on-close
    >
      <el-table
        v-loading="loadingStudents"
        :data="currentRoomStudents"
        border
        stripe
        empty-text="该房间暂无学生入住"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="gender" label="性别" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.gender === 'M' ? 'primary' : row.gender === 'F' ? 'danger' : 'info'" effect="light" size="small">
              {{ row.gender === 'M' ? '男' : row.gender === 'F' ? '女' : '其他' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="150" />
        <el-table-column prop="major" label="专业" min-width="180" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import {
  House, OfficeBuilding, Search, RefreshRight, Plus, Edit, Delete, Check, StarFilled, User
} from '@element-plus/icons-vue'
import type { DormBuilding, DormRoomVO, PageResult } from '@/api/dorm'
import { createRoom, deleteRoom, getBuildingsList, getRoomsPage, updateRoom } from '@/api/dorm'
import { studentApi } from '@/api/student'

type RoomForm = {
  id?: number
  buildingId: number | null
  roomNumber: string
  capacity: number
  currentCount: number
}

const buildings = ref<DormBuilding[]>([])
const tableData = ref<DormRoomVO[]>([])
const loading = ref(false)

const dialogVisible = ref(false)
const dialogTitle = computed(() => (form.value.id ? '编辑房间' : '新增房间'))
const saving = ref(false)

const studentDialogVisible = ref(false)
const currentRoomStudents = ref<{ id: number; name: string; gender: string; phone: string; major: string }[]>([])
const currentRoomNumber = ref('')
const loadingStudents = ref(false)

const total = ref(0)

const query = reactive({
  current: 1,
  size: 10,
  buildingId: undefined as number | undefined,
  roomNumber: '' as string
})

const formRef = ref<FormInstance | null>(null)
const form = ref<RoomForm>({
  id: undefined,
  buildingId: null,
  roomNumber: '',
  capacity: 1,
  currentCount: 0
})

const rules = reactive<FormRules>({
  buildingId: [{ required: true, message: '请选择楼栋', trigger: 'change' }],
  roomNumber: [{ required: true, message: '请输入房间号', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'change' }],
  currentCount: [{ required: true, message: '请输入当前入住数', trigger: 'change' }]
})

const loadBuildings = async () => {
  const res = await getBuildingsList()
  const body = res.data
  if (body.code !== 0) throw new Error(body.msg || '加载楼栋失败')
  buildings.value = body.data || []
}

const loadRooms = async () => {
  loading.value = true
  try {
    const res = await getRoomsPage({
      current: query.current,
      size: query.size,
      buildingId: query.buildingId,
      roomNumber: query.roomNumber || undefined
    })
    const body = res.data
    if (body.code !== 0) throw new Error(body.msg || '加载房间失败')
    const page = body.data as PageResult<DormRoomVO>
    tableData.value = page.records || []
    total.value = page.total || 0
  } catch (e: any) {
    ElMessage.error(e?.message || '加载房间失败')
  } finally {
    loading.value = false
  }
}

const onSearch = async () => {
  query.current = 1
  await loadRooms()
}

const onReset = () => {
  query.buildingId = undefined
  query.roomNumber = ''
  query.current = 1
  void loadRooms()
}

const onPageChange = (page: number) => {
  query.current = page
  void loadRooms()
}

const openAdd = () => {
  form.value = {
    id: undefined,
    buildingId: null,
    roomNumber: '',
    capacity: 1,
    currentCount: 0
  }
  dialogVisible.value = true
}

const openEdit = (row: DormRoomVO) => {
  form.value = {
    id: row.id,
    buildingId: row.buildingId,
    roomNumber: row.roomNumber,
    capacity: row.capacity,
    currentCount: row.currentCount
  }
  dialogVisible.value = true
}

const handleViewStudents = async (row: DormRoomVO) => {
  currentRoomNumber.value = `${row.buildingName} - ${row.roomNumber}`
  studentDialogVisible.value = true
  loadingStudents.value = true
  try {
    const res = await studentApi.getStudentsByRoomId(row.id)
    if (res.data.code === 0) {
      currentRoomStudents.value = res.data.data || []
    } else {
      ElMessage.error(res.data.msg || '获取学生列表失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '获取学生列表失败')
  } finally {
    loadingStudents.value = false
  }
}

const onDelete = (id: number) => {
  return deleteRoom(id)
    .then((res) => {
      const body = res.data
      if (body.code !== 0) throw new Error(body.msg || '删除失败')
      ElMessage.success('删除成功')
      void loadRooms()
    })
    .catch((e: any) => {
      ElMessage.error(e?.message || '删除失败')
    })
}

const onSave = () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      saving.value = true
      const payload = {
        buildingId: form.value.buildingId as number,
        roomNumber: form.value.roomNumber.trim(),
        capacity: form.value.capacity,
        currentCount: form.value.currentCount
      }
      if (form.value.id) {
        const res = await updateRoom(form.value.id, payload)
        const body = res.data
        if (body.code !== 0) throw new Error(body.msg || '更新失败')
        ElMessage.success('更新成功')
      } else {
        const res = await createRoom(payload)
        const body = res.data
        if (body.code !== 0) throw new Error(body.msg || '新增失败')
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      await loadRooms()
    } catch (e: any) {
      ElMessage.error(e?.message || '保存失败')
    } finally {
      saving.value = false
    }
  })
}

onMounted(async () => {
  try {
    await loadBuildings()
    await loadRooms()
  } catch (e: any) {
    ElMessage.error(e?.message || '初始化失败')
  }
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
  margin-bottom: 20px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: var(--app-text-primary);
}

.search-card {
  margin-bottom: 16px;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.cell-building {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cell-count {
  display: flex;
  align-items: center;
  gap: 8px;
}

.count-text {
  font-size: 13px;
  color: var(--app-color-success);
  font-weight: 500;
}

.count-text.count-full {
  color: var(--app-color-danger);
}

.cell-duty {
  color: var(--app-text-primary);
}

.cell-duty-empty {
  color: var(--app-text-placeholder);
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
