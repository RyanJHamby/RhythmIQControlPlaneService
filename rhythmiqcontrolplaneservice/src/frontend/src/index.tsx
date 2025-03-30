import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import { ProfilePage } from './pages/ProfilePage';

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);

root.render(
  <React.StrictMode>
    <ProfilePage />
  </React.StrictMode>
); 