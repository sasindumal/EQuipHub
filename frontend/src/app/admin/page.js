'use client';

import { useState, useEffect } from 'react';
import DashboardLayout from '@/components/layouts/DashboardLayout';
import { adminAPI, userAPI } from '@/lib/api';
import {
    HiOutlineOfficeBuilding,
    HiOutlineUsers,
    HiOutlineUserGroup,
    HiOutlineAcademicCap,
    HiOutlineCheckCircle,
    HiOutlineClock,
} from 'react-icons/hi';

export default function AdminDashboard() {
    const [stats, setStats] = useState({
        totalDepartments: 0,
        totalUsers: 0,
        totalStaff: 0,
        totalStudents: 0,
    });
    const [loading, setLoading] = useState(true);
    const [recentUsers, setRecentUsers] = useState([]);

    useEffect(() => {
        loadDashboard();
    }, []);

    const loadDashboard = async () => {
        try {
            const [deptRes, usersRes, staffRes, studentsRes] = await Promise.allSettled([
                adminAPI.getAllDepartments(),
                userAPI.getAllUsers(),
                userAPI.getAllStaff(),
                userAPI.getAllStudents(),
            ]);

            const departments = deptRes.status === 'fulfilled' ? (deptRes.value.data?.data?.departments || deptRes.value.data?.data || []) : [];
            const users = usersRes.status === 'fulfilled' ? (usersRes.value.data?.data?.users || usersRes.value.data?.data || []) : [];
            const staff = staffRes.status === 'fulfilled' ? (staffRes.value.data?.data?.staff || staffRes.value.data?.data || []) : [];
            const students = studentsRes.status === 'fulfilled' ? (studentsRes.value.data?.data?.students || studentsRes.value.data?.data || []) : [];

            setStats({
                totalDepartments: Array.isArray(departments) ? departments.length : 0,
                totalUsers: Array.isArray(users) ? users.length : 0,
                totalStaff: Array.isArray(staff) ? staff.length : 0,
                totalStudents: Array.isArray(students) ? students.length : 0,
            });

            if (Array.isArray(users)) {
                setRecentUsers(users.slice(0, 5));
            }
        } catch (err) {
            console.error('Dashboard load error:', err);
        } finally {
            setLoading(false);
        }
    };

    const statCards = [
        { label: 'Departments', value: stats.totalDepartments, icon: HiOutlineOfficeBuilding, color: 'blue' },
        { label: 'Total Users', value: stats.totalUsers, icon: HiOutlineUsers, color: 'purple' },
        { label: 'Staff Members', value: stats.totalStaff, icon: HiOutlineUserGroup, color: 'green' },
        { label: 'Students', value: stats.totalStudents, icon: HiOutlineAcademicCap, color: 'orange' },
    ];

    const getRoleBadge = (role) => {
        const map = {
            SYSTEMADMIN: 'badge-primary',
            DEPARTMENTADMIN: 'badge-info',
            HEADOFDEPARTMENT: 'badge-success',
            LECTURER: 'badge-warning',
            INSTRUCTOR: 'badge-muted',
            STUDENT: 'badge-primary',
            TECHNICALOFFICER: 'badge-success',
        };
        return map[role] || 'badge-muted';
    };

    const getStatusBadge = (status) => {
        if (status === 'ACTIVE') return 'badge-success';
        if (status === 'PENDING') return 'badge-warning';
        if (status === 'SUSPENDED') return 'badge-danger';
        return 'badge-muted';
    };

    const formatRole = (role) => {
        const map = {
            SYSTEMADMIN: 'System Admin',
            DEPARTMENTADMIN: 'Dept Admin',
            HEADOFDEPARTMENT: 'Head of Dept',
            LECTURER: 'Lecturer',
            INSTRUCTOR: 'Instructor',
            APPOINTEDLECTURER: 'Appointed Lec',
            TECHNICALOFFICER: 'Technical Officer',
            STUDENT: 'Student',
        };
        return map[role] || role;
    };

    return (
        <DashboardLayout pageTitle="Dashboard" pageSubtitle="System overview and quick stats">
            {/* Stats */}
            <div className="stats-grid">
                {statCards.map((stat) => (
                    <div key={stat.label} className="stat-card">
                        <div className="stat-card-header">
                            <span className="stat-card-label">{stat.label}</span>
                            <div className={`stat-card-icon ${stat.color}`}>
                                <stat.icon />
                            </div>
                        </div>
                        <div className="stat-card-value">
                            {loading ? <div className="skeleton" style={{ width: 60, height: 36 }} /> : stat.value}
                        </div>
                    </div>
                ))}
            </div>

            {/* Recent Users */}
            <div className="content-card">
                <div className="content-card-header">
                    <h2 className="content-card-title">Recent Users</h2>
                </div>
                <div className="table-container">
                    <table className="table">
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>Email</th>
                                <th>Role</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                [...Array(3)].map((_, i) => (
                                    <tr key={i}>
                                        <td><div className="skeleton" style={{ width: 120, height: 16 }} /></td>
                                        <td><div className="skeleton" style={{ width: 180, height: 16 }} /></td>
                                        <td><div className="skeleton" style={{ width: 80, height: 24, borderRadius: 12 }} /></td>
                                        <td><div className="skeleton" style={{ width: 60, height: 24, borderRadius: 12 }} /></td>
                                    </tr>
                                ))
                            ) : recentUsers.length > 0 ? (
                                recentUsers.map((u) => (
                                    <tr key={u.userId || u.id}>
                                        <td style={{ fontWeight: 600 }}>{u.firstName} {u.lastName}</td>
                                        <td style={{ color: 'var(--secondary)' }}>{u.email}</td>
                                        <td><span className={`badge ${getRoleBadge(u.role)}`}>{formatRole(u.role)}</span></td>
                                        <td><span className={`badge ${getStatusBadge(u.status)}`}>{u.status}</span></td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan={4} className="text-center text-secondary" style={{ padding: 40 }}>
                                        No users found
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
