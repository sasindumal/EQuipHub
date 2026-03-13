'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { requestAPI, equipmentAPI } from '@/lib/api';
import { useAuth } from '@/lib/auth';
import { HiOutlinePlusCircle, HiOutlineTrash } from 'react-icons/hi';

export default function NewRequestPage() {
    const router = useRouter();
    const { user } = useAuth();
    const [equipment, setEquipment] = useState([]);
    const [loadingEq, setLoadingEq] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError]   = useState(null);
    const [success, setSuccess] = useState(null);

    const [form, setForm] = useState({
        requestType: 'COURSEWORK',
        purpose:     '',
        borrowDate:  '',
        returnDate:  '',
        notes:       '',
        items:       [{ equipmentId: '', quantity: 1, notes: '' }],
    });

    useEffect(() => {
        equipmentAPI.getAllEquipment()
            .then(res => setEquipment(res.data?.data || res.data || []))
            .catch(() => {})
            .finally(() => setLoadingEq(false));
    }, []);

    const setField = (key, val) => setForm(f => ({ ...f, [key]: val }));
    const setItem  = (idx, key, val) => setForm(f => ({
        ...f,
        items: f.items.map((it, i) => i === idx ? { ...it, [key]: val } : it),
    }));
    const addItem    = () => setForm(f => ({ ...f, items: [...f.items, { equipmentId: '', quantity: 1, notes: '' }] }));
    const removeItem = (idx) => setForm(f => ({ ...f, items: f.items.filter((_, i) => i !== idx) }));

    const handleSubmit = async () => {
        if (!form.purpose.trim() || !form.borrowDate || !form.returnDate) {
            setError('Please fill in purpose, borrow date, and return date.'); return;
        }
        if (form.items.some(it => !it.equipmentId)) {
            setError('Please select equipment for all items.'); return;
        }
        setSubmitting(true); setError(null);
        try {
            const payload = {
                ...form,
                studentId:    user.userId || user.id,
                departmentId: user.departmentId,
            };
            const res    = await requestAPI.createRequest(payload);
            const newReq = res.data?.data || res.data;
            const newId  = newReq?.requestId || newReq?.id;
            // Auto-submit draft
            if (newId) await requestAPI.submitRequest(newId);
            setSuccess('Request submitted successfully!');
            setTimeout(() => router.push('/student/requests'), 1500);
        } catch (e) {
            setError(e.response?.data?.message || 'Submission failed.');
        } finally { setSubmitting(false); }
    };

    return (
        <DashboardLayout pageTitle="New Borrow Request" pageSubtitle="Fill in the details to borrow equipment">
            {error   && <div className="alert alert-danger"  style={{ marginBottom: 16 }}>{error}</div>}
            {success && <div className="alert alert-success" style={{ marginBottom: 16 }}>{success}</div>}

            <div className="content-card">
                <div className="content-card-header"><h2 className="content-card-title">Request Details</h2></div>
                <div style={{ padding: '0 20px 24px', display: 'flex', flexDirection: 'column', gap: 16 }}>

                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 16 }}>
                        <div>
                            <label className="form-label">Request Type</label>
                            <select className="form-input" value={form.requestType} onChange={e => setField('requestType', e.target.value)}>
                                <option value="COURSEWORK">Coursework</option>
                                <option value="RESEARCH">Research</option>
                                <option value="PERSONAL">Personal Project</option>
                            </select>
                        </div>
                        <div>
                            <label className="form-label">Borrow Date</label>
                            <input type="date" className="form-input" value={form.borrowDate} onChange={e => setField('borrowDate', e.target.value)} />
                        </div>
                        <div>
                            <label className="form-label">Return Date</label>
                            <input type="date" className="form-input" value={form.returnDate} onChange={e => setField('returnDate', e.target.value)} />
                        </div>
                    </div>

                    <div>
                        <label className="form-label">Purpose <span style={{ color: '#ef4444' }}>*</span></label>
                        <textarea className="form-input" rows={3} placeholder="Describe the purpose of borrowing this equipment…"
                            value={form.purpose} onChange={e => setField('purpose', e.target.value)} />
                    </div>

                    <div>
                        <label className="form-label">Notes (optional)</label>
                        <input className="form-input" placeholder="Any additional notes…"
                            value={form.notes} onChange={e => setField('notes', e.target.value)} />
                    </div>

                    {/* Equipment Items */}
                    <div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
                            <label className="form-label" style={{ margin: 0 }}>Equipment Items</label>
                            <button className="btn btn-outline btn-sm" onClick={addItem}
                                style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                                <HiOutlinePlusCircle /> Add Item
                            </button>
                        </div>
                        {form.items.map((item, idx) => (
                            <div key={idx} style={{ display: 'grid', gridTemplateColumns: '1fr auto auto auto', gap: 10, marginBottom: 10, alignItems: 'end' }}>
                                <div>
                                    {idx === 0 && <label className="form-label">Equipment</label>}
                                    <select className="form-input" value={item.equipmentId}
                                        onChange={e => setItem(idx, 'equipmentId', e.target.value)}>
                                        <option value="">— Select Equipment —</option>
                                        {loadingEq ? (
                                            <option disabled>Loading…</option>
                                        ) : (
                                            (Array.isArray(equipment) ? equipment : equipment?.data || []).map(eq => (
                                                <option key={eq.equipmentId || eq.id} value={eq.equipmentId || eq.id}>
                                                    {eq.name || eq.equipmentName}
                                                </option>
                                            ))
                                        )}
                                    </select>
                                </div>
                                <div style={{ minWidth: 80 }}>
                                    {idx === 0 && <label className="form-label">Qty</label>}
                                    <input type="number" min={1} className="form-input" value={item.quantity}
                                        onChange={e => setItem(idx, 'quantity', Number(e.target.value))} />
                                </div>
                                <div style={{ minWidth: 140 }}>
                                    {idx === 0 && <label className="form-label">Notes</label>}
                                    <input className="form-input" placeholder="Item notes…" value={item.notes}
                                        onChange={e => setItem(idx, 'notes', e.target.value)} />
                                </div>
                                {form.items.length > 1 && (
                                    <button className="btn btn-sm" style={{ background: '#fee2e2', color: '#dc2626', border: '1px solid #fca5a5' }}
                                        onClick={() => removeItem(idx)} title="Remove">
                                        <HiOutlineTrash />
                                    </button>
                                )}
                            </div>
                        ))}
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10, marginTop: 8 }}>
                        <button className="btn btn-outline" onClick={() => router.back()}>Cancel</button>
                        <button className="btn" onClick={handleSubmit} disabled={submitting}>
                            {submitting ? 'Submitting…' : 'Submit Request'}
                        </button>
                    </div>
                </div>
            </div>
        </DashboardLayout>
    );
}
