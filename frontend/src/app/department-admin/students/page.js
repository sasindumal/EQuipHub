'use client';

import { useState, useEffect } from 'react';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { deptAdminAPI } from '@/lib/api';
import { HiOutlineSearch, HiOutlineAcademicCap } from 'react-icons/hi';

export default function DeptStudentsPage() {
    const [students, setStudents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');

    useEffect(() => { loadStudents(); }, []);

    const loadStudents = async () => {
        try {
            const res = await deptAdminAPI.getMyDepartmentStudents();
            const data = res.data?.data?.students || res.data?.data || [];
            setStudents(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const getStatusBadge = (s) => s === 'ACTIVE' ? 'badge-success' : s === 'PENDING' ? 'badge-warning' : s === 'SUSPENDED' ? 'badge-danger' : 'badge-muted';

    const filtered = students.filter((s) =>
        `${s.firstName} ${s.lastName} ${s.email} ${s.indexNumber || ''}`.toLowerCase().includes(search.toLowerCase())
    );

    return (
        <DashboardLayout pageTitle="Students" pageSubtitle="Department students">
            <div className="action-bar">
                <div className="search-bar">
                    <div className="search-input-wrapper">
                        <HiOutlineSearch className="search-icon" />
                        <input className="search-input" placeholder="Search students..." value={search} onChange={(e) => setSearch(e.target.value)} />
                    </div>
                </div>
            </div>

            <div className="content-card">
                <div className="table-container">
                    <table className="table">
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th className="hide-mobile">Email</th>
                                <th>Index No.</th>
                                <th>Semester</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                [...Array(5)].map((_, i) => (
                                    <tr key={i}>
                                        <td><div className="skeleton" style={{ width: 130, height: 16 }} /></td>
                                        <td className="hide-mobile"><div className="skeleton" style={{ width: 180, height: 16 }} /></td>
                                        <td><div className="skeleton" style={{ width: 60, height: 16 }} /></td>
                                        <td><div className="skeleton" style={{ width: 40, height: 16 }} /></td>
                                        <td><div className="skeleton" style={{ width: 60, height: 24, borderRadius: 12 }} /></td>
                                    </tr>
                                ))
                            ) : filtered.length > 0 ? (
                                filtered.map((s) => (
                                    <tr key={s.userId || s.id}>
                                        <td>
                                            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                                                <div className="sidebar-avatar" style={{ width: 32, height: 32, fontSize: 11 }}>
                                                    {(s.firstName || '?')[0]}{(s.lastName || '?')[0]}
                                                </div>
                                                <span style={{ fontWeight: 600 }}>{s.firstName} {s.lastName}</span>
                                            </div>
                                        </td>
                                        <td className="hide-mobile" style={{ color: 'var(--secondary)' }}>{s.email}</td>
                                        <td><span className="badge badge-primary">{s.indexNumber || '—'}</span></td>
                                        <td>{s.semesterYear || '—'}</td>
                                        <td><span className={`badge ${getStatusBadge(s.status)}`}>{s.status}</span></td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan={5}>
                                        <div className="empty-state">
                                            <HiOutlineAcademicCap className="empty-state-icon" />
                                            <div className="empty-state-title">No students found</div>
                                            <div className="empty-state-text">Students will appear here after they register and are approved.</div>
                                        </div>
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </DashboardLayout>
    );
}
