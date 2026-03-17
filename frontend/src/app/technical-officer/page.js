'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { inspectionAPI, requestAPI, equipmentAPI } from '@/lib/api';
import {
    HiOutlineClipboardList,
    HiOutlineDesktopComputer,
    HiOutlineEye,
    HiOutlineSearch,
    HiOutlineRefresh,
    HiOutlineCheckCircle,
    HiOutlineArrowRight,
} from 'react-icons/hi';

export default function TODashboardPage() {
    const [queue, setQueue]       = useState([]);
    const [myInsp, setMyInsp]     = useState([]);
    const [unack, setUnack]       = useState([]);
    const [equipment, setEquip]   = useState([]);
    const [loading, setLoading]   = useState(true);
    const [error, setError]       = useState(null);

    useEffect(() => { load(); }, []);

    const load = async () => {
        setLoading(true); setError(null);
        try {
            const [qRes, iRes, uRes, eRes] = await Promise.allSettled([
                requestAPI.getDepartmentRequests(),
                inspectionAPI.getMyInspections(),
                inspectionAPI.getUnacknowledged(),
                equipmentAPI.getAllEquipment(),
            ]);
            const qData = qRes.status === 'fulfilled' ? (qRes.value.data?.data?.requests || qRes.value.data?.requests || []) : [];
            const iData = iRes.status === 'fulfilled' ? (iRes.value.data?.data?.inspections || []) : [];
            const uData = uRes.status === 'fulfilled' ? (uRes.value.data?.data?.inspections || []) : [];
            const eData = eRes.status === 'fulfilled' ? (eRes.value.data?.data?.equipment || eRes.value.data?.data || eRes.value.data || []) : [];
            setQueue(Array.isArray(qData) ? qData.filter(r => r.status === 'APPROVED') : []);
            setMyInsp(Array.isArray(iData) ? iData.slice(0, 5) : []);
            setUnack(Array.isArray(uData) ? uData : []);
            setEquip(Array.isArray(eData) ? eData : []);
        } catch (e) {
            setError('Failed to load dashboard data.');
        } finally { setLoading(false); }
    };

    const totalEquip     = equipment.length;
    const availableEquip = equipment.filter(e => e.status === 'AVAILABLE').length;
    const maintEquip     = equipment.filter(e => e.status === 'MAINTENANCE').length;

    const stats = [
        { label: 'Ready to Issue',        value: queue.length,  color: 'var(--primary-light)', href: '/technical-officer/issue'        },
        { label: 'Unacknowledged Damage', value: unack.length,  color: 'var(--primary)',       href: '/technical-officer/returns'       },
        { label: 'My Inspections Today',  value: myInsp.length, color: 'var(--secondary)',     href: '/technical-officer/inspections'   },
        { label: 'Total Equipment',       value: totalEquip,    color: 'var(--primary-light)', href: '/technical-officer/equipment'     },
        { label: 'Available Equipment',   value: availableEquip,color: 'var(--success, green)',href: '/technical-officer/equipment'     },
        { label: 'In Maintenance',        value: maintEquip,    color: 'var(--primary)',       href: '/technical-officer/equipment'     },
    ];

    return (
        <DashboardLayout pageTitle="TO Dashboard" pageSubtitle="Technical Officer — Equipment issue & return management">
            {error && <div className="alert alert-danger" style={{ marginBottom: 16 }}>{error}</div>}

            {/* Stat cards */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: 16, marginBottom: 24 }}>
                {stats.map(s => (
                    <Link key={s.label} href={s.href} style={{ textDecoration: 'none' }}>
                        <div className="content-card" style={{ padding: 20, cursor: 'pointer' }}>
                            <div style={{ fontSize: 32, fontWeight: 800, color: s.color }}>{loading ? '—' : s.value}</div>
                            <div style={{ fontSize: 13, color: 'var(--secondary)', marginTop: 4 }}>{s.label}</div>
                        </div>
                    </Link>
                ))}
            </div>

            {/* Ready to Issue */}
            <div className="content-card" style={{ marginBottom: 20 }}>
                <div className="content-card-header">
                    <h2 className="content-card-title">Approved Requests — Ready to Issue</h2>
                    <Link href="/technical-officer/issue" style={{ fontSize: 13, color: 'var(--primary)', display: 'flex', alignItems: 'center', gap: 4 }}>
                        View All <HiOutlineArrowRight />
                    </Link>
                </div>
                <div className="table-container">
                    <table className="table">
                        <thead>
                            <tr><th>Request ID</th><th>Student</th><th>Items</th><th>Due Date</th><th>Action</th></tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                [...Array(3)].map((_, i) => (
                                    <tr key={i}>{[120,140,60,90,80].map((w,j)=><td key={j}><div className="skeleton" style={{width:w,height:16}}/></td>)}</tr>
                                ))
                            ) : queue.length > 0 ? (
                                queue.slice(0, 8).map(r => (
                                    <tr key={r.requestId || r.id}>
                                        <td style={{ fontWeight: 600, fontSize: 13 }}>{r.requestId || r.id}</td>
                                        <td>{r.studentName || r.requesterName || '—'}</td>
                                        <td>{r.items?.length || r.itemCount || 1}</td>
                                        <td style={{ fontSize: 12, color: 'var(--secondary)' }}>
                                            {r.returnDate ? new Date(r.returnDate).toLocaleDateString() : '—'}
                                        </td>
                                        <td>
                                            <Link href={`/technical-officer/issue?requestId=${r.requestId || r.id}`}
                                                className="btn btn-sm" style={{ fontSize: 12 }}>
                                                Issue <HiOutlineArrowRight style={{ marginLeft: 4 }} />
                                            </Link>
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr><td colSpan={5} style={{ textAlign: 'center', padding: 32, color: 'var(--secondary)' }}>No approved requests pending issue</td></tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Unacknowledged Damage */}
            {unack.length > 0 && (
                <div className="content-card">
                    <div className="content-card-header">
                        <h2 className="content-card-title" style={{ color: 'var(--primary)' }}>⚠ Unacknowledged Damage Inspections</h2>
                    </div>
                    <div className="table-container">
                        <table className="table">
                            <thead><tr><th>Inspection ID</th><th>Equipment</th><th>Damage Level</th><th>Student</th></tr></thead>
                            <tbody>
                                {unack.map(i => (
                                    <tr key={i.inspectionId || i.id}>
                                        <td style={{ fontSize: 13 }}>{i.inspectionId || i.id}</td>
                                        <td>{i.equipmentName || '—'}</td>
                                        <td><span className="badge badge-danger">{i.damageLevel ?? '—'}</span></td>
                                        <td>{i.studentName || '—'}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}
        </DashboardLayout>
    );
}
