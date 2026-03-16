'use client';

import { useState, useEffect } from 'react';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { adminAPI } from '@/lib/api';
import {
    HiOutlineSearch,
    HiOutlinePlus,
    HiOutlinePencil,
    HiOutlineTrash,
    HiOutlineX,
    HiOutlineOfficeBuilding,
} from 'react-icons/hi';

export default function DepartmentsPage() {
    const [departments, setDepartments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [editingDept, setEditingDept] = useState(null);
    const [form, setForm] = useState({ name: '', code: '', description: '' });
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => { loadDepartments(); }, []);

    const loadDepartments = async () => {
        try {
            const res = await adminAPI.getAllDepartments();
            const data = res.data?.data?.departments || res.data?.data || res.data || [];
            setDepartments(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error('Load departments error:', err);
        } finally {
            setLoading(false);
        }
    };

    const openCreate = () => {
        setEditingDept(null);
        setForm({ name: '', code: '', description: '' });
        setError('');
        setShowModal(true);
    };

    const openEdit = (dept) => {
        setEditingDept(dept);
        setForm({
            name: dept.name || '',
            code: dept.code || dept.departmentCode || '',
            description: dept.description || '',
        });
        setError('');
        setShowModal(true);
    };

    const handleSave = async (e) => {
        e.preventDefault();
        if (!form.name.trim() || !form.code.trim()) {
            setError('Name and code are required');
            return;
        }
        setSaving(true);
        setError('');
        try {
            if (editingDept) {
                await adminAPI.updateDepartment(editingDept.departmentId || editingDept.id, form);
            } else {
                await adminAPI.createDepartment(form);
            }
            setShowModal(false);
            loadDepartments();
        } catch (err) {
            setError(err.response?.data?.message || 'Operation failed');
        } finally {
            setSaving(false);
        }
    };

    const handleDeactivate = async (dept) => {
        if (!confirm(`Are you sure you want to deactivate "${dept.name}"?`)) return;
        try {
            await adminAPI.deactivateDepartment(dept.departmentId || dept.id);
            loadDepartments();
        } catch (err) {
            alert(err.response?.data?.message || 'Failed to deactivate');
        }
    };

    const filtered = departments.filter(
        (d) =>
            (d.name || '').toLowerCase().includes(search.toLowerCase()) ||
            (d.code || d.departmentCode || '').toLowerCase().includes(search.toLowerCase())
    );

    return (
        <DashboardLayout pageTitle="Departments" pageSubtitle="Manage university departments">
            <div className="action-bar">
                <div className="search-bar">
                    <div className="search-input-wrapper">
                        <HiOutlineSearch className="search-icon" />
                        <input
                            className="search-input"
                            placeholder="Search departments..."
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                        />
                    </div>
                </div>
                <button className="btn btn-primary" onClick={openCreate}>
                    <HiOutlinePlus /> Add Department
                </button>
            </div>

            <div className="content-card">
                <div className="table-container">
                    <table className="table">
                        <thead>
                            <tr>
                                <th>Department</th>
                                <th>Code</th>
                                <th>Description</th>
                                <th>Status</th>
                                <th style={{ textAlign: 'right' }}>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                [...Array(3)].map((_, i) => (
                                    <tr key={i}>
                                        <td><div className="skeleton" style={{ width: 150, height: 16 }} /></td>
                                        <td><div className="skeleton" style={{ width: 60, height: 16 }} /></td>
                                        <td><div className="skeleton" style={{ width: 200, height: 16 }} /></td>
                                        <td><div className="skeleton" style={{ width: 60, height: 24, borderRadius: 12 }} /></td>
                                        <td><div className="skeleton" style={{ width: 80, height: 16 }} /></td>
                                    </tr>
                                ))
                            ) : filtered.length > 0 ? (
                                filtered.map((dept) => (
                                    <tr key={dept.departmentId || dept.id}>
                                        <td>
                                            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                                                <div className="stat-card-icon blue" style={{ width: 36, height: 36, borderRadius: 8, fontSize: 16 }}>
                                                    <HiOutlineOfficeBuilding />
                                                </div>
                                                <span style={{ fontWeight: 600 }}>{dept.name}</span>
                                            </div>
                                        </td>
                                        <td><span className="badge badge-primary">{dept.code || dept.departmentCode}</span></td>
                                        <td style={{ color: 'var(--secondary)', maxWidth: 250, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                            {dept.description || '—'}
                                        </td>
                                        <td>
                                            <span className={`badge ${dept.active !== false ? 'badge-success' : 'badge-danger'}`}>
                                                {dept.active !== false ? 'Active' : 'Inactive'}
                                            </span>
                                        </td>
                                        <td style={{ textAlign: 'right' }}>
                                            <div style={{ display: 'flex', gap: 6, justifyContent: 'flex-end' }}>
                                                <button className="btn btn-ghost btn-sm" onClick={() => openEdit(dept)}>
                                                    <HiOutlinePencil />
                                                </button>
                                                <button className="btn btn-ghost btn-sm" style={{ color: 'var(--danger)' }} onClick={() => handleDeactivate(dept)}>
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
                                            <HiOutlineOfficeBuilding className="empty-state-icon" />
                                            <div className="empty-state-title">No departments found</div>
                                            <div className="empty-state-text">Create your first department to get started.</div>
                                        </div>
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Create/Edit Modal */}
            {showModal && (
                <div className="modal-overlay" onClick={() => setShowModal(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>{editingDept ? 'Edit Department' : 'Create Department'}</h2>
                            <button className="modal-close" onClick={() => setShowModal(false)}>
                                <HiOutlineX />
                            </button>
                        </div>
                        <form onSubmit={handleSave}>
                            <div className="modal-body">
                                {error && <div className="auth-alert error" style={{ marginBottom: 16 }}>{error}</div>}
                                <div className="form-group">
                                    <label className="form-label">Department Name</label>
                                    <input
                                        className="form-input"
                                        placeholder="e.g. Computer Science & Engineering"
                                        value={form.name}
                                        onChange={(e) => setForm({ ...form, name: e.target.value })}
                                        required
                                    />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Department Code</label>
                                    <input
                                        className="form-input"
                                        placeholder="e.g. CSE"
                                        value={form.code}
                                        onChange={(e) => setForm({ ...form, code: e.target.value.toUpperCase() })}
                                        required
                                    />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Description</label>
                                    <textarea
                                        className="form-input"
                                        placeholder="Brief description of the department"
                                        value={form.description}
                                        onChange={(e) => setForm({ ...form, description: e.target.value })}
                                        rows={3}
                                        style={{ resize: 'vertical' }}
                                    />
                                </div>
                            </div>
                            <div className="modal-footer">
                                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>
                                    Cancel
                                </button>
                                <button type="submit" className="btn btn-primary" disabled={saving}>
                                    {saving ? <><span className="spinner" /> Saving...</> : (editingDept ? 'Update' : 'Create')}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </DashboardLayout>
    );
}
