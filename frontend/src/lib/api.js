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
      // Try to refresh token
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
// AuthController: @RequestMapping("/auth") → actual: /api/v1/auth
export const authAPI = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
  verifyEmail: (data) => api.post('/auth/verify-email', data),
  resendCode: (email) => api.post(`/auth/resend-code?email=${encodeURIComponent(email)}`),
  getCurrentUser: () => api.get('/auth/me'),
  refreshToken: () => api.post('/auth/refresh'),
};

// ─── Admin APIs ───────────────────────────────────────────
// AdminController: @RequestMapping("/api/v1/admin") → actual: /api/v1/api/v1/admin
export const adminAPI = {
  getDashboard: () => api.get('/api/v1/admin/dashboard'),
  // Departments
  getAllDepartments: (activeOnly = false) => api.get(`/api/v1/admin/departments?activeOnly=${activeOnly}`),
  getDepartmentById: (id) => api.get(`/api/v1/admin/departments/${id}`),
  createDepartment: (data, initConfig = true) => api.post(`/api/v1/admin/departments?initConfig=${initConfig}`, data),
  updateDepartment: (id, data) => api.put(`/api/v1/admin/departments/${id}`, data),
  deactivateDepartment: (id) => api.delete(`/api/v1/admin/departments/${id}`),
  // Department Config
  getAllConfigurations: () => api.get('/api/v1/admin/config'),
  getConfig: (deptId) => api.get(`/api/v1/admin/config/${deptId}`),
  initializeConfig: (deptId) => api.post(`/api/v1/admin/config/${deptId}/initialize`),
  updateConfig: (deptId, data) => api.put(`/api/v1/admin/config/${deptId}`, data),
  resetConfig: (deptId) => api.post(`/api/v1/admin/config/${deptId}/reset`),
  getSystemDefaults: () => api.get('/api/v1/admin/config/system-defaults'),
};

// ─── User Management APIs ─────────────────────────────────
// UserManagementController: @RequestMapping("/api/v1/users") → actual: /api/v1/api/v1/users
export const userAPI = {
  getAllUsers: () => api.get('/api/v1/users'),
  getMyProfile: () => api.get('/api/v1/users/me'),
  getAllStaff: () => api.get('/api/v1/users/staff'),
  getAllStudents: () => api.get('/api/v1/users/students'),
  searchUsers: (keyword) => api.get(`/api/v1/users/search?keyword=${encodeURIComponent(keyword)}`),
  getUsersByDepartment: (deptId) => api.get(`/api/v1/users/department/${deptId}`),
  getUsersByRole: (role) => api.get(`/api/v1/users/role/${role}`),
  getUserById: (userId) => api.get(`/api/v1/users/${userId}`),
  createStaff: (data) => api.post('/api/v1/users', data),
  updateUser: (userId, data) => api.put(`/api/v1/users/${userId}`, data),
  suspendUser: (userId) => api.patch(`/api/v1/users/${userId}/suspend`),
  activateUser: (userId) => api.patch(`/api/v1/users/${userId}/activate`),
  resetPassword: (userId, data) => api.post(`/api/v1/users/${userId}/reset-password`, data),
  deleteUser: (userId) => api.delete(`/api/v1/users/${userId}`),
  getDepartmentStats: (deptId) => api.get(`/api/v1/users/department/${deptId}/stats`),
  getSystemAdmins: () => api.get('/api/v1/users/system-admins'),
};

// ─── Department Admin APIs ────────────────────────────────
// DepartmentAdminController: @RequestMapping("/api/v1/department-admin") → actual: /api/v1/api/v1/department-admin
export const deptAdminAPI = {
  getMyDepartment: () => api.get('/api/v1/department-admin/my-department'),
  updateMyDepartment: (data) => api.put('/api/v1/department-admin/my-department', data),
  getMyDepartmentUsers: () => api.get('/api/v1/department-admin/my-department/users'),
  getMyDepartmentStaff: () => api.get('/api/v1/department-admin/my-department/staff'),
  getMyDepartmentStudents: () => api.get('/api/v1/department-admin/my-department/students'),
  getMyDepartmentStats: () => api.get('/api/v1/department-admin/my-department/stats'),
  getMyDepartmentConfig: () => api.get('/api/v1/department-admin/my-department/config'),
  updateMyDepartmentConfig: (data) => api.put('/api/v1/department-admin/my-department/config', data),
  resetMyDepartmentConfig: () => api.post('/api/v1/department-admin/my-department/config/reset'),
  getActiveDepartments: () => api.get('/api/v1/department-admin/departments'),
  getDepartmentById: (id) => api.get(`/api/v1/department-admin/departments/${id}`),
};

// ─── Equipment APIs ────────────────────────────────────────
export const equipmentAPI = {
  getAllEquipment: () => api.get('/equipment'),
  getEquipmentById: (id) => api.get(`/equipment/${id}`),
  createEquipment: (data) => api.post('/equipment', data),
  updateEquipment: (id, data) => api.put(`/equipment/${id}`, data),
  updateEquipmentStatus: (id, data) => api.patch(`/equipment/${id}/status`, data),
};

export default api;
