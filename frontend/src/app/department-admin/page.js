'use client';

import { useState, useEffect } from 'react';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { deptAdminAPI } from '@/lib/api';
import {
    HiOutlineUserGroup,
    HiOutlineAcademicCap,
    HiOutlineOfficeBuilding,
    HiOutlineClipboardList,
} from 'react-icons/hi';

export default function DepartmentAdminDashboard() {
    const [department, setDepartment] = useState(null);
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => { loadData(); }, []);

    const loadData = async () => {
        try {
            const [deptRes, statsRes] = await Promise.allSettled([
                deptAdminAPI.getMyDepartment(),
                deptAdminAPI.getMyDepartmentStats(),
            ]);
            if (deptRes.status === 'fulfilled') {
                setDepartment(deptRes.value.data?.data || deptRes.value.data);
            }
            if (statsRes.status === 'fulfilled') {
                setStats(statsRes.value.data?.data || statsRes.value.data);
            }
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const statCards = [
        { label: 'Department', value: department?.name || '—', icon: HiOutlineOfficeBuilding, color: 'blue', isText: true },
        { label: 'Staff Members', value: stats?.totalStaff || stats?.staffCount || 0, icon: HiOutlineUserGroup, color: 'green' },
        { label: 'Students', value: stats?.totalStudents || stats?.studentCount || 0, icon: HiOutlineAcademicCap, color: 'orange' },
        { label: 'Total Users', value: stats?.totalUsers || stats?.userCount || 0, icon: HiOutlineClipboardList, color: 'purple' },
    ];

    return (
        <DashboardLayout pageTitle="Department Dashboard" pageSubtitle={department?.name || 'Loading...'}>
            <div className="stats-grid">
                {statCards.map((stat) => (
                    <div key={stat.label} className="stat-card">
                        <div className="stat-card-header">
                            <span className="stat-card-label">{stat.label}</span>
                            <div className={`stat-card-icon ${stat.color}`}>
                                <stat.icon />
                            </div>
                        </div>
                        <div className={stat.isText ? '' : 'stat-card-value'} style={stat.isText ? { fontSize: 'var(--font-size-lg)', fontWeight: 700, color: 'var(--primary)' } : {}}>
                            {loading ? <div className="skeleton" style={{ width: stat.isText ? 140 : 60, height: stat.isText ? 24 : 36 }} /> : stat.value}
                        </div>
                    </div>
                ))}
            </div>

            {/* Department Info Card */}
            {!loading && department && (
                <div className="content-card">
                    <div className="content-card-header">
                        <h2 className="content-card-title">Department Information</h2>
                    </div>
                    <div className="content-card-body">
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: 16 }}>
                            {[
                                { label: 'Name', value: department.name },
                                { label: 'Code', value: department.code || department.departmentCode },
                                { label: 'Description', value: department.description || '—' },
                                { label: 'Status', value: department.active !== false ? 'Active' : 'Inactive' },
                            ].map((item) => (
                                <div key={item.label} style={{ padding: 14, background: 'var(--bg-light)', borderRadius: 'var(--radius-sm)' }}>
                                    <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--secondary)', marginBottom: 4, textTransform: 'uppercase', letterSpacing: '0.04em', fontWeight: 600 }}>
                                        {item.label}
                                    </div>
                                    <div style={{ fontSize: 'var(--font-size-sm)', fontWeight: 600 }}>
                                        {item.value}
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            )}
        </DashboardLayout>
    );
}
