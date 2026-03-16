// EQuipHub Mobile — Design Tokens
// Glassmorphism palette with enhanced responsive design

export const COLORS = {
  primary:      '#3D52A0',
  primaryLight: '#7091E6',
  secondary:    '#8697C4',
  muted:        '#ADBBDA',
  background:   '#EDE8F5',
  black:        '#000000',
  white:        '#FFFFFF',

  // Glassmorphism card colors
  glass: {
    light:  'rgba(255,255,255,0.25)',
    medium: 'rgba(255,255,255,0.40)',
    heavy:  'rgba(255,255,255,0.60)',
    dark:   'rgba(61,82,160,0.15)',
  },

  // Status colors
  danger:       '#C0392B',
  dangerLight:  '#FDEDEC',
  success:      '#27AE60',
  successLight: '#E8F5E9',
  warning:      '#F39C12',
  warningLight: '#FFF8E1',
  info:         '#2980B9',
  infoLight:    '#EBF5FB',

  // Text colors
  text:         '#1A1A2E',
  textSecondary:'#64748B',
  textLight:    '#FFFFFF',

  // Border colors
  border:       'rgba(173,187,218,0.35)',
  borderLight:  'rgba(255,255,255,0.5)',

  // Input colors
  inputBg:      'rgba(255,255,255,0.70)',
};

export const SHADOWS = {
  glass: {
    shadowColor: '#3D52A0',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.15,
    shadowRadius: 24,
    elevation: 8,
  },
  sm: {
    shadowColor: '#3D52A0',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.08,
    shadowRadius: 8,
    elevation: 2,
  },
  md: {
    shadowColor: '#3D52A0',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.12,
    shadowRadius: 16,
    elevation: 4,
  },
  lg: {
    shadowColor: '#3D52A0',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.16,
    shadowRadius: 24,
    elevation: 8,
  },
};

export const GLASS = {
  card: {
    backgroundColor: COLORS.glass.heavy,
    borderRadius: 24,
    borderWidth: 1,
    borderColor: COLORS.borderLight,
    padding: 20,
    ...SHADOWS.glass,
  },
  cardSmall: {
    backgroundColor: COLORS.glass.medium,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: COLORS.borderLight,
    padding: 16,
    ...SHADOWS.md,
  },
  input: {
    backgroundColor: COLORS.inputBg,
    borderRadius: 16,
    borderWidth: 1.5,
    borderColor: COLORS.border,
    paddingHorizontal: 16,
    paddingVertical: 14,
  },
  button: {
    backgroundColor: COLORS.primary,
    borderRadius: 16,
    paddingVertical: 16,
    paddingHorizontal: 24,
    alignItems: 'center',
    justifyContent: 'center',
    ...SHADOWS.md,
  },
  buttonOutline: {
    backgroundColor: 'transparent',
    borderRadius: 16,
    borderWidth: 2,
    borderColor: COLORS.primary,
    paddingVertical: 14,
    paddingHorizontal: 24,
    alignItems: 'center',
    justifyContent: 'center',
  },
};

export const RADIUS = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 20,
  xxl: 24,
  full: 999,
};

export const FONT = {
  xs:   11,
  sm:   13,
  base: 15,
  lg:   17,
  xl:   20,
  xxl:  24,
  xxxl: 28,
  hero: 36,
};

export const SPACING = {
  xs:  4,
  sm:  8,
  md:  12,
  lg:  16,
  xl:  20,
  xxl: 24,
  xxxl: 32,
};

export const STATUS_COLORS = {
  AVAILABLE:   { bg: COLORS.successLight, text: COLORS.success, label: 'Available'   },
  IN_USE:      { bg: COLORS.warningLight, text: COLORS.warning, label: 'In Use'      },
  INUSE:       { bg: COLORS.warningLight, text: COLORS.warning, label: 'In Use'      },
  BORROWED:    { bg: COLORS.warningLight, text: COLORS.warning, label: 'Borrowed'    },
  MAINTENANCE: { bg: COLORS.dangerLight,  text: COLORS.danger,  label: 'Maintenance' },
  INACTIVE:    { bg: '#F1F5F9',           text: '#94A3B8',      label: 'Inactive'    },
  DAMAGED:     { bg: COLORS.dangerLight,  text: COLORS.danger,  label: 'Damaged'     },
  ACTIVE:      { bg: COLORS.successLight, text: COLORS.success, label: 'Active'      },
  SUSPENDED:   { bg: COLORS.dangerLight,  text: COLORS.danger,  label: 'Suspended'   },
  PENDING:     { bg: COLORS.warningLight, text: COLORS.warning, label: 'Pending'     },
  PENDINGAPPROVAL: { bg: COLORS.warningLight, text: COLORS.warning, label: 'Pending' },
  APPROVED:    { bg: COLORS.successLight, text: COLORS.success, label: 'Approved'    },
  REJECTED:    { bg: COLORS.dangerLight,  text: COLORS.danger,  label: 'Rejected'    },
  CANCELLED:   { bg: '#F1F5F9',           text: '#94A3B8',      label: 'Cancelled'   },
  RETURNED:    { bg: '#F1F5F9',           text: '#94A3B8',      label: 'Returned'    },
  COMPLETED:   { bg: COLORS.successLight, text: COLORS.success, label: 'Completed'   },
  OVERDUE:     { bg: COLORS.dangerLight,  text: COLORS.danger,  label: 'Overdue'     },
  DRAFT:       { bg: '#F1F5F9',           text: '#94A3B8',      label: 'Draft'       },
};

export const ROLE_LABELS = {
  SYSTEMADMIN:       'System Admin',
  DEPARTMENTADMIN:   'Dept Admin',
  HEADOFDEPARTMENT:  'Head of Department',
  LECTURER:          'Lecturer',
  INSTRUCTOR:        'Instructor',
  APPOINTEDLECTURER: 'Appointed Lecturer',
  TECHNICALOFFICER:  'Technical Officer',
  STUDENT:           'Student',
};
