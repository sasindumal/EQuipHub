import axios from 'axios';
import * as SecureStore from 'expo-secure-store';
import { Platform } from 'react-native';

// ─── Config ───────────────────────────────────────────────
// On Android emulator, localhost maps to 10.0.2.2
// On iOS simulator / physical device over LAN, use your machine's IP
const API_BASE_URL = process.env.EXPO_PUBLIC_API_URL || Platform.select({
  android: 'http://10.0.2.2:8080/api/v1',
  ios:     'http://localhost:8080/api/v1',
  default: 'http://localhost:8080/api/v1',
});

// Allow runtime override via env or manual config
let _baseURL = API_BASE_URL;
export const setBaseURL = (url) => { _baseURL = url; api.defaults.baseURL = url; };
export const getBaseURL = () => _baseURL;

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

// ─── Token helpers ────────────────────────────────────────
const TOKEN_KEY = 'equiphub_token';
const USER_KEY  = 'equiphub_user';

export const tokenStore = {
  getToken:  async () => SecureStore.getItemAsync(TOKEN_KEY),
  setToken:  async (t) => SecureStore.setItemAsync(TOKEN_KEY, t),
  removeToken: async () => SecureStore.deleteItemAsync(TOKEN_KEY),
  getUser:   async () => {
    const u = await SecureStore.getItemAsync(USER_KEY);
    return u ? JSON.parse(u) : null;
  },
  setUser:  async (u) => SecureStore.setItemAsync(USER_KEY, JSON.stringify(u)),
  removeUser: async () => SecureStore.deleteItemAsync(USER_KEY),
  clear: async () => {
    await SecureStore.deleteItemAsync(TOKEN_KEY);
    await SecureStore.deleteItemAsync(USER_KEY);
  },
};

// ─── Request interceptor ─────────────────────────────────
api.interceptors.request.use(async (config) => {
  try {
    const token = await SecureStore.getItemAsync(TOKEN_KEY);
    if (token) config.headers.Authorization = `Bearer ${token}`;
  } catch { /* ignore */ }
  return config;
});

// ─── Response interceptor — handle 401 ───────────────────
let logoutHandler = null;
export const setLogoutHandler = (fn) => { logoutHandler = fn; };

api.interceptors.response.use(
  (res) => res,
  async (err) => {
    if (err.response?.status === 401) {
      const token = await SecureStore.getItemAsync(TOKEN_KEY).catch(() => null);
      if (token && !err.config._retry) {
        err.config._retry = true;
        try {
          const res = await axios.post(`${_baseURL}/auth/refresh`, null, {
            headers: { Authorization: `Bearer ${token}` },
          });
          const newToken = res.data.token;
          await SecureStore.setItemAsync(TOKEN_KEY, newToken);
          err.config.headers.Authorization = `Bearer ${newToken}`;
          return api(err.config);
        } catch {
          await tokenStore.clear();
          if (logoutHandler) logoutHandler();
        }
      } else {
        await tokenStore.clear();
        if (logoutHandler) logoutHandler();
      }
    }
    return Promise.reject(err);
  }
);

// ─── Auth APIs ────────────────────────────────────────────
export const authAPI = {
  login:          (data)  => api.post('/auth/login', data),
  register:       (data)  => api.post('/auth/register', data),
  verifyEmail:    (data)  => api.post('/auth/verify-email', data),
  resendCode:     (email) => api.post(`/auth/resend-code?email=${encodeURIComponent(email)}`),
  getCurrentUser: ()      => api.get('/auth/me'),
  refreshToken:   ()      => api.post('/auth/refresh'),
};

// ─── Admin APIs ───────────────────────────────────────────
export const adminAPI = {
  getDashboard:         ()                     => api.get('/admin/dashboard'),
  getAllDepartments:     (activeOnly = false)   => api.get(`/admin/departments?activeOnly=${activeOnly}`),
  getDepartmentById:    (id)                   => api.get(`/admin/departments/${id}`),
  createDepartment:     (data, init = true)    => api.post(`/admin/departments?initConfig=${init}`, data),
  updateDepartment:     (id, data)             => api.put(`/admin/departments/${id}`, data),
  deactivateDepartment: (id)                   => api.delete(`/admin/departments/${id}`),
  getAllConfigurations:  ()                     => api.get('/admin/config'),
  getSystemDefaults:    ()                     => api.get('/admin/config/system-defaults'),
  resetConfig:          (deptId)               => api.post(`/admin/config/${deptId}/reset`),
};

// ─── User APIs ────────────────────────────────────────────
export const userAPI = {
  getAllUsers:      ()             => api.get('/users'),
  getMyProfile:    ()             => api.get('/users/me'),
  getAllStaff:      ()             => api.get('/users/staff'),
  getAllStudents:   ()             => api.get('/users/students'),
  searchUsers:     (kw)           => api.get(`/users/search?keyword=${encodeURIComponent(kw)}`),
  getUserById:     (id)           => api.get(`/users/${id}`),
  createStaff:     (data)         => api.post('/users', data),
  suspendUser:     (id)           => api.patch(`/users/${id}/suspend`),
  activateUser:    (id)           => api.patch(`/users/${id}/activate`),
  resetPassword:   (id, data)     => api.post(`/users/${id}/reset-password`, data),
  deleteUser:      (id)           => api.delete(`/users/${id}`),
};

// ─── Department Admin APIs ───────────────────────────────
export const deptAdminAPI = {
  getMyDepartment:         ()     => api.get('/department-admin/my-department'),
  getMyDepartmentStaff:    ()     => api.get('/department-admin/my-department/staff'),
  getMyDepartmentStudents: ()     => api.get('/department-admin/my-department/students'),
  getMyDepartmentStats:    ()     => api.get('/department-admin/my-department/stats'),
  getMyDepartmentConfig:   ()     => api.get('/department-admin/my-department/config'),
  resetMyDepartmentConfig: ()     => api.post('/department-admin/my-department/config/reset'),
};

// ─── Equipment APIs ──────────────────────────────────────
export const equipmentAPI = {
  getAllEquipment:        ()         => api.get('/equipment'),
  getEquipmentById:      (id)       => api.get(`/equipment/${id}`),
  createEquipment:       (data)     => api.post('/equipment', data),
  updateEquipment:       (id, data) => api.put(`/equipment/${id}`, data),
  updateEquipmentStatus: (id, data) => api.patch(`/equipment/${id}/status`, data),
};

// ─── Request APIs ────────────────────────────────────────
export const requestAPI = {
  getDepartmentRequests: ()                    => api.get('/requests/department'),
  getMyRequests:         (page = 0, size = 50) => api.get(`/requests/my?page=${page}&size=${size}`),
  getRequestById:        (id)                  => api.get(`/requests/${id}`),
  createRequest:         (data)                => api.post('/requests', data),
  submitRequest:         (id)                  => api.post(`/requests/${id}/submit`),
  cancelRequest:         (id)                  => api.post(`/requests/${id}/cancel`),
};

// ─── Approval APIs ───────────────────────────────────────
export const approvalAPI = {
  getMyQueue:       ()                       => api.get('/approvals/my-queue'),
  processDecision:  (reqId, stage, data)     => api.post(`/approvals/requests/${reqId}/decide?stage=${stage}`, data),
};

// ─── Inspection APIs ─────────────────────────────────────
export const inspectionAPI = {
  issueEquipment:    (data)   => api.post('/inspections/issue', data),
  processReturn:     (data)   => api.post('/inspections/return', data),
  getMyInspections:  ()       => api.get('/inspections/my-inspections'),
  getUnacknowledged: ()       => api.get('/inspections/unacknowledged'),
};

// ─── Penalty APIs ────────────────────────────────────────
export const penaltyAPI = {
  getMyPenalties:           ()             => api.get('/penalties/my'),
  getMySummary:             ()             => api.get('/penalties/my/summary'),
  submitAppeal:             (data)         => api.post('/penalties/appeals', data),
  getDepartmentPenalties:   (deptId)       => api.get(`/penalties/departments/${deptId}`),
  approvePenalty:            (id)           => api.post(`/penalties/${id}/approve`),
  waivePenalty:              (id, reason)   => api.post(`/penalties/${id}/waive?reason=${encodeURIComponent(reason)}`),
};

export default api;
