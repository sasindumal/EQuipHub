# EQuipHub Web Application

Ultramodern Liquid Glass login interface for EQuipHub.

## Features

- **Liquid Glass UI Design** - Glassmorphism with blurred backgrounds and soft shadows
- **Responsive Layout** - Works seamlessly on desktop, tablet, and mobile
- **Modern Animations** - Smooth transitions and floating effects
- **Form Validation** - Real-time email and password validation
- **Password Toggle** - Show/hide password option
- **Social Login** - Quick sign-in options
- **Loading States** - Visual feedback during authentication
- **Success Animation** - Celebratory feedback on login

## Color System

Adheres to EQuipHub's approved color palette:
- **Primary Deep Blue**: #3D52A0
- **Accent Blue**: #7091E6
- **Soft Blue**: #8697C4
- **Muted Neutral**: #ADBBDA
- **Background Light**: #EDE8F5
- **Black**: #000000
- **White**: #FFFFFF

## Installation

```bash
npm install
```

## Development

```bash
npm run dev
```

Starts the development server at `http://localhost:3000`

## Build

```bash
npm run build
```

Creates an optimized production build in the `dist` folder.

## Project Structure

```
src/
├── components/
│   ├── LoginForm.tsx      # Main login form component
│   └── LoginForm.css      # Liquid Glass styling
├── assets/
│   └── logo.png           # EQuipHub logo
├── main.tsx               # React entry point
└── index.css              # Global styles & theme variables

index.html                  # HTML entry point
vite.config.ts            # Vite configuration
tsconfig.json             # TypeScript configuration
```

## Key Components

### LoginForm.tsx
- Email and password input fields
- Real-time form validation
- Show/hide password toggle
- Remember me checkbox
- Forgot password link
- Social login buttons
- Loading state with spinner
- Success message display

### Styling Features
- Backdrop blur effects (glassmorphism)
- Soft shadows and gradients
- Smooth animations and micro-interactions
- Responsive grid layout
- Mobile-first design approach

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Technologies

- **React 18** - UI framework
- **TypeScript** - Type safety
- **Vite** - Build tool and dev server
- **CSS3** - Styling with animations

## Notes

The form uses the official EQuipHub logo from the project assets directory.
All colors and styling follow the Liquid Glass UI theme specifications.
