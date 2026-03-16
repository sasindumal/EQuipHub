'use client';

import { useState, useEffect } from 'react';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { penaltyAPI } from '@/lib/api';
import {
    HiOutlineExclamationCircle,
    HiOutlineRefresh,
    HiOutlineEye,
} from 'react-icons/hi';

const TYPE_MAP = {
    LATE_RETURN:  { badge: 'badge-warning', label: 'Late Return'  },
    DAMAGE:       { badge: 'badge-danger',  label: 'Damage'       },
    LOSS:         { badge: 'badge-danger',  label: 'Loss'         },
    LAB_OVERRIDE: { badge: 'badge-warning', label: 'Lab Override' },
};

const STATUS_MAP = {
    PENDING:  { badge: 'badge-warning', label: 'Pending Approval' },
    APPROVED: { badge: 'badge-danger',  label: 'Active'           },
    WAIVED:   { badge: 'badge-success', label: 'Waived'           },
    APPEALED: { badge: 'badge-muted',   label: 'Appealed'         },
};

export default function StudentPenaltiesPage() {
    const [penalties, setPenalties] = useState([]);
    const [summary, setSummary]     = useState(null);
    const [loading, setLoading]     = useState(true);
    const [error, setError]         = useState(null);
    const [success, setSuccess]     = useState(null);
    const [viewPenalty, setView]    = useState(null);
    const [appealModal, setAppeal]  = useState(null);
    const [appealText, setAppealText] = useState('');
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => { load(); }, []);

    const load = async () => {
        setLoading(true); setError(null);
        try {
            const [pRes, sRes] = await Promise.allSettled([
                penaltyAPI.getMyPenalties(),
                penaltyAPI.getMySummary(),
            ]);
            setPenalties(pRes.status === 'fulfilled' ? (pRes.value.data || []) : []);
            setSummary(sRes.status === 'fulfilled'  ? (sRes.value.data)        : null);
        } catch (e) { setError('Failed to load penalties.'); }
        finally { setLoading(false); }
    };

    const flash = (msg, isError = false) => {
        isError ? setError(msg) : setSuccess(msg);
        setTimeout(() => isError ? setError(null) : setSuccess(null), 4000);
    };

    const handleAppeal = async () => {
        if (!appealModal || !appealText.trim()) return;
        setSubmitting(true);
        try {
            await penaltyAPI.submitAppeal({ penaltyId: appealModal, reason: appealText });
            flash('Appeal submitted successfully');
            setAppeal(null); setAppealText(''); load();
        } catch (e) { flash(e.response?.data?.message || 'Appeal submission failed', true); }
        finally { setSubmitting(false); }
    };

    const totalPoints = penalties.filter(p => p.status === 'APPROVED').reduce((acc, p) => acc + (p.points || 0), 0);

    return (
        <DashboardLayout pageTitle="My Penalties" pageSubtitle="Your penalty history and borrowing eligibility">
            {error   && <div className="alert alert-danger"  style={{ marginBottom: 16 }}>{error}</div>}
            {success && <div className="alert alert-success" style={{ marginBottom: 16 }}>{success}</div>}

            {/* Summary card */}
            {summary && (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: 16, marginBottom: 24 }}>
                    {[
                        { label: 'Total Active Points', value: totalPoints,                    color: totalPoints > 0 ? 'var(--primary)' : 'var(--primary-light)' },
                        { label: 'Penalty Level',       value: summary.penaltyLevel || 'NONE', color: 'var(--secondary)'         },
                        { label: 'Can Borrow',          value: summary.canBorrow ? 'Yes' : 'No', color: summary.canBorrow ? 'var(--primary-light)' : 'var(--primary)' },
                    ].map(s => (
                        <div key={s.label} className="content-card" style={{ padding: 20 }}>
                            <div style={{ fontSize: 28, fontWeight: 800, color: s.color }}>{s.value}</div>
                            <div style={{ fontSize: 13, color: 'var(--secondary)', marginTop: 4 }}>{s.label}</div>
                        </div>
                    ))}
                </div>
            )}

            <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 12 }}>
                <button className="btn btn-outline btn-sm" onClick={load} style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <HiOutlineRefresh /> Refresh
                </button>
            </div>

            <div className="content-card">
                <div className="content-card-header">
                    <h2 className="content-card-title">Penalty Records</h2>
                    <span style={{ fontSize: 13, color: 'var(--secondary)' }}>{!loading && `${penalties.length} records`}</span>
                </div>
                <div className="table-container">
                    <table className="table">
                        <thead>
                            <tr><th>Type</th><th>Points</th><th>Reason</th><th>Issued</th><th>Status</th><th>Actions</th></tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                [...Array(3)].map((_,i) => <tr key={i}>{[100,50,180,90,80,100].map((w,j)=><td key={j}><div className="skeleton" style={{width:w,height:16}}/></td>)}</tr>)
                            ) : penalties.length > 0 ? (
                                penalties.map(p => {
                                    const tc = TYPE_MAP[p.penaltyType]  || { badge: 'badge-muted', label: p.penaltyType };
                                    const sc = STATUS_MAP[p.status]     || { badge: 'badge-muted', label: p.status     };
                                    const canAppeal = p.status === 'APPROVED';
                                    return (
                                        <tr key={p.penaltyId || p.id}>
                                            <td><span className={`badge ${tc.badge}`}>{tc.label}</span></td>
                                            <td style={{ fontWeight: 700, color: 'var(--primary)' }}>{p.points}</td>
                                            <td style={{ fontSize: 13, maxWidth: 220 }}>{p.reason || '—'}</td>
                                            <td style={{ fontSize: 12, color: 'var(--secondary)' }}>
                                                {p.createdAt ? new Date(p.createdAt).toLocaleDateString() : '—'}
                                            </td>
                                            <td><span className={`badge ${sc.badge}`}>{sc.label}</span></td>
                                            <td>
                                                <div style={{ display: 'flex', gap: 6 }}>
                                                    <button className="btn btn-outline btn-sm" onClick={() => setView(p)} title="View">
                                                        <HiOutlineEye />
                                                    </button>
                                                    {canAppeal && (
                                                        <button
                                                            className="btn btn-approve btn-sm"
                                                            style={{ fontSize: 12 }}
                                                            onClick={() => { setAppeal(p.penaltyId || p.id); setAppealText(''); }}
                                                        >
                                                            Appeal
                                                        </button>
                                                    )}
                                                </div>
                                            </td>
                                        </tr>
                                    );
                                })
                            ) : (
                                <tr><td colSpan={6} style={{ textAlign: 'center', padding: 48, color: 'var(--secondary)' }}>
                                    <HiOutlineExclamationCircle style={{ fontSize: 32, display: 'block', margin: '0 auto 8px', color: 'var(--primary-light)' }} />
                                    No penalties on your record!
                                </td></tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* View Modal */}
            {viewPenalty && (
                <div className="modal-overlay">
                    <div className="modal-content" style={{ padding: 28, maxWidth: 440 }}>
                        <h3 style={{ margin: '0 0 18px', fontWeight: 700 }}>Penalty Details</h3>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                            {[
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

            {/* Appeal Modal */}
            {appealModal && (
                <div className="modal-overlay">
                    <div className="modal-content" style={{ padding: 28, maxWidth: 420 }}>
                        <h3 style={{ margin: '0 0 8px', fontWeight: 700 }}>Submit Appeal</h3>
                        <p style={{ color: 'var(--secondary)', fontSize: 14, marginBottom: 16 }}>Explain why you believe this penalty should be waived.</p>
                        <textarea className="form-input" rows={4} placeholder="Your appeal reason…"
                            value={appealText} onChange={e => setAppealText(e.target.value)} />
                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10, marginTop: 16 }}>
                            <button className="btn btn-outline" onClick={() => setAppeal(null)}>Cancel</button>
                            <button className="btn" onClick={handleAppeal} disabled={submitting || !appealText.trim()}>
                                {submitting ? 'Submitting…' : 'Submit Appeal'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </DashboardLayout>
    );
}
