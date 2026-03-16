'use client';

import { useState, useEffect, Suspense } from 'react';
import { useSearchParams } from 'next/navigation';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { inspectionAPI, requestAPI } from '@/lib/api';
import { HiOutlineCheckCircle, HiOutlineRefresh } from 'react-icons/hi';

function IssuePageInner() {
    const params       = useSearchParams();
    const prefill      = params.get('requestId') || '';
    const [requestId, setRequestId] = useState(prefill);
    const [request, setRequest]     = useState(null);
    const [items, setItems]         = useState([]);
    const [loading, setLoading]     = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError]         = useState(null);
    const [success, setSuccess]     = useState(null);

    const loadRequest = async () => {
        if (!requestId.trim()) return;
        setLoading(true); setError(null); setRequest(null);
        try {
            const res = await requestAPI.getRequestById(requestId.trim());
            const data = res.data?.data || res.data;
            setRequest(data);
            const equips = data?.items || data?.equipmentItems || [];
            setItems(equips.map(eq => ({
                equipmentId:    eq.equipmentId || eq.id,
                equipmentName:  eq.equipmentName || eq.name || 'Equipment',
                conditionBefore: 'GOOD',
                notes:          '',
            })));
        } catch (e) {
            setError('Request not found or access denied.');
        } finally { setLoading(false); }
    };

    useEffect(() => { if (prefill) loadRequest(); }, []);

    const updateItem = (idx, field, val) => {
        setItems(prev => prev.map((it, i) => i === idx ? { ...it, [field]: val } : it));
    };

    const handleSubmit = async () => {
        if (!request || items.length === 0) return;
        setSubmitting(true); setError(null);
        try {
            await inspectionAPI.issueEquipment({ requestId: request.requestId || request.id, items });
            setSuccess(`Equipment issued successfully for request ${request.requestId || request.id}`);
            setRequest(null); setItems([]); setRequestId('');
        } catch (e) {
            setError(e.response?.data?.message || 'Issue failed. Check backend.');
        } finally { setSubmitting(false); }
    };

    const conditionOptions = ['EXCELLENT', 'GOOD', 'FAIR', 'POOR'];

    return (
        <DashboardLayout pageTitle="Issue Equipment" pageSubtitle="Pre-issuance inspection & handover to student">
            {error   && <div className="alert alert-danger"  style={{ marginBottom: 16 }}>{error}</div>}
            {success && <div className="alert alert-success" style={{ marginBottom: 16 }}>{success}</div>}

            <div className="content-card" style={{ marginBottom: 20 }}>
                <div className="content-card-header">
                    <h2 className="content-card-title">Load Request</h2>
                </div>
                <div style={{ padding: '0 20px 20px', display: 'flex', gap: 10, alignItems: 'flex-end' }}>
                    <div style={{ flex: 1 }}>
                        <label className="form-label">Request ID</label>
                        <input className="form-input" placeholder="e.g. REQ-2026-00001"
                            value={requestId} onChange={e => setRequestId(e.target.value)}
                            onKeyDown={e => e.key === 'Enter' && loadRequest()} />
                    </div>
                    <button className="btn" onClick={loadRequest} disabled={loading}>
                        {loading ? 'Loading…' : 'Load'}
                    </button>
                </div>
            </div>

            {request && (
                <>
                    <div className="content-card" style={{ marginBottom: 20 }}>
                        <div className="content-card-header"><h2 className="content-card-title">Request Info</h2></div>
                        <div style={{ padding: '0 20px 20px', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: 12 }}>
                            {[
                                { label: 'Request ID', value: request.requestId || request.id },
                                { label: 'Student',    value: request.studentName || request.requesterName || '—' },
                                { label: 'Status',     value: request.status },
                                { label: 'Return By',  value: request.returnDate ? new Date(request.returnDate).toLocaleDateString() : '—' },
                            ].map(({ label, value }) => (
                                <div key={label} style={{ padding: '10px 12px', background: 'var(--bg-light)', borderRadius: 'var(--radius-sm)' }}>
                                    <div style={{ fontSize: 11, color: 'var(--secondary)', marginBottom: 4 }}>{label}</div>
                                    <div style={{ fontWeight: 600, fontSize: 13 }}>{value}</div>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div className="content-card">
                        <div className="content-card-header"><h2 className="content-card-title">Pre-Issuance Inspection</h2></div>
                        <div style={{ padding: '0 20px 20px' }}>
                            {items.length === 0 ? (
                                <p style={{ color: 'var(--secondary)', fontSize: 14 }}>No equipment items found in this request.</p>
                            ) : (
                                <>
                                    {items.map((item, idx) => (
                                        <div key={idx} style={{ marginBottom: 16, padding: 16, border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)' }}>
                                            <div style={{ fontWeight: 600, marginBottom: 10 }}>{item.equipmentName}</div>
                                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: 12 }}>
                                                <div>
                                                    <label className="form-label">Condition Before</label>
                                                    <select className="form-input" value={item.conditionBefore}
                                                        onChange={e => updateItem(idx, 'conditionBefore', e.target.value)}>
                                                        {conditionOptions.map(o => <option key={o}>{o}</option>)}
                                                    </select>
                                                </div>
                                                <div>
                                                    <label className="form-label">Notes</label>
                                                    <input className="form-input" placeholder="Inspection notes…"
                                                        value={item.notes} onChange={e => updateItem(idx, 'notes', e.target.value)} />
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                    <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 8 }}>
                                        <button className="btn" onClick={handleSubmit} disabled={submitting}
                                            style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                            <HiOutlineCheckCircle />
                                            {submitting ? 'Issuing…' : 'Confirm Issue to Student'}
                                        </button>
                                    </div>
                                </>
                            )}
                        </div>
                    </div>
                </>
            )}
        </DashboardLayout>
    );
}

export default function IssuePage() {
    return <Suspense fallback={<div>Loading…</div>}><IssuePageInner /></Suspense>;
}
