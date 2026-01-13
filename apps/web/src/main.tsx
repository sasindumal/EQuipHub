import React from 'react';
import ReactDOM from 'react-dom/client';
import { LoginForm } from './components/LoginForm';
import './index.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <LoginForm />
  </React.StrictMode>
);
