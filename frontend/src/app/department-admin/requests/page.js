'use client';

import DashboardLayout from '@/components/layouts/DashboardLayout';
import { HiOutlineClipboardList } from 'react-icons/hi';

export default function DeptRequestsPage() {
    return (
        <DashboardLayout pageTitle="Requests" pageSubtitle="Equipment requests overview">
            <div className="content-card">
                <div className="content-card-body">
                    <div className="empty-state">
                        <HiOutlineClipboardList className="empty-state-icon" />
                        <div className="empty-state-title">Request Management Coming Soon</div>
                        <div className="empty-state-text">
                            View and manage equipment requests from students and staff in your department.
                        </div>
                    </div>
                </div>
            </div>
        </DashboardLayout>
    );
}
