'use client';

import { useState, useEffect } from 'react';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { requestAPI, approvalAPI } from '@/lib/api';
import {
    HiOutlineClipboardList,
    HiOutlineCheckCircle,
    HiOutlineXCircle,
    HiOutlineRefresh,
    HiOutlineSearch,
    HiOutlineEye,
} from 'react-icons/hi';

// The backend uses these exact status strings
const STATUS_MAP = {
    DRAFT:                 { badge: 'badge-muted',    label: 'Draft'              },
    PENDINGAPPROVAL:       { badge: 'badge-warning',  label: 'Pending Approval'   },
    PENDINGRECOMMENDATION: { badge: 'badge-warning',  label: 'Pending Recommend.' },
    APPROVED:              { badge: 'badge-success',  label: 'Approved'           },
    REJECTED:              { badge: 'badge-danger',   label: 'Rejected'           },
    INUSE:                 { badge: 'badge-primary',  label: 'In Use'             },
    RETURNED:              { badge: 'badge-muted',    label: 'Returned'           },
    COMPLETED:             { badge: 'badge-success',  label: 'Completed'          },
    OVERDUE:               { badge: 'badge-danger',   label: 'Overdue'            },
    CANCELLED:             { badge: 'badge-muted',    label: 'Cancelled'          },
    // legacy aliases (backend may return either)
    PENDING:               { badge: 'badge-warning',  label: 'Pending'            },
};

// Requests that need dept-admin action
const ACTIONABLE = ['PENDINGAPPROVAL', 'PENDINGRECOMMENDATION', 'PENDING'];

export default function DeptRequestsPage() {
    const [requests, setRequests]       = useState([]);
    const [loading, setLoading]         = useState(true);
    const [actionId, setActionId]       = useState(null);
    const [search, setSearch]           = useState('');
    const [filterStatus, setFilter]     = useState('ALL');
    const [error, setError]             = useState(null);
    const [success, setSuccess]         = useState(null);
    const [viewRequest, setViewRequest] = useState(null);
    const [rejectNote, setRejectNote]   = useState('');
    const [showReject, setShowReject]   = useState(null); // { id, stage }

    useEffect(() => { load(); }, []);

    const load = async () => {
        setLoading(true); setError(null);
        try {
            // getDepartmentRequests() → GET /requests/department
            const res  = await requestAPI.getDepartmentRequests();
            const raw  = res.data?.data || res.data || [];
            // backend may return { requests: [] } or a plain array
            const list = Array.isArray(raw) ? raw : (raw.requests || raw.content || []);
            setRequests(list);
        } catch (e) {
            setError('Failed to load requests. Is the backend running?');
        } finally {
            setLoading(false);
        }
    };

    const flash = (msg, isError = false) => {
        isError ? setError(msg) : setSuccess(msg);
        setTimeout(() => isError ? setError(null) : setSuccess(null), 4500);
    };

    // Resolve the correct approval stage from the request status
    const resolveStage = (status) => {
        if (status === 'PENDINGRECOMMENDATION') return 'LECTURERAPPROVAL';
        return 'DEPARTMENTADMINAPPROVAL';
    };

    const handleApprove = async (req) => {
        const id    = req.requestId || req.id;
        const stage = resolveStage(req.status);
        setActionId(id);
        try {
            // Use the proper approval workflow endpoint
            await approvalAPI.processDecision(id, stage, { action: 'APPROVE', comments: '' });
            flash('Request approved successfully');
            load();
        } catch (e) {
            // Fallback to legacy PATCH endpoint if approval workflow not wired
            try {
                await requestAPI.approveRequest(id);
                flash('Request approved successfully');
                load();
            } catch (e2) {
                flash(e2.response?.data?.message || e.response?.data?.message || 'Approval failed', true);
            }
        } finally {
            setActionId(null);
        }
    };

    const handleReject = async () => {
        if (!showReject) return;
        const { id, stage } = showReject;
        setActionId(id);
        try {
            await approvalAPI.processDecision(id, stage, { action: 'REJECT', comments: rejectNote });
            flash('Request rejected');
            setShowReject(null); setRejectNote('');
            load();
        } catch (e) {
            // Fallback
            try {
                await requestAPI.rejectRequest(id, { reason: rejectNote });
                flash('Request rejected');
                setShowReject(null); setRejectNote('');
                load();
            } catch (e2) {
                flash(e2.response?.data?.message || e.response?.data?.message || 'Rejection failed', true);
            }
        } finally {
            setActionId(null);
        }
    };

    const filtered = requests.filter((r) => {
        const name = `${r.requesterName || ''} ${r.studentName || ''} ${r.equipmentName || ''} ${r.requestId || ''}`.toLowerCase();
        const matchSearch = !search || name.includes(search.toLowerCase());
        const matchStatus = filterStatus === 'ALL' || r.status === filterStatus;
        return matchSearch && matchStatus;
    });

    const pending  = requests.filter(r => ACTIONABLE.includes(r.status)).length;
    const approved = requests.filter(r => r.status === 'APPROVED').length;
    const inUse    = requests.filter(r => r.status === 'INUSE').length;
    const overdue  = requests.filter(r => r.status === 'OVERDUE').length;

    return (
        <DashboardLayout pageTitle="Requests" pageSubtitle="Equipment borrow requests from students & staff">

            {error   && <div className="alert alert-danger"  style={{ marginBottom: 16 }}>{error}</div>}
            {success && <div className="alert alert-success" style={{ marginBottom: 16 }}>{success}</div>}

            {/* Summary pills */}
            <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 20 }}>
                {[
                    { label: 'Needs Action', value: pending,          color: '#f59e0b' },
                    { label: 'Approved',     value: approved,         color: '#10b981' },
                    { label: 'In Use',       value: inUse,            color: '#3b82f6' },
                    { label: 'Overdue',      value: overdue,          color: '#ef4444' },
                    { label: 'Total',        value: requests.length,  color: 'var(--primary)' },
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
                    <HiOutlineSearch style={{
                        position: 'absolute', left: 10, top: '50%',
                        transform: 'translateY(-50%)', color: 'var(--secondary)',
                    }} />
                    <input className="form-input" style={{ paddingLeft: 32 }}
                        placeholder="Search by student, equipment or request ID…"
                        value={search} onChange={(e) => setSearch(e.target.value)} />
                </div>
                <select className="form-input" style={{ width: 190 }}
                    value={filterStatus} onChange={(e) => setFilter(e.target.value)}>
                    <option value="ALL">All Statuses</option>
                    {Object.entries(STATUS_MAP).map(([key, val]) => (
                        <option key={key} value={key}>{val.label}</option>
                    ))}
                </select>
                <button className="btn btn-outline btn-sm" onClick={load}
                    style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <HiOutlineRefresh /> Refresh
                </button>
            </div>

            {/* Table */}
            <div className="content-card">
                <div className="content-card-header">
                    <h2 className="content-card-title">Borrow Requests</h2>
                    <span style={{ fontSize: 13, color: 'var(--secondary)' }}>
                        {!loading && `${filtered.length} requests`}
                    </span>
                </div>
                <div className="table-container">
                    <table className="table">
                        <thead>
                            <tr>
                                <th>Requester</th>
                                <th>Equipment</th>
                                <th>Qty</th>
                                <th>Borrow Date</th>
                                <th>Return Date</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                [...Array(5)].map((_, i) => (
                                    <tr key={i}>
                                        {[140,160,40,100,100,120,120].map((w, j) => (
                                            <td key={j}><div className="skeleton" style={{ width: w, height: 16 }} /></td>
                                        ))}
                                    </tr>
                                ))
                            ) : filtered.length > 0 ? (
                                filtered.map((req) => {
                                    const id        = req.requestId || req.id;
                                    const sc        = STATUS_MAP[req.status] || { badge: 'badge-muted', label: req.status };
                                    const isActionable = ACTIONABLE.includes(req.status);
                                    const isActing  = actionId === id;
                                    const stage     = resolveStage(req.status);
                                    return (
                                        <tr key={id}>
                                            <td>
                                                <div style={{ fontWeight: 600 }}>
                                                    {req.studentName || req.requesterName ||
                                                     `${req.firstName || ''} ${req.lastName || ''}`.trim() || '—'}
                                                </div>
                                                <div style={{ fontSize: 12, color: 'var(--secondary)' }}>
                                                    {req.requesterRole || req.role || ''}
                                                </div>
                                            </td>
                                            <td style={{ fontWeight: 500 }}>
                                                {req.equipmentName || req.items?.[0]?.equipmentName || req.equipment?.name || '—'}
                                            </td>
                                            <td>{req.quantity || req.items?.[0]?.quantity || 1}</td>
                                            <td style={{ fontSize: 13 }}>
                                                {req.borrowDate ? new Date(req.borrowDate).toLocaleDateString() : '—'}
                                            </td>
                                            <td style={{ fontSize: 13 }}>
                                                {req.returnDate ? new Date(req.returnDate).toLocaleDateString() : '—'}
                                            </td>
                                            <td><span className={`badge ${sc.badge}`}>{sc.label}</span></td>
                                            <td>
                                                <div style={{ display: 'flex', gap: 6 }}>
                                                    <button className="btn btn-outline btn-sm"
                                                        onClick={() => setViewRequest(req)}
                                                        title="View details">
                                                        <HiOutlineEye />
                                                    </button>
                                                    {isActionable && (
                                                        <>
                                                            <button
                                                                className="btn btn-sm"
                                                                style={{ background: '#dcfce7', color: '#16a34a', border: '1px solid #86efac' }}
                                                                onClick={() => handleApprove(req)}
                                                                disabled={isActing}
                                                                title="Approve"
                                                            >
                                                                {isActing ? '…' : <HiOutlineCheckCircle />}
                                                            </button>
                                                            <button
                                                                className="btn btn-sm"
                                                                style={{ background: '#fee2e2', color: '#dc2626', border: '1px solid #fca5a5' }}
                                                                onClick={() => { setShowReject({ id, stage }); setRejectNote(''); }}
                                                                disabled={isActing}
                                                                title="Reject"
                                                            >
                                                                <HiOutlineXCircle />
                                                            </button>
                                                        </>
                                                    )}
                                                </div>
                                            </td>
                                        </tr>
                                    );
                                })
                            ) : (
                                <tr>
                                    <td colSpan={7} style={{ textAlign: 'center', padding: 48, color: 'var(--secondary)' }}>
                                        <HiOutlineClipboardList style={{ fontSize: 32, display: 'block', margin: '0 auto 8px' }} />
                                        {search || filterStatus !== 'ALL'
                                            ? 'No requests match your filters'
                                            : 'No borrow requests yet'}
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* ── View Detail Modal ── */}
            {viewRequest && (
                <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: 16 }}>
                    <div style={{ background: 'var(--bg-card)', borderRadius: 'var(--radius)', padding: 28, width: '100%', maxWidth: 500, boxShadow: 'var(--shadow-lg)', maxHeight: '90vh', overflowY: 'auto' }}>
                        <h3 style={{ margin: '0 0 18px', fontWeight: 700 }}>Request Details</h3>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                            {[
                                { label: 'Request ID',  value: viewRequest.requestId || viewRequest.id },
                                { label: 'Requester',   value: viewRequest.studentName || viewRequest.requesterName || `${viewRequest.firstName || ''} ${viewRequest.lastName || ''}`.trim() },
                                { label: 'Role',        value: viewRequest.requesterRole || viewRequest.role },
                                { label: 'Equipment',   value: viewRequest.equipmentName || viewRequest.items?.[0]?.equipmentName || viewRequest.equipment?.name },
                                { label: 'Quantity',    value: viewRequest.quantity || viewRequest.items?.[0]?.quantity || 1 },
                                { label: 'Borrow Date', value: viewRequest.borrowDate ? new Date(viewRequest.borrowDate).toLocaleDateString() : '—' },
                                { label: 'Return Date', value: viewRequest.returnDate ? new Date(viewRequest.returnDate).toLocaleDateString() : '—' },
                                { label: 'Status',      value: STATUS_MAP[viewRequest.status]?.label || viewRequest.status },
                                { label: 'Purpose',     value: viewRequest.purpose || viewRequest.reason || '—' },
                                { label: 'Notes',       value: viewRequest.notes || '—' },
                            ].map(({ label, value }) => (
                                <div key={label} style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 12px', background: 'var(--bg-light)', borderRadius: 'var(--radius-sm)' }}>
                                    <span style={{ color: 'var(--secondary)', fontSize: 13 }}>{label}</span>
                                    <span style={{ fontWeight: 600, fontSize: 13, textAlign: 'right', maxWidth: '60%' }}>{value ?? '—'}</span>
                                </div>
                            ))}
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 20, gap: 10 }}>
                            {ACTIONABLE.includes(viewRequest.status) && (
                                <>
                                    <button
                                        className="btn btn-sm"
                                        style={{ background: '#dcfce7', color: '#16a34a', border: '1px solid #86efac' }}
                                        onClick={() => { handleApprove(viewRequest); setViewRequest(null); }}
                                    >
                                        Approve
                                    </button>
                                    <button
                                        className="btn btn-sm"
                                        style={{ background: '#fee2e2', color: '#dc2626', border: '1px solid #fca5a5' }}
                                        onClick={() => {
                                            setShowReject({ id: viewRequest.requestId || viewRequest.id, stage: resolveStage(viewRequest.status) });
                                            setViewRequest(null);
                                        }}
                                    >
                                        Reject
                                    </button>
                                </>
                            )}
                            <button className="btn btn-outline" onClick={() => setViewRequest(null)}>Close</button>
                        </div>
                    </div>
                </div>
            )}

            {/* ── Reject Modal ── */}
            {showReject && (
                <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: 16 }}>
                    <div style={{ background: 'var(--bg-card)', borderRadius: 'var(--radius)', padding: 28, width: '100%', maxWidth: 420, boxShadow: 'var(--shadow-lg)' }}>
                        <h3 style={{ margin: '0 0 8px', fontWeight: 700 }}>Reject Request</h3>
                        <p style={{ color: 'var(--secondary)', fontSize: 14, marginBottom: 16 }}>
                            Provide a reason that will be shown to the requester.
                        </p>
                        <textarea className="form-input" rows={4}
                            placeholder="Rejection reason…"
                            value={rejectNote} onChange={(e) => setRejectNote(e.target.value)} />
                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10, marginTop: 16 }}>
                            <button className="btn btn-outline" onClick={() => setShowReject(null)}>Cancel</button>
                            <button
                                className="btn"
                                style={{ background: '#dc2626', color: '#fff', border: 'none' }}
                                onClick={handleReject}
                                disabled={!!actionId}
                            >
                                {actionId ? 'Rejecting…' : 'Confirm Reject'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </DashboardLayout>
    );
}
