'use client';

import { useState, useEffect } from 'react';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { deptAdminAPI, userAPI } from '@/lib/api';
import {
    HiOutlineSearch,
    HiOutlinePlus,
    HiOutlineX,
    HiOutlineBan,
    HiOutlineCheckCircle,
    HiOutlineKey,
    HiOutlineUserGroup,
} from 'react-icons/hi';

export default function DeptStaffPage() {
    const [staff, setStaff] = useState([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [createForm, setCreateForm] = useState({
        email: '', firstName: '', lastName: '', phone: '',
        role: 'LECTURER', departmentId: '', temporaryPassword: '', sendWelcomeEmail: true,
    });
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => { loadStaff(); }, []);

    const loadStaff = async () => {
        try {
            const res = await deptAdminAPI.getMyDepartmentStaff();
            const data = res.data?.data?.staff || res.data?.data || [];
            setStaff(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleCreate = async (e) => {
        e.preventDefault();
        setSaving(true);
        setError('');
        try {
            await userAPI.createStaff(createForm);
            setShowCreateModal(false);
            setCreateForm({ email: '', firstName: '', lastName: '', phone: '', role: 'LECTURER', departmentId: '', temporaryPassword: '', sendWelcomeEmail: true });
            loadStaff();
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to create');
        } finally {
            setSaving(false);
        }
    };

    const handleSuspend = async (u) => {
        if (!confirm(`Suspend "${u.firstName} ${u.lastName}"?`)) return;
        try { await userAPI.suspendUser(u.userId || u.id); loadStaff(); } catch (err) { alert(err.response?.data?.message || 'Failed'); }
    };

    const handleActivate = async (u) => {
        try { await userAPI.activateUser(u.userId || u.id); loadStaff(); } catch (err) { alert(err.response?.data?.message || 'Failed'); }
    };

    const formatRole = (r) => {
        const m = { DEPARTMENTADMIN: 'Dept Admin', HEADOFDEPARTMENT: 'HOD', LECTURER: 'Lecturer', INSTRUCTOR: 'Instructor', APPOINTEDLECTURER: 'Appt Lec', TECHNICALOFFICER: 'Tech Officer' };
        return m[r] || r;
    };
    const getRoleBadge = (r) => {
        const m = { DEPARTMENTADMIN: 'badge-info', HEADOFDEPARTMENT: 'badge-success', LECTURER: 'badge-warning', TECHNICALOFFICER: 'badge-success' };
        return m[r] || 'badge-muted';
    };
    const getStatusBadge = (s) => s === 'ACTIVE' ? 'badge-success' : s === 'SUSPENDED' ? 'badge-danger' : 'badge-warning';

    const filtered = staff.filter((s) =>
        `${s.firstName} ${s.lastName} ${s.email}`.toLowerCase().includes(search.toLowerCase())
    );

    const staffRoles = ['LECTURER', 'INSTRUCTOR', 'APPOINTEDLECTURER', 'TECHNICALOFFICER'];

    return (
        <DashboardLayout pageTitle="Staff" pageSubtitle="Manage department staff members">
            <div className="action-bar">
                <div className="search-bar">
                    <div className="search-input-wrapper">
                        <HiOutlineSearch className="search-icon" />
                        <input className="search-input" placeholder="Search staff..." value={search} onChange={(e) => setSearch(e.target.value)} />
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
                                <th>Name</th>
                                <th className="hide-mobile">Email</th>
                                <th>Role</th>
                                <th>Status</th>
                                <th style={{ textAlign: 'right' }}>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                [...Array(3)].map((_, i) => (
                                    <tr key={i}>
                                        <td><div className="skeleton" style={{ width: 130, height: 16 }} /></td>
                                        <td className="hide-mobile"><div className="skeleton" style={{ width: 180, height: 16 }} /></td>
                                        <td><div className="skeleton" style={{ width: 80, height: 24, borderRadius: 12 }} /></td>
                                        <td><div className="skeleton" style={{ width: 60, height: 24, borderRadius: 12 }} /></td>
                                        <td><div className="skeleton" style={{ width: 80, height: 16 }} /></td>
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
                                                    <button className="btn btn-ghost btn-sm" title="Suspend" style={{ color: 'var(--warning)' }} onClick={() => handleSuspend(u)}><HiOutlineBan /></button>
                                                ) : u.status === 'SUSPENDED' ? (
                                                    <button className="btn btn-ghost btn-sm" title="Activate" style={{ color: 'var(--success)' }} onClick={() => handleActivate(u)}><HiOutlineCheckCircle /></button>
                                                ) : null}
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan={5}>
                                        <div className="empty-state">
                                            <HiOutlineUserGroup className="empty-state-icon" />
                                            <div className="empty-state-title">No staff found</div>
                                            <div className="empty-state-text">Add staff members to your department.</div>
                                        </div>
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {showCreateModal && (
                <div className="modal-overlay" onClick={() => setShowCreateModal(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>Add Staff Member</h2>
                            <button className="modal-close" onClick={() => setShowCreateModal(false)}><HiOutlineX /></button>
                        </div>
                        <form onSubmit={handleCreate}>
                            <div className="modal-body">
                                {error && <div className="auth-alert error" style={{ marginBottom: 16 }}>{error}</div>}
                                <div className="form-row">
                                    <div className="form-group">
                                        <label className="form-label">First Name</label>
                                        <input className="form-input" value={createForm.firstName} onChange={(e) => setCreateForm({ ...createForm, firstName: e.target.value })} required />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Last Name</label>
                                        <input className="form-input" value={createForm.lastName} onChange={(e) => setCreateForm({ ...createForm, lastName: e.target.value })} required />
                                    </div>
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Email</label>
                                    <input type="email" className="form-input" value={createForm.email} onChange={(e) => setCreateForm({ ...createForm, email: e.target.value })} required />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Role</label>
                                    <select className="form-select" value={createForm.role} onChange={(e) => setCreateForm({ ...createForm, role: e.target.value })} required>
                                        {staffRoles.map((r) => <option key={r} value={r}>{formatRole(r)}</option>)}
                                    </select>
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Temporary Password</label>
                                    <input type="password" className="form-input" placeholder="Min 8 characters" value={createForm.temporaryPassword} onChange={(e) => setCreateForm({ ...createForm, temporaryPassword: e.target.value })} required minLength={8} />
                                </div>
                            </div>
                            <div className="modal-footer">
                                <button type="button" className="btn btn-secondary" onClick={() => setShowCreateModal(false)}>Cancel</button>
                                <button type="submit" className="btn btn-primary" disabled={saving}>
                                    {saving ? <><span className="spinner" /> Adding...</> : 'Add Staff'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </DashboardLayout>
    );
}
