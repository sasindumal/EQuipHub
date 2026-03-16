'use client';

import { useState, useEffect } from 'react';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { penaltyAPI } from '@/lib/api';
import { useAuth } from '@/lib/auth';
import {
    HiOutlineExclamationCircle,
    HiOutlineSearch,
    HiOutlineRefresh,
    HiOutlineEye,
    HiOutlineCheckCircle,
    HiOutlineX,
} from 'react-icons/hi';

const TYPE_MAP = {
    LATE_RETURN:  { badge: 'badge-warning', label: 'Late Return'  },
    DAMAGE:       { badge: 'badge-danger',  label: 'Damage'       },
    LOSS:         { badge: 'badge-danger',  label: 'Loss'         },
    LAB_OVERRIDE: { badge: 'badge-warning', label: 'Lab Override' },
};

const STATUS_MAP = {
    PENDING:  { badge: 'badge-warning', label: 'Pending'  },
    APPROVED: { badge: 'badge-danger',  label: 'Active'   },
    WAIVED:   { badge: 'badge-success', label: 'Waived'   },
    APPEALED: { badge: 'badge-muted',   label: 'Appealed' },
};

export default function DeptPenaltiesPage() {
    const { user } = useAuth();
    const [penalties, setPenalties]   = useState([]);
    const [loading, setLoading]       = useState(true);
    const [error, setError]           = useState(null);
    const [success, setSuccess]       = useState(null);
    const [search, setSearch]         = useState('');
    const [filterStatus, setFilter]   = useState('ALL');
    const [viewPenalty, setView]      = useState(null);
    const [waiveModal, setWaiveModal] = useState(null);
    const [waiveReason, setWaiveReason] = useState('');
    const [actionId, setActionId]     = useState(null);

    useEffect(() => { load(); }, [user]);

    const load = async () => {
        if (!user?.departmentId) return;
        setLoading(true); setError(null);
        try {
            const res = await penaltyAPI.getDepartmentPenalties(user.departmentId);
            const data = res.data?.data || res.data || [];
            setPenalties(Array.isArray(data) ? data : []);
        } catch (e) {
            setError('Failed to load penalties.');
        } finally { setLoading(false); }
    };

    const flash = (msg, isError = false) => {
        isError ? setError(msg) : setSuccess(msg);
        setTimeout(() => isError ? setError(null) : setSuccess(null), 4000);
    };

    const handleApprove = async (id) => {
        setActionId(id);
        try {
            await penaltyAPI.approvePenalty(id);
            flash('Penalty approved and activated');
            load();
        } catch (e) {
            flash(e.response?.data?.message || 'Approval failed', true);
        } finally { setActionId(null); }
    };

    const handleWaive = async () => {
        if (!waiveModal || !waiveReason.trim()) return;
        setActionId(waiveModal);
        try {
            await penaltyAPI.waivePenalty(waiveModal, waiveReason);
            flash('Penalty waived successfully');
            setWaiveModal(null); setWaiveReason('');
            load();
        } catch (e) {
            flash(e.response?.data?.message || 'Waive failed', true);
        } finally { setActionId(null); }
    };

    const filtered = penalties.filter((p) => {
        const txt = `${p.studentName || ''} ${p.penaltyType || ''}`.toLowerCase();
        const matchSearch = !search || txt.includes(search.toLowerCase());
        const matchStatus = filterStatus === 'ALL' || p.status === filterStatus;
        return matchSearch && matchStatus;
    });

    const pending  = penalties.filter(p => p.status === 'PENDING').length;
    const active   = penalties.filter(p => p.status === 'APPROVED').length;
    const waived   = penalties.filter(p => p.status === 'WAIVED').length;

    return (
        <DashboardLayout pageTitle="Penalties" pageSubtitle="Manage student penalty points in your department">

            {error   && <div className="alert alert-danger"  style={{ marginBottom: 16 }}>{error}</div>}
            {success && <div className="alert alert-success" style={{ marginBottom: 16 }}>{success}</div>}

            {/* Summary pills */}
            <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 20 }}>
                {[
                    { label: 'Pending Approval', value: pending,          color: '#f59e0b' },
                    { label: 'Active',           value: active,           color: '#ef4444' },
                    { label: 'Waived',           value: waived,           color: '#10b981' },
                    { label: 'Total',            value: penalties.length, color: 'var(--primary)' },
                ].map((s) => (
                    <div key={s.label} style={{
                        padding: '8px 16px', borderRadius: 'var(--radius-sm)',
                        background: 'var(--bg-light)', border: '1px solid var(--border)', fontSize: 13,
                    }}>
                        <span style={{ color: s.color, fontWeight: 700, marginRight: 6 }}>{s.value}</span>
                        {s.label}
                    </div>
                ))}
            </div>

            {/* Toolbar */}
            <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', marginBottom: 16, alignItems: 'center' }}>
                <div style={{ position: 'relative', flex: 1, minWidth: 200 }}>
                    <HiOutlineSearch style={{ position: 'absolute', left: 10, top: '50%', transform: 'translateY(-50%)', color: 'var(--secondary)' }} />
                    <input className="form-input" style={{ paddingLeft: 32 }}
                        placeholder="Search by student or type…"
                        value={search} onChange={(e) => setSearch(e.target.value)} />
                </div>
                <select className="form-input" style={{ width: 160 }}
                    value={filterStatus} onChange={(e) => setFilter(e.target.value)}>
                    <option value="ALL">All Statuses</option>
                    <option value="PENDING">Pending</option>
                    <option value="APPROVED">Active</option>
                    <option value="WAIVED">Waived</option>
                    <option value="APPEALED">Appealed</option>
                </select>
                <button className="btn btn-outline btn-sm" onClick={load}
                    style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <HiOutlineRefresh /> Refresh
                </button>
            </div>

            {/* Table */}
            <div className="content-card">
                <div className="content-card-header">
                    <h2 className="content-card-title">Penalty Records</h2>
                    <span style={{ fontSize: 13, color: 'var(--secondary)' }}>
                        {!loading && `${filtered.length} records`}
                    </span>
                </div>
                <div className="table-container">
                    <table className="table">
                        <thead>
                            <tr>
                                <th>Student</th>
                                <th>Type</th>
                                <th>Points</th>
                                <th>Reason</th>
                                <th>Issued</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                [...Array(5)].map((_, i) => (
                                    <tr key={i}>
                                        {[140, 100, 50, 160, 90, 80, 120].map((w, j) => (
                                            <td key={j}><div className="skeleton" style={{ width: w, height: 16 }} /></td>
                                        ))}
                                    </tr>
                                ))
                            ) : filtered.length > 0 ? (
                                filtered.map((p) => {
                                    const tc = TYPE_MAP[p.penaltyType]   || { badge: 'badge-muted', label: p.penaltyType };
                                    const sc = STATUS_MAP[p.status]      || { badge: 'badge-muted', label: p.status };
                                    return (
                                        <tr key={p.penaltyId || p.id}>
                                            <td>
                                                <div style={{ fontWeight: 600 }}>{p.studentName || '—'}</div>
                                                <div style={{ fontSize: 12, color: 'var(--secondary)' }}>{p.studentId}</div>
                                            </td>
                                            <td><span className={`badge ${tc.badge}`}>{tc.label}</span></td>
                                            <td style={{ fontWeight: 700, color: '#ef4444' }}>{p.points}</td>
                                            <td style={{ fontSize: 13, maxWidth: 200 }}>{p.reason || '—'}</td>
                                            <td style={{ fontSize: 12, color: 'var(--secondary)' }}>
                                                {p.createdAt ? new Date(p.createdAt).toLocaleDateString() : '—'}
                                            </td>
                                            <td><span className={`badge ${sc.badge}`}>{sc.label}</span></td>
                                            <td>
                                                <div style={{ display: 'flex', gap: 6 }}>
                                                    <button className="btn btn-outline btn-sm" onClick={() => setView(p)} title="View">
                                                        <HiOutlineEye />
                                                    </button>
                                                    {p.status === 'PENDING' && (
                                                        <button
                                                            className="btn btn-sm"
                                                            style={{ background: '#dcfce7', color: '#16a34a', border: '1px solid #86efac' }}
                                                            onClick={() => handleApprove(p.penaltyId || p.id)}
                                                            disabled={actionId === (p.penaltyId || p.id)}
                                                            title="Approve Penalty"
                                                        >
                                                            {actionId === (p.penaltyId || p.id) ? '…' : <HiOutlineCheckCircle />}
                                                        </button>
                                                    )}
                                                    {p.status === 'APPROVED' && (
                                                        <button
                                                            className="btn btn-sm"
                                                            style={{ background: '#fef9c3', color: '#854d0e', border: '1px solid #fde047' }}
                                                            onClick={() => { setWaiveModal(p.penaltyId || p.id); setWaiveReason(''); }}
                                                            title="Waive Penalty"
                                                        >
                                                            Waive
                                                        </button>
                                                    )}
                                                </div>
                                            </td>
                                        </tr>
                                    );
                                })
                            ) : (
                                <tr>
                                    <td colSpan={7} style={{ textAlign: 'center', padding: 48, color: 'var(--secondary)' }}>
                                        <HiOutlineExclamationCircle style={{ fontSize: 32, display: 'block', margin: '0 auto 8px' }} />
                                        {search || filterStatus !== 'ALL' ? 'No penalties match filters' : 'No penalties recorded'}
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* View Modal */}
            {viewPenalty && (
                <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: 16 }}>
                    <div style={{ background: 'var(--bg-card)', borderRadius: 'var(--radius)', padding: 28, width: '100%', maxWidth: 460, boxShadow: 'var(--shadow-lg)' }}>
                        <h3 style={{ margin: '0 0 18px', fontWeight: 700 }}>Penalty Details</h3>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                            {[
                                { label: 'Student',    value: viewPenalty.studentName },
                                { label: 'Type',       value: TYPE_MAP[viewPenalty.penaltyType]?.label || viewPenalty.penaltyType },
                                { label: 'Points',     value: viewPenalty.points },
                                { label: 'Status',     value: STATUS_MAP[viewPenalty.status]?.label || viewPenalty.status },
                                { label: 'Reason',     value: viewPenalty.reason || '—' },
                                { label: 'Request ID', value: viewPenalty.requestId || '—' },
                                { label: 'Issued By',  value: viewPenalty.issuedByName || '—' },
                                { label: 'Issued On',  value: viewPenalty.createdAt ? new Date(viewPenalty.createdAt).toLocaleString() : '—' },
                            ].map(({ label, value }) => (
                                <div key={label} style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 12px', background: 'var(--bg-light)', borderRadius: 'var(--radius-sm)' }}>
                                    <span style={{ color: 'var(--secondary)', fontSize: 13 }}>{label}</span>
                                    <span style={{ fontWeight: 600, fontSize: 13 }}>{value ?? '—'}</span>
                                </div>
                            ))}
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 20 }}>
                            <button className="btn btn-outline" onClick={() => setView(null)}>Close</button>
                        </div>
                    </div>
                </div>
            )}

            {/* Waive Modal */}
            {waiveModal && (
                <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: 16 }}>
                    <div style={{ background: 'var(--bg-card)', borderRadius: 'var(--radius)', padding: 28, width: '100%', maxWidth: 420, boxShadow: 'var(--shadow-lg)' }}>
                        <h3 style={{ margin: '0 0 8px', fontWeight: 700 }}>Waive Penalty</h3>
                        <p style={{ color: 'var(--secondary)', fontSize: 14, marginBottom: 16 }}>Provide a reason for waiving this penalty.</p>
                        <textarea className="form-input" rows={4}
                            placeholder="Waive reason (required)…"
                            value={waiveReason} onChange={(e) => setWaiveReason(e.target.value)} />
                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10, marginTop: 16 }}>
                            <button className="btn btn-outline" onClick={() => setWaiveModal(null)}>Cancel</button>
                            <button className="btn" style={{ background: '#f59e0b', color: '#fff', border: 'none' }}
                                onClick={handleWaive} disabled={!!actionId || !waiveReason.trim()}>
                                {actionId ? 'Waiving…' : 'Confirm Waive'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </DashboardLayout>
    );
}
