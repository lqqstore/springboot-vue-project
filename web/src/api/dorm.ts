import request, { type Result } from '@/utils/request'

export interface DormBuilding {
  id: number
  name: string
  location?: string
}

export interface DormRoom {
  id: number
  buildingId: number
  roomNumber: string
  capacity: number
  currentCount: number
}

export interface PageResult<T> {
  total: number
  size: number
  current: number
  records: T[]
}

export function getBuildingsList() {
  return request.get<Result<DormBuilding[]>>('/dorm/buildings/list')
}

export function getRoomsPage(params: {
  current: number
  size: number
  buildingId?: number
  roomNumber?: string
}) {
  return request.get<Result<PageResult<DormRoom>>>('/dorm/rooms/page', { params })
}

export function createRoom(data: {
  buildingId: number
  roomNumber: string
  capacity: number
  currentCount: number
}) {
  return request.post<Result<null>>('/dorm/rooms', data)
}

export function updateRoom(id: number, data: {
  buildingId: number
  roomNumber: string
  capacity: number
  currentCount: number
}) {
  return request.put<Result<null>>(`/dorm/rooms/${id}`, data)
}

export function deleteRoom(id: number) {
  return request.delete<Result<null>>(`/dorm/rooms/${id}`)
}

