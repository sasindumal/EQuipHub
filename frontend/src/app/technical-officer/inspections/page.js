'use client';

import { useState, useEffect } from 'react';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { inspectionAPI } from '@/lib/api';
import {
    HiOutlineEye,
    HiOutlineRefresh,
    HiOutlineSearch,
    HiOutlineClipboardList,
    HiOutlineCheckCircle,
} from 'react-icons/hi';

const CONDITION_COLOR = {
    EXCELLENT: { badge: 'badge-success', label: 'Excellent' },
    GOOD:      { badge: 'badge-success', label: 'Good'      },
    FAIR:      { badge: 'badge-warning', label: 'Fair'      },
    POOR:      { badge: 'badge-danger',  label: 'Poor'      },
    DAMAGED:   { badge: 'badge-danger',  label: 'Damaged'   },
};

const TYPE_MAP = {
    PRE_ISSUE:   { badge: 'badge-primary', label: 'Pre-Issue'   },
    POST_RETURN: { badge: 'badge-warning', label: 'Post-Return' },
    ISSUE:       { badge: 'badge-primary', label: 'Issue'       },
    RETURN:      { badge: 'badge-warning', label: 'Return'      },
};

export default function TOInspectionsPage() {
    const [inspections, setInspections] = useState([]);
    const [unack, setUnack]            = useState([]);
    const [stats, setStats]            = useState(null);
    const [loading, setLoading]        = useState(true);
    const [error, setError]            = useState(null);
    const [success, setSuccess]        = useState(null);
    const [search, setSearch]          = useState('');
    const [filterType, setFilterType]  = useState('ALL');
    const [viewInsp, setViewInsp]      = useState(null);

    useEffect(() => { load(); }, []);

    const load = async () => {
        setLoading(true); setError(null);
        try {
            const [iRes, uRes, sRes] = await Promise.allSettled([
                inspectionAPI.getMyInspections(),
                inspectionAPI.getUnacknowledged(),
                inspectionAPI.getMyDeptStats(),
            ]);
            const iData = iRes.status === 'fulfilled' ? (iRes.value.data?.data?.inspections || iRes.value.data?.data || iRes.value.data || []) : [];
            const uData = uRes.status === 'fulfilled' ? (uRes.value.data?.data?.inspections || uRes.value.data?.data || uRes.value.data || []) : [];
            const sData = sRes.status === 'fulfilled' ? (sRes.value.data?.data || sRes.value.data) : null;
            setInspections(Array.isArray(iData) ? iData : []);
            setUnack(Array.isArray(uData) ? uData : []);
            setStats(sData);
        } catch (e) {
            setError('Failed to load inspections.');
        } finally { setLoading(false); }
    };

    const flash = (msg, isError = false) => {
        isError ? setError(msg) : setSuccess(msg);
        setTimeout(() => isError ? setError(null) : setSuccess(null), 4000);
    };

    const filtered = inspections.filter((i) => {
        const txt = `${i.inspectionId || ''} ${i.equipmentName || ''} ${i.studentName || ''}`.toLowerCase();
        const matchSearch = !search || txt.includes(search.toLowerCase());
        const matchType = filterType === 'ALL' || i.inspectionType === filterType || i.type === filterType;
        return matchSearch && matchType;
    });

    const totalInsp  = inspections.length;
    const unackCount = unack.length;

    return (
        <DashboardLayout pageTitle="Inspections" pageSubtitle="Equipment inspection history and damage reports">
            {error   && <div className="alert alert-danger"  style={{ marginBottom: 16 }}>{error}</div>}
            {success && <div className="alert alert-success" style={{ marginBottom: 16 }}>{success}</div>}

            {/* Summary stats */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: 16, marginBottom: 24 }}>
                {[
                    { label: 'Total Inspections', value: totalInsp,  color: 'var(--primary)' },
                    { label: 'Unacknowledged',     value: unackCount, color: unackCount > 0 ? 'var(--primary)' : 'var(--primary-light)' },
                    ...(stats ? [
                        { label: 'This Month', value: stats.thisMonth ?? stats.monthlyCount ?? '—', color: 'var(--secondary)' },
                    ] : []),
                ].map(s => (
                    <div key={s.label} className="content-card" style={{ padding: 20 }}>
                        <div style={{ fontSize: 32, fontWeight: 800, color: s.color }}>{loading ? '—' : s.value}</div>
                        <div style={{ fontSize: 13, color: 'var(--secondary)', marginTop: 4 }}>{s.label}</div>
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
                        placeholder="Search inspections…"
                        value={search} onChange={(e) => setSearch(e.target.value)} />
                </div>
                <select className="form-input" style={{ width: 160 }}
                    value={filterType} onChange={(e) => setFilterType(e.target.value)}>
                    <option value="ALL">All Types</option>
                    <option value="PRE_ISSUE">Pre-Issue</option>
                    <option value="POST_RETURN">Post-Return</option>
                    <option value="ISSUE">Issue</option>
                    <option value="RETURN">Return</option>
                </select>
                <button className="btn btn-outline btn-sm" onClick={load}
                    style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <HiOutlineRefresh /> Refresh
                </button>
            </div>

            {/* Unacknowledged damage banner */}
            {unackCount > 0 && (
                <div style={{
                    padding: 16, marginBottom: 20,
                    background: 'rgba(61, 82, 160, 0.06)',
                    border: '1px solid rgba(61, 82, 160, 0.15)',
                    borderRadius: 'var(--radius-sm)',
                    display: 'flex', alignItems: 'center', gap: 12,
                }}>
                    <div style={{ fontSize: 24, color: 'var(--primary)' }}>⚠</div>
                    <div>
                        <div style={{ fontWeight: 600, color: 'var(--primary)' }}>
                            {unackCount} unacknowledged damage inspection(s)
                        </div>
                        <div style={{ fontSize: 13, color: 'var(--secondary)', marginTop: 2 }}>
                            Students have not yet acknowledged the damage report for these inspections.
                        </div>
                    </div>
                </div>
            )}

            {/* Inspections Table */}
            <div className="content-card">
                <div className="content-card-header">
                    <h2 className="content-card-title">Inspection Records</h2>
                    <span style={{ fontSize: 13, color: 'var(--secondary)' }}>
                        {!loading && `${filtered.length} inspections`}
                    </span>
                </div>
                <div className="table-container">
                    <table className="table">
                        <thead>
                            <tr>
                                <th>Inspection ID</th>
                                <th>Equipment</th>
                                <th>Type</th>
                                <th>Condition</th>
                                <th>Student</th>
                                <th>Date</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                [...Array(5)].map((_, i) => (
                                    <tr key={i}>{[120, 160, 90, 80, 140, 90, 60].map((w, j) => (
                                        <td key={j}><div className="skeleton" style={{ width: w, height: 16 }} /></td>
                                    ))}</tr>
                                ))
                            ) : filtered.length > 0 ? (
                                filtered.map(insp => {
                                    const cond = insp.conditionBefore || insp.conditionAfter || insp.condition;
                                    const cc = CONDITION_COLOR[cond] || { badge: 'badge-muted', label: cond || '—' };
                                    const tc = TYPE_MAP[insp.inspectionType || insp.type] || { badge: 'badge-muted', label: insp.inspectionType || insp.type || '—' };
                                    return (
                                        <tr key={insp.inspectionId || insp.id}>
                                            <td style={{ fontWeight: 600, fontSize: 13 }}>{insp.inspectionId || insp.id}</td>
                                            <td>{insp.equipmentName || '—'}</td>
                                            <td><span className={`badge ${tc.badge}`}>{tc.label}</span></td>
                                            <td><span className={`badge ${cc.badge}`}>{cc.label}</span></td>
                                            <td>{insp.studentName || insp.borrowerName || '—'}</td>
                                            <td style={{ fontSize: 12, color: 'var(--secondary)' }}>
                                                {insp.createdAt ? new Date(insp.createdAt).toLocaleDateString() : (insp.inspectionDate ? new Date(insp.inspectionDate).toLocaleDateString() : '—')}
                                            </td>
                                            <td>
                                                <button className="btn btn-outline btn-sm"
                                                    onClick={() => setViewInsp(insp)} title="View Details">
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
                                        {search || filterType !== 'ALL'
                                            ? 'No inspections match your filters'
                                            : 'No inspections recorded yet'}
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* View Detail Modal */}
            {viewInsp && (
                <div className="modal-overlay" onClick={() => setViewInsp(null)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 500 }}>
                        <div className="modal-header">
                            <h2>Inspection Details</h2>
                            <button className="modal-close" onClick={() => setViewInsp(null)}>✕</button>
                        </div>
                        <div className="modal-body">
                            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                                {[
                                    { label: 'Inspection ID',    value: viewInsp.inspectionId || viewInsp.id },
                                    { label: 'Request ID',       value: viewInsp.requestId || '—' },
                                    { label: 'Equipment',        value: viewInsp.equipmentName || '—' },
                                    { label: 'Type',             value: (TYPE_MAP[viewInsp.inspectionType || viewInsp.type]?.label) || viewInsp.inspectionType || viewInsp.type || '—' },
                                    { label: 'Condition Before', value: viewInsp.conditionBefore || '—' },
                                    { label: 'Condition After',  value: viewInsp.conditionAfter || '—' },
                                    { label: 'Damage Level',     value: viewInsp.damageLevel ?? '—' },
                                    { label: 'Student',          value: viewInsp.studentName || viewInsp.borrowerName || '—' },
                                    { label: 'Inspector',        value: viewInsp.inspectorName || '—' },
                                    { label: 'Notes',            value: viewInsp.notes || '—' },
                                    { label: 'Acknowledged',     value: viewInsp.acknowledged ? 'Yes' : 'No' },
                                    { label: 'Date',             value: viewInsp.createdAt ? new Date(viewInsp.createdAt).toLocaleString() : '—' },
                                ].map(({ label, value }) => (
                                    <div key={label} style={{
                                        display: 'flex', justifyContent: 'space-between',
                                        padding: '8px 12px', background: 'var(--bg-light)',
                                        borderRadius: 'var(--radius-sm)',
                                    }}>
                                        <span style={{ color: 'var(--secondary)', fontSize: 13 }}>{label}</span>
                                        <span style={{ fontWeight: 600, fontSize: 13, textAlign: 'right', maxWidth: '55%' }}>{value ?? '—'}</span>
                                    </div>
                                ))}
                            </div>
                        </div>
                        <div className="modal-footer">
                            <button className="btn btn-outline" onClick={() => setViewInsp(null)}>Close</button>
                        </div>
                    </div>
                </div>
            )}
        </DashboardLayout>
    );
}
