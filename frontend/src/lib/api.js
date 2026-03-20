import axios from 'axios';

// Backend has context-path: /api/v1, so all endpoints are under /api/v1
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000,
});

// Request interceptor — attach JWT
api.interceptors.request.use(
  (config) => {
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('equiphub_token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor — handle 401
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    const url = originalRequest?.url || '';
    const isAuthEndpoint = url.includes('/auth/');

    if (
      error.response?.status === 401 &&
      typeof window !== 'undefined' &&
      !isAuthEndpoint &&
      !originalRequest._retry
    ) {
      originalRequest._retry = true;

      const token = localStorage.getItem('equiphub_token');
      if (token) {
        try {
          const res = await axios.post(`${API_BASE_URL}/auth/refresh`, null, {
            headers: { Authorization: `Bearer ${token}` },
          });
          const newToken = res.data.token;
          if (newToken) {
            localStorage.setItem('equiphub_token', newToken);
            originalRequest.headers.Authorization = `Bearer ${newToken}`;
            return api(originalRequest);
          }
        } catch {
          localStorage.removeItem('equiphub_token');
          localStorage.removeItem('equiphub_user');
          window.location.href = '/login';
          return Promise.reject(error);
        }
      }
    }

    return Promise.reject(error);
  }
);

// --- Auth APIs -----------------------------------------------------------
export const authAPI = {
  login:           (data)  => api.post('/auth/login', data),
  register:        (data)  => api.post('/auth/register', data),
  verifyEmail:     (data)  => api.post('/auth/verify-email', data),
  resendCode:      (email) => api.post(`/auth/resend-code?email=${encodeURIComponent(email)}`),
  getCurrentUser:  ()      => api.get('/auth/me'),
  refreshToken:    ()      => api.post('/auth/refresh'),
};

// --- Admin APIs ----------------------------------------------------------
export const adminAPI = {
  getDashboard:          ()                      => api.get('/admin/dashboard'),
  getAllDepartments:     (activeOnly = false)    => api.get(`/departments?activeOnly=${activeOnly}`),
  getDepartmentById:    (id)                    => api.get(`/departments/${id}`),
  createDepartment:     (data, initConfig=true) => api.post(`/admin/departments?initConfig=${initConfig}`, data),
  updateDepartment:     (id, data)              => api.put(`/departments/${id}`, data),
  deactivateDepartment: (id)                    => api.delete(`/departments/${id}`),
  getAllConfigurations:  ()           => api.get('/admin/config'),
  getConfig:            (deptId)     => api.get(`/admin/config/${deptId}`),
  initializeConfig:     (deptId)     => api.post(`/admin/config/${deptId}/initialize`),
  updateConfig:         (deptId, d)  => api.put(`/admin/config/${deptId}`, d),
  resetConfig:          (deptId)     => api.post(`/admin/config/${deptId}/reset`),
  getSystemDefaults:    ()           => api.get('/admin/config/system-defaults'),
};

// --- User Management APIs ------------------------------------------------
export const userAPI = {
  getAllUsers:           ()              => api.get('/users'),
  getMyProfile:         ()              => api.get('/users/me'),
  getAllStaff:           ()              => api.get('/users/staff'),
  getAllStudents:        ()              => api.get('/users/students'),
  searchUsers:          (keyword)       => api.get(`/users/search?keyword=${encodeURIComponent(keyword)}`),
  getUsersByDepartment: (deptId)        => api.get(`/users/department/${deptId}`),
  getUsersByRole:       (role)          => api.get(`/users/role/${role}`),
  getUserById:          (userId)        => api.get(`/users/${userId}`),
  createStaff:          (data)          => api.post('/users', data),
  updateUser:           (userId, data)  => api.put(`/users/${userId}`, data),
  suspendUser:          (userId)        => api.patch(`/users/${userId}/suspend`),
  activateUser:         (userId)        => api.patch(`/users/${userId}/activate`),
  resetPassword:        (userId, data)  => api.post(`/users/${userId}/reset-password`, data),
  deleteUser:           (userId)        => api.delete(`/users/${userId}`),
  getDepartmentStats:   (deptId)        => api.get(`/users/department/${deptId}/stats`),
  getSystemAdmins:      ()              => api.get('/users/system-admins'),
};

// --- Department Admin APIs -----------------------------------------------
export const deptAdminAPI = {
  getMyDepartment:          ()     => api.get('/department-admin/my-department'),
  updateMyDepartment:       (data) => api.put('/department-admin/my-department', data),
  getMyDepartmentUsers:     ()     => api.get('/department-admin/my-department/users'),
  getMyDepartmentStaff:     ()     => api.get('/department-admin/my-department/staff'),
  getMyDepartmentStudents:  ()     => api.get('/department-admin/my-department/students'),
  getMyDepartmentStats:     ()     => api.get('/department-admin/my-department/stats'),
  getMyDepartmentConfig:    ()     => api.get('/department-admin/my-department/config'),
  updateMyDepartmentConfig: (data) => api.put('/department-admin/my-department/config', data),
  resetMyDepartmentConfig:  ()     => api.post('/department-admin/my-department/config/reset'),
  getActiveDepartments:     ()     => api.get('/department-admin/departments'),
  getDepartmentById:        (id)   => api.get(`/department-admin/departments/${id}`),
};

// --- Course APIs ---------------------------------------------------------
export const courseAPI = {
  // Write: DEPARTMENTADMIN / SYSTEMADMIN only
  createCourse:           (data)            => api.post('/courses', data),
  updateCourse:           (courseId, data)  => api.put(`/courses/${courseId}`, data),
  deleteCourse:           (courseId)        => api.delete(`/courses/${courseId}`),
  // Read: all authenticated users
  getAllCourses:           ()               => api.get('/courses'),
  getCourseById:          (courseId)        => api.get(`/courses/${courseId}`),
  getCoursesByDepartment: (departmentId)    => api.get(`/courses/department/${departmentId}`),
};

// --- Equipment Category APIs ---------------------------------------------
export const equipmentCategoryAPI = {
  getAll:          ()         => api.get('/equipment-categories'),
  getById:         (id)       => api.get(`/equipment-categories/${id}`),
  createCategory:  (data)     => api.post('/equipment-categories', data),
  updateCategory:  (id, data) => api.put(`/equipment-categories/${id}`, data),
};

// --- Equipment APIs ------------------------------------------------------
export const equipmentAPI = {
  getAllEquipment:        ()                         => api.get('/equipment'),
  getEquipmentById:      (id)                       => api.get(`/equipment/${id}`),
  getByDepartment:       (deptId, activeOnly = true) => api.get(`/equipment/department/${deptId}?activeOnly=${activeOnly}`),
  getAvailableByDept:    (deptId)                   => api.get(`/equipment/department/${deptId}/available`),
  createEquipment:       (data)                     => api.post('/equipment', data),
  updateEquipment:       (id, data)                 => api.put(`/equipment/${id}`, data),
  updateEquipmentStatus: (id, data)                 => api.patch(`/equipment/${id}/status`, data),
};

// --- Borrow Request APIs -------------------------------------------------
export const requestAPI = {
  getDepartmentRequests:     ()                         => api.get('/requests/department'),
  getAllRequests:             ()                         => api.get('/requests'),
  getMyRequests:             (page = 0, size = 20)      => api.get(`/requests/my?page=${page}&size=${size}`),
  getRequestById:            (id)                       => api.get(`/requests/${id}`),
  createRequest:             (data)                     => api.post('/requests', data),
  submitRequest:             (requestId)                => api.post(`/requests/${requestId}/submit`),
  updateRequest:             (requestId, data)          => api.put(`/requests/${requestId}`, data),
  cancelRequest:             (requestId)                => api.post(`/requests/${requestId}/cancel`),
  getByStatus:               (status, page = 0)         => api.get(`/requests/status/${status}?page=${page}`),
  getDepartmentRequestsById: (departmentId, page = 0)   => api.get(`/requests/department/${departmentId}?page=${page}`),
  getDepartmentStats:        (departmentId)             => api.get(`/requests/department/${departmentId}/stats`),
  getDepartmentStatsSelf:    ()                         => api.get('/requests/department/stats'),
  getPendingCount:           (departmentId)             => api.get(`/requests/department/${departmentId}/pending`),
  getEmergencyRequests:      (departmentId)             => api.get(`/requests/department/${departmentId}/emergency`),
  getMyDepartmentRequests:   (page = 0)                 => api.get(`/requests/my-department?page=${page}`),
  getMyDepartmentStats:      ()                         => api.get('/requests/my-department/stats'),
  getSlaBreached:            ()                         => api.get('/requests/sla-breached'),
  approveRequest:            (id)                       => api.patch(`/requests/${id}/decide?status=APPROVED`,data),
  rejectRequest:             (id, data)                 => api.patch(`/requests/${id}/decide?status=REJECTED`, data),
  returnEquipment:           (id)                       => api.patch(`/requests/${id}/return`),
};

// --- Approval Workflow APIs ----------------------------------------------
export const approvalAPI = {
  attemptAutoApproval: (requestId)              => api.post(`/approvals/requests/${requestId}/auto-approve`),
  processDecision:     (requestId, stage, data) => api.post(`/approvals/requests/${requestId}/decide?stage=${stage}`, data),
  getMyQueue:          ()                       => api.get('/approvals/my-queue'),
  getDepartmentQueue:  (departmentId)           => api.get(`/approvals/departments/${departmentId}/queue`),
  getApprovalHistory:  (requestId)              => api.get(`/approvals/requests/${requestId}/history`),
  getDepartmentStats:  (departmentId)           => api.get(`/approvals/departments/${departmentId}/stats`),
  getNextStage:        (requestId)              => api.get(`/approvals/requests/${requestId}/next-stage`),
};

// --- Inspection APIs -----------------------------------------------------
export const inspectionAPI = {
  issueEquipment:        (data)                   => api.post('/inspections/issue', data),
  processReturn:         (data)                   => api.post('/inspections/return', data),
  acknowledgeInspection: (inspectionId)           => api.post(`/inspections/${inspectionId}/acknowledge`),
  getByRequest:          (requestId)              => api.get(`/inspections/request/${requestId}`),
  getMyInspections:      ()                       => api.get('/inspections/my-inspections'),
  getUnacknowledged:     ()                       => api.get('/inspections/unacknowledged'),
  getMyDeptDamageReport: (days = 30)              => api.get(`/inspections/my-department/damage-report?days=${days}`),
  getMyDeptStats:        ()                       => api.get('/inspections/my-department/stats'),
  getDamageReport:       (departmentId, days = 30) => api.get(`/inspections/department/${departmentId}/damage-report?days=${days}`),
  getDepartmentStats:    (departmentId)           => api.get(`/inspections/department/${departmentId}/stats`),
};

// --- Penalty APIs --------------------------------------------------------
export const penaltyAPI = {
  createPenalty:                (data)                  => api.post('/penalties', data),
  approvePenalty:               (penaltyId)             => api.post(`/penalties/${penaltyId}/approve`),
  waivePenalty:                 (penaltyId, reason)     => api.post(`/penalties/${penaltyId}/waive?reason=${encodeURIComponent(reason)}`),
  getMyPenalties:               ()                      => api.get('/penalties/my'),
  getMySummary:                 ()                      => api.get('/penalties/my/summary'),
  getStudentPenalties:          (studentId)             => api.get(`/penalties/students/${studentId}`),
  getStudentSummary:            (studentId)             => api.get(`/penalties/students/${studentId}/summary`),
  canStudentBorrow:             (studentId)             => api.get(`/penalties/students/${studentId}/can-borrow`),
  getDepartmentPenalties:       (departmentId)          => api.get(`/penalties/departments/${departmentId}`),
  getDepartmentPendingPenalties:(departmentId)          => api.get(`/penalties/departments/${departmentId}/pending`),
  submitAppeal:                 (data)                  => api.post('/penalties/appeals', data),
  decideAppeal:                 (penaltyId, data)       => api.post(`/penalties/appeals/${penaltyId}/decide`, data),
};

export default api;
