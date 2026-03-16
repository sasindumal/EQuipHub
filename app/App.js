import React from 'react';
import { StatusBar } from 'expo-status-bar';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { Ionicons } from '@expo/vector-icons';

import { AuthProvider, useAuth } from './src/context/AuthContext';
import { LoadingScreen } from './src/components/UI';
import { COLORS, FONT } from './src/lib/theme';

// Auth screens
import LoginScreen from './src/screens/auth/LoginScreen';
import RegisterScreen from './src/screens/auth/RegisterScreen';
import VerifyEmailScreen from './src/screens/auth/VerifyEmailScreen';

// Student screens
import StudentDashboard from './src/screens/student/DashboardScreen';
import StudentEquipment from './src/screens/student/EquipmentScreen';
import StudentRequests from './src/screens/student/RequestsScreen';
import StudentPenalties from './src/screens/student/PenaltiesScreen';
import StudentNewRequest from './src/screens/student/NewRequestScreen';

// Admin screens
import AdminDashboard from './src/screens/admin/DashboardScreen';

// Department Admin screens
import { DeptDashboard, DeptRequests, DeptEquipment, DeptStaff, DeptStudents } from './src/screens/deptAdmin/Screens';

// Technical Officer screens
import { TODashboard, TOIssue, TOReturn } from './src/screens/technicalOfficer/Screens';

// Lecturer screens
import LecturerApproval from './src/screens/lecturer/ApprovalScreen';

// Shared screens
import ProfileScreen from './src/screens/ProfileScreen';

const Stack = createNativeStackNavigator();
const Tab   = createBottomTabNavigator();

// ─── Tab bar styling ─────────────────────────────────────
const tabBarStyle = {
  backgroundColor: COLORS.white,
  borderTopColor: COLORS.border,
  paddingBottom: 4,
  height: 58,
};

const screenOpts = {
  headerStyle: { backgroundColor: COLORS.white },
  headerTintColor: COLORS.text,
  headerTitleStyle: { fontWeight: '700', fontSize: FONT.lg },
  headerShadowVisible: false,
};

// ─── Student Tabs ─────────────────────────────────────────
function StudentTabs() {
  return (
    <Tab.Navigator screenOptions={({ route }) => ({
      ...screenOpts,
      tabBarIcon: ({ focused, color, size }) => {
        const icons = { StudentHome: 'home', StudentEquipment: 'cube', StudentRequests: 'document-text', StudentPenalties: 'alert-circle', StudentProfile: 'person' };
        return <Ionicons name={`${icons[route.name]}${focused ? '' : '-outline'}`} size={22} color={color} />;
      },
      tabBarActiveTintColor: COLORS.primary,
      tabBarInactiveTintColor: COLORS.muted,
      tabBarStyle,
      tabBarLabelStyle: { fontSize: FONT.xs, fontWeight: '600' },
    })}>
      <Tab.Screen name="StudentHome" component={StudentDashboard} options={{ title: 'Home', headerTitle: 'EQuipHub' }} />
      <Tab.Screen name="StudentEquipment" component={StudentEquipment} options={{ title: 'Equipment' }} />
      <Tab.Screen name="StudentRequests" component={StudentRequests} options={{ title: 'Requests' }} />
      <Tab.Screen name="StudentPenalties" component={StudentPenalties} options={{ title: 'Penalties' }} />
      <Tab.Screen name="StudentProfile" component={ProfileScreen} options={{ title: 'Profile' }} />
    </Tab.Navigator>
  );
}

// ─── Admin Tabs ───────────────────────────────────────────
function AdminTabs() {
  return (
    <Tab.Navigator screenOptions={({ route }) => ({
      ...screenOpts,
      tabBarIcon: ({ focused, color }) => {
        const icons = { AdminHome: 'grid', AdminProfile: 'person' };
        return <Ionicons name={`${icons[route.name]}${focused ? '' : '-outline'}`} size={22} color={color} />;
      },
      tabBarActiveTintColor: COLORS.primary,
      tabBarInactiveTintColor: COLORS.muted,
      tabBarStyle,
      tabBarLabelStyle: { fontSize: FONT.xs, fontWeight: '600' },
    })}>
      <Tab.Screen name="AdminHome" component={AdminDashboard} options={{ title: 'Dashboard', headerTitle: 'System Admin' }} />
      <Tab.Screen name="AdminProfile" component={ProfileScreen} options={{ title: 'Profile' }} />
    </Tab.Navigator>
  );
}

// ─── Dept Admin Tabs ─────────────────────────────────────
function DeptAdminTabs() {
  return (
    <Tab.Navigator screenOptions={({ route }) => ({
      ...screenOpts,
      tabBarIcon: ({ focused, color }) => {
        const icons = { DeptHome: 'grid', DeptEquipment: 'cube', DeptRequests: 'document-text', DeptPeople: 'people', DeptProfile: 'person' };
        return <Ionicons name={`${icons[route.name]}${focused ? '' : '-outline'}`} size={22} color={color} />;
      },
      tabBarActiveTintColor: COLORS.primary,
      tabBarInactiveTintColor: COLORS.muted,
      tabBarStyle,
      tabBarLabelStyle: { fontSize: FONT.xs, fontWeight: '600' },
    })}>
      <Tab.Screen name="DeptHome" component={DeptDashboard} options={{ title: 'Home', headerTitle: 'Department' }} />
      <Tab.Screen name="DeptEquipment" component={DeptEquipment} options={{ title: 'Equipment' }} />
      <Tab.Screen name="DeptRequests" component={DeptRequests} options={{ title: 'Requests' }} />
      <Tab.Screen name="DeptPeople" component={DeptStaff} options={{ title: 'Staff' }} />
      <Tab.Screen name="DeptProfile" component={ProfileScreen} options={{ title: 'Profile' }} />
    </Tab.Navigator>
  );
}

// ─── Technical Officer Tabs ──────────────────────────────
function TOTabs() {
  return (
    <Tab.Navigator screenOptions={({ route }) => ({
      ...screenOpts,
      tabBarIcon: ({ focused, color }) => {
        const icons = { TOHome: 'grid', TOIssue: 'swap-horizontal', TOReturn: 'arrow-down-circle', TOProfile: 'person' };
        return <Ionicons name={`${icons[route.name]}${focused ? '' : '-outline'}`} size={22} color={color} />;
      },
      tabBarActiveTintColor: COLORS.primary,
      tabBarInactiveTintColor: COLORS.muted,
      tabBarStyle,
      tabBarLabelStyle: { fontSize: FONT.xs, fontWeight: '600' },
    })}>
      <Tab.Screen name="TOHome" component={TODashboard} options={{ title: 'Home', headerTitle: 'Technical Officer' }} />
      <Tab.Screen name="TOIssue" component={TOIssue} options={{ title: 'Issue' }} />
      <Tab.Screen name="TOReturn" component={TOReturn} options={{ title: 'Returns' }} />
      <Tab.Screen name="TOProfile" component={ProfileScreen} options={{ title: 'Profile' }} />
    </Tab.Navigator>
  );
}

// ─── Lecturer Tabs ────────────────────────────────────────
function LecturerTabs() {
  return (
    <Tab.Navigator screenOptions={({ route }) => ({
      ...screenOpts,
      tabBarIcon: ({ focused, color }) => {
        const icons = { LecApprovals: 'checkmark-done', LecProfile: 'person' };
        return <Ionicons name={`${icons[route.name]}${focused ? '' : '-outline'}`} size={22} color={color} />;
      },
      tabBarActiveTintColor: COLORS.primary,
      tabBarInactiveTintColor: COLORS.muted,
      tabBarStyle,
      tabBarLabelStyle: { fontSize: FONT.xs, fontWeight: '600' },
    })}>
      <Tab.Screen name="LecApprovals" component={LecturerApproval} options={{ title: 'Approvals', headerTitle: 'Approval Queue' }} />
      <Tab.Screen name="LecProfile" component={ProfileScreen} options={{ title: 'Profile' }} />
    </Tab.Navigator>
  );
}

// ─── Root navigator ───────────────────────────────────────
function getTabsForRole(role) {
  switch (role) {
    case 'SYSTEMADMIN':                                return AdminTabs;
    case 'DEPARTMENTADMIN': case 'HEADOFDEPARTMENT':   return DeptAdminTabs;
    case 'TECHNICALOFFICER':                           return TOTabs;
    case 'LECTURER': case 'APPOINTEDLECTURER': case 'INSTRUCTOR': return LecturerTabs;
    case 'STUDENT': default:                           return StudentTabs;
  }
}

function RootNavigator() {
  const { user, loading } = useAuth();

  if (loading) return <LoadingScreen />;

  return (
    <Stack.Navigator screenOptions={{ headerShown: false, animation: 'slide_from_right' }}>
      {!user ? (
        <>
          <Stack.Screen name="Login" component={LoginScreen} />
          <Stack.Screen name="Register" component={RegisterScreen} />
          <Stack.Screen name="VerifyEmail" component={VerifyEmailScreen} />
        </>
      ) : (
        <>
          <Stack.Screen name="MainTabs" component={getTabsForRole(user.role)} />
          <Stack.Screen name="StudentNewRequest" component={StudentNewRequest}
            options={{ headerShown: true, title: 'New Request', ...screenOpts }} />
        </>
      )}
    </Stack.Navigator>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <NavigationContainer>
        <RootNavigator />
        <StatusBar style="dark" />
      </NavigationContainer>
    </AuthProvider>
  );
}
