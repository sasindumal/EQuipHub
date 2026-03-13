'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { requestAPI } from '@/lib/api';
import {
    HiOutlineClipboardList,
    HiOutlineSearch,
    HiOutlineRefresh,
    HiOutlinePlusCircle,
    HiOutlineEye,
    HiOutlineX,
} from 'react-icons/hi';

const STATUS_COLOR = {
    DRAFT:                  { bg: '#f1f5f9', text: '#475569' },
    PENDINGAPPROVAL:        { bg: '#fef9c3', text: '#854d0e' },
    PENDINGRECOMMENDATION:  { bg: '#fef9c3', text: '#854d0e' },
    APPROVED:               { bg: '#dcfce7', text: '#166534' },
    INUSE:                  { bg: '#dbeafe', text: '#1e40af' },
    RETURNED:               { bg: '#f1f5f9', text: '#6b7280' },
    COMPLETED:              { bg: '#d1fae5', text: '#065f46' },
    REJECTED:               { bg: '#fee2e2', text: '#991b1b' },
    CANCELLED:              { bg: '#f3f4f6', text: '#9ca3af' },
    OVERDUE:                { bg: '#fee2e2', text: '#991b1b' },
};

export default function StudentRequestsPage() {
    const [requests, setRequests]   = useState([]);
    const [loading, setLoading]     = useState(true);
    const [error, setError]         = useState(null);
    const [success, setSuccess]     = useState(null);
    const [search, setSearch]       = useState('');
    const [filterStatus, setFilter] = useState('ALL');
    const [viewReq, setViewReq]     = useState(null);
    const [cancelId, setCancelId]   = useState(null);
    const [actionId, setActionId]   = useState(null);

    useEffect(() => { load(); }, []);

    const load = async () => {
        setLoading(true); setError(null);
        try {
            const res  = await requestAPI.getMyRequests();
            const data = res.data?.data?.requests || res.data?.requests || res.data || [];
            setRequests(Array.isArray(data) ? data : []);
        } catch (e) { setError('Failed to load requests.'); }
        finally { setLoading(false); }
    };

    const flash = (msg, isError = false) => {
        isError ? setError(msg) : setSuccess(msg);
        setTimeout(() => isError ? setError(null) : setSuccess(null), 4000);
    };

    const handleCancel = async (id) => {
        setActionId(id);
        try {
            await requestAPI.cancelRequest(id);
            flash('Request cancelled'); setCancelId(null); load();
        } catch (e) { flash(e.response?.data?.message || 'Cancel failed', true); }
        finally { setActionId(null); }
    };

    const canCancel = (status) => ['DRAFT','PENDINGAPPROVAL','PENDINGRECOMMENDATION'].includes(status);

    const filtered = requests.filter(r => {
        const txt = `${r.requestId || ''} ${r.equipmentName || ''}`.toLowerCase();
        return (!search || txt.includes(search.toLowerCase())) &&
               (filterStatus === 'ALL' || r.status === filterStatus);
    });

    return (
        <DashboardLayout pageTitle="My Requests" pageSubtitle="All your equipment borrow requests">
            {error   && <div className="alert alert-danger"  style={{ marginBottom: 16 }}>{error}</div>}
            {success && <div className="alert alert-success" style={{ marginBottom: 16 }}>{success}</div>}

            {/* Toolbar */}
            <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', marginBottom: 16, alignItems: 'center' }}>
                <Link href="/student/requests/new" className="btn" style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <HiOutlinePlusCircle /> New Request
                </Link>
                <div style={{ position: 'relative', flex: 1, minWidth: 180 }}>
                    <HiOutlineSearch style={{ position: 'absolute', left: 10, top: '50%', transform: 'translateY(-50%)', color: 'var(--secondary)' }} />
                    <input className="form-input" style={{ paddingLeft: 32 }} placeholder="Search requests…"
                        value={search} onChange={e => setSearch(e.target.value)} />
                </div>
                <select className="form-input" style={{ width: 180 }}
                    value={filterStatus} onChange={e => setFilter(e.target.value)}>
                    <option value="ALL">All Statuses</option>
                    {Object.keys(STATUS_COLOR).map(s => <option key={s} value={s}>{s}</option>)}
                </select>
                <button className="btn btn-outline btn-sm" onClick={load} style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <HiOutlineRefresh /> Refresh
                </button>
            </div>

            <div className="content-card">
                <div className="content-card-header">
                    <h2 className="content-card-title">Borrow Requests</h2>
                    <span style={{ fontSize: 13, color: 'var(--secondary)' }}>{!loading && `${filtered.length} requests`}</span>
                </div>
                <div className="table-container">
                    <table className="table">
                        <thead>
                            <tr><th>Request ID</th><th>Equipment</th><th>Borrow Date</th><th>Return Date</th><th>Status</th><th>Actions</th></tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                [...Array(5)].map((_,i) => <tr key={i}>{[120,160,90,90,80,80].map((w,j)=><td key={j}><div className="skeleton" style={{width:w,height:16}}/></td>)}</tr>)
                            ) : filtered.length > 0 ? (
                                filtered.map(r => {
                                    const id  = r.requestId || r.id;
                                    const sc  = STATUS_COLOR[r.status] || { bg: '#f1f5f9', text: '#475569' };
                                    return (
                                        <tr key={id}>
                                            <td style={{ fontWeight: 600, fontSize: 13 }}>{id}</td>
                                            <td>{r.equipmentName || r.items?.[0]?.equipmentName || '—'}</td>
                                            <td style={{ fontSize: 12, color: 'var(--secondary)' }}>
                                                {r.borrowDate ? new Date(r.borrowDate).toLocaleDateString() : '—'}
                                            </td>
                                            <td style={{ fontSize: 12, color: 'var(--secondary)' }}>
                                                {r.returnDate ? new Date(r.returnDate).toLocaleDateString() : '—'}
                                            </td>
                                            <td>
                                                <span style={{ padding: '2px 10px', borderRadius: 20, fontSize: 12, fontWeight: 600, background: sc.bg, color: sc.text }}>
                                                    {r.status}
                                                </span>
                                            </td>
                                            <td>
                                                <div style={{ display: 'flex', gap: 6 }}>
                                                    <button className="btn btn-outline btn-sm" onClick={() => setViewReq(r)} title="View">
                                                        <HiOutlineEye />
                                                    </button>
                                                    {canCancel(r.status) && (
                                                        <button
                                                            className="btn btn-sm"
                                                            style={{ background: '#fee2e2', color: '#dc2626', border: '1px solid #fca5a5' }}
                                                            onClick={() => setCancelId(id)}
                                                            title="Cancel"
                                                        >
                                                            <HiOutlineX />
                                                        </button>
                                                    )}
                                                </div>
                                            </td>
                                        </tr>
                                    );
                                })
                            ) : (
                                <tr><td colSpan={6} style={{ textAlign: 'center', padding: 48, color: 'var(--secondary)' }}>
                                    <HiOutlineClipboardList style={{ fontSize: 32, display: 'block', margin: '0 auto 8px' }} />
                                    {search || filterStatus !== 'ALL' ? 'No requests match filters' : 'No requests yet — create your first!'}
                                </td></tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* View Modal */}
            {viewReq && (
                <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: 16 }}>
                    <div style={{ background: 'var(--bg-card)', borderRadius: 'var(--radius)', padding: 28, width: '100%', maxWidth: 480, boxShadow: 'var(--shadow-lg)' }}>
                        <h3 style={{ margin: '0 0 18px', fontWeight: 700 }}>Request Details</h3>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                            {[
                                { label: 'Request ID',  value: viewReq.requestId || viewReq.id },
                                { label: 'Equipment',   value: viewReq.equipmentName || '—' },
                                { label: 'Status',      value: viewReq.status },
                                { label: 'Borrow Date', value: viewReq.borrowDate  ? new Date(viewReq.borrowDate).toLocaleDateString()  : '—' },
                                { label: 'Return Date', value: viewReq.returnDate  ? new Date(viewReq.returnDate).toLocaleDateString()  : '—' },
                                { label: 'Purpose',     value: viewReq.purpose || '—' },
                                { label: 'Notes',       value: viewReq.notes || '—' },
                            ].map(({ label, value }) => (
                                <div key={label} style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 12px', background: 'var(--bg-light)', borderRadius: 'var(--radius-sm)' }}>
                                    <span style={{ color: 'var(--secondary)', fontSize: 13 }}>{label}</span>
                                    <span style={{ fontWeight: 600, fontSize: 13 }}>{value ?? '—'}</span>
                                </div>
                            ))}
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 20 }}>
                            <button className="btn btn-outline" onClick={() => setViewReq(null)}>Close</button>
                        </div>
                    </div>
                </div>
            )}

            {/* Cancel Confirm Modal */}
            {cancelId && (
                <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: 16 }}>
                    <div style={{ background: 'var(--bg-card)', borderRadius: 'var(--radius)', padding: 28, width: '100%', maxWidth: 380, boxShadow: 'var(--shadow-lg)' }}>
                        <h3 style={{ margin: '0 0 12px', fontWeight: 700 }}>Cancel Request?</h3>
                        <p style={{ color: 'var(--secondary)', fontSize: 14, marginBottom: 20 }}>This will cancel request <strong>{cancelId}</strong>. This action cannot be undone.</p>
                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10 }}>
                            <button className="btn btn-outline" onClick={() => setCancelId(null)}>Back</button>
                            <button className="btn" style={{ background: '#dc2626', color: '#fff', border: 'none' }}
                                onClick={() => handleCancel(cancelId)} disabled={!!actionId}>
                                {actionId ? 'Cancelling…' : 'Yes, Cancel'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </DashboardLayout>
    );
}
