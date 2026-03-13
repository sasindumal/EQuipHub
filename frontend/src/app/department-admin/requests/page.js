'use client';

import { useState, useEffect } from 'react';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { requestAPI } from '@/lib/api';
import {
    HiOutlineClipboardList,
    HiOutlineCheckCircle,
    HiOutlineXCircle,
    HiOutlineRefresh,
    HiOutlineSearch,
    HiOutlineEye,
} from 'react-icons/hi';

const STATUS_MAP = {
    PENDING:  { badge: 'badge-warning', label: 'Pending'  },
    APPROVED: { badge: 'badge-success', label: 'Approved' },
    REJECTED: { badge: 'badge-danger',  label: 'Rejected' },
    RETURNED: { badge: 'badge-muted',   label: 'Returned' },
    OVERDUE:  { badge: 'badge-danger',  label: 'Overdue'  },
};

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
    const [showReject, setShowReject]   = useState(null);

    useEffect(() => { load(); }, []);

    const load = async () => {
        setLoading(true);
        setError(null);
        try {
            const res = await requestAPI.getDepartmentRequests();
            const data = res.data?.data || res.data || [];
            setRequests(Array.isArray(data) ? data : []);
        } catch (e) {
            setError('Failed to load requests. Is the backend running?');
        } finally {
            setLoading(false);
        }
    };

    const flash = (msg, isError = false) => {
        isError ? setError(msg) : setSuccess(msg);
        setTimeout(() => isError ? setError(null) : setSuccess(null), 4000);
    };

    const handleApprove = async (id) => {
        setActionId(id);
        try {
            await requestAPI.approveRequest(id);
            flash('Request approved successfully');
            load();
        } catch (e) {
            flash(e.response?.data?.message || 'Approval failed', true);
        } finally {
            setActionId(null);
        }
    };

    const handleReject = async () => {
        if (!showReject) return;
        setActionId(showReject);
        try {
            await requestAPI.rejectRequest(showReject, { reason: rejectNote });
            flash('Request rejected');
            setShowReject(null);
            setRejectNote('');
            load();
        } catch (e) {
            flash(e.response?.data?.message || 'Rejection failed', true);
        } finally {
            setActionId(null);
        }
    };

    const filtered = requests.filter((r) => {
        const name = `${r.requesterName || ''} ${r.equipmentName || ''}`.toLowerCase();
        const matchSearch = !search || name.includes(search.toLowerCase());
        const matchStatus = filterStatus === 'ALL' || r.status === filterStatus;
        return matchSearch && matchStatus;
    });

    const pending  = requests.filter((r) => r.status === 'PENDING').length;
    const approved = requests.filter((r) => r.status === 'APPROVED').length;
    const overdue  = requests.filter((r) => r.status === 'OVERDUE').length;

    return (
        <DashboardLayout pageTitle="Requests" pageSubtitle="Equipment borrow requests from students & staff">

            {error   && <div className="alert alert-danger"  style={{ marginBottom: 16 }}>{error}</div>}
            {success && <div className="alert alert-success" style={{ marginBottom: 16 }}>{success}</div>}

            {/* summary pills */}
            <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 20 }}>
                {[
                    { label: 'Pending',  value: pending,          color: '#f59e0b' },
                    { label: 'Approved', value: approved,         color: '#10b981' },
                    { label: 'Overdue',  value: overdue,          color: '#ef4444' },
                    { label: 'Total',    value: requests.length,  color: 'var(--primary)' },
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

            {/* toolbar */}
            <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', marginBottom: 16, alignItems: 'center' }}>
                <div style={{ position: 'relative', flex: 1, minWidth: 200 }}>
                    <HiOutlineSearch style={{
                        position: 'absolute', left: 10, top: '50%',
                        transform: 'translateY(-50%)', color: 'var(--secondary)',
                    }} />
                    <input className="form-input" style={{ paddingLeft: 32 }}
                        placeholder="Search by requester or equipment…"
                        value={search} onChange={(e) => setSearch(e.target.value)} />
                </div>
                <select className="form-input" style={{ width: 160 }}
                    value={filterStatus} onChange={(e) => setFilter(e.target.value)}>
                    <option value="ALL">All Statuses</option>
                    <option value="PENDING">Pending</option>
                    <option value="APPROVED">Approved</option>
                    <option value="REJECTED">Rejected</option>
                    <option value="RETURNED">Returned</option>
                    <option value="OVERDUE">Overdue</option>
                </select>
                <button className="btn btn-outline btn-sm" onClick={load}
                    style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <HiOutlineRefresh /> Refresh
                </button>
            </div>

            {/* table */}
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
                                        {[140, 160, 40, 100, 100, 80, 120].map((w, j) => (
                                            <td key={j}><div className="skeleton" style={{ width: w, height: 16 }} /></td>
                                        ))}
                                    </tr>
                                ))
                            ) : filtered.length > 0 ? (
                                filtered.map((req) => {
                                    const id = req.requestId || req.id;
                                    const sc = STATUS_MAP[req.status] || { badge: 'badge-muted', label: req.status };
                                    const isPending = req.status === 'PENDING';
                                    const isActing  = actionId === id;
                                    return (
                                        <tr key={id}>
                                            <td>
                                                <div style={{ fontWeight: 600 }}>
                                                    {req.requesterName || `${req.firstName || ''} ${req.lastName || ''}`.trim() || '—'}
                                                </div>
                                                <div style={{ fontSize: 12, color: 'var(--secondary)' }}>
                                                    {req.requesterRole || req.role || ''}
                                                </div>
                                            </td>
                                            <td style={{ fontWeight: 500 }}>{req.equipmentName || req.equipment?.name || '—'}</td>
                                            <td>{req.quantity || 1}</td>
                                            <td style={{ fontSize: 13 }}>
                                                {req.borrowDate ? new Date(req.borrowDate).toLocaleDateString() : '—'}
                                            </td>
                                            <td style={{ fontSize: 13 }}>
                                                {req.returnDate ? new Date(req.returnDate).toLocaleDateString() : '—'}
                                            </td>
                                            <td><span className={`badge ${sc.badge}`}>{sc.label}</span></td>
                                            <td>
                                                <div style={{ display: 'flex', gap: 6 }}>
                                                    <button
                                                        className="btn btn-outline btn-sm"
                                                        onClick={() => setViewRequest(req)}
                                                        title="View details"
                                                    >
                                                        <HiOutlineEye />
                                                    </button>
                                                    {isPending && (
                                                        <>
                                                            <button
                                                                className="btn btn-sm"
                                                                style={{ background: '#dcfce7', color: '#16a34a', border: '1px solid #86efac' }}
                                                                onClick={() => handleApprove(id)}
                                                                disabled={isActing}
                                                                title="Approve"
                                                            >
                                                                {isActing ? '…' : <HiOutlineCheckCircle />}
                                                            </button>
                                                            <button
                                                                className="btn btn-sm"
                                                                style={{ background: '#fee2e2', color: '#dc2626', border: '1px solid #fca5a5' }}
                                                                onClick={() => { setShowReject(id); setRejectNote(''); }}
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
                <div style={{
                    position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    zIndex: 1000, padding: 16,
                }}>
                    <div style={{
                        background: 'var(--bg-card)', borderRadius: 'var(--radius)',
                        padding: 28, width: '100%', maxWidth: 480,
                        boxShadow: 'var(--shadow-lg)',
                    }}>
                        <h3 style={{ margin: '0 0 18px', fontWeight: 700 }}>Request Details</h3>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                            {[
                                { label: 'Requester',   value: viewRequest.requesterName || `${viewRequest.firstName || ''} ${viewRequest.lastName || ''}`.trim() },
                                { label: 'Role',        value: viewRequest.requesterRole || viewRequest.role },
                                { label: 'Equipment',   value: viewRequest.equipmentName || viewRequest.equipment?.name },
                                { label: 'Quantity',    value: viewRequest.quantity },
                                { label: 'Borrow Date', value: viewRequest.borrowDate  ? new Date(viewRequest.borrowDate).toLocaleDateString()  : '—' },
                                { label: 'Return Date', value: viewRequest.returnDate  ? new Date(viewRequest.returnDate).toLocaleDateString()  : '—' },
                                { label: 'Status',      value: viewRequest.status },
                                { label: 'Purpose',     value: viewRequest.purpose || viewRequest.reason || '—' },
                                { label: 'Notes',       value: viewRequest.notes || '—' },
                            ].map(({ label, value }) => (
                                <div key={label} style={{
                                    display: 'flex', justifyContent: 'space-between',
                                    padding: '8px 12px', background: 'var(--bg-light)',
                                    borderRadius: 'var(--radius-sm)',
                                }}>
                                    <span style={{ color: 'var(--secondary)', fontSize: 13 }}>{label}</span>
                                    <span style={{ fontWeight: 600, fontSize: 13 }}>{value ?? '—'}</span>
                                </div>
                            ))}
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 20, gap: 10 }}>
                            {viewRequest.status === 'PENDING' && (
                                <>
                                    <button
                                        className="btn btn-sm"
                                        style={{ background: '#dcfce7', color: '#16a34a', border: '1px solid #86efac' }}
                                        onClick={() => { handleApprove(viewRequest.requestId || viewRequest.id); setViewRequest(null); }}
                                    >
                                        Approve
                                    </button>
                                    <button
                                        className="btn btn-sm"
                                        style={{ background: '#fee2e2', color: '#dc2626', border: '1px solid #fca5a5' }}
                                        onClick={() => { setShowReject(viewRequest.requestId || viewRequest.id); setViewRequest(null); }}
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

            {/* ── Reject with Reason Modal ── */}
            {showReject && (
                <div style={{
                    position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    zIndex: 1000, padding: 16,
                }}>
                    <div style={{
                        background: 'var(--bg-card)', borderRadius: 'var(--radius)',
                        padding: 28, width: '100%', maxWidth: 420,
                        boxShadow: 'var(--shadow-lg)',
                    }}>
                        <h3 style={{ margin: '0 0 8px', fontWeight: 700 }}>Reject Request</h3>
                        <p style={{ color: 'var(--secondary)', fontSize: 14, marginBottom: 16 }}>
                            Optionally provide a reason that will be shown to the requester.
                        </p>
                        <textarea
                            className="form-input" rows={4}
                            placeholder="Rejection reason (optional)…"
                            value={rejectNote}
                            onChange={(e) => setRejectNote(e.target.value)}
                        />
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
