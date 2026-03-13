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
    if (error.response?.status === 401 && typeof window !== 'undefined') {
      const token = localStorage.getItem('equiphub_token');
      if (token && !error.config._retry) {
        error.config._retry = true;
        try {
          const res = await axios.post(`${API_BASE_URL}/auth/refresh`, null, {
            headers: { Authorization: `Bearer ${token}` },
          });
          const newToken = res.data.token;
          localStorage.setItem('equiphub_token', newToken);
          error.config.headers.Authorization = `Bearer ${newToken}`;
          return api(error.config);
        } catch {
          localStorage.removeItem('equiphub_token');
          localStorage.removeItem('equiphub_user');
          window.location.href = '/login';
        }
      } else {
        localStorage.removeItem('equiphub_token');
        localStorage.removeItem('equiphub_user');
        window.location.href = '/login';
      }
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
  // Departments
  getAllDepartments:     (activeOnly = false)    => api.get(`/admin/departments?activeOnly=${activeOnly}`),
  getDepartmentById:    (id)                    => api.get(`/admin/departments/${id}`),
  createDepartment:     (data, initConfig=true) => api.post(`/admin/departments?initConfig=${initConfig}`, data),
  updateDepartment:     (id, data)              => api.put(`/admin/departments/${id}`, data),
  deactivateDepartment: (id)                    => api.delete(`/admin/departments/${id}`),
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

// ─── Equipment APIs ────────────────────────────────────────
export const equipmentAPI = {
  getAllEquipment:        ()          => api.get('/equipment'),
  getEquipmentById:      (id)        => api.get(`/equipment/${id}`),
  createEquipment:       (data)      => api.post('/equipment', data),
  updateEquipment:       (id, data)  => api.put(`/equipment/${id}`, data),
  updateEquipmentStatus: (id, data)  => api.patch(`/equipment/${id}/status`, data),
};

// ─── Borrow Request APIs ─────────────────────────────────────
export const requestAPI = {
  getDepartmentRequests: ()         => api.get('/requests/department'),
  getAllRequests:         ()         => api.get('/requests'),
  getMyRequests:         ()         => api.get('/requests/my'),
  getRequestById:        (id)       => api.get(`/requests/${id}`),
  createRequest:         (data)     => api.post('/requests', data),
  approveRequest:        (id)       => api.patch(`/requests/${id}/approve`),
  rejectRequest:         (id, data) => api.patch(`/requests/${id}/reject`, data),
  returnEquipment:       (id)       => api.patch(`/requests/${id}/return`),
  cancelRequest:         (id)       => api.delete(`/requests/${id}`),
};

export default api;
