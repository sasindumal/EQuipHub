'use client';

import { useState, useEffect } from 'react';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { userAPI, adminAPI } from '@/lib/api';
import {
    HiOutlineSearch, HiOutlinePlus, HiOutlineX,
    HiOutlineBan, HiOutlineCheckCircle, HiOutlineTrash,
    HiOutlineKey, HiOutlineUsers, HiOutlinePencil,
    HiOutlineRefresh, HiOutlineExclamationCircle,
} from 'react-icons/hi';

const ROLE_LABELS = {
    SYSTEMADMIN: 'Sys Admin', DEPARTMENTADMIN: 'Dept Admin',
    HEADOFDEPARTMENT: 'HOD', LECTURER: 'Lecturer',
    INSTRUCTOR: 'Instructor', APPOINTEDLECTURER: 'Appt Lec',
    TECHNICALOFFICER: 'Tech Officer', STUDENT: 'Student',
};
const ROLE_BADGE = {
    SYSTEMADMIN: 'badge-primary', DEPARTMENTADMIN: 'badge-info',
    HEADOFDEPARTMENT: 'badge-success', LECTURER: 'badge-warning',
    STUDENT: 'badge-primary', TECHNICALOFFICER: 'badge-success',
};
const STATUS_BADGE = {
    ACTIVE: 'badge-success', PENDING: 'badge-warning',
    SUSPENDED: 'badge-danger', INACTIVE: 'badge-muted',
};
const STAFF_ROLES = ['DEPARTMENTADMIN','HEADOFDEPARTMENT','LECTURER','INSTRUCTOR','APPOINTEDLECTURER','TECHNICALOFFICER'];
const ALL_FILTER_ROLES = ['all','SYSTEMADMIN','DEPARTMENTADMIN','LECTURER','STUDENT','TECHNICALOFFICER'];

const EMPTY_CREATE = {
    email: '', firstName: '', lastName: '', phone: '',
    role: '', departmentId: '', temporaryPassword: '', sendWelcomeEmail: true,
};
const EMPTY_EDIT = { firstName: '', lastName: '', phone: '', role: '', departmentId: '' };
const EMPTY_RESET = { newPassword: '', confirm: '' };

export default function AdminUsersPage() {
    const [users, setUsers]           = useState([]);
    const [departments, setDepts]     = useState([]);
    const [loading, setLoading]       = useState(true);
    const [saving, setSaving]         = useState(false);
    const [search, setSearch]         = useState('');
    const [roleFilter, setRoleFilter] = useState('all');
    const [error, setError]           = useState('');
    const [success, setSuccess]       = useState('');

    // modals
    const [modal, setModal]           = useState(null); // 'create' | 'edit' | 'reset' | 'delete'
    const [target, setTarget]         = useState(null);
    const [createForm, setCreate]     = useState(EMPTY_CREATE);
    const [editForm, setEdit]         = useState(EMPTY_EDIT);
    const [resetForm, setReset]       = useState(EMPTY_RESET);

    useEffect(() => { load(); }, []);

    // debounced search
    useEffect(() => {
        const t = setTimeout(() => {
            if (search.trim().length >= 2) searchUsers();
            else if (search.trim().length === 0) load();
        }, 400);
        return () => clearTimeout(t);
    }, [search]);

    const load = async () => {
        setLoading(true);
        try {
            const [uRes, dRes] = await Promise.allSettled([
                userAPI.getAllUsers(),
                adminAPI.getAllDepartments(),
            ]);
            if (uRes.status === 'fulfilled') {
                const d = uRes.value.data?.data?.users || uRes.value.data?.data || uRes.value.data || [];
                setUsers(Array.isArray(d) ? d : []);
            }
            if (dRes.status === 'fulfilled') {
                const d = dRes.value.data?.data?.departments || dRes.value.data?.data || [];
                setDepts(Array.isArray(d) ? d : []);
            }
        } finally { setLoading(false); }
    };

    const searchUsers = async () => {
        setLoading(true);
        try {
            const res = await userAPI.searchUsers(search);
            const d = res.data?.data?.results || res.data?.data || [];
            setUsers(Array.isArray(d) ? d : []);
        } finally { setLoading(false); }
    };

    const flash = (msg, isErr = false) => {
        isErr ? setError(msg) : setSuccess(msg);
        setTimeout(() => isErr ? setError('') : setSuccess(''), 5000);
    };

    const closeModal = () => { setModal(null); setTarget(null); setError(''); };

    // ── CREATE ──
    const openCreate = () => { setCreate(EMPTY_CREATE); setError(''); setModal('create'); };

    const handleCreate = async (e) => {
        e.preventDefault(); setSaving(true); setError('');
        try {
            await userAPI.createStaff(createForm);
            flash('User created successfully');
            closeModal(); load();
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to create user');
        } finally { setSaving(false); }
    };

    // ── EDIT ──
    const openEdit = (u) => {
        setTarget(u);
        setEdit({
            firstName:    u.firstName    || '',
            lastName:     u.lastName     || '',
            phone:        u.phone        || '',
            role:         u.role         || '',
            departmentId: u.departmentId || '',
        });
        setError(''); setModal('edit');
    };

    const handleEdit = async (e) => {
        e.preventDefault(); setSaving(true); setError('');
        try {
            await userAPI.updateUser(target.userId || target.id, editForm);
            flash('User updated successfully');
            closeModal(); load();
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to update user');
        } finally { setSaving(false); }
    };

    // ── RESET PASSWORD ──
    const openReset = (u) => { setTarget(u); setReset(EMPTY_RESET); setError(''); setModal('reset'); };

    const handleReset = async (e) => {
        e.preventDefault();
        if (resetForm.newPassword.length < 8) { setError('Password must be at least 8 characters'); return; }
        if (resetForm.newPassword !== resetForm.confirm) { setError('Passwords do not match'); return; }
        setSaving(true); setError('');
        try {
            await userAPI.resetPassword(target.userId || target.id, { newPassword: resetForm.newPassword });
            flash('Password reset successfully');
            closeModal();
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to reset password');
        } finally { setSaving(false); }
    };

    // ── SUSPEND / ACTIVATE ──
    const handleSuspend = async (u) => {
        try { await userAPI.suspendUser(u.userId || u.id); load(); flash(`${u.firstName} suspended`); }
        catch (err) { flash(err.response?.data?.message || 'Failed to suspend', true); }
    };
    const handleActivate = async (u) => {
        try { await userAPI.activateUser(u.userId || u.id); load(); flash(`${u.firstName} activated`); }
        catch (err) { flash(err.response?.data?.message || 'Failed to activate', true); }
    };

    // ── DELETE ──
    const openDelete = (u) => { setTarget(u); setModal('delete'); };
    const handleDelete = async () => {
        setSaving(true);
        try {
            await userAPI.deleteUser(target.userId || target.id);
            flash('User deleted');
            closeModal(); load();
        } catch (err) {
            flash(err.response?.data?.message || 'Failed to delete', true);
            closeModal();
        } finally { setSaving(false); }
    };

    const filtered = users.filter(u => roleFilter === 'all' || u.role === roleFilter);

    return (
        <DashboardLayout pageTitle="Users" pageSubtitle="Manage all system users">

            {error   && <div className="alert alert-danger"  style={{ marginBottom: 16 }}>{error}</div>}
            {success && <div className="alert alert-success" style={{ marginBottom: 16 }}>{success}</div>}

            {/* Toolbar */}
            <div className="action-bar">
                <div style={{ display: 'flex', gap: 12, alignItems: 'center', flexWrap: 'wrap', flex: 1 }}>
                    <div className="search-bar">
                        <div className="search-input-wrapper">
                            <HiOutlineSearch className="search-icon" />
                            <input className="search-input" placeholder="Search by name or email…"
                                value={search} onChange={e => setSearch(e.target.value)} />
                        </div>
                    </div>
                    <div className="filter-tabs">
                        {ALL_FILTER_ROLES.map(f => (
                            <button key={f} className={`filter-tab ${roleFilter === f ? 'active' : ''}`}
                                onClick={() => setRoleFilter(f)}>
                                {f === 'all' ? 'All' : ROLE_LABELS[f] || f}
                            </button>
                        ))}
                    </div>
                </div>
                <div style={{ display: 'flex', gap: 8 }}>
                    <button className="btn btn-outline btn-sm" onClick={load} style={{ display:'flex', alignItems:'center', gap:4 }}>
                        <HiOutlineRefresh /> Refresh
                    </button>
                    <button className="btn btn-primary" onClick={openCreate}>
                        <HiOutlinePlus /> Add Staff
                    </button>
                </div>
            </div>

            {/* Summary pills */}
            <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 20 }}>
                {['ACTIVE','PENDING','SUSPENDED'].map(s => {
                    const cnt = users.filter(u => u.status === s).length;
                    return (
                        <div key={s} style={{ padding: '6px 14px', borderRadius: 'var(--radius-sm)', background: 'var(--bg-light)', border: '1px solid var(--border)', fontSize: 13 }}>
                            <span style={{ color: 'var(--secondary)', marginRight: 4 }}>{s}:</span>
                            <strong>{cnt}</strong>
                        </div>
                    );
                })}
                <div style={{ padding: '6px 14px', borderRadius: 'var(--radius-sm)', background: 'var(--bg-light)', border: '1px solid var(--border)', fontSize: 13 }}>
                    <span style={{ color: 'var(--secondary)', marginRight: 4 }}>Total:</span>
                    <strong>{users.length}</strong>
                </div>
            </div>

            {/* Table */}
            <div className="content-card">
                <div className="table-container">
                    <table className="table">
                        <thead><tr>
                            <th>User</th>
                            <th className="hide-mobile">Email</th>
                            <th>Role</th>
                            <th className="hide-mobile">Department</th>
                            <th>Status</th>
                            <th style={{ textAlign: 'right' }}>Actions</th>
                        </tr></thead>
                        <tbody>
                            {loading ? (
                                [...Array(6)].map((_, i) => (
                                    <tr key={i}>
                                        {[130,180,80,100,60,100].map((w,j) => (
                                            <td key={j} className={j===1||j===3?'hide-mobile':''}>
                                                <div className="skeleton" style={{ width: w, height: 16 }} />
                                            </td>
                                        ))}
                                    </tr>
                                ))
                            ) : filtered.length > 0 ? (
                                filtered.map(u => (
                                    <tr key={u.userId || u.id}>
                                        <td>
                                            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                                                <div className="sidebar-avatar" style={{ width: 32, height: 32, fontSize: 11 }}>
                                                    {(u.firstName||'?')[0]}{(u.lastName||'?')[0]}
                                                </div>
                                                <div>
                                                    <div style={{ fontWeight: 600 }}>{u.firstName} {u.lastName}</div>
                                                    {u.indexNumber && <div style={{ fontSize: 11, color: 'var(--secondary)' }}>{u.indexNumber}</div>}
                                                </div>
                                            </div>
                                        </td>
                                        <td className="hide-mobile" style={{ color:'var(--secondary)', fontSize:13 }}>{u.email}</td>
                                        <td><span className={`badge ${ROLE_BADGE[u.role]||'badge-muted'}`}>{ROLE_LABELS[u.role]||u.role}</span></td>
                                        <td className="hide-mobile" style={{ fontSize:13 }}>{u.departmentName || u.departmentId || '—'}</td>
                                        <td><span className={`badge ${STATUS_BADGE[u.status]||'badge-muted'}`}>{u.status}</span></td>
                                        <td style={{ textAlign:'right' }}>
                                            <div style={{ display:'flex', gap:4, justifyContent:'flex-end' }}>
                                                <button className="btn btn-ghost btn-sm" title="Edit" onClick={() => openEdit(u)}>
                                                    <HiOutlinePencil />
                                                </button>
                                                {u.status === 'ACTIVE' ? (
                                                    <button className="btn btn-ghost btn-sm" title="Suspend"
                                                        style={{ color:'var(--warning)' }} onClick={() => handleSuspend(u)}>
                                                        <HiOutlineBan />
                                                    </button>
                                                ) : u.status === 'SUSPENDED' ? (
                                                    <button className="btn btn-ghost btn-sm" title="Activate"
                                                        style={{ color:'var(--success)' }} onClick={() => handleActivate(u)}>
                                                        <HiOutlineCheckCircle />
                                                    </button>
                                                ) : null}
                                                <button className="btn btn-ghost btn-sm" title="Reset Password"
                                                    onClick={() => openReset(u)}>
                                                    <HiOutlineKey />
                                                </button>
                                                <button className="btn btn-ghost btn-sm" title="Delete"
                                                    style={{ color:'var(--danger)' }} onClick={() => openDelete(u)}>
                                                    <HiOutlineTrash />
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr><td colSpan={6}>
                                    <div className="empty-state">
                                        <HiOutlineUsers className="empty-state-icon" />
                                        <div className="empty-state-title">No users found</div>
                                        <div className="empty-state-text">Try adjusting your search or filter.</div>
                                    </div>
                                </td></tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* ── CREATE MODAL ── */}
            {modal === 'create' && (
                <div className="modal-overlay" onClick={closeModal}>
                    <div className="modal-content" style={{ maxWidth: 560 }} onClick={e => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>Create Staff User</h2>
                            <button className="modal-close" onClick={closeModal}><HiOutlineX /></button>
                        </div>
                        <form onSubmit={handleCreate}>
                            <div className="modal-body">
                                {error && <div className="alert alert-danger" style={{ marginBottom: 12 }}>{error}</div>}
                                <div className="form-row">
                                    <div className="form-group">
                                        <label className="form-label">First Name *</label>
                                        <input className="form-input" value={createForm.firstName}
                                            onChange={e => setCreate({...createForm, firstName: e.target.value})} required />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Last Name *</label>
                                        <input className="form-input" value={createForm.lastName}
                                            onChange={e => setCreate({...createForm, lastName: e.target.value})} required />
                                    </div>
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Email *</label>
                                    <input type="email" className="form-input" value={createForm.email}
                                        onChange={e => setCreate({...createForm, email: e.target.value})} required />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Phone</label>
                                    <input className="form-input" placeholder="+94 77 123 4567" value={createForm.phone}
                                        onChange={e => setCreate({...createForm, phone: e.target.value})} />
                                </div>
                                <div className="form-row">
                                    <div className="form-group">
                                        <label className="form-label">Role *</label>
                                        <select className="form-select" value={createForm.role}
                                            onChange={e => setCreate({...createForm, role: e.target.value})} required>
                                            <option value="">Select role…</option>
                                            {STAFF_ROLES.map(r => <option key={r} value={r}>{ROLE_LABELS[r]||r}</option>)}
                                        </select>
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Department *</label>
                                        <select className="form-select" value={createForm.departmentId}
                                            onChange={e => setCreate({...createForm, departmentId: e.target.value})} required>
                                            <option value="">Select department…</option>
                                            {departments.map(d => <option key={d.departmentId||d.id} value={d.departmentId||d.id}>{d.name}</option>)}
                                        </select>
                                    </div>
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Temporary Password *</label>
                                    <input type="password" className="form-input" placeholder="Min 8 characters"
                                        value={createForm.temporaryPassword} minLength={8} required
                                        onChange={e => setCreate({...createForm, temporaryPassword: e.target.value})} />
                                </div>
                                <div className="form-group">
                                    <label style={{ display:'flex', alignItems:'center', gap:8, cursor:'pointer', fontSize:'var(--font-size-sm)' }}>
                                        <input type="checkbox" checked={createForm.sendWelcomeEmail}
                                            onChange={e => setCreate({...createForm, sendWelcomeEmail: e.target.checked})} />
                                        Send welcome email with credentials
                                    </label>
                                </div>
                            </div>
                            <div className="modal-footer">
                                <button type="button" className="btn btn-secondary" onClick={closeModal}>Cancel</button>
                                <button type="submit" className="btn btn-primary" disabled={saving}>
                                    {saving ? 'Creating…' : 'Create User'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* ── EDIT MODAL ── */}
            {modal === 'edit' && target && (
                <div className="modal-overlay" onClick={closeModal}>
                    <div className="modal-content" style={{ maxWidth: 520 }} onClick={e => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>Edit User — {target.firstName} {target.lastName}</h2>
                            <button className="modal-close" onClick={closeModal}><HiOutlineX /></button>
                        </div>
                        <form onSubmit={handleEdit}>
                            <div className="modal-body">
                                {error && <div className="alert alert-danger" style={{ marginBottom: 12 }}>{error}</div>}
                                <div className="form-row">
                                    <div className="form-group">
                                        <label className="form-label">First Name *</label>
                                        <input className="form-input" value={editForm.firstName}
                                            onChange={e => setEdit({...editForm, firstName: e.target.value})} required />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Last Name *</label>
                                        <input className="form-input" value={editForm.lastName}
                                            onChange={e => setEdit({...editForm, lastName: e.target.value})} required />
                                    </div>
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Phone</label>
                                    <input className="form-input" placeholder="+94 77 123 4567" value={editForm.phone}
                                        onChange={e => setEdit({...editForm, phone: e.target.value})} />
                                </div>
                                <div className="form-row">
                                    <div className="form-group">
                                        <label className="form-label">Role *</label>
                                        <select className="form-select" value={editForm.role}
                                            onChange={e => setEdit({...editForm, role: e.target.value})} required>
                                            <option value="">Select role…</option>
                                            {STAFF_ROLES.map(r => <option key={r} value={r}>{ROLE_LABELS[r]||r}</option>)}
                                        </select>
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Department</label>
                                        <select className="form-select" value={editForm.departmentId}
                                            onChange={e => setEdit({...editForm, departmentId: e.target.value})}>
                                            <option value="">No change / unassigned</option>
                                            {departments.map(d => <option key={d.departmentId||d.id} value={d.departmentId||d.id}>{d.name}</option>)}
                                        </select>
                                    </div>
                                </div>
                                <p style={{ fontSize: 12, color: 'var(--secondary)', marginTop: 4 }}>
                                    Note: email and index number cannot be changed after account creation.
                                </p>
                            </div>
                            <div className="modal-footer">
                                <button type="button" className="btn btn-secondary" onClick={closeModal}>Cancel</button>
                                <button type="submit" className="btn btn-primary" disabled={saving}>
                                    {saving ? 'Saving…' : 'Save Changes'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* ── RESET PASSWORD MODAL ── */}
            {modal === 'reset' && target && (
                <div className="modal-overlay" onClick={closeModal}>
                    <div className="modal-content" style={{ maxWidth: 420 }} onClick={e => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>Reset Password</h2>
                            <button className="modal-close" onClick={closeModal}><HiOutlineX /></button>
                        </div>
                        <form onSubmit={handleReset}>
                            <div className="modal-body">
                                {error && <div className="alert alert-danger" style={{ marginBottom: 12 }}>{error}</div>}
                                <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--secondary)', marginBottom: 16 }}>
                                    Set a new temporary password for <strong>{target.firstName} {target.lastName}</strong>.
                                </p>
                                <div className="form-group">
                                    <label className="form-label">New Password *</label>
                                    <input type="password" className="form-input" placeholder="Min 8 characters"
                                        value={resetForm.newPassword} minLength={8} required
                                        onChange={e => setReset({...resetForm, newPassword: e.target.value})} />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Confirm Password *</label>
                                    <input type="password" className="form-input" placeholder="Repeat password"
                                        value={resetForm.confirm} required
                                        onChange={e => setReset({...resetForm, confirm: e.target.value})} />
                                </div>
                            </div>
                            <div className="modal-footer">
                                <button type="button" className="btn btn-secondary" onClick={closeModal}>Cancel</button>
                                <button type="submit" className="btn btn-primary" disabled={saving}>
                                    {saving ? 'Resetting…' : 'Reset Password'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* ── DELETE CONFIRM MODAL ── */}
            {modal === 'delete' && target && (
                <div className="modal-overlay" onClick={closeModal}>
                    <div className="modal-content" style={{ maxWidth: 400, textAlign: 'center', padding: 28 }} onClick={e => e.stopPropagation()}>
                        <HiOutlineExclamationCircle style={{ fontSize: 44, color: 'var(--danger)', marginBottom: 12 }} />
                        <h3 style={{ margin: '0 0 8px', fontWeight: 700 }}>Delete User?</h3>
                        <p style={{ fontSize: 14, color: 'var(--secondary)', marginBottom: 24 }}>
                            This will permanently delete <strong>{target.firstName} {target.lastName}</strong>.
                            This action cannot be undone.
                        </p>
                        <div style={{ display:'flex', gap:10, justifyContent:'center' }}>
                            <button className="btn btn-secondary" onClick={closeModal}>Cancel</button>
                            <button className="btn btn-primary" style={{ background:'var(--danger)' }}
                                onClick={handleDelete} disabled={saving}>
                                {saving ? 'Deleting…' : 'Delete'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </DashboardLayout>
    );
}
