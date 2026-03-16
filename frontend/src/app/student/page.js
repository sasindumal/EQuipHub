'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { requestAPI, penaltyAPI } from '@/lib/api';
import { useAuth } from '@/lib/auth';
import {
    HiOutlineClipboardList,
    HiOutlineExclamationCircle,
    HiOutlinePlusCircle,
    HiOutlineArrowRight,
} from 'react-icons/hi';

export default function StudentDashboardPage() {
    const { user } = useAuth();
    const [requests, setRequests]     = useState([]);
    const [penalties, setPenalties]   = useState([]);
    const [loading, setLoading]       = useState(true);
    const [error, setError]           = useState(null);

    useEffect(() => { load(); }, []);

    const load = async () => {
        setLoading(true); setError(null);
        try {
            const [rRes, pRes] = await Promise.allSettled([
                requestAPI.getMyRequests(),
                penaltyAPI.getMyPenalties(),
            ]);
            const rData = rRes.status === 'fulfilled' ? (rRes.value.data?.data?.requests || rRes.value.data?.requests || rRes.value.data || []) : [];
            const pData = pRes.status === 'fulfilled' ? (pRes.value.data || []) : [];
            setRequests(Array.isArray(rData) ? rData : []);
            setPenalties(Array.isArray(pData) ? pData : []);
        } catch (e) {
            setError('Failed to load dashboard.');
        } finally { setLoading(false); }
    };

    const active    = requests.filter(r => ['INUSE','IN_USE','APPROVED'].includes(r.status)).length;
    const pending   = requests.filter(r => ['PENDINGAPPROVAL','PENDINGRECOMMENDATION','PENDING'].includes(r.status)).length;
    const penActive = penalties.filter(p => p.status === 'APPROVED').length;

    const STATUS_COLOR = { DRAFT:'#8697C4', PENDINGAPPROVAL:'#8697C4', PENDINGRECOMMENDATION:'#8697C4', APPROVED:'#7091E6', INUSE:'#3D52A0', RETURNED:'#ADBBDA', REJECTED:'#3D52A0', CANCELLED:'#ADBBDA' };

    return (
        <DashboardLayout pageTitle="My Dashboard" pageSubtitle={`Welcome back, ${user?.firstName || 'Student'}!`}>
            {error && <div className="alert alert-danger" style={{ marginBottom: 16 }}>{error}</div>}

            {/* Stat cards */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: 16, marginBottom: 24 }}>
                {[
                    { label: 'Active Borrows',   value: active,            color: 'var(--primary)', href: '/student/requests' },
                    { label: 'Pending Approval', value: pending,           color: 'var(--secondary)', href: '/student/requests' },
                    { label: 'Active Penalties', value: penActive,         color: 'var(--primary)', href: '/student/penalties' },
                    { label: 'Total Requests',   value: requests.length,   color: 'var(--primary-light)', href: '/student/requests' },
                ].map(s => (
                    <Link key={s.label} href={s.href} style={{ textDecoration: 'none' }}>
                        <div className="content-card" style={{ padding: 20, cursor: 'pointer' }}>
                            <div style={{ fontSize: 32, fontWeight: 800, color: s.color }}>{loading ? '—' : s.value}</div>
                            <div style={{ fontSize: 13, color: 'var(--secondary)', marginTop: 4 }}>{s.label}</div>
                        </div>
                    </Link>
                ))}
            </div>

            {/* Quick actions */}
            <div style={{ display: 'flex', gap: 10, marginBottom: 24 }}>
                <Link href="/student/requests/new" className="btn btn-primary" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <HiOutlinePlusCircle /> New Borrow Request
                </Link>
                <Link href="/student/requests" className="btn btn-outline" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <HiOutlineClipboardList /> My Requests
                </Link>
            </div>

            {/* Recent requests */}
            <div className="content-card" style={{ marginBottom: 20 }}>
                <div className="content-card-header">
                    <h2 className="content-card-title">Recent Requests</h2>
                    <Link href="/student/requests" style={{ fontSize: 13, color: 'var(--primary)', display: 'flex', alignItems: 'center', gap: 4 }}>
                        View All <HiOutlineArrowRight />
                    </Link>
                </div>
                <div className="table-container">
                    <table className="table">
                        <thead><tr><th>Request ID</th><th>Equipment</th><th>Status</th><th>Return Date</th></tr></thead>
                        <tbody>
                            {loading ? (
                                [...Array(4)].map((_,i) => <tr key={i}>{[120,160,90,90].map((w,j)=><td key={j}><div className="skeleton" style={{width:w,height:16}}/></td>)}</tr>)
                            ) : requests.length > 0 ? (
                                requests.slice(0, 5).map(r => (
                                    <tr key={r.requestId || r.id}>
                                        <td style={{ fontWeight: 600, fontSize: 13 }}>{r.requestId || r.id}</td>
                                        <td>{r.equipmentName || (r.items?.[0]?.equipmentName) || '—'}</td>
                                        <td>
                                            <span style={{ padding: '2px 10px', borderRadius: 20, fontSize: 12, fontWeight: 600,
                                                background: (STATUS_COLOR[r.status] || '#94a3b8') + '22',
                                                color: STATUS_COLOR[r.status] || '#94a3b8'
                                            }}>{r.status}</span>
                                        </td>
                                        <td style={{ fontSize: 12, color: 'var(--secondary)' }}>
                                            {r.returnDate ? new Date(r.returnDate).toLocaleDateString() : '—'}
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr><td colSpan={4} style={{ textAlign: 'center', padding: 32, color: 'var(--secondary)' }}>No requests yet</td></tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Active penalties warning */}
            {penActive > 0 && (
                <div style={{ padding: 16, background: 'rgba(61, 82, 160, 0.06)', border: '1px solid rgba(61, 82, 160, 0.15)', borderRadius: 'var(--radius-sm)', display: 'flex', alignItems: 'center', gap: 12 }}>
                    <HiOutlineExclamationCircle style={{ color: 'var(--primary)', fontSize: 24, flexShrink: 0 }} />
                    <div>
                        <div style={{ fontWeight: 600, color: 'var(--primary)' }}>You have {penActive} active penalty point(s)</div>
                        <div style={{ fontSize: 13, color: 'var(--secondary)', marginTop: 2 }}>Active penalties may restrict your borrowing eligibility.</div>
                    </div>
                    <Link href="/student/penalties" className="btn btn-primary btn-sm" style={{ marginLeft: 'auto' }}>View</Link>
                </div>
            )}
        </DashboardLayout>
    );
}
