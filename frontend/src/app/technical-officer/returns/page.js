'use client';

import { useState } from 'react';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { inspectionAPI, requestAPI } from '@/lib/api';
import { HiOutlineCheckCircle } from 'react-icons/hi';

export default function ReturnPage() {
    const [requestId, setRequestId]   = useState('');
    const [request, setRequest]       = useState(null);
    const [items, setItems]           = useState([]);
    const [loading, setLoading]       = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError]           = useState(null);
    const [success, setSuccess]       = useState(null);

    const loadRequest = async () => {
        if (!requestId.trim()) return;
        setLoading(true); setError(null); setRequest(null);
        try {
            const res  = await requestAPI.getRequestById(requestId.trim());
            const data = res.data?.data || res.data;
            if (data.status !== 'INUSE' && data.status !== 'IN_USE') {
                setError(`Request status is ${data.status}. Only IN_USE requests can be returned.`);
                setLoading(false); return;
            }
            setRequest(data);
            const equips = data?.items || data?.equipmentItems || [];
            setItems(equips.map(eq => ({
                equipmentId:   eq.equipmentId || eq.id,
                equipmentName: eq.equipmentName || eq.name || 'Equipment',
                conditionAfter: 'GOOD',
                damageLevel:    0,
                notes:          '',
            })));
        } catch (e) {
            setError('Request not found or access denied.');
        } finally { setLoading(false); }
    };

    const updateItem = (idx, field, val) => {
        setItems(prev => prev.map((it, i) => i === idx ? { ...it, [field]: val } : it));
    };

    const handleSubmit = async () => {
        if (!request || items.length === 0) return;
        setSubmitting(true); setError(null);
        try {
            await inspectionAPI.processReturn({ requestId: request.requestId || request.id, items });
            setSuccess(`Return processed for ${request.requestId || request.id}`);
            setRequest(null); setItems([]); setRequestId('');
        } catch (e) {
            setError(e.response?.data?.message || 'Return processing failed.');
        } finally { setSubmitting(false); }
    };

    const conditionOptions = ['EXCELLENT', 'GOOD', 'FAIR', 'POOR', 'DAMAGED'];

    return (
        <DashboardLayout pageTitle="Process Return" pageSubtitle="Post-return inspection — record equipment condition">
            {error   && <div className="alert alert-danger"  style={{ marginBottom: 16 }}>{error}</div>}
            {success && <div className="alert alert-success" style={{ marginBottom: 16 }}>{success}</div>}

            <div className="content-card" style={{ marginBottom: 20 }}>
                <div className="content-card-header"><h2 className="content-card-title">Load Request</h2></div>
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
                <div className="content-card">
                    <div className="content-card-header"><h2 className="content-card-title">Post-Return Inspection</h2></div>
                    <div style={{ padding: '0 20px 20px' }}>
                        {items.map((item, idx) => (
                            <div key={idx} style={{ marginBottom: 16, padding: 16, border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)' }}>
                                <div style={{ fontWeight: 600, marginBottom: 10 }}>{item.equipmentName}</div>
                                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))', gap: 12 }}>
                                    <div>
                                        <label className="form-label">Condition After</label>
                                        <select className="form-input" value={item.conditionAfter}
                                            onChange={e => updateItem(idx, 'conditionAfter', e.target.value)}>
                                            {conditionOptions.map(o => <option key={o}>{o}</option>)}
                                        </select>
                                    </div>
                                    <div>
                                        <label className="form-label">Damage Level (0–5)</label>
                                        <input type="number" min={0} max={5} className="form-input"
                                            value={item.damageLevel}
                                            onChange={e => updateItem(idx, 'damageLevel', Number(e.target.value))} />
                                    </div>
                                    <div>
                                        <label className="form-label">Notes</label>
                                        <input className="form-input" placeholder="Notes…"
                                            value={item.notes} onChange={e => updateItem(idx, 'notes', e.target.value)} />
                                    </div>
                                </div>
                            </div>
                        ))}
                        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 8 }}>
                            <button className="btn" onClick={handleSubmit} disabled={submitting}
                                style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                <HiOutlineCheckCircle />
                                {submitting ? 'Processing…' : 'Confirm Return'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </DashboardLayout>
    );
}
