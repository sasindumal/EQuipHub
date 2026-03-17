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

    // Skip interception for auth endpoints — prevents refresh loops
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
          // Refresh failed — token is truly expired, clear and redirect
          localStorage.removeItem('equiphub_token');
          localStorage.removeItem('equiphub_user');
          window.location.href = '/login';
          return Promise.reject(error);
        }
      }
      // No token at all — don't force redirect here, let AuthProvider handle it
    }

    return Promise.reject(error);
  }
);

// ─── Auth APIs ────────────────────────────────────────────
export const authAPI = {
  login:           (data)  => api.post('/auth/login', data),
  register:        (data)  => api.post('/auth/register', data),
  verifyEmail:     (data)  => api.post('/auth/verify-email', data),
  resendCode:      (email) => api.post(`/auth/resend-code?email=${encodeURIComponent(email)}`),
  getCurrentUser:  ()      => api.get('/auth/me'),
  refreshToken:    ()      => api.post('/auth/refresh'),
};

// ─── Admin APIs ───────────────────────────────────────────
// AdminController base: /admin  (baseURL already includes /api/v1)
export const adminAPI = {
  getDashboard:          ()                      => api.get('/admin/dashboard'),
  // Departments — NOTE: CRUD lives on /departments (DepartmentController),
  // NOT /admin/departments (AdminController only has POST+GET there).
  // Bug fix: deactivateDepartment was calling DELETE /admin/departments/:id
  // which does not exist → 405 Method Not Allowed. Corrected to /departments/:id.
  getAllDepartments:     (activeOnly = false)    => api.get(`/departments?activeOnly=${activeOnly}`),
  getDepartmentById:    (id)                    => api.get(`/departments/${id}`),
  createDepartment:     (data, initConfig=true) => api.post(`/admin/departments?initConfig=${initConfig}`, data),
  updateDepartment:     (id, data)              => api.put(`/departments/${id}`, data),
  deactivateDepartment: (id)                    => api.delete(`/departments/${id}`),
  // Department Config
  getAllConfigurations:  ()           => api.get('/admin/config'),
  getConfig:            (deptId)     => api.get(`/admin/config/${deptId}`),
  initializeConfig:     (deptId)     => api.post(`/admin/config/${deptId}/initialize`),
  updateConfig:         (deptId, d)  => api.put(`/admin/config/${deptId}`, d),
  resetConfig:          (deptId)     => api.post(`/admin/config/${deptId}/reset`),
  getSystemDefaults:    ()           => api.get('/admin/config/system-defaults'),
};

// ─── User Management APIs ─────────────────────────────────
// UserManagementController base: /users
export const userAPI = {
  getAllUsers:          ()              => api.get('/users'),
  getMyProfile:        ()              => api.get('/users/me'),
  getAllStaff:          ()              => api.get('/users/staff'),
  getAllStudents:       ()              => api.get('/users/students'),
  searchUsers:         (keyword)       => api.get(`/users/search?keyword=${encodeURIComponent(keyword)}`),
  getUsersByDepartment:(deptId)        => api.get(`/users/department/${deptId}`),
  getUsersByRole:      (role)          => api.get(`/users/role/${role}`),
  getUserById:         (userId)        => api.get(`/users/${userId}`),
  createStaff:         (data)          => api.post('/users', data),
  updateUser:          (userId, data)  => api.put(`/users/${userId}`, data),
  suspendUser:         (userId)        => api.patch(`/users/${userId}/suspend`),
  activateUser:        (userId)        => api.patch(`/users/${userId}/activate`),
  resetPassword:       (userId, data)  => api.post(`/users/${userId}/reset-password`, data),
  deleteUser:          (userId)        => api.delete(`/users/${userId}`),
  getDepartmentStats:  (deptId)        => api.get(`/users/department/${deptId}/stats`),
  getSystemAdmins:     ()              => api.get('/users/system-admins'),
};

// ─── Department Admin APIs ────────────────────────────────
// DepartmentAdminController base: /department-admin
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

// ─── Equipment Category APIs ──────────────────────────────
// EquipmentCategoryController base: /equipment-categories
// IMPORTANT: Always use these to populate the category dropdown.
// Never hardcode category IDs — they are auto-generated by the DB.
export const equipmentCategoryAPI = {
  getAll:    ()    => api.get('/equipment-categories'),
  getById:   (id)  => api.get(`/equipment-categories/${id}`),
};

// ─── Equipment APIs ────────────────────────────────────────
export const equipmentAPI = {
  getAllEquipment:        ()          => api.get('/equipment'),
  getEquipmentById:      (id)        => api.get(`/equipment/${id}`),
  getByDepartment:       (deptId, activeOnly = true) => api.get(`/equipment/department/${deptId}?activeOnly=${activeOnly}`),
  getAvailableByDept:    (deptId)    => api.get(`/equipment/department/${deptId}/available`),
  createEquipment:       (data)      => api.post('/equipment', data),
  updateEquipment:       (id, data)  => api.put(`/equipment/${id}`, data),
  updateEquipmentStatus: (id, data)  => api.patch(`/equipment/${id}/status`, data),
};

// ─── Borrow Request APIs ──────────────────────────────────
// RequestController base: /requests
export const requestAPI = {
  // existing
  getDepartmentRequests:   ()                    => api.get('/requests/department'),
  getAllRequests:           ()                    => api.get('/requests'),
  getMyRequests:           (page = 0, size = 20) => api.get(`/requests/my?page=${page}&size=${size}`),
  getRequestById:          (id)                  => api.get(`/requests/${id}`),
  createRequest:           (data)                => api.post('/requests', data),

  // submit DRAFT → PENDING
  submitRequest:           (requestId)           => api.post(`/requests/${requestId}/submit`),

  // update DRAFT
  updateRequest:           (requestId, data)     => api.put(`/requests/${requestId}`, data),

  // cancel
  cancelRequest:           (requestId)           => api.post(`/requests/${requestId}/cancel`),

  // filter by status (DRAFT | PENDINGAPPROVAL | APPROVED | INUSE | RETURNED | COMPLETED ...)
  getByStatus:             (status, page = 0)    => api.get(`/requests/status/${status}?page=${page}`),

  // department helpers
  getDepartmentRequestsById: (departmentId, page = 0) =>
    api.get(`/requests/department/${departmentId}?page=${page}`),
  getDepartmentStats:      (departmentId)        => api.get(`/requests/department/${departmentId}/stats`),
  getDepartmentStatsSelf:  ()                    => api.get('/requests/department/stats'),
  getPendingCount:         (departmentId)        => api.get(`/requests/department/${departmentId}/pending`),
  getEmergencyRequests:    (departmentId)        => api.get(`/requests/department/${departmentId}/emergency`),
  getMyDepartmentRequests: (page = 0)            => api.get(`/requests/my-department?page=${page}`),
  getMyDepartmentStats:    ()                    => api.get('/requests/my-department/stats'),

  // admin only
  getSlaBreached:          ()                    => api.get('/requests/sla-breached'),

  // legacy aliases kept for backward-compat with existing pages
  approveRequest:          (id)                  => api.patch(`/requests/${id}/approve`),
  rejectRequest:           (id, data)            => api.patch(`/requests/${id}/reject`, data),
  returnEquipment:         (id)                  => api.patch(`/requests/${id}/return`),
};

// ─── Approval Workflow APIs ───────────────────────────────
// ApprovalController base: /approvals
export const approvalAPI = {
  // POST /approvals/requests/{requestId}/auto-approve  → TO, DEPTADMIN
  attemptAutoApproval:  (requestId)             => api.post(`/approvals/requests/${requestId}/auto-approve`),

  // POST /approvals/requests/{requestId}/decide?stage=LECTURERAPPROVAL
  // Body: { action: 'APPROVE'|'REJECT'|'RECOMMEND'|'MODIFY', comments }
  processDecision:      (requestId, stage, data) =>
    api.post(`/approvals/requests/${requestId}/decide?stage=${stage}`, data),

  // GET /approvals/my-queue  → Lecturer, TO, HOD, DEPTADMIN
  getMyQueue:           ()                      => api.get('/approvals/my-queue'),

  // GET /approvals/departments/{departmentId}/queue  → DEPTADMIN, HOD
  getDepartmentQueue:   (departmentId)          => api.get(`/approvals/departments/${departmentId}/queue`),

  // GET /approvals/requests/{requestId}/history  → All roles
  getApprovalHistory:   (requestId)             => api.get(`/approvals/requests/${requestId}/history`),

  // GET /approvals/departments/{departmentId}/stats  → DEPTADMIN, HOD
  getDepartmentStats:   (departmentId)          => api.get(`/approvals/departments/${departmentId}/stats`),

  // GET /approvals/requests/{requestId}/next-stage  → DEPTADMIN, TO, HOD
  getNextStage:         (requestId)             => api.get(`/approvals/requests/${requestId}/next-stage`),
};

// ─── Inspection APIs ──────────────────────────────────────
// InspectionController base: /inspections
export const inspectionAPI = {
  // POST /inspections/issue  → TECHNICALOFFICER
  // Body: { requestId, items: [{ equipmentId, conditionBefore, notes }] }
  issueEquipment:       (data)                  => api.post('/inspections/issue', data),

  // POST /inspections/return  → TECHNICALOFFICER
  // Body: { requestId, items: [{ equipmentId, conditionAfter, damageLevel, notes }] }
  processReturn:        (data)                  => api.post('/inspections/return', data),

  // POST /inspections/{inspectionId}/acknowledge  → STUDENT
  acknowledgeInspection: (inspectionId)         => api.post(`/inspections/${inspectionId}/acknowledge`),

  // GET /inspections/request/{requestId}  → All roles
  getByRequest:         (requestId)             => api.get(`/inspections/request/${requestId}`),

  // GET /inspections/my-inspections  → TECHNICALOFFICER
  getMyInspections:     ()                      => api.get('/inspections/my-inspections'),

  // GET /inspections/unacknowledged  → TO, DEPTADMIN
  getUnacknowledged:    ()                      => api.get('/inspections/unacknowledged'),

  // GET /inspections/my-department/damage-report?days=30  → TO, DEPTADMIN, HOD
  getMyDeptDamageReport: (days = 30)            => api.get(`/inspections/my-department/damage-report?days=${days}`),

  // GET /inspections/my-department/stats  → TO, DEPTADMIN, HOD
  getMyDeptStats:       ()                      => api.get('/inspections/my-department/stats'),

  // GET /inspections/department/{departmentId}/damage-report?days=30
  getDamageReport:      (departmentId, days = 30) =>
    api.get(`/inspections/department/${departmentId}/damage-report?days=${days}`),

  // GET /inspections/department/{departmentId}/stats
  getDepartmentStats:   (departmentId)          => api.get(`/inspections/department/${departmentId}/stats`),
};

// ─── Penalty APIs ─────────────────────────────────────────
// PenaltyController base: /penalties
export const penaltyAPI = {
  // POST /penalties  → TO, DEPTADMIN, HOD
  // Body: { studentId, requestId, penaltyType, points, reason }
  createPenalty:        (data)                  => api.post('/penalties', data),

  // POST /penalties/{penaltyId}/approve  → HOD, DEPTADMIN
  approvePenalty:       (penaltyId)             => api.post(`/penalties/${penaltyId}/approve`),

  // POST /penalties/{penaltyId}/waive?reason=...  → HOD only
  waivePenalty:         (penaltyId, reason)     =>
    api.post(`/penalties/${penaltyId}/waive?reason=${encodeURIComponent(reason)}`),

  // GET /penalties/my  → STUDENT
  getMyPenalties:       ()                      => api.get('/penalties/my'),

  // GET /penalties/my/summary  → STUDENT
  getMySummary:         ()                      => api.get('/penalties/my/summary'),

  // GET /penalties/students/{studentId}
  getStudentPenalties:  (studentId)             => api.get(`/penalties/students/${studentId}`),

  // GET /penalties/students/{studentId}/summary
  getStudentSummary:    (studentId)             => api.get(`/penalties/students/${studentId}/summary`),

  // GET /penalties/students/{studentId}/can-borrow  → TO, DEPTADMIN
  canStudentBorrow:     (studentId)             => api.get(`/penalties/students/${studentId}/can-borrow`),

  // GET /penalties/departments/{departmentId}
  getDepartmentPenalties: (departmentId)        => api.get(`/penalties/departments/${departmentId}`),

  // GET /penalties/departments/{departmentId}/pending
  getDepartmentPendingPenalties: (departmentId) => api.get(`/penalties/departments/${departmentId}/pending`),

  // POST /penalties/appeals  → STUDENT
  // Body: { penaltyId, reason }
  submitAppeal:         (data)                  => api.post('/penalties/appeals', data),

  // POST /penalties/appeals/{penaltyId}/decide  → HOD, DEPTADMIN
  // Body: { decision: 'APPROVE'|'REJECT', reason }
  decideAppeal:         (penaltyId, data)       => api.post(`/penalties/appeals/${penaltyId}/decide`, data),
};

export default api;
