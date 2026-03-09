'use client';

import { useState, useEffect } from 'react';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { userAPI, adminAPI } from '@/lib/api';
import {
    HiOutlineSearch,
    HiOutlinePlus,
    HiOutlineX,
    HiOutlineBan,
    HiOutlineCheckCircle,
    HiOutlineTrash,
    HiOutlineKey,
    HiOutlineUsers,
} from 'react-icons/hi';

export default function UsersPage() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');
    const [activeFilter, setActiveFilter] = useState('all');
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showResetModal, setShowResetModal] = useState(false);
    const [selectedUser, setSelectedUser] = useState(null);
    const [departments, setDepartments] = useState([]);
    const [createForm, setCreateForm] = useState({
        email: '', firstName: '', lastName: '', phone: '',
        role: '', departmentId: '', temporaryPassword: '', sendWelcomeEmail: true,
    });
    const [resetForm, setResetForm] = useState({ newPassword: '' });
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => { loadData(); }, []);

    const loadData = async () => {
        try {
            const [usersRes, deptsRes] = await Promise.allSettled([
                userAPI.getAllUsers(),
                adminAPI.getAllDepartments(),
            ]);
            if (usersRes.status === 'fulfilled') {
                const data = usersRes.value.data?.data?.users || usersRes.value.data?.data || [];
                setUsers(Array.isArray(data) ? data : []);
            }
            if (deptsRes.status === 'fulfilled') {
                const data = deptsRes.value.data?.data?.departments || deptsRes.value.data?.data || [];
                setDepartments(Array.isArray(data) ? data : []);
            }
        } catch (err) {
            console.error('Load error:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = async () => {
        if (search.trim().length < 2) return loadData();
        setLoading(true);
        try {
            const res = await userAPI.searchUsers(search);
            const data = res.data?.data?.results || res.data?.data || [];
            setUsers(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const timeout = setTimeout(() => {
            if (search.trim().length >= 2) handleSearch();
            else if (search.trim().length === 0) loadData();
        }, 400);
        return () => clearTimeout(timeout);
    }, [search]);

    const handleCreateStaff = async (e) => {
        e.preventDefault();
        setSaving(true);
        setError('');
        try {
            await userAPI.createStaff(createForm);
            setShowCreateModal(false);
            setCreateForm({ email: '', firstName: '', lastName: '', phone: '', role: '', departmentId: '', temporaryPassword: '', sendWelcomeEmail: true });
            loadData();
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to create user');
        } finally {
            setSaving(false);
        }
    };

    const handleSuspend = async (user) => {
        if (!confirm(`Suspend "${user.firstName} ${user.lastName}"?`)) return;
        try {
            await userAPI.suspendUser(user.userId || user.id);
            loadData();
        } catch (err) {
            alert(err.response?.data?.message || 'Failed to suspend');
        }
    };

    const handleActivate = async (user) => {
        try {
            await userAPI.activateUser(user.userId || user.id);
            loadData();
        } catch (err) {
            alert(err.response?.data?.message || 'Failed to activate');
        }
    };

    const handleDelete = async (user) => {
        if (!confirm(`Are you sure you want to delete "${user.firstName} ${user.lastName}"? This action cannot be undone.`)) return;
        try {
            await userAPI.deleteUser(user.userId || user.id);
            loadData();
        } catch (err) {
            alert(err.response?.data?.message || 'Failed to delete');
        }
    };

    const handleResetPassword = async (e) => {
        e.preventDefault();
        if (resetForm.newPassword.length < 8) {
            setError('Password must be at least 8 characters');
            return;
        }
        setSaving(true);
        setError('');
        try {
            await userAPI.resetPassword(selectedUser.userId || selectedUser.id, resetForm);
            setShowResetModal(false);
            setResetForm({ newPassword: '' });
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to reset password');
        } finally {
            setSaving(false);
        }
    };

    const filters = ['all', 'SYSTEMADMIN', 'DEPARTMENTADMIN', 'LECTURER', 'STUDENT', 'TECHNICALOFFICER'];
    const formatRole = (r) => {
        const m = { SYSTEMADMIN: 'Sys Admin', DEPARTMENTADMIN: 'Dept Admin', HEADOFDEPARTMENT: 'HOD', LECTURER: 'Lecturer', INSTRUCTOR: 'Instructor', APPOINTEDLECTURER: 'Appt Lec', TECHNICALOFFICER: 'Tech Officer', STUDENT: 'Student' };
        return m[r] || r;
    };
    const getRoleBadge = (role) => {
        const m = { SYSTEMADMIN: 'badge-primary', DEPARTMENTADMIN: 'badge-info', HEADOFDEPARTMENT: 'badge-success', LECTURER: 'badge-warning', STUDENT: 'badge-primary', TECHNICALOFFICER: 'badge-success' };
        return m[role] || 'badge-muted';
    };
    const getStatusBadge = (s) => s === 'ACTIVE' ? 'badge-success' : s === 'PENDING' ? 'badge-warning' : s === 'SUSPENDED' ? 'badge-danger' : 'badge-muted';

    const filtered = users.filter((u) => {
        if (activeFilter !== 'all' && u.role !== activeFilter) return false;
        return true;
    });

    const staffRoles = ['DEPARTMENTADMIN', 'HEADOFDEPARTMENT', 'LECTURER', 'INSTRUCTOR', 'APPOINTEDLECTURER', 'TECHNICALOFFICER'];

    return (
        <DashboardLayout pageTitle="Users" pageSubtitle="Manage all system users">
            <div className="action-bar">
                <div style={{ display: 'flex', gap: 12, alignItems: 'center', flexWrap: 'wrap', flex: 1 }}>
                    <div className="search-bar">
                        <div className="search-input-wrapper">
                            <HiOutlineSearch className="search-icon" />
                            <input
                                className="search-input"
                                placeholder="Search by name or email..."
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                            />
                        </div>
                    </div>
                    <div className="filter-tabs">
                        {filters.map((f) => (
                            <button
                                key={f}
                                className={`filter-tab ${activeFilter === f ? 'active' : ''}`}
                                onClick={() => setActiveFilter(f)}
                            >
                                {f === 'all' ? 'All' : formatRole(f)}
                            </button>
                        ))}
                    </div>
                </div>
                <button className="btn btn-primary" onClick={() => { setError(''); setShowCreateModal(true); }}>
                    <HiOutlinePlus /> Add Staff
                </button>
            </div>

            <div className="content-card">
                <div className="table-container">
                    <table className="table">
                        <thead>
                            <tr>
                                <th>User</th>
                                <th className="hide-mobile">Email</th>
                                <th>Role</th>
                                <th>Status</th>
                                <th style={{ textAlign: 'right' }}>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                [...Array(5)].map((_, i) => (
                                    <tr key={i}>
                                        <td><div className="skeleton" style={{ width: 130, height: 16 }} /></td>
                                        <td className="hide-mobile"><div className="skeleton" style={{ width: 180, height: 16 }} /></td>
                                        <td><div className="skeleton" style={{ width: 80, height: 24, borderRadius: 12 }} /></td>
                                        <td><div className="skeleton" style={{ width: 60, height: 24, borderRadius: 12 }} /></td>
                                        <td><div className="skeleton" style={{ width: 100, height: 16 }} /></td>
                                    </tr>
                                ))
                            ) : filtered.length > 0 ? (
                                filtered.map((u) => (
                                    <tr key={u.userId || u.id}>
                                        <td>
                                            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                                                <div className="sidebar-avatar" style={{ width: 32, height: 32, fontSize: 11 }}>
                                                    {(u.firstName || '?')[0]}{(u.lastName || '?')[0]}
                                                </div>
                                                <span style={{ fontWeight: 600 }}>{u.firstName} {u.lastName}</span>
                                            </div>
                                        </td>
                                        <td className="hide-mobile" style={{ color: 'var(--secondary)' }}>{u.email}</td>
                                        <td><span className={`badge ${getRoleBadge(u.role)}`}>{formatRole(u.role)}</span></td>
                                        <td><span className={`badge ${getStatusBadge(u.status)}`}>{u.status}</span></td>
                                        <td style={{ textAlign: 'right' }}>
                                            <div style={{ display: 'flex', gap: 4, justifyContent: 'flex-end' }}>
                                                {u.status === 'ACTIVE' ? (
                                                    <button className="btn btn-ghost btn-sm" title="Suspend" style={{ color: 'var(--warning)' }} onClick={() => handleSuspend(u)}>
                                                        <HiOutlineBan />
                                                    </button>
                                                ) : (u.status === 'SUSPENDED') ? (
                                                    <button className="btn btn-ghost btn-sm" title="Activate" style={{ color: 'var(--success)' }} onClick={() => handleActivate(u)}>
                                                        <HiOutlineCheckCircle />
                                                    </button>
                                                ) : null}
                                                <button className="btn btn-ghost btn-sm" title="Reset Password" onClick={() => { setSelectedUser(u); setResetForm({ newPassword: '' }); setError(''); setShowResetModal(true); }}>
                                                    <HiOutlineKey />
                                                </button>
                                                <button className="btn btn-ghost btn-sm" title="Delete" style={{ color: 'var(--danger)' }} onClick={() => handleDelete(u)}>
                                                    <HiOutlineTrash />
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan={5}>
                                        <div className="empty-state">
                                            <HiOutlineUsers className="empty-state-icon" />
                                            <div className="empty-state-title">No users found</div>
                                            <div className="empty-state-text">Try adjusting your search or filter.</div>
                                        </div>
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Create Staff Modal */}
            {showCreateModal && (
                <div className="modal-overlay" onClick={() => setShowCreateModal(false)}>
                    <div className="modal-content" style={{ maxWidth: 560 }} onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>Create Staff User</h2>
                            <button className="modal-close" onClick={() => setShowCreateModal(false)}><HiOutlineX /></button>
                        </div>
                        <form onSubmit={handleCreateStaff}>
                            <div className="modal-body">
                                {error && <div className="auth-alert error" style={{ marginBottom: 16 }}>{error}</div>}
                                <div className="form-row">
                                    <div className="form-group">
                                        <label className="form-label">First Name</label>
                                        <input className="form-input" placeholder="John" value={createForm.firstName} onChange={(e) => setCreateForm({ ...createForm, firstName: e.target.value })} required />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Last Name</label>
                                        <input className="form-input" placeholder="Doe" value={createForm.lastName} onChange={(e) => setCreateForm({ ...createForm, lastName: e.target.value })} required />
                                    </div>
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Email</label>
                                    <input type="email" className="form-input" placeholder="john@eng.jfn.ac.lk" value={createForm.email} onChange={(e) => setCreateForm({ ...createForm, email: e.target.value })} required />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Phone (optional)</label>
                                    <input className="form-input" placeholder="+94 77 123 4567" value={createForm.phone} onChange={(e) => setCreateForm({ ...createForm, phone: e.target.value })} />
                                </div>
                                <div className="form-row">
                                    <div className="form-group">
                                        <label className="form-label">Role</label>
                                        <select className="form-select" value={createForm.role} onChange={(e) => setCreateForm({ ...createForm, role: e.target.value })} required>
                                            <option value="">Select role</option>
                                            {staffRoles.map((r) => (
                                                <option key={r} value={r}>{formatRole(r)}</option>
                                            ))}
                                        </select>
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Department</label>
                                        <select className="form-select" value={createForm.departmentId} onChange={(e) => setCreateForm({ ...createForm, departmentId: e.target.value })} required>
                                            <option value="">Select department</option>
                                            {departments.map((d) => (
                                                <option key={d.departmentId || d.id} value={d.departmentId || d.id}>{d.name}</option>
                                            ))}
                                        </select>
                                    </div>
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Temporary Password</label>
                                    <input type="password" className="form-input" placeholder="Min 8 characters" value={createForm.temporaryPassword} onChange={(e) => setCreateForm({ ...createForm, temporaryPassword: e.target.value })} required minLength={8} />
                                </div>
                                <div className="form-group">
                                    <label style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer', fontSize: 'var(--font-size-sm)' }}>
                                        <input type="checkbox" checked={createForm.sendWelcomeEmail} onChange={(e) => setCreateForm({ ...createForm, sendWelcomeEmail: e.target.checked })} />
                                        Send welcome email with credentials
                                    </label>
                                </div>
                            </div>
                            <div className="modal-footer">
                                <button type="button" className="btn btn-secondary" onClick={() => setShowCreateModal(false)}>Cancel</button>
                                <button type="submit" className="btn btn-primary" disabled={saving}>
                                    {saving ? <><span className="spinner" /> Creating...</> : 'Create User'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Reset Password Modal */}
            {showResetModal && (
                <div className="modal-overlay" onClick={() => setShowResetModal(false)}>
                    <div className="modal-content" style={{ maxWidth: 420 }} onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>Reset Password</h2>
                            <button className="modal-close" onClick={() => setShowResetModal(false)}><HiOutlineX /></button>
                        </div>
                        <form onSubmit={handleResetPassword}>
                            <div className="modal-body">
                                {error && <div className="auth-alert error" style={{ marginBottom: 16 }}>{error}</div>}
                                <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--secondary)', marginBottom: 16 }}>
                                    Set a new temporary password for <strong>{selectedUser?.firstName} {selectedUser?.lastName}</strong>.
                                </p>
                                <div className="form-group">
                                    <label className="form-label">New Password</label>
                                    <input type="password" className="form-input" placeholder="Min 8 characters" value={resetForm.newPassword} onChange={(e) => setResetForm({ newPassword: e.target.value })} required minLength={8} />
                                </div>
                            </div>
                            <div className="modal-footer">
                                <button type="button" className="btn btn-secondary" onClick={() => setShowResetModal(false)}>Cancel</button>
                                <button type="submit" className="btn btn-primary" disabled={saving}>
                                    {saving ? <><span className="spinner" /> Resetting...</> : 'Reset Password'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </DashboardLayout>
    );
}
