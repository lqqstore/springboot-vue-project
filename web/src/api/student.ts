import request, { type Result } from '@/utils/request'

interface Student {
  id: number
  userId: number
  name: string
  gender: string
  phone: string
  major: string
}

interface StudentDorm {
  id: number
  studentId: number
  roomId: number
  checkInDate: string
}

interface StudentAddRequest {
  userId: number
  name: string
  gender: string
  phone: string
  major: string
}

interface StudentUpdateRequest {
  name: string
  gender: string
  phone: string
  major: string
}

interface StudentDormAssignRequest {
  studentId: number
  roomId: number
  checkInDate: string
}

export const studentApi = {
  getStudentList: () => {
    return request.get<Result<Student[]>>('/student/list')
  },
  getStudentById: (id: number) => {
    return request.get<Result<Student>>(`/student/${id}`)
  },
  getStudentByUserId: (userId: number) => {
    return request.get<Result<Student>>(`/student/user/${userId}`)
  },
  addStudent: (data: StudentAddRequest) => {
    return request.post<Result<Student>>('/student', data)
  },
  updateStudent: (id: number, data: StudentUpdateRequest) => {
    return request.put<Result<Student>>(`/student/${id}`, data)
  },
  deleteStudent: (id: number) => {
    return request.delete<Result<null>>(`/student/${id}`)
  },
  assignDorm: (data: StudentDormAssignRequest) => {
    return request.post<Result<StudentDorm>>('/student/assign-dorm', data)
  },
  removeDormAssignment: (studentId: number) => {
    return request.delete<Result<null>>(`/student/remove-dorm/${studentId}`)
  },
  getStudentsByRoomId: (roomId: number) => {
    return request.get<Result<Student[]>>(`/student/room/${roomId}`)
  },
  getStudentDormAssignment: (studentId: number) => {
    return request.get<Result<StudentDorm>>(`/student/dorm-assignment/${studentId}`)
  }
}
