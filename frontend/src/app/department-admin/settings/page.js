'use client';

import { useState, useEffect } from 'react';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { deptAdminAPI } from '@/lib/api';
import { HiOutlineCog, HiOutlineRefresh } from 'react-icons/hi';

export default function DeptSettingsPage() {
    const [config, setConfig] = useState(null);
    const [department, setDepartment] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => { loadData(); }, []);

    const loadData = async () => {
        try {
            const [configRes, deptRes] = await Promise.allSettled([
                deptAdminAPI.getMyDepartmentConfig(),
                deptAdminAPI.getMyDepartment(),
            ]);
            if (configRes.status === 'fulfilled') {
                setConfig(configRes.value.data?.data || configRes.value.data);
            }
            if (deptRes.status === 'fulfilled') {
                setDepartment(deptRes.value.data?.data || deptRes.value.data);
            }
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleReset = async () => {
        if (!confirm('Reset department configuration to system defaults?')) return;
        try {
            await deptAdminAPI.resetMyDepartmentConfig();
            loadData();
        } catch (err) {
            alert(err.response?.data?.message || 'Failed to reset');
        }
    };

    return (
        <DashboardLayout pageTitle="Settings" pageSubtitle="Department configuration">
            {/* Department Details */}
            <div className="content-card" style={{ marginBottom: 24 }}>
                <div className="content-card-header">
                    <h2 className="content-card-title">Department Details</h2>
                </div>
                <div className="content-card-body">
                    {loading ? (
                        <div style={{ display: 'grid', gap: 12 }}>
                            {[...Array(3)].map((_, i) => <div key={i} className="skeleton" style={{ height: 20, width: '40%' }} />)}
                        </div>
                    ) : department ? (
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: 16 }}>
                            {[
                                { label: 'Department Name', value: department.name },
                                { label: 'Code', value: department.code || department.departmentCode },
                                { label: 'Status', value: department.active !== false ? 'Active' : 'Inactive' },
                            ].map((item) => (
                                <div key={item.label} style={{ padding: 14, background: 'var(--bg-light)', borderRadius: 'var(--radius-sm)' }}>
                                    <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--secondary)', marginBottom: 4, textTransform: 'uppercase', letterSpacing: '0.04em', fontWeight: 600 }}>
                                        {item.label}
                                    </div>
                                    <div style={{ fontSize: 'var(--font-size-sm)', fontWeight: 600 }}>{item.value}</div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <p style={{ color: 'var(--secondary)' }}>Unable to load department details.</p>
                    )}
                </div>
            </div>

            {/* Configuration */}
            <div className="content-card">
                <div className="content-card-header">
                    <h2 className="content-card-title">Configuration</h2>
                    <button className="btn btn-secondary btn-sm" onClick={handleReset}>
                        <HiOutlineRefresh /> Reset to Defaults
                    </button>
                </div>
                <div className="content-card-body">
                    {loading ? (
                        <div style={{ display: 'grid', gap: 12 }}>
                            {[...Array(4)].map((_, i) => <div key={i} className="skeleton" style={{ height: 20, width: '50%' }} />)}
                        </div>
                    ) : config ? (
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: 16 }}>
                            {Object.entries(config).filter(([k]) => !['departmentId', 'id', 'createdAt', 'updatedAt'].includes(k)).map(([key, value]) => (
                                <div key={key} style={{ padding: 14, background: 'var(--bg-light)', borderRadius: 'var(--radius-sm)' }}>
                                    <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--secondary)', marginBottom: 4, textTransform: 'uppercase', letterSpacing: '0.04em', fontWeight: 600 }}>
                                        {key.replace(/([A-Z])/g, ' $1').trim()}
                                    </div>
                                    <div style={{ fontSize: 'var(--font-size-sm)', fontWeight: 600 }}>
                                        {typeof value === 'boolean' ? (value ? 'Yes' : 'No') : String(value)}
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className="empty-state" style={{ padding: 40 }}>
                            <HiOutlineCog className="empty-state-icon" />
                            <div className="empty-state-title">No configuration found</div>
                            <div className="empty-state-text">Configuration will be available once initialized by a system administrator.</div>
                        </div>
                    )}
                </div>
            </div>
        </DashboardLayout>
    );
}
