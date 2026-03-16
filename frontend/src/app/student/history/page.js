'use client';

import { useState, useEffect, useMemo } from 'react';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { requestAPI } from '@/lib/api';
import {
    HiOutlineClipboardList,
    HiOutlineSearch,
    HiOutlineRefresh,
    HiOutlineEye,
    HiOutlineCalendar,
    HiOutlineFilter,
} from 'react-icons/hi';

const STATUS_MAP = {
    DRAFT:                 { badge: 'badge-muted',    label: 'Draft',             color: '#ADBBDA' },
    PENDINGAPPROVAL:       { badge: 'badge-warning',  label: 'Pending Approval',  color: '#8697C4' },
    PENDINGRECOMMENDATION: { badge: 'badge-warning',  label: 'Pending Recommend.',color: '#8697C4' },
    APPROVED:              { badge: 'badge-success',  label: 'Approved',          color: '#7091E6' },
    INUSE:                 { badge: 'badge-primary',  label: 'In Use',            color: '#3D52A0' },
    RETURNED:              { badge: 'badge-muted',    label: 'Returned',          color: '#ADBBDA' },
    COMPLETED:             { badge: 'badge-success',  label: 'Completed',         color: '#7091E6' },
    REJECTED:              { badge: 'badge-danger',   label: 'Rejected',          color: '#3D52A0' },
    CANCELLED:             { badge: 'badge-muted',    label: 'Cancelled',         color: '#ADBBDA' },
    OVERDUE:               { badge: 'badge-danger',   label: 'Overdue',           color: '#3D52A0' },
    PENDING:               { badge: 'badge-warning',  label: 'Pending',           color: '#8697C4' },
    IN_USE:                { badge: 'badge-primary',  label: 'In Use',            color: '#3D52A0' },
};

export default function StudentHistoryPage() {
    const [requests, setRequests] = useState([]);
    const [loading, setLoading]   = useState(true);
    const [error, setError]       = useState(null);
    const [search, setSearch]     = useState('');
    const [filterStatus, setFilter] = useState('ALL');
    const [dateFrom, setDateFrom] = useState('');
    const [dateTo, setDateTo]     = useState('');
    const [viewReq, setViewReq]   = useState(null);
    const [sortBy, setSortBy]     = useState('newest');

    useEffect(() => { load(); }, []);

    const load = async () => {
        setLoading(true); setError(null);
        try {
            const res  = await requestAPI.getMyRequests();
            const data = res.data?.data?.requests || res.data?.requests || res.data || [];
            setRequests(Array.isArray(data) ? data : []);
        } catch (e) { setError('Failed to load request history.'); }
        finally { setLoading(false); }
    };

    const filtered = useMemo(() => {
        let result = requests.filter(r => {
            const txt = `${r.requestId || ''} ${r.equipmentName || ''} ${r.purpose || ''}`.toLowerCase();
            const matchSearch = !search || txt.includes(search.toLowerCase());
            const matchStatus = filterStatus === 'ALL' || r.status === filterStatus;

            let matchDate = true;
            if (dateFrom) {
                const reqDate = new Date(r.createdAt || r.borrowDate);
                matchDate = matchDate && reqDate >= new Date(dateFrom);
            }
            if (dateTo) {
                const reqDate = new Date(r.createdAt || r.borrowDate);
                matchDate = matchDate && reqDate <= new Date(dateTo + 'T23:59:59');
            }
            return matchSearch && matchStatus && matchDate;
        });

        if (sortBy === 'newest') {
            result.sort((a, b) => new Date(b.createdAt || b.borrowDate || 0) - new Date(a.createdAt || a.borrowDate || 0));
        } else if (sortBy === 'oldest') {
            result.sort((a, b) => new Date(a.createdAt || a.borrowDate || 0) - new Date(b.createdAt || b.borrowDate || 0));
        } else if (sortBy === 'status') {
            result.sort((a, b) => (a.status || '').localeCompare(b.status || ''));
        }

        return result;
    }, [requests, search, filterStatus, dateFrom, dateTo, sortBy]);

    // Stats
    const completed = requests.filter(r => ['RETURNED', 'COMPLETED'].includes(r.status)).length;
    const rejected  = requests.filter(r => r.status === 'REJECTED').length;
    const cancelled = requests.filter(r => r.status === 'CANCELLED').length;
    const total     = requests.length;

    return (
        <DashboardLayout pageTitle="Request History" pageSubtitle="Complete history of all your equipment requests">
            {error && <div className="alert alert-danger" style={{ marginBottom: 16 }}>{error}</div>}

            {/* Summary */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))', gap: 16, marginBottom: 24 }}>
                {[
                    { label: 'Total Requests', value: total,     color: 'var(--primary)' },
                    { label: 'Completed',       value: completed, color: 'var(--primary-light)' },
                    { label: 'Rejected',        value: rejected,  color: 'var(--secondary)' },
                    { label: 'Cancelled',       value: cancelled, color: 'var(--muted)' },
                ].map(s => (
                    <div key={s.label} className="content-card" style={{ padding: 16 }}>
                        <div style={{ fontSize: 28, fontWeight: 800, color: s.color }}>{loading ? '—' : s.value}</div>
                        <div style={{ fontSize: 12, color: 'var(--secondary)', marginTop: 4 }}>{s.label}</div>
                    </div>
                ))}
            </div>

            {/* Toolbar */}
            <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', marginBottom: 16, alignItems: 'center' }}>
                <div style={{ position: 'relative', flex: 1, minWidth: 180 }}>
                    <HiOutlineSearch style={{ position: 'absolute', left: 10, top: '50%', transform: 'translateY(-50%)', color: 'var(--secondary)' }} />
                    <input className="form-input" style={{ paddingLeft: 32 }}
                        placeholder="Search by ID, equipment, purpose…"
                        value={search} onChange={(e) => setSearch(e.target.value)} />
                </div>
                <select className="form-input" style={{ width: 170 }}
                    value={filterStatus} onChange={(e) => setFilter(e.target.value)}>
                    <option value="ALL">All Statuses</option>
                    {Object.entries(STATUS_MAP).map(([key, val]) => (
                        <option key={key} value={key}>{val.label}</option>
                    ))}
                </select>
                <select className="form-input" style={{ width: 140 }}
                    value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
                    <option value="newest">Newest First</option>
                    <option value="oldest">Oldest First</option>
                    <option value="status">By Status</option>
                </select>
                <button className="btn btn-outline btn-sm" onClick={load}
                    style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <HiOutlineRefresh /> Refresh
                </button>
            </div>

            {/* Date range filters */}
            <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', marginBottom: 16, alignItems: 'center' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 13, color: 'var(--secondary)' }}>
                    <HiOutlineCalendar /> From:
                </div>
                <input type="date" className="form-input" style={{ width: 160 }}
                    value={dateFrom} onChange={(e) => setDateFrom(e.target.value)} />
                <div style={{ fontSize: 13, color: 'var(--secondary)' }}>To:</div>
                <input type="date" className="form-input" style={{ width: 160 }}
                    value={dateTo} onChange={(e) => setDateTo(e.target.value)} />
                {(dateFrom || dateTo) && (
                    <button className="btn btn-outline btn-sm" onClick={() => { setDateFrom(''); setDateTo(''); }}>
                        Clear Dates
                    </button>
                )}
            </div>

            {/* Table */}
            <div className="content-card">
                <div className="content-card-header">
                    <h2 className="content-card-title">Request History</h2>
                    <span style={{ fontSize: 13, color: 'var(--secondary)' }}>
                        {!loading && `${filtered.length} of ${total} requests`}
                    </span>
                </div>
                <div className="table-container">
                    <table className="table">
                        <thead>
                            <tr>
                                <th>Request ID</th>
                                <th>Equipment</th>
                                <th>Type</th>
                                <th>Borrow Date</th>
                                <th>Return Date</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                [...Array(5)].map((_, i) => (
                                    <tr key={i}>{[120, 160, 80, 90, 90, 80, 60].map((w, j) => (
                                        <td key={j}><div className="skeleton" style={{ width: w, height: 16 }} /></td>
                                    ))}</tr>
                                ))
                            ) : filtered.length > 0 ? (
                                filtered.map(r => {
                                    const sc = STATUS_MAP[r.status] || { badge: 'badge-muted', label: r.status };
                                    return (
                                        <tr key={r.requestId || r.id}>
                                            <td style={{ fontWeight: 600, fontSize: 13 }}>{r.requestId || r.id}</td>
                                            <td>{r.equipmentName || r.items?.[0]?.equipmentName || '—'}</td>
                                            <td>
                                                <span style={{
                                                    padding: '2px 8px', borderRadius: 4, fontSize: 11,
                                                    background: 'rgba(61, 82, 160, 0.06)', color: 'var(--primary)',
                                                    fontWeight: 600,
                                                }}>
                                                    {r.requestType || '—'}
                                                </span>
                                            </td>
                                            <td style={{ fontSize: 12, color: 'var(--secondary)' }}>
                                                {r.borrowDate ? new Date(r.borrowDate).toLocaleDateString() : (r.fromDateTime ? new Date(r.fromDateTime).toLocaleDateString() : '—')}
                                            </td>
                                            <td style={{ fontSize: 12, color: 'var(--secondary)' }}>
                                                {r.returnDate ? new Date(r.returnDate).toLocaleDateString() : (r.toDateTime ? new Date(r.toDateTime).toLocaleDateString() : '—')}
                                            </td>
                                            <td><span className={`badge ${sc.badge}`}>{sc.label}</span></td>
                                            <td>
                                                <button className="btn btn-outline btn-sm" onClick={() => setViewReq(r)} title="View Details">
                                                    <HiOutlineEye />
                                                </button>
                                            </td>
                                        </tr>
                                    );
                                })
                            ) : (
                                <tr>
                                    <td colSpan={7} style={{ textAlign: 'center', padding: 48, color: 'var(--secondary)' }}>
                                        <HiOutlineClipboardList style={{ fontSize: 32, display: 'block', margin: '0 auto 8px' }} />
                                        {search || filterStatus !== 'ALL' || dateFrom || dateTo
                                            ? 'No requests match your filters'
                                            : 'No request history yet'
                                        }
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* View Detail Modal */}
            {viewReq && (
                <div className="modal-overlay" onClick={() => setViewReq(null)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 500 }}>
                        <div className="modal-header">
                            <h2>Request Details</h2>
                            <button className="modal-close" onClick={() => setViewReq(null)}>✕</button>
                        </div>
                        <div className="modal-body">
                            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                                {[
                                    { label: 'Request ID',   value: viewReq.requestId || viewReq.id },
                                    { label: 'Equipment',    value: viewReq.equipmentName || viewReq.items?.[0]?.equipmentName || '—' },
                                    { label: 'Request Type', value: viewReq.requestType || '—' },
                                    { label: 'Status',       value: STATUS_MAP[viewReq.status]?.label || viewReq.status },
                                    { label: 'Borrow Date',  value: viewReq.borrowDate ? new Date(viewReq.borrowDate).toLocaleDateString() : '—' },
                                    { label: 'Return Date',  value: viewReq.returnDate ? new Date(viewReq.returnDate).toLocaleDateString() : '—' },
                                    { label: 'Purpose',      value: viewReq.purpose || viewReq.description || '—' },
                                    { label: 'Notes',        value: viewReq.notes || '—' },
                                    { label: 'Created',      value: viewReq.createdAt ? new Date(viewReq.createdAt).toLocaleString() : '—' },
                                    { label: 'Last Updated', value: viewReq.updatedAt ? new Date(viewReq.updatedAt).toLocaleString() : '—' },
                                ].map(({ label, value }) => (
                                    <div key={label} style={{
                                        display: 'flex', justifyContent: 'space-between',
                                        padding: '8px 12px', background: 'var(--bg-light)',
                                        borderRadius: 'var(--radius-sm)',
                                    }}>
                                        <span style={{ color: 'var(--secondary)', fontSize: 13 }}>{label}</span>
                                        <span style={{ fontWeight: 600, fontSize: 13, textAlign: 'right', maxWidth: '60%' }}>{value ?? '—'}</span>
                                    </div>
                                ))}
                            </div>

                            {/* Timeline / status notes */}
                            {viewReq.rejectionReason && (
                                <div style={{ marginTop: 16, padding: 12, background: 'rgba(61, 82, 160, 0.05)', borderRadius: 'var(--radius-sm)', border: '1px solid rgba(61, 82, 160, 0.1)' }}>
                                    <div style={{ fontSize: 12, color: 'var(--primary)', fontWeight: 600, marginBottom: 4 }}>Rejection Reason</div>
                                    <div style={{ fontSize: 13 }}>{viewReq.rejectionReason}</div>
                                </div>
                            )}
                        </div>
                        <div className="modal-footer">
                            <button className="btn btn-outline" onClick={() => setViewReq(null)}>Close</button>
                        </div>
                    </div>
                </div>
            )}
        </DashboardLayout>
    );
}
