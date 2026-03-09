'use client';

import DashboardLayout from '@/components/layouts/DashboardLayout';
import { HiOutlineChartBar } from 'react-icons/hi';

export default function AnalyticsPage() {
    return (
        <DashboardLayout pageTitle="Analytics" pageSubtitle="System analytics and reports">
            <div className="content-card">
                <div className="content-card-body">
                    <div className="empty-state">
                        <HiOutlineChartBar className="empty-state-icon" />
                        <div className="empty-state-title">Analytics Coming Soon</div>
                        <div className="empty-state-text">
                            System-wide analytics, usage reports, and equipment utilization charts will be available here.
                        </div>
                    </div>
                </div>
            </div>
        </DashboardLayout>
    );
}
