'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { approvalAPI, requestAPI } from '@/lib/api';
import {
    HiOutlineClipboardList,
    HiOutlineCheckCircle,
    HiOutlineXCircle,
    HiOutlineArrowRight,
    HiOutlineRefresh,
} from 'react-icons/hi';

export default function LecturerDashboardPage() {
    const [queue, setQueue]   = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError]   = useState(null);
    const [success, setSuccess] = useState(null);
    const [actionId, setActionId] = useState(null);
    const [rejectModal, setRejectModal] = useState(null);
    const [rejectNote, setRejectNote]   = useState('');

    useEffect(() => { load(); }, []);

    const load = async () => {
        setLoading(true); setError(null);
        try {
            const res  = await approvalAPI.getMyQueue();
            const data = res.data;
            setQueue(Array.isArray(data) ? data : []);
        } catch (e) {
            setError('Failed to load approval queue.');
        } finally { setLoading(false); }
    };

    const flash = (msg, isError = false) => {
        isError ? setError(msg) : setSuccess(msg);
        setTimeout(() => isError ? setError(null) : setSuccess(null), 4000);
    };

    const handleDecision = async (requestId, stage, action, comments = '') => {
        setActionId(requestId);
        try {
            await approvalAPI.processDecision(requestId, stage, { action, comments });
            flash(`Request ${action.toLowerCase()}d successfully`);
            setRejectModal(null); setRejectNote('');
            load();
        } catch (e) {
            flash(e.response?.data?.message || 'Action failed', true);
        } finally { setActionId(null); }
    };

    return (
        <DashboardLayout pageTitle="Approval Queue" pageSubtitle="Review and action equipment requests assigned to you">
            {error   && <div className="alert alert-danger"  style={{ marginBottom: 16 }}>{error}</div>}
            {success && <div className="alert alert-success" style={{ marginBottom: 16 }}>{success}</div>}

            {/* Summary */}
            <div style={{ display: 'flex', gap: 12, marginBottom: 20 }}>
                <div style={{ padding: '8px 16px', borderRadius: 'var(--radius-sm)', background: 'var(--bg-light)', border: '1px solid var(--border)', fontSize: 13 }}>
                    <span style={{ color: 'var(--secondary)', fontWeight: 700, marginRight: 6 }}>{loading ? '—' : queue.length}</span>
                    Pending Action
                </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 12 }}>
                <button className="btn btn-outline btn-sm" onClick={load} style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <HiOutlineRefresh /> Refresh
                </button>
            </div>

            <div className="content-card">
                <div className="content-card-header">
                    <h2 className="content-card-title">Requests Pending Your Approval</h2>
                    <span style={{ fontSize: 13, color: 'var(--secondary)' }}>{!loading && `${queue.length} items`}</span>
                </div>
                <div className="table-container">
                    <table className="table">
                        <thead>
                            <tr><th>Request ID</th><th>Student</th><th>Purpose</th><th>Stage</th><th>Submitted</th><th>Actions</th></tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                [...Array(4)].map((_,i) => (
                                    <tr key={i}>{[120,140,160,100,90,120].map((w,j)=><td key={j}><div className="skeleton" style={{width:w,height:16}}/></td>)}</tr>
                                ))
                            ) : queue.length > 0 ? (
                                queue.map(item => {
                                    const reqId = item.requestId || item.id;
                                    const stage = item.currentStage || item.stage || 'LECTURERAPPROVAL';
                                    return (
                                        <tr key={reqId}>
                                            <td style={{ fontWeight: 600, fontSize: 13 }}>{reqId}</td>
                                            <td>{item.studentName || item.requesterName || '—'}</td>
                                            <td style={{ fontSize: 13, maxWidth: 180 }}>{item.purpose || item.reason || '—'}</td>
                                            <td><span className="badge badge-warning">{stage}</span></td>
                                            <td style={{ fontSize: 12, color: 'var(--secondary)' }}>
                                                {item.submittedAt ? new Date(item.submittedAt).toLocaleDateString() : '—'}
                                            </td>
                                            <td>
                                                <div style={{ display: 'flex', gap: 6 }}>
                                                    <button
                                                        className="btn btn-approve btn-sm"
                                                        onClick={() => handleDecision(reqId, stage, 'APPROVE')}
                                                        disabled={!!actionId}
                                                        title="Approve"
                                                    >
                                                        {actionId === reqId ? '…' : <HiOutlineCheckCircle />}
                                                    </button>
                                                    <button
                                                        className="btn btn-reject btn-sm"
                                                        onClick={() => { setRejectModal({ reqId, stage }); setRejectNote(''); }}
                                                        disabled={!!actionId}
                                                        title="Reject"
                                                    >
                                                        <HiOutlineXCircle />
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    );
                                })
                            ) : (
                                <tr><td colSpan={6} style={{ textAlign: 'center', padding: 48, color: 'var(--secondary)' }}>
                                    <HiOutlineClipboardList style={{ fontSize: 32, display: 'block', margin: '0 auto 8px' }} />
                                    No requests pending your approval
                                </td></tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Reject Modal */}
            {rejectModal && (
                <div className="modal-overlay">
                    <div className="modal-content" style={{ padding: 28, maxWidth: 420 }}>
                        <h3 style={{ margin: '0 0 8px', fontWeight: 700 }}>Reject Request</h3>
                        <p style={{ color: 'var(--secondary)', fontSize: 14, marginBottom: 16 }}>Provide a reason for rejection.</p>
                        <textarea className="form-input" rows={4} placeholder="Rejection reason…"
                            value={rejectNote} onChange={e => setRejectNote(e.target.value)} />
                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10, marginTop: 16 }}>
                            <button className="btn btn-outline" onClick={() => setRejectModal(null)}>Cancel</button>
                            <button className="btn btn-primary"
                                onClick={() => handleDecision(rejectModal.reqId, rejectModal.stage, 'REJECT', rejectNote)}
                                disabled={!!actionId}>
                                {actionId ? 'Rejecting…' : 'Confirm Reject'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </DashboardLayout>
    );
}
