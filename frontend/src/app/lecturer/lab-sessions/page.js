'use client';

import { useState, useEffect, useCallback } from 'react';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { useAuth } from '@/lib/auth';
import { requestAPI, equipmentAPI, userAPI } from '@/lib/api';
import {
    HiOutlinePlus, HiOutlineX, HiOutlineRefresh,
    HiOutlineSearch, HiOutlineBeaker, HiOutlineTrash,
    HiOutlineCheckCircle, HiOutlineExclamationCircle,
    HiOutlineClock, HiOutlineCalendar,
} from 'react-icons/hi';

// ── Status badge colours ────────────────────────────────────────────────────
const STATUS_BADGE = {
    DRAFT:                 'badge-muted',
    PENDINGRECOMMENDATION: 'badge-warning',
    PENDINGAPPROVAL:       'badge-warning',
    APPROVED:              'badge-success',
    REJECTED:              'badge-danger',
    CANCELLED:             'badge-muted',
    INUSE:                 'badge-info',
    COMPLETED:             'badge-success',
};

const STATUS_LABEL = {
    DRAFT:                 'Draft',
    PENDINGRECOMMENDATION: 'Pending TO Check',
    PENDINGAPPROVAL:       'Pending Approval',
    APPROVED:              'Approved',
    REJECTED:              'Rejected',
    CANCELLED:             'Cancelled',
    INUSE:                 'In Use',
    COMPLETED:             'Completed',
};

const EMPTY_FORM = {
    studentId:   '',
    description: '',
    fromDateTime: '',
    toDateTime:  '',
    slaHours:    24,
    priorityLevel: 3,
    notes:       '',
};

export default function LabSessionsPage() {
    const { user } = useAuth();

    // list state
    const [requests, setRequests]   = useState([]);
    const [loading, setLoading]     = useState(true);
    const [search, setSearch]       = useState('');

    // modal state
    const [modal, setModal]         = useState(null); // 'create' | 'view' | 'cancel'
    const [target, setTarget]       = useState(null);

    // form state
    const [form, setForm]           = useState(EMPTY_FORM);
    const [items, setItems]         = useState([{ equipmentId: '', quantityRequested: 1, notes: '' }]);
    const [saving, setSaving]       = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [cancelling, setCancelling] = useState(false);

    // lookup data
    const [equipment, setEquipment] = useState([]);
    const [students, setStudents]   = useState([]);
    const [lookupLoading, setLookupLoading] = useState(false);

    // feedback
    const [error, setError]         = useState('');
    const [success, setSuccess]     = useState('');

    // ── Load all lab session requests created by this instructor ────────────
    const load = useCallback(async () => {
        setLoading(true);
        try {
            const res  = await requestAPI.getMyRequests(0, 100);
            const data = res.data?.content || res.data || [];
            const labReqs = (Array.isArray(data) ? data : []).filter(
                r => r.requestType === 'LABSESSION'
            );
            setRequests(labReqs);
        } catch {
            flash('Failed to load lab session requests', true);
        } finally {
            setLoading(false);
        }
    }, []);

    // ── Load equipment + students for the create form ───────────────────────
    const loadLookups = useCallback(async () => {
        if (equipment.length > 0 && students.length > 0) return; // already loaded
        setLookupLoading(true);
        try {
            const [eqRes, stuRes] = await Promise.all([
                user?.departmentId
                    ? equipmentAPI.getAvailableByDept(user.departmentId)
                    : equipmentAPI.getAllEquipment(),
                userAPI.getAllStudents(),
            ]);
            setEquipment(
                Array.isArray(eqRes.data?.data || eqRes.data)
                    ? (eqRes.data?.data || eqRes.data)
                    : []
            );
            const stuRaw = stuRes.data?.data || stuRes.data;
            setStudents(Array.isArray(stuRaw) ? stuRaw : []);
        } catch {
            flash('Failed to load equipment / student list', true);
        } finally {
            setLookupLoading(false);
        }
    }, [user?.departmentId, equipment.length, students.length]);

    useEffect(() => { load(); }, [load]);

    // ── Feedback helper ─────────────────────────────────────────────────────
    const flash = (msg, isErr = false) => {
        isErr ? setError(msg) : setSuccess(msg);
        setTimeout(() => isErr ? setError('') : setSuccess(''), 5000);
    };

    // ── Open create modal ───────────────────────────────────────────────────
    const openCreate = () => {
        setForm(EMPTY_FORM);
        setItems([{ equipmentId: '', quantityRequested: 1, notes: '' }]);
        setError('');
        loadLookups();
        setModal('create');
    };

    const closeModal = () => { setModal(null); setTarget(null); setError(''); };

    // ── Equipment line items ─────────────────────────────────────────────────
    const addItem = () =>
        setItems(prev => [...prev, { equipmentId: '', quantityRequested: 1, notes: '' }]);

    const removeItem = idx =>
        setItems(prev => prev.filter((_, i) => i !== idx));

    const updateItem = (idx, field, value) =>
        setItems(prev => prev.map((it, i) => i === idx ? { ...it, [field]: value } : it));

    // ── CREATE draft request ────────────────────────────────────────────────
    const handleCreate = async (e) => {
        e.preventDefault();
        if (!form.studentId) { setError('Please select a student'); return; }
        if (items.some(it => !it.equipmentId)) { setError('Please select equipment for all line items'); return; }
        if (!form.fromDateTime || !form.toDateTime) { setError('Please set start and end date/time'); return; }
        if (new Date(form.toDateTime) <= new Date(form.fromDateTime)) {
            setError('End date/time must be after start date/time'); return;
        }

        setSaving(true); setError('');
        try {
            const payload = {
                requestType:   'LABSESSION',
                studentId:     form.studentId,
                instructorId:  user.userId,
                departmentId:  user.departmentId,
                fromDateTime:  new Date(form.fromDateTime).toISOString(),
                toDateTime:    new Date(form.toDateTime).toISOString(),
                description:   form.description,
                priorityLevel: Number(form.priorityLevel),
                slaHours:      Number(form.slaHours),
                isEmergency:   false,
                items: items.map(it => ({
                    equipmentId:       it.equipmentId,
                    quantityRequested: Number(it.quantityRequested),
                    notes:             it.notes,
                })),
            };
            const res = await requestAPI.createRequest(payload);
            const created = res.data?.data || res.data;
            flash('Lab session request created as Draft');
            closeModal();
            load();

            // Auto-submit after creation
            if (created?.requestId) {
                setTimeout(async () => {
                    try {
                        await requestAPI.submitRequest(created.requestId);
                        flash('Request submitted for TO availability check');
                        load();
                    } catch (err) {
                        flash(err.response?.data?.message || 'Auto-submit failed — submit manually from the list', true);
                        load();
                    }
                }, 600);
            }
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to create request');
        } finally {
            setSaving(false);
        }
    };

    // ── SUBMIT a draft manually ─────────────────────────────────────────────
    const handleSubmit = async (req) => {
        setSubmitting(req.requestId);
        try {
            await requestAPI.submitRequest(req.requestId);
            flash('Request submitted for TO availability check');
            load();
        } catch (err) {
            flash(err.response?.data?.message || 'Submit failed', true);
        } finally { setSubmitting(null); }
    };

    // ── CANCEL ──────────────────────────────────────────────────────────────
    const handleCancel = async () => {
        setCancelling(true);
        try {
            await requestAPI.cancelRequest(target.requestId);
            flash('Request cancelled');
            closeModal(); load();
        } catch (err) {
            flash(err.response?.data?.message || 'Cancel failed', true);
            closeModal();
        } finally { setCancelling(false); }
    };

    // ── Filtering ────────────────────────────────────────────────────────────
    const filtered = requests.filter(r => {
        const q = search.toLowerCase();
        return (
            (r.requestId || '').toLowerCase().includes(q) ||
            (r.studentName || '').toLowerCase().includes(q) ||
            (r.description || '').toLowerCase().includes(q)
        );
    });

    const fmtDT = (iso) => iso ? new Date(iso).toLocaleString('en-GB', { dateStyle: 'medium', timeStyle: 'short' }) : '—';
    const fmtDate = (iso) => iso ? new Date(iso).toLocaleDateString('en-GB', { dateStyle: 'medium' }) : '—';

    const canCancelStatus = ['DRAFT', 'PENDINGRECOMMENDATION', 'PENDINGAPPROVAL', 'MODIFICATIONPROPOSED'];

    return (
        <DashboardLayout pageTitle="Lab Sessions" pageSubtitle="Manage lab session equipment requests">

            {error   && <div className="alert alert-danger"  style={{ marginBottom: 16 }}>{error}</div>}
            {success && <div className="alert alert-success" style={{ marginBottom: 16 }}>{success}</div>}

            {/* ── Toolbar ───────────────────────────────────────────── */}
            <div className="action-bar">
                <div className="search-bar">
                    <div className="search-input-wrapper">
                        <HiOutlineSearch className="search-icon" />
                        <input className="search-input" placeholder="Search by ID, student, description…"
                            value={search} onChange={e => setSearch(e.target.value)} />
                    </div>
                </div>
                <div style={{ display: 'flex', gap: 8 }}>
                    <button className="btn btn-outline btn-sm" onClick={load}
                        style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                        <HiOutlineRefresh /> Refresh
                    </button>
                    <button className="btn btn-primary" onClick={openCreate}
                        style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <HiOutlinePlus /> New Lab Session
                    </button>
                </div>
            </div>

            {/* ── Stats strip ───────────────────────────────────────── */}
            <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 20 }}>
                {['DRAFT','PENDINGRECOMMENDATION','APPROVED','COMPLETED'].map(s => {
                    const cnt = requests.filter(r => r.status === s).length;
                    if (cnt === 0) return null;
                    return (
                        <div key={s} style={{ padding: '6px 14px', borderRadius: 'var(--radius-sm)', background: 'var(--bg-light)', border: '1px solid var(--border)', fontSize: 13 }}>
                            <span style={{ color: 'var(--secondary)', marginRight: 4 }}>{STATUS_LABEL[s] || s}:</span>
                            <strong>{cnt}</strong>
                        </div>
                    );
                })}
                <div style={{ padding: '6px 14px', borderRadius: 'var(--radius-sm)', background: 'var(--bg-light)', border: '1px solid var(--border)', fontSize: 13 }}>
                    <span style={{ color: 'var(--secondary)', marginRight: 4 }}>Total:</span>
                    <strong>{requests.length}</strong>
                </div>
            </div>

            {/* ── Requests Table ────────────────────────────────────── */}
            <div className="content-card">
                <div className="table-container">
                    <table className="table">
                        <thead><tr>
                            <th>Request ID</th>
                            <th>Student</th>
                            <th className="hide-mobile">Session Window</th>
                            <th className="hide-mobile">Items</th>
                            <th>Status</th>
                            <th style={{ textAlign: 'right' }}>Actions</th>
                        </tr></thead>
                        <tbody>
                            {loading ? (
                                [...Array(4)].map((_, i) => (
                                    <tr key={i}>
                                        {[100, 140, 200, 50, 80, 80].map((w, j) => (
                                            <td key={j} className={j === 2 || j === 3 ? 'hide-mobile' : ''}>
                                                <div className="skeleton" style={{ width: w, height: 16 }} />
                                            </td>
                                        ))}
                                    </tr>
                                ))
                            ) : filtered.length > 0 ? (
                                filtered.map(req => (
                                    <tr key={req.requestId}>
                                        <td>
                                            <div style={{ fontWeight: 600, fontSize: 13 }}>{req.requestId}</div>
                                            <div style={{ fontSize: 11, color: 'var(--secondary)' }}>{fmtDate(req.submittedAt || req.createdAt)}</div>
                                        </td>
                                        <td style={{ fontSize: 13 }}>
                                            <div>{req.studentName || '—'}</div>
                                            {req.studentEmail && <div style={{ fontSize: 11, color: 'var(--secondary)' }}>{req.studentEmail}</div>}
                                        </td>
                                        <td className="hide-mobile" style={{ fontSize: 12, color: 'var(--secondary)' }}>
                                            <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                                                <HiOutlineCalendar />
                                                {fmtDT(req.fromDateTime)}
                                            </div>
                                            <div style={{ display: 'flex', alignItems: 'center', gap: 4, marginTop: 2 }}>
                                                <HiOutlineClock />
                                                {fmtDT(req.toDateTime)}
                                            </div>
                                        </td>
                                        <td className="hide-mobile" style={{ fontSize: 13 }}>
                                            {req.items?.length ?? '—'}
                                        </td>
                                        <td>
                                            <span className={`badge ${STATUS_BADGE[req.status] || 'badge-muted'}`}>
                                                {STATUS_LABEL[req.status] || req.status}
                                            </span>
                                        </td>
                                        <td style={{ textAlign: 'right' }}>
                                            <div style={{ display: 'flex', gap: 4, justifyContent: 'flex-end' }}>
                                                {/* Submit button — only for DRAFT */}
                                                {req.status === 'DRAFT' && (
                                                    <button
                                                        className="btn btn-primary btn-sm"
                                                        title="Submit for TO check"
                                                        disabled={submitting === req.requestId}
                                                        onClick={() => handleSubmit(req)}
                                                        style={{ fontSize: 12, display: 'flex', alignItems: 'center', gap: 4 }}
                                                    >
                                                        {submitting === req.requestId ? '…' : <><HiOutlineCheckCircle /> Submit</>}
                                                    </button>
                                                )}
                                                {/* Cancel button */}
                                                {canCancelStatus.includes(req.status) && (
                                                    <button
                                                        className="btn btn-ghost btn-sm"
                                                        title="Cancel"
                                                        style={{ color: 'var(--danger)' }}
                                                        onClick={() => { setTarget(req); setModal('cancel'); }}
                                                    >
                                                        <HiOutlineTrash />
                                                    </button>
                                                )}
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr><td colSpan={6}>
                                    <div className="empty-state">
                                        <HiOutlineBeaker className="empty-state-icon" />
                                        <div className="empty-state-title">No lab session requests</div>
                                        <div className="empty-state-text">Click "New Lab Session" to create your first request.</div>
                                    </div>
                                </td></tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* ════════════════════════════════════════════════════════
                CREATE MODAL
            ════════════════════════════════════════════════════════ */}
            {modal === 'create' && (
                <div className="modal-overlay" onClick={closeModal}>
                    <div className="modal-content"
                        style={{ maxWidth: 620, maxHeight: '90vh', overflowY: 'auto' }}
                        onClick={e => e.stopPropagation()}>

                        <div className="modal-header">
                            <h2>New Lab Session Request</h2>
                            <button className="modal-close" onClick={closeModal}><HiOutlineX /></button>
                        </div>

                        <form onSubmit={handleCreate}>
                            <div className="modal-body">
                                {error && <div className="alert alert-danger" style={{ marginBottom: 12 }}>{error}</div>}
                                {lookupLoading && (
                                    <div style={{ textAlign: 'center', padding: 12, color: 'var(--secondary)', fontSize: 13 }}>
                                        Loading equipment &amp; students…
                                    </div>
                                )}

                                {/* ── Student ── */}
                                <div className="form-group">
                                    <label className="form-label">Student *</label>
                                    <select className="form-select" required
                                        value={form.studentId}
                                        onChange={e => setForm({ ...form, studentId: e.target.value })}>
                                        <option value="">— Select student —</option>
                                        {students.map(s => (
                                            <option key={s.userId} value={s.userId}>
                                                {s.firstName} {s.lastName} {s.indexNumber ? `(${s.indexNumber})` : ''}
                                            </option>
                                        ))}
                                    </select>
                                </div>

                                {/* ── Session window ── */}
                                <div className="form-row">
                                    <div className="form-group">
                                        <label className="form-label">From *</label>
                                        <input type="datetime-local" className="form-input" required
                                            value={form.fromDateTime}
                                            min={new Date().toISOString().slice(0, 16)}
                                            onChange={e => setForm({ ...form, fromDateTime: e.target.value })} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">To *</label>
                                        <input type="datetime-local" className="form-input" required
                                            value={form.toDateTime}
                                            min={form.fromDateTime || new Date().toISOString().slice(0, 16)}
                                            onChange={e => setForm({ ...form, toDateTime: e.target.value })} />
                                    </div>
                                </div>

                                {/* ── Description ── */}
                                <div className="form-group">
                                    <label className="form-label">Session Description</label>
                                    <textarea className="form-input" rows={3}
                                        placeholder="Lab exercise, experiment purpose, module…"
                                        value={form.description}
                                        onChange={e => setForm({ ...form, description: e.target.value })} />
                                </div>

                                {/* ── Priority / SLA ── */}
                                <div className="form-row">
                                    <div className="form-group">
                                        <label className="form-label">Priority (1–5)</label>
                                        <input type="number" className="form-input" min={1} max={5}
                                            value={form.priorityLevel}
                                            onChange={e => setForm({ ...form, priorityLevel: e.target.value })} />
                                    </div>
                                    <div className="form-group">
                                        <label className="form-label">SLA Hours</label>
                                        <input type="number" className="form-input" min={1} max={168}
                                            value={form.slaHours}
                                            onChange={e => setForm({ ...form, slaHours: e.target.value })} />
                                    </div>
                                </div>

                                {/* ── Equipment line items ── */}
                                <div className="form-group">
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                                        <label className="form-label" style={{ margin: 0 }}>Equipment Items *</label>
                                        <button type="button" className="btn btn-outline btn-sm"
                                            onClick={addItem}
                                            style={{ display: 'flex', alignItems: 'center', gap: 4, fontSize: 12 }}>
                                            <HiOutlinePlus /> Add Item
                                        </button>
                                    </div>

                                    {items.map((it, idx) => (
                                        <div key={idx} style={{
                                            display: 'grid',
                                            gridTemplateColumns: '1fr 80px 32px',
                                            gap: 8, marginBottom: 8, alignItems: 'center',
                                        }}>
                                            <select className="form-select" required
                                                value={it.equipmentId}
                                                onChange={e => updateItem(idx, 'equipmentId', e.target.value)}>
                                                <option value="">— Select equipment —</option>
                                                {equipment.map(eq => (
                                                    <option key={eq.equipmentId} value={eq.equipmentId}>
                                                        {eq.name}{eq.totalQuantity ? ` (${eq.totalQuantity} total)` : ''}
                                                    </option>
                                                ))}
                                            </select>
                                            <input type="number" className="form-input" min={1} max={50}
                                                placeholder="Qty" value={it.quantityRequested}
                                                onChange={e => updateItem(idx, 'quantityRequested', e.target.value)} />
                                            {items.length > 1 && (
                                                <button type="button"
                                                    onClick={() => removeItem(idx)}
                                                    style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--danger)', fontSize: 18 }}>
                                                    <HiOutlineX />
                                                </button>
                                            )}
                                        </div>
                                    ))}
                                </div>
                            </div>

                            <div className="modal-footer">
                                <button type="button" className="btn btn-secondary" onClick={closeModal}>Cancel</button>
                                <button type="submit" className="btn btn-primary"
                                    disabled={saving || lookupLoading}
                                    style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    {saving ? 'Creating…' : <><HiOutlineBeaker /> Create &amp; Submit</>}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* ════════════════════════════════════════════════════════
                CANCEL CONFIRM
            ════════════════════════════════════════════════════════ */}
            {modal === 'cancel' && target && (
                <div className="modal-overlay" onClick={closeModal}>
                    <div className="modal-content"
                        style={{ maxWidth: 400, textAlign: 'center', padding: 28 }}
                        onClick={e => e.stopPropagation()}>
                        <HiOutlineExclamationCircle style={{ fontSize: 44, color: 'var(--danger)', marginBottom: 12 }} />
                        <h3 style={{ margin: '0 0 8px', fontWeight: 700 }}>Cancel Request?</h3>
                        <p style={{ fontSize: 14, color: 'var(--secondary)', marginBottom: 24 }}>
                            This will cancel <strong>{target.requestId}</strong> for student
                            {' '}<strong>{target.studentName || 'unknown'}</strong>.
                            This cannot be undone.
                        </p>
                        <div style={{ display: 'flex', gap: 10, justifyContent: 'center' }}>
                            <button className="btn btn-secondary" onClick={closeModal}>Keep It</button>
                            <button className="btn btn-primary"
                                style={{ background: 'var(--danger)' }}
                                onClick={handleCancel}
                                disabled={cancelling}>
                                {cancelling ? 'Cancelling…' : 'Yes, Cancel'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </DashboardLayout>
    );
}
