'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { requestAPI, equipmentAPI, approvalAPI } from '@/lib/api';
import { useAuth } from '@/lib/auth';
import { HiOutlinePlusCircle, HiOutlineTrash } from 'react-icons/hi';

// Convert a date string 'YYYY-MM-DD' to a full ISO LocalDateTime string
// Backend @NotNull LocalDateTime fromDateTime expects e.g. "2026-03-20T00:00:00"
const toDateTime = (dateStr, endOfDay = false) => {
    if (!dateStr) return null;
    return `${dateStr}T${endOfDay ? '23:59:59' : '00:00:00'}`;
};

export default function NewRequestPage() {
    const router = useRouter();
    const { user } = useAuth();
    const [equipment, setEquipment] = useState([]);
    const [loadingEq, setLoadingEq]   = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError]           = useState(null);
    const [success, setSuccess]       = useState(null);
    const [step, setStep]             = useState('form'); // 'form' | 'success'

    const [form, setForm] = useState({
        requestType:   'COURSEWORK',
        purpose:       '',
        borrowDate:    '',   // UI only  → mapped to fromDateTime
        returnDate:    '',   // UI only  → mapped to toDateTime
        priorityLevel: 1,    // 1 = Normal, 2 = High, 3 = Emergency
        isEmergency:   false,
        items: [{ equipmentId: '', quantityRequested: 1, notes: '' }],
    });

    useEffect(() => {
        if (!user?.departmentId) return;
        const loadEquipment = async () => {
            try {
                let res;
                if (user?.departmentId) {
                    res = await equipmentAPI.getByDepartment(user.departmentId);
                } else {
                    res = await equipmentAPI.getAllEquipment();
                }
                const raw  = res.data?.data || res.data || [];
                const data = raw.equipment || raw;
                const list = Array.isArray(data) ? data : (data.content || []);
                setEquipment(list);
            } catch {
                setEquipment([]);
            } finally {
                setLoadingEq(false);
            }
        };
        loadEquipment();
    }, [user?.departmentId]);

    const setField = (key, val) => setForm(f => ({ ...f, [key]: val }));
    const setItem  = (idx, key, val) => setForm(f => ({
        ...f,
        items: f.items.map((it, i) => i === idx ? { ...it, [key]: val } : it),
    }));
    const addItem    = () => setForm(f => ({ ...f, items: [...f.items, { equipmentId: '', quantityRequested: 1, notes: '' }] }));
    const removeItem = idx => setForm(f => ({ ...f, items: f.items.filter((_, i) => i !== idx) }));

    const handleSubmit = async () => {
        // --- Client-side validation ---
        if (!form.purpose.trim()) {
            setError('Purpose is required.'); return;
        }
        if (!form.borrowDate || !form.returnDate) {
            setError('Borrow date and return date are required.'); return;
        }
        if (new Date(form.returnDate) <= new Date(form.borrowDate)) {
            setError('Return date must be after borrow date.'); return;
        }
        if (form.items.some(it => !it.equipmentId)) {
            setError('Please select equipment for every item.'); return;
        }
        if (form.items.some(it => !it.quantityRequested || it.quantityRequested < 1)) {
            setError('Quantity must be at least 1 for every item.'); return;
        }

        setSubmitting(true); setError(null);
        let createdId = null;

        try {
            // ----------------------------------------------------------------
            // Build payload matching backend CreateRequestDTO exactly:
            //   studentId          UUID          @NotNull
            //   departmentId       UUID          @NotNull
            //   requestType        Enum          @NotNull  (COURSEWORK|RESEARCH|PERSONAL|LABSESSION|EXTRACURRICULAR)
            //   fromDateTime       LocalDateTime @NotNull  (ISO string)
            //   toDateTime         LocalDateTime @NotNull  (ISO string)
            //   priorityLevel      Integer       @NotNull  (1-3)
            //   slaHours           Integer       @NotNull  (default 48)
            //   description        String        optional
            //   isEmergency        Boolean       optional
            //   items[]
            //     equipmentId      UUID          @NotNull
            //     quantityRequested Integer      @NotNull  (NOT "quantity")
            //     notes            String        optional
            // ----------------------------------------------------------------
            const studentId = user?.userId || user?.id;
            if (!studentId) throw new Error('User session expired. Please log in again.');

            const payload = {
                studentId:    studentId,
                departmentId: user?.departmentId,
                requestType:  form.requestType,
                fromDateTime: toDateTime(form.borrowDate, false),
                toDateTime:   toDateTime(form.returnDate, true),
                description:  form.purpose.trim() || null,
                priorityLevel: form.isEmergency ? 3 : form.priorityLevel,
                slaHours:     48,
                isEmergency:  form.isEmergency,
                emergencyJustification: form.isEmergency ? form.purpose.trim() : null,
                items: form.items.map(it => ({
                    equipmentId:       it.equipmentId,
                    quantityRequested: Number(it.quantityRequested) || 1,
                    notes:             it.notes || null,
                })),
            };

            // Step 1: Create DRAFT
            const createRes = await requestAPI.createRequest(payload);
            const newReq    = createRes.data?.data || createRes.data;
            createdId       = newReq?.requestId || newReq?.id;

            if (!createdId) throw new Error('Server did not return a request ID.');

            // Step 2: Submit DRAFT → PENDINGAPPROVAL
            await requestAPI.submitRequest(createdId);

            // Step 3: Attempt auto-approval (silent — eligible for COURSEWORK + low qty)
            try {
                await approvalAPI.attemptAutoApproval(createdId);
            } catch {
                // Not eligible — safe to ignore, request stays in PENDINGAPPROVAL
            }

            setSuccess(`Request ${createdId} submitted! It is now pending approval.`);
            setStep('success');
            setTimeout(() => router.push('/student/requests'), 2500);

        } catch (e) {
            const serverMsg = e.response?.data?.message
                || (e.response?.data?.errors ? Object.values(e.response.data.errors).join(', ') : null)
                || e.message
                || 'Submission failed. Please try again.';
            setError(createdId
                ? `Request ${createdId} was saved as a draft but could not be submitted: ${serverMsg}`
                : serverMsg
            );
        } finally {
            setSubmitting(false);
        }
    };

    // ── Success screen ──────────────────────────────────────────────────────
    if (step === 'success') {
        return (
            <DashboardLayout pageTitle="Request Submitted" pageSubtitle="Your request is now pending approval">
                <div style={{ textAlign: 'center', padding: '60px 20px' }}>
                    <div style={{ fontSize: 56, marginBottom: 16 }}>✅</div>
                    <h2 style={{ fontWeight: 700, marginBottom: 8 }}>Request Submitted!</h2>
                    <p style={{ color: 'var(--secondary)', marginBottom: 24 }}>{success}</p>
                    <p style={{ color: 'var(--secondary)', fontSize: 13 }}>Redirecting to My Requests…</p>
                </div>
            </DashboardLayout>
        );
    }

    // ── Form ────────────────────────────────────────────────────────────────
    return (
        <DashboardLayout pageTitle="New Borrow Request" pageSubtitle="Fill in the details to borrow equipment">
            {error && <div className="alert alert-danger" style={{ marginBottom: 16 }}>{error}</div>}

            <div className="content-card">
                <div className="content-card-header">
                    <h2 className="content-card-title">Request Details</h2>
                </div>
                <div style={{ padding: '0 20px 24px', display: 'flex', flexDirection: 'column', gap: 16 }}>

                    {/* Type + Dates */}
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: 16 }}>
                        <div>
                            <label className="form-label">Request Type</label>
                            <select className="form-input" value={form.requestType} onChange={e => setField('requestType', e.target.value)}>
                                <option value="COURSEWORK">Coursework</option>
                                <option value="RESEARCH">Research</option>
                                <option value="PERSONAL">Personal Project</option>
                                <option value="EXTRACURRICULAR">Extracurricular</option>
                            </select>
                        </div>
                        <div>
                            <label className="form-label">Borrow Date                         <span style={{ color: 'var(--primary)' }}>*</span></label>
                            <input type="date" className="form-input"
                                min={new Date().toISOString().split('T')[0]}
                                value={form.borrowDate} onChange={e => setField('borrowDate', e.target.value)} />
                        </div>
                        <div>
                            <label className="form-label">Return Date                         <span style={{ color: 'var(--primary)' }}>*</span></label>
                            <input type="date" className="form-input"
                                min={form.borrowDate || new Date().toISOString().split('T')[0]}
                                value={form.returnDate} onChange={e => setField('returnDate', e.target.value)} />
                        </div>
                    </div>

                    {/* Priority + Emergency */}
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: 16 }}>
                        <div>
                            <label className="form-label">Priority</label>
                            <select className="form-input" value={form.priorityLevel} onChange={e => setField('priorityLevel', Number(e.target.value))}>
                                <option value={1}>Normal</option>
                                <option value={2}>High</option>
                            </select>
                        </div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 10, paddingTop: 22 }}>
                            <input type="checkbox" id="emergency" checked={form.isEmergency}
                                onChange={e => setField('isEmergency', e.target.checked)}
                                style={{ width: 16, height: 16, accentColor: 'var(--primary)' }} />
                            <label htmlFor="emergency" style={{ fontSize: 14, color: 'var(--text-main)', cursor: 'pointer' }}>
                                Mark as Emergency
                            </label>
                        </div>
                    </div>

                    {/* Purpose */}
                    <div>
                        <label className="form-label">
                            Purpose / Description                         <span style={{ color: 'var(--primary)' }}>*</span>
                        </label>
                        <textarea className="form-input" rows={3}
                            placeholder="Describe why you need this equipment…"
                            value={form.purpose} onChange={e => setField('purpose', e.target.value)} />
                    </div>

                    {/* Equipment Items */}
                    <div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
                            <label className="form-label" style={{ margin: 0 }}>
                                Equipment Items                         <span style={{ color: 'var(--primary)' }}>*</span>
                            </label>
                            <button className="btn btn-outline btn-sm" onClick={addItem}
                                style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                                <HiOutlinePlusCircle /> Add Item
                            </button>
                        </div>

                        {form.items.map((item, idx) => (
                            <div key={idx} style={{
                                display: 'grid',
                                gridTemplateColumns: `1fr 90px 1fr${form.items.length > 1 ? ' 36px' : ''}`,
                                gap: 10, marginBottom: 10, alignItems: 'end',
                            }}>
                                <div>
                                    {idx === 0 && <label className="form-label">Equipment</label>}
                                    <select className="form-input" value={item.equipmentId}
                                        onChange={e => setItem(idx, 'equipmentId', e.target.value)}>
                                        <option value="">— Select Equipment —</option>
                                        {loadingEq
                                            ? <option disabled>Loading…</option>
                                            : equipment.map(eq => (
                                                <option key={eq.equipmentId || eq.id} value={eq.equipmentId || eq.id}>
                                                    {eq.name || eq.equipmentName}
                                                    {eq.availableQuantity != null ? ` (${eq.availableQuantity} avail.)` : ''}
                                                </option>
                                            ))
                                        }
                                    </select>
                                </div>
                                <div>
                                    {idx === 0 && <label className="form-label">Qty</label>}
                                    {/* Field name is quantityRequested to match backend DTO */}
                                    <input type="number" min={1} className="form-input"
                                        value={item.quantityRequested}
                                        onChange={e => setItem(idx, 'quantityRequested', Number(e.target.value))} />
                                </div>
                                <div>
                                    {idx === 0 && <label className="form-label">Item Notes</label>}
                                    <input className="form-input" placeholder="Notes for this item…"
                                        value={item.notes}
                                        onChange={e => setItem(idx, 'notes', e.target.value)} />
                                </div>
                                {form.items.length > 1 && (
                                    <button className="btn btn-reject btn-sm"
                                        style={{ alignSelf: idx === 0 ? 'flex-end' : 'auto' }}
                                        onClick={() => removeItem(idx)} title="Remove item">
                                        <HiOutlineTrash />
                                    </button>
                                )}
                            </div>
                        ))}
                    </div>

                    {/* Actions */}
                    <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10, marginTop: 8, paddingTop: 12, borderTop: '1px solid var(--border)' }}>
                        <button className="btn btn-outline" onClick={() => router.back()} disabled={submitting}>
                            Cancel
                        </button>
                        <button className="btn" onClick={handleSubmit} disabled={submitting} style={{ minWidth: 150 }}>
                            {submitting ? 'Submitting…' : '📤 Submit Request'}
                        </button>
                    </div>
                </div>
            </div>
        </DashboardLayout>
    );
}
