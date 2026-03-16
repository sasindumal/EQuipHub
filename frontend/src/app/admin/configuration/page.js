'use client';

import { useState, useEffect } from 'react';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { adminAPI } from '@/lib/api';
import {
    HiOutlineCog,
    HiOutlineRefresh,
    HiOutlineX,
} from 'react-icons/hi';

export default function ConfigurationPage() {
    const [configs, setConfigs] = useState([]);
    const [defaults, setDefaults] = useState(null);
    const [loading, setLoading] = useState(true);
    const [showEditModal, setShowEditModal] = useState(false);
    const [selectedConfig, setSelectedConfig] = useState(null);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => { loadData(); }, []);

    const loadData = async () => {
        try {
            const [configRes, defaultRes] = await Promise.allSettled([
                adminAPI.getAllConfigurations(),
                adminAPI.getSystemDefaults(),
            ]);
            if (configRes.status === 'fulfilled') {
                const data = configRes.value.data?.data?.configurations || configRes.value.data?.data || [];
                setConfigs(Array.isArray(data) ? data : []);
            }
            if (defaultRes.status === 'fulfilled') {
                setDefaults(defaultRes.value.data?.data || defaultRes.value.data);
            }
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleReset = async (deptId) => {
        if (!confirm('Reset this department configuration to system defaults?')) return;
        try {
            await adminAPI.resetConfig(deptId);
            loadData();
        } catch (err) {
            alert(err.response?.data?.message || 'Failed to reset');
        }
    };

    return (
        <DashboardLayout pageTitle="Configuration" pageSubtitle="System and department configurations">
            {/* System Defaults */}
            <div className="content-card" style={{ marginBottom: 24 }}>
                <div className="content-card-header">
                    <h2 className="content-card-title">System Defaults</h2>
                </div>
                <div className="content-card-body">
                    {loading ? (
                        <div style={{ display: 'grid', gap: 12 }}>
                            {[...Array(4)].map((_, i) => <div key={i} className="skeleton" style={{ height: 20, width: '60%' }} />)}
                        </div>
                    ) : defaults ? (
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: 16 }}>
                            {Object.entries(defaults).map(([key, value]) => (
                                <div key={key} style={{ padding: 12, background: 'var(--bg-light)', borderRadius: 'var(--radius-sm)' }}>
                                    <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--secondary)', marginBottom: 4, textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                                        {key.replace(/([A-Z])/g, ' $1').trim()}
                                    </div>
                                    <div style={{ fontSize: 'var(--font-size-sm)', fontWeight: 600 }}>
                                        {typeof value === 'boolean' ? (value ? 'Yes' : 'No') : String(value)}
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <p style={{ color: 'var(--secondary)', fontSize: 'var(--font-size-sm)' }}>No defaults configured</p>
                    )}
                </div>
            </div>

            {/* Department Configurations */}
            <div className="content-card">
                <div className="content-card-header">
                    <h2 className="content-card-title">Department Configurations</h2>
                </div>
                <div className="table-container">
                    <table className="table">
                        <thead>
                            <tr>
                                <th>Department</th>
                                <th>Status</th>
                                <th style={{ textAlign: 'right' }}>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                [...Array(3)].map((_, i) => (
                                    <tr key={i}>
                                        <td><div className="skeleton" style={{ width: 180, height: 16 }} /></td>
                                        <td><div className="skeleton" style={{ width: 80, height: 24, borderRadius: 12 }} /></td>
                                        <td><div className="skeleton" style={{ width: 80, height: 16 }} /></td>
                                    </tr>
                                ))
                            ) : configs.length > 0 ? (
                                configs.map((config) => (
                                    <tr key={config.departmentId || config.id}>
                                        <td style={{ fontWeight: 600 }}>{config.departmentName || config.departmentId}</td>
                                        <td><span className="badge badge-success">Configured</span></td>
                                        <td style={{ textAlign: 'right' }}>
                                            <div style={{ display: 'flex', gap: 6, justifyContent: 'flex-end' }}>
                                                <button className="btn btn-ghost btn-sm" title="Reset" onClick={() => handleReset(config.departmentId || config.id)}>
                                                    <HiOutlineRefresh />
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan={3}>
                                        <div className="empty-state">
                                            <HiOutlineCog className="empty-state-icon" />
                                            <div className="empty-state-title">No configurations found</div>
                                            <div className="empty-state-text">Department configurations will appear here once departments are created.</div>
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
