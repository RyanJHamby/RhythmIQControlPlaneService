import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { SpotifyProvider } from './contexts/SpotifyContext';
import { LoginPage } from './pages/LoginPage';
import { SpotifyCallback } from './components/SpotifyLogin/SpotifyCallback';
import { useSpotify } from './contexts/SpotifyContext';
import { LikedSongs } from './components/LikedSongs/LikedSongs';
import { Playlists } from './components/Playlists/Playlists';
import './App.css';

const PrivateRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, isLoading } = useSpotify();

  console.log('PrivateRoute render:', { isAuthenticated, isLoading });

  if (isLoading) {
    console.log('PrivateRoute: Loading...');
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
      </div>
    );
  }

  console.log('PrivateRoute: Auth check', { isAuthenticated });
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" />;
};

const Dashboard: React.FC = () => {
  const { userProfile, logout } = useSpotify();

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <div className="user-info">
          {userProfile?.images[0] && (
            <img
              src={userProfile.images[0].url}
              alt={userProfile.display_name}
              className="profile-image"
            />
          )}
          <span>Welcome, {userProfile?.display_name}</span>
        </div>
        <button onClick={logout} className="logout-button">
          Logout
        </button>
      </header>
      <main className="dashboard-content">
        <div className="dashboard-stack">
          <LikedSongs />
          <Playlists />
        </div>
      </main>
    </div>
  );
};

const App: React.FC = () => {
  return (
    <SpotifyProvider>
      <Router>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/api/spotify/callback" element={<SpotifyCallback />} />
          <Route
            path="/dashboard"
            element={
              <PrivateRoute>
                <Dashboard />
              </PrivateRoute>
            }
          />
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </Router>
    </SpotifyProvider>
  );
};

export default App; 