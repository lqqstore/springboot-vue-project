<template>
  <el-container>
    <el-main>
      <div class="home-title">宿舍管理</div>
      <DormRooms />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import DormRooms from '@/views/dorm/rooms/index.vue'
</script>

<style scoped>
.home-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--app-text-primary);
}
</style>

