'use client';

import { useState, useEffect } from 'react';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { useAuth } from '@/lib/auth';
import { equipmentAPI, equipmentCategoryAPI } from '@/lib/api';
import {
    HiOutlineDesktopComputer,
    HiOutlinePlus,
    HiOutlinePencil,
    HiOutlineTrash,
    HiOutlineRefresh,
    HiOutlineSearch,
    HiOutlineXCircle,
    HiOutlineSwitchHorizontal,
} from 'react-icons/hi';

const STATUS_COLORS = {
    AVAILABLE:   { badge: 'badge-success', label: 'Available'   },
    INUSE:       { badge: 'badge-warning', label: 'In Use'      },
    MAINTENANCE: { badge: 'badge-danger',  label: 'Maintenance' },
    DAMAGED:     { badge: 'badge-danger',  label: 'Damaged'     },
    ARCHIVED:    { badge: 'badge-muted',   label: 'Archived'    },
};

const generateId = () => {
    if (typeof crypto !== 'undefined' && crypto.randomUUID) return crypto.randomUUID();
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
        const r = (Math.random() * 16) | 0;
        return (c === 'x' ? r : (r & 0x3) | 0x8).toString(16);
    });
};

const EMPTY_FORM = {
    equipmentId: '', name: '', description: '', serialNumber: '',
    categoryId: '', type: 'BORROWABLE', totalQuantity: 1, currentLocation: '',
};

const EMPTY_STATUS_FORM = { status: 'MAINTENANCE', reason: '' };

export default function TOEquipmentPage() {
    const { user } = useAuth();  // ← departmentId lives here

    const [equipment, setEquipment]   = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading]       = useState(true);
    const [saving, setSaving]         = useState(false);
    const [search, setSearch]         = useState('');
    const [filterStatus, setFilter]   = useState('ALL');
    const [error, setError]           = useState(null);
    const [success, setSuccess]       = useState(null);

    // Modals
    const [showModal, setShowModal]        = useState(false);
    const [editTarget, setEditTarget]      = useState(null);
    const [form, setForm]                  = useState(EMPTY_FORM);
    const [formErrors, setFormErrors]      = useState({});
    const [confirmId, setConfirmId]        = useState(null);
    const [showStatusModal, setShowStatus] = useState(false);
    const [statusTarget, setStatusTarget]  = useState(null);
    const [statusForm, setStatusForm]      = useState(EMPTY_STATUS_FORM);
    const [statusSaving, setStatusSaving]  = useState(false);

    useEffect(() => { initPage(); }, []);

    const initPage = async () => {
        setLoading(true); setError(null);
        try {
            const [eqRes, catRes] = await Promise.allSettled([
                equipmentAPI.getAllEquipment(),
                equipmentCategoryAPI.getAll(),
            ]);
            const eqRaw  = eqRes.status  === 'fulfilled'
                ? (eqRes.value.data?.data?.equipment || eqRes.value.data?.data || eqRes.value.data || [])
                : [];
            const catRaw = catRes.status === 'fulfilled'
                ? (catRes.value.data?.data || catRes.value.data || [])
                : [];
            setEquipment(Array.isArray(eqRaw)  ? eqRaw  : []);
            setCategories(Array.isArray(catRaw) ? catRaw : []);
        } catch {
            setError('Failed to load equipment. Is the backend running?');
        } finally { setLoading(false); }
    };

    const flash = (msg, isErr = false) => {
        isErr ? setError(msg) : setSuccess(msg);
        setTimeout(() => isErr ? setError(null) : setSuccess(null), 5000);
    };

    // ── Equipment CRUD ────────────────────────────────────────────────────
    const openAdd = () => {
        setEditTarget(null);
        setForm({ ...EMPTY_FORM, equipmentId: generateId() });
        setFormErrors({}); setShowModal(true);
    };

    const openEdit = (item) => {
        setEditTarget(item);
        setForm({
            equipmentId:     item.equipmentId || item.id || '',
            name:            item.name            || '',
            description:     item.description     || '',
            serialNumber:    item.serialNumber    || '',
            categoryId:      item.categoryId      || '',
            type:            item.type            || 'BORROWABLE',
            totalQuantity:   item.totalQuantity   ?? 1,
            currentLocation: item.currentLocation || '',
        });
        setFormErrors({}); setShowModal(true);
    };

    const validate = () => {
        const errs = {};
        if (!form.name.trim())            errs.name            = 'Name is required';
        if (!form.serialNumber.trim())    errs.serialNumber    = 'Serial number is required';
        if (!form.categoryId)             errs.categoryId      = 'Category is required';
        if (!form.currentLocation.trim()) errs.currentLocation = 'Location is required';
        if (form.totalQuantity < 1)       errs.totalQuantity   = 'Quantity must be ≥ 1';
        setFormErrors(errs);
        return Object.keys(errs).length === 0;
    };

    const handleSave = async () => {
        if (!validate()) return;

        // ── Guard: TO must have a departmentId in their JWT ──
        const deptId = user?.departmentId;
        if (!editTarget && !deptId) {
            flash('Your account has no department assigned. Contact your administrator.', true);
            return;
        }

        setSaving(true);
        try {
            if (editTarget) {
                await equipmentAPI.updateEquipment(editTarget.equipmentId || editTarget.id, {
                    name:            form.name,
                    description:     form.description,
                    serialNumber:    form.serialNumber,
                    categoryId:      parseInt(form.categoryId),
                    type:            form.type,
                    totalQuantity:   form.totalQuantity,
                    currentLocation: form.currentLocation,
                });
                flash('Equipment updated successfully');
            } else {
                // departmentId is injected from the authenticated user's JWT profile
                await equipmentAPI.createEquipment({
                    equipmentId:     form.equipmentId,
                    name:            form.name.trim(),
                    description:     form.description.trim() || null,
                    serialNumber:    form.serialNumber.trim(),
                    categoryId:      parseInt(form.categoryId),
                    type:            form.type,
                    departmentId:    deptId,          // ← THE FIX
                    totalQuantity:   form.totalQuantity,
                    currentLocation: form.currentLocation.trim(),
                });
                flash('Equipment added successfully!');
            }
            setShowModal(false); initPage();
        } catch (e) {
            const msg = e.response?.data?.message ||
                (e.response?.data?.errors && Object.values(e.response.data.errors).join(', ')) ||
                'Save failed. Check all fields and try again.';
            flash(msg, true);
        } finally { setSaving(false); }
    };

    const handleRetire = async (id) => {
        try {
            await equipmentAPI.updateEquipmentStatus(id, { status: 'ARCHIVED', reason: 'Retired by Technical Officer' });
            flash('Equipment retired (archived)'); setConfirmId(null); initPage();
        } catch (e) {
            flash(e.response?.data?.message || 'Failed to retire equipment', true); setConfirmId(null);
        }
    };

    // ── Status change modal ───────────────────────────────────────────────
    const openStatus = (item) => {
        setStatusTarget(item);
        setStatusForm({ status: 'MAINTENANCE', reason: '' });
        setShowStatus(true);
    };

    const handleStatusSave = async () => {
        if (!statusForm.reason.trim() && ['MAINTENANCE', 'DAMAGED'].includes(statusForm.status)) {
            flash('Reason is mandatory for MAINTENANCE and DAMAGED transitions.', true);
            return;
        }
        setStatusSaving(true);
        try {
            const id = statusTarget.equipmentId || statusTarget.id;
            await equipmentAPI.updateEquipmentStatus(id, {
                status: statusForm.status,
                reason: statusForm.reason || undefined,
            });
            flash(`Status updated to ${statusForm.status}`);
            setShowStatus(false); initPage();
        } catch (e) {
            flash(e.response?.data?.message || 'Failed to update status', true);
        } finally { setStatusSaving(false); }
    };

    const filtered = equipment.filter((e) => {
        const matchSearch = !search ||
            e.name?.toLowerCase().includes(search.toLowerCase()) ||
            e.serialNumber?.toLowerCase().includes(search.toLowerCase()) ||
            e.categoryName?.toLowerCase().includes(search.toLowerCase());
        const matchStatus = filterStatus === 'ALL' || e.status === filterStatus;
        return matchSearch && matchStatus;
    });

    return (
        <DashboardLayout pageTitle="Equipment" pageSubtitle="Register, update, and manage department equipment">

            {error   && <div className="alert alert-danger"  style={{ marginBottom: 16 }}>{error}</div>}
            {success && <div className="alert alert-success" style={{ marginBottom: 16 }}>{success}</div>}

            {/* Dept info banner */}
            {user?.departmentId && (
                <div style={{ marginBottom: 16, fontSize: 13, color: 'var(--secondary)' }}>
                    Department ID: <strong style={{ fontFamily: 'monospace' }}>{user.departmentId}</strong>
                </div>
            )}

            {/* Toolbar */}
            <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', marginBottom: 20, alignItems: 'center' }}>
                <div style={{ position: 'relative', flex: 1, minWidth: 200 }}>
                    <HiOutlineSearch style={{ position: 'absolute', left: 10, top: '50%', transform: 'translateY(-50%)', color: 'var(--secondary)' }} />
                    <input className="form-input" style={{ paddingLeft: 32 }}
                        placeholder="Search by name, serial, category…"
                        value={search} onChange={(e) => setSearch(e.target.value)} />
                </div>
                <select className="form-input" style={{ width: 160 }} value={filterStatus} onChange={(e) => setFilter(e.target.value)}>
                    <option value="ALL">All Statuses</option>
                    <option value="AVAILABLE">Available</option>
                    <option value="INUSE">In Use</option>
                    <option value="MAINTENANCE">Maintenance</option>
                    <option value="DAMAGED">Damaged</option>
                    <option value="ARCHIVED">Archived</option>
                </select>
                <button className="btn btn-outline btn-sm" onClick={initPage} style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <HiOutlineRefresh /> Refresh
                </button>
                <button
                    className="btn btn-primary"
                    onClick={openAdd}
                    disabled={!user?.departmentId}
                    title={!user?.departmentId ? 'No department assigned to your account' : ''}
                    style={{ display: 'flex', alignItems: 'center', gap: 6 }}
                >
                    <HiOutlinePlus /> Add Equipment
                </button>
            </div>

            {/* Status summary pills */}
            <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 20 }}>
                {['AVAILABLE', 'INUSE', 'MAINTENANCE', 'DAMAGED', 'ARCHIVED'].map((s) => {
                    const count = equipment.filter((e) => e.status === s).length;
                    return (
                        <div key={s} style={{ padding: '8px 16px', borderRadius: 'var(--radius-sm)', background: 'var(--bg-light)', border: '1px solid var(--border)', fontSize: 13 }}>
                            <span style={{ color: 'var(--secondary)', marginRight: 6 }}>{STATUS_COLORS[s]?.label || s}:</span>
                            <strong>{count}</strong>
                        </div>
                    );
                })}
            </div>

            {/* Equipment Table */}
            <div className="content-card">
                <div className="content-card-header">
                    <h2 className="content-card-title">Equipment List</h2>
                    <span style={{ fontSize: 13, color: 'var(--secondary)' }}>{!loading && `${filtered.length} items`}</span>
                </div>
                <div className="table-container">
                    <table className="table">
                        <thead><tr>
                            <th>Name</th><th>Serial No.</th><th>Category</th>
                            <th>Qty</th><th>Location</th><th>Status</th><th>Actions</th>
                        </tr></thead>
                        <tbody>
                            {loading ? (
                                [...Array(5)].map((_, i) => (
                                    <tr key={i}>{[180,120,100,40,100,80,120].map((w,j) => (
                                        <td key={j}><div className="skeleton" style={{ width: w, height: 16 }} /></td>
                                    ))}</tr>
                                ))
                            ) : filtered.length > 0 ? (
                                filtered.map((item) => {
                                    const id = item.equipmentId || item.id;
                                    const sc = STATUS_COLORS[item.status] || { badge: 'badge-muted', label: item.status };
                                    return (
                                        <tr key={id}>
                                            <td>
                                                <div style={{ fontWeight: 600 }}>{item.name}</div>
                                                {item.description && <div style={{ fontSize: 12, color: 'var(--secondary)', marginTop: 2 }}>{item.description}</div>}
                                            </td>
                                            <td style={{ fontSize: 13, fontFamily: 'monospace' }}>{item.serialNumber || '—'}</td>
                                            <td style={{ fontSize: 13 }}>{item.categoryName || item.categoryId || '—'}</td>
                                            <td style={{ fontWeight: 600 }}>{item.totalQuantity ?? 1}</td>
                                            <td style={{ fontSize: 13 }}>{item.currentLocation || '—'}</td>
                                            <td><span className={`badge ${sc.badge}`}>{sc.label}</span></td>
                                            <td>
                                                <div style={{ display: 'flex', gap: 6 }}>
                                                    <button className="btn btn-outline btn-sm" onClick={() => openEdit(item)} title="Edit details">
                                                        <HiOutlinePencil />
                                                    </button>
                                                    <button className="btn btn-outline btn-sm" onClick={() => openStatus(item)} title="Change status">
                                                        <HiOutlineSwitchHorizontal />
                                                    </button>
                                                    {item.status !== 'ARCHIVED' && (
                                                        <button className="btn btn-reject btn-sm" onClick={() => setConfirmId(id)} title="Retire">
                                                            <HiOutlineTrash />
                                                        </button>
                                                    )}
                                                </div>
                                            </td>
                                        </tr>
                                    );
                                })
                            ) : (
                                <tr><td colSpan={7} style={{ textAlign: 'center', padding: 48, color: 'var(--secondary)' }}>
                                    <HiOutlineDesktopComputer style={{ fontSize: 32, display: 'block', margin: '0 auto 8px' }} />
                                    {search || filterStatus !== 'ALL' ? 'No equipment matches your filters.' : 'No equipment yet. Click "Add Equipment" to get started.'}
                                </td></tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* ── Add / Edit Modal ── */}
            {showModal && (
                <div className="modal-overlay">
                    <div className="modal-content" style={{ padding: 28, maxWidth: 560 }}>
                        <h3 style={{ margin: '0 0 20px', fontSize: 18, fontWeight: 700 }}>
                            {editTarget ? 'Edit Equipment' : 'Add New Equipment'}
                        </h3>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 14 }}>
                            <div style={{ gridColumn: '1 / -1' }}>
                                <label className="form-label">Name *</label>
                                <input className={`form-input ${formErrors.name ? 'input-error' : ''}`}
                                    value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="e.g. Oscilloscope" />
                                {formErrors.name && <p className="form-error">{formErrors.name}</p>}
                            </div>
                            <div>
                                <label className="form-label">Serial Number *</label>
                                <input className={`form-input ${formErrors.serialNumber ? 'input-error' : ''}`}
                                    value={form.serialNumber} onChange={(e) => setForm({ ...form, serialNumber: e.target.value })} placeholder="SN-XXXX" />
                                {formErrors.serialNumber && <p className="form-error">{formErrors.serialNumber}</p>}
                            </div>
                            <div>
                                <label className="form-label">Category *</label>
                                <select className={`form-input ${formErrors.categoryId ? 'input-error' : ''}`}
                                    value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })}>
                                    <option value="">Select category…</option>
                                    {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                                </select>
                                {formErrors.categoryId && <p className="form-error">{formErrors.categoryId}</p>}
                            </div>
                            <div>
                                <label className="form-label">Type *</label>
                                <select className="form-input" value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
                                    <option value="BORROWABLE">Borrowable</option>
                                    <option value="LABDEDICATED">Lab Dedicated</option>
                                </select>
                            </div>
                            <div>
                                <label className="form-label">Quantity *</label>
                                <input type="number" min={1} className={`form-input ${formErrors.totalQuantity ? 'input-error' : ''}`}
                                    value={form.totalQuantity} onChange={(e) => setForm({ ...form, totalQuantity: parseInt(e.target.value) || 1 })} />
                                {formErrors.totalQuantity && <p className="form-error">{formErrors.totalQuantity}</p>}
                            </div>
                            <div>
                                <label className="form-label">Location *</label>
                                <input className={`form-input ${formErrors.currentLocation ? 'input-error' : ''}`}
                                    value={form.currentLocation} onChange={(e) => setForm({ ...form, currentLocation: e.target.value })} placeholder="e.g. Lab 3 / Room 201" />
                                {formErrors.currentLocation && <p className="form-error">{formErrors.currentLocation}</p>}
                            </div>
                            <div style={{ gridColumn: '1 / -1' }}>
                                <label className="form-label">Description</label>
                                <textarea className="form-input" rows={3}
                                    value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} placeholder="Optional description…" />
                            </div>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10, marginTop: 20 }}>
                            <button className="btn btn-outline" onClick={() => setShowModal(false)} disabled={saving}>Cancel</button>
                            <button className="btn btn-primary" onClick={handleSave} disabled={saving}>
                                {saving ? 'Saving…' : editTarget ? 'Save Changes' : 'Add Equipment'}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* ── Status Change Modal ── */}
            {showStatusModal && statusTarget && (
                <div className="modal-overlay">
                    <div className="modal-content" style={{ padding: 28, maxWidth: 440 }}>
                        <h3 style={{ margin: '0 0 6px', fontWeight: 700, fontSize: 18 }}>Update Equipment Status</h3>
                        <p style={{ margin: '0 0 20px', color: 'var(--secondary)', fontSize: 13 }}>{statusTarget.name}</p>
                        <div style={{ marginBottom: 14 }}>
                            <label className="form-label">New Status *</label>
                            <select className="form-input" value={statusForm.status} onChange={(e) => setStatusForm({ ...statusForm, status: e.target.value })}>
                                <option value="AVAILABLE">Available</option>
                                <option value="MAINTENANCE">Maintenance</option>
                                <option value="DAMAGED">Damaged</option>
                                <option value="ARCHIVED">Archived</option>
                            </select>
                        </div>
                        <div style={{ marginBottom: 20 }}>
                            <label className="form-label">
                                Reason {['MAINTENANCE','DAMAGED'].includes(statusForm.status) ? '*' : '(optional)'}
                            </label>
                            <textarea className="form-input" rows={3}
                                value={statusForm.reason} onChange={(e) => setStatusForm({ ...statusForm, reason: e.target.value })}
                                placeholder="Describe the reason for this status change…" />
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10 }}>
                            <button className="btn btn-outline" onClick={() => setShowStatus(false)} disabled={statusSaving}>Cancel</button>
                            <button className="btn btn-primary" onClick={handleStatusSave} disabled={statusSaving}>
                                {statusSaving ? 'Updating…' : 'Update Status'}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* ── Confirm Retire Modal ── */}
            {confirmId && (
                <div className="modal-overlay">
                    <div className="modal-content" style={{ padding: 28, maxWidth: 400, textAlign: 'center' }}>
                        <HiOutlineXCircle style={{ fontSize: 40, color: 'var(--primary)', marginBottom: 12 }} />
                        <h3 style={{ margin: '0 0 8px', fontWeight: 700 }}>Retire Equipment?</h3>
                        <p style={{ color: 'var(--secondary)', fontSize: 14, marginBottom: 20 }}>
                            This will archive the item. It cannot be borrowed once retired.
                        </p>
                        <div style={{ display: 'flex', gap: 10, justifyContent: 'center' }}>
                            <button className="btn btn-outline" onClick={() => setConfirmId(null)}>Cancel</button>
                            <button className="btn btn-primary" onClick={() => handleRetire(confirmId)}>Retire</button>
                        </div>
                    </div>
                </div>
            )}
        </DashboardLayout>
    );
}
