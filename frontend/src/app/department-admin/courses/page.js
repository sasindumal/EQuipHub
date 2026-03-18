'use client';

import { useState, useEffect } from 'react';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { useAuth } from '@/lib/auth';
import { courseAPI } from '@/lib/api';
import {
    HiOutlineSearch, HiOutlinePlus, HiOutlineX,
    HiOutlinePencil, HiOutlineTrash, HiOutlineRefresh,
    HiOutlineBookOpen, HiOutlineExclamationCircle,
} from 'react-icons/hi';

const EMPTY_FORM = {
    courseId: '', courseCode: '', courseName: '',
    semesterOffered: 1, credits: 3.0, labRequired: false,
};

const SEM_OPTIONS = [1, 2, 3, 4, 5, 6, 7, 8];

export default function DeptCoursesPage() {
    const { user } = useAuth();

    const [courses, setCourses]   = useState([]);
    const [loading, setLoading]   = useState(true);
    const [saving, setSaving]     = useState(false);
    const [search, setSearch]     = useState('');
    const [error, setError]       = useState('');
    const [success, setSuccess]   = useState('');
    const [modal, setModal]       = useState(null); // 'create' | 'edit' | 'delete'
    const [target, setTarget]     = useState(null);
    const [form, setForm]         = useState(EMPTY_FORM);

    const departmentId = user?.departmentId;

    useEffect(() => { load(); }, [departmentId]);

    const load = async () => {
        setLoading(true);
        try {
            const res  = departmentId
                ? await courseAPI.getCoursesByDepartment(departmentId)
                : await courseAPI.getAllCourses();
            const data = res.data?.data || res.data || [];
            setCourses(Array.isArray(data) ? data : []);
        } catch (err) {
            flash(err.response?.data?.message || 'Failed to load courses', true);
        } finally {
            setLoading(false);
        }
    };

    const flash = (msg, isErr = false) => {
        isErr ? setError(msg) : setSuccess(msg);
        setTimeout(() => isErr ? setError('') : setSuccess(''), 5000);
    };

    const closeModal = () => { setModal(null); setTarget(null); setError(''); };

    // ── CREATE ──
    const openCreate = () => {
        setForm({ ...EMPTY_FORM });
        setError('');
        setModal('create');
    };

    const handleCreate = async (e) => {
        e.preventDefault();
        if (!departmentId) { setError('No department assigned to your account'); return; }
        setSaving(true); setError('');
        try {
            await courseAPI.createCourse({
                ...form,
                departmentId,
                semesterOffered: Number(form.semesterOffered),
                credits: Number(form.credits),
            });
            flash('Course created successfully');
            closeModal(); load();
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to create course');
        } finally { setSaving(false); }
    };

    // ── EDIT ──
    const openEdit = (c) => {
        setTarget(c);
        setForm({
            courseId:       c.courseId,
            courseCode:     c.courseCode,
            courseName:     c.courseName,
            semesterOffered: c.semesterOffered,
            credits:        c.credits,
            labRequired:    c.labRequired || false,
        });
        setError('');
        setModal('edit');
    };

    const handleEdit = async (e) => {
        e.preventDefault(); setSaving(true); setError('');
        try {
            await courseAPI.updateCourse(target.courseId, {
                courseCode:      form.courseCode,
                courseName:      form.courseName,
                semesterOffered: Number(form.semesterOffered),
                credits:         Number(form.credits),
                labRequired:     form.labRequired,
            });
            flash('Course updated successfully');
            closeModal(); load();
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to update course');
        } finally { setSaving(false); }
    };

    // ── DELETE ──
    const openDelete = (c) => { setTarget(c); setModal('delete'); };

    const handleDelete = async () => {
        setSaving(true);
        try {
            await courseAPI.deleteCourse(target.courseId);
            flash('Course deleted');
            closeModal(); load();
        } catch (err) {
            flash(err.response?.data?.message || 'Failed to delete course', true);
            closeModal();
        } finally { setSaving(false); }
    };

    const filtered = courses.filter(c =>
        `${c.courseId} ${c.courseCode} ${c.courseName}`.toLowerCase()
            .includes(search.toLowerCase())
    );

    const labBadge = (lab) => lab
        ? <span className="badge badge-info">Lab</span>
        : <span className="badge badge-muted">No Lab</span>;

    const semBadge = (sem) => (
        <span className="badge badge-warning">Sem {sem}</span>
    );

    return (
        <DashboardLayout pageTitle="Courses" pageSubtitle="Manage department courses">

            {error   && <div className="alert alert-danger"  style={{ marginBottom: 16 }}>{error}</div>}
            {success && <div className="alert alert-success" style={{ marginBottom: 16 }}>{success}</div>}

            {/* Toolbar */}
            <div className="action-bar">
                <div className="search-bar">
                    <div className="search-input-wrapper">
                        <HiOutlineSearch className="search-icon" />
                        <input className="search-input" placeholder="Search courses…"
                            value={search} onChange={e => setSearch(e.target.value)} />
                    </div>
                </div>
                <div style={{ display: 'flex', gap: 8 }}>
                    <button className="btn btn-outline btn-sm" onClick={load}
                        style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                        <HiOutlineRefresh /> Refresh
                    </button>
                    <button className="btn btn-primary" onClick={openCreate}
                        disabled={!departmentId}
                        title={!departmentId ? 'No department assigned' : ''}>
                        <HiOutlinePlus /> Add Course
                    </button>
                </div>
            </div>

            {/* Stats strip */}
            <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 20 }}>
                {[1, 2, 3, 4, 5, 6, 7, 8].map(sem => {
                    const count = courses.filter(c => c.semesterOffered === sem).length;
                    if (count === 0) return null;
                    return (
                        <div key={sem} style={{ padding: '6px 14px', borderRadius: 'var(--radius-sm)', background: 'var(--bg-light)', border: '1px solid var(--border)', fontSize: 13 }}>
                            <span style={{ color: 'var(--secondary)', marginRight: 4 }}>Sem {sem}:</span>
                            <strong>{count}</strong>
                        </div>
                    );
                })}
                <div style={{ padding: '6px 14px', borderRadius: 'var(--radius-sm)', background: 'var(--bg-light)', border: '1px solid var(--border)', fontSize: 13 }}>
                    <span style={{ color: 'var(--secondary)', marginRight: 4 }}>Total:</span>
                    <strong>{courses.length}</strong>
                </div>
            </div>

            {/* Table */}
            <div className="content-card">
                <div className="table-container">
                    <table className="table">
                        <thead><tr>
                            <th>Course</th>
                            <th className="hide-mobile">Code</th>
                            <th>Semester</th>
                            <th className="hide-mobile">Credits</th>
                            <th>Lab</th>
                            <th style={{ textAlign: 'right' }}>Actions</th>
                        </tr></thead>
                        <tbody>
                            {loading ? (
                                [...Array(4)].map((_, i) => (
                                    <tr key={i}>
                                        {[200, 80, 60, 50, 60, 80].map((w, j) => (
                                            <td key={j} className={j === 1 || j === 3 ? 'hide-mobile' : ''}>
                                                <div className="skeleton" style={{ width: w, height: 16 }} />
                                            </td>
                                        ))}
                                    </tr>
                                ))
                            ) : filtered.length > 0 ? (
                                filtered.map(c => (
                                    <tr key={c.courseId}>
                                        <td>
                                            <div style={{ fontWeight: 600 }}>{c.courseName}</div>
                                            <div style={{ fontSize: 12, color: 'var(--secondary)' }}>{c.courseId}</div>
                                        </td>
                                        <td className="hide-mobile">
                                            <code style={{ fontSize: 12, background: 'var(--bg-light)', padding: '2px 6px', borderRadius: 4 }}>
                                                {c.courseCode}
                                            </code>
                                        </td>
                                        <td>{semBadge(c.semesterOffered)}</td>
                                        <td className="hide-mobile" style={{ color: 'var(--secondary)', fontSize: 13 }}>{c.credits}</td>
                                        <td>{labBadge(c.labRequired)}</td>
                                        <td style={{ textAlign: 'right' }}>
                                            <div style={{ display: 'flex', gap: 4, justifyContent: 'flex-end' }}>
                                                <button className="btn btn-ghost btn-sm" title="Edit" onClick={() => openEdit(c)}>
                                                    <HiOutlinePencil />
                                                </button>
                                                <button className="btn btn-ghost btn-sm" title="Delete"
                                                    style={{ color: 'var(--danger)' }} onClick={() => openDelete(c)}>
                                                    <HiOutlineTrash />
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr><td colSpan={6}>
                                    <div className="empty-state">
                                        <HiOutlineBookOpen className="empty-state-icon" />
                                        <div className="empty-state-title">No courses found</div>
                                        <div className="empty-state-text">Add courses offered by your department.</div>
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
                    <div className="modal-content" style={{ maxWidth: 520 }} onClick={e => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>Add Course</h2>
                            <button className="modal-close" onClick={closeModal}><HiOutlineX /></button>
                        </div>
                        <form onSubmit={handleCreate}>
                            <div className="modal-body">
                                {error && <div className="alert alert-danger" style={{ marginBottom: 12 }}>{error}</div>}
                                <div className="form-row">
                                    <div className="form-group">
                                        <label className="form-label">Course ID *</label>
                                        <input className="form-input" placeholder="e.g. CE2030"
                                            value={form.courseId} maxLength={20} required
                                            onChange={e => setForm({ ...form, courseId: e.target.value.toUpperCase() })} />
                                        <small style={{ color: 'var(--secondary)', fontSize: 11 }}>Unique identifier, cannot be changed later</small>
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Course Code *</label>
                                        <input className="form-input" placeholder="e.g. CE2030"
                                            value={form.courseCode} maxLength={20} required
                                            onChange={e => setForm({ ...form, courseCode: e.target.value.toUpperCase() })} />
                                    </div>
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Course Name *</label>
                                    <input className="form-input" placeholder="e.g. Digital Electronics"
                                        value={form.courseName} maxLength={255} required
                                        onChange={e => setForm({ ...form, courseName: e.target.value })} />
                                </div>
                                <div className="form-row">
                                    <div className="form-group">
                                        <label className="form-label">Semester Offered *</label>
                                        <select className="form-select" value={form.semesterOffered} required
                                            onChange={e => setForm({ ...form, semesterOffered: e.target.value })}>
                                            {SEM_OPTIONS.map(s => <option key={s} value={s}>Semester {s}</option>)}
                                        </select>
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Credits *</label>
                                        <input type="number" className="form-input" min={0.5} max={6} step={0.5}
                                            value={form.credits} required
                                            onChange={e => setForm({ ...form, credits: e.target.value })} />
                                    </div>
                                </div>
                                <div className="form-group">
                                    <label style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer', fontSize: 'var(--font-size-sm)' }}>
                                        <input type="checkbox" checked={form.labRequired}
                                            onChange={e => setForm({ ...form, labRequired: e.target.checked })} />
                                        This course requires lab equipment
                                    </label>
                                </div>
                            </div>
                            <div className="modal-footer">
                                <button type="button" className="btn btn-secondary" onClick={closeModal}>Cancel</button>
                                <button type="submit" className="btn btn-primary" disabled={saving}>
                                    {saving ? 'Creating…' : 'Create Course'}
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
                            <h2>Edit — {target.courseName}</h2>
                            <button className="modal-close" onClick={closeModal}><HiOutlineX /></button>
                        </div>
                        <form onSubmit={handleEdit}>
                            <div className="modal-body">
                                {error && <div className="alert alert-danger" style={{ marginBottom: 12 }}>{error}</div>}
                                <div className="form-group">
                                    <label className="form-label">Course ID</label>
                                    <input className="form-input" value={target.courseId} disabled
                                        style={{ opacity: 0.6, cursor: 'not-allowed' }} />
                                    <small style={{ color: 'var(--secondary)', fontSize: 11 }}>Course ID cannot be changed</small>
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Course Code *</label>
                                    <input className="form-input" value={form.courseCode} maxLength={20} required
                                        onChange={e => setForm({ ...form, courseCode: e.target.value.toUpperCase() })} />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Course Name *</label>
                                    <input className="form-input" value={form.courseName} maxLength={255} required
                                        onChange={e => setForm({ ...form, courseName: e.target.value })} />
                                </div>
                                <div className="form-row">
                                    <div className="form-group">
                                        <label className="form-label">Semester Offered *</label>
                                        <select className="form-select" value={form.semesterOffered} required
                                            onChange={e => setForm({ ...form, semesterOffered: e.target.value })}>
                                            {SEM_OPTIONS.map(s => <option key={s} value={s}>Semester {s}</option>)}
                                        </select>
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">Credits *</label>
                                        <input type="number" className="form-input" min={0.5} max={6} step={0.5}
                                            value={form.credits} required
                                            onChange={e => setForm({ ...form, credits: e.target.value })} />
                                    </div>
                                </div>
                                <div className="form-group">
                                    <label style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer', fontSize: 'var(--font-size-sm)' }}>
                                        <input type="checkbox" checked={form.labRequired}
                                            onChange={e => setForm({ ...form, labRequired: e.target.checked })} />
                                        This course requires lab equipment
                                    </label>
                                </div>
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

            {/* ── DELETE CONFIRM ── */}
            {modal === 'delete' && target && (
                <div className="modal-overlay" onClick={closeModal}>
                    <div className="modal-content" style={{ maxWidth: 400, textAlign: 'center', padding: 28 }}
                        onClick={e => e.stopPropagation()}>
                        <HiOutlineExclamationCircle style={{ fontSize: 44, color: 'var(--danger)', marginBottom: 12 }} />
                        <h3 style={{ margin: '0 0 8px', fontWeight: 700 }}>Delete Course?</h3>
                        <p style={{ fontSize: 14, color: 'var(--secondary)', marginBottom: 24 }}>
                            This will permanently delete <strong>{target.courseName}</strong> ({target.courseCode}).
                            Existing requests referencing this course are not affected.
                        </p>
                        <div style={{ display: 'flex', gap: 10, justifyContent: 'center' }}>
                            <button className="btn btn-secondary" onClick={closeModal}>Cancel</button>
                            <button className="btn btn-primary" style={{ background: 'var(--danger)' }}
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
