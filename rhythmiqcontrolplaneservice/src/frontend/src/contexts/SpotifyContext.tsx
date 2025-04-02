import React, { createContext, useContext, useState, useEffect } from 'react';
import { spotifyService } from '../services/spotifyService';

interface SpotifyContextType {
  isAuthenticated: boolean;
  userProfile: any | null;
  login: () => void;
  logout: () => void;
  loading: boolean;
  error: string | null;
  handleAuthSuccess: (code: string) => Promise<void>;
}

const SpotifyContext = createContext<SpotifyContextType | undefined>(undefined);

export const SpotifyProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [userProfile, setUserProfile] = useState<any | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // Check if we have a stored token and it's valid
    const checkAuth = async () => {
      try {
        const profile = await spotifyService.getUserProfile();
        setUserProfile(profile);
        setIsAuthenticated(true);
      } catch (err) {
        setIsAuthenticated(false);
        setUserProfile(null);
      } finally {
        setLoading(false);
      }
    };

    checkAuth();
  }, []);

  const login = () => {
    const clientId = process.env.REACT_APP_SPOTIFY_CLIENT_ID;
    const redirectUri = `${window.location.origin}/api/spotify/callback`;
    const scopes = [
      'user-read-private',
      'user-read-email',
      'playlist-read-private',
      'playlist-read-collaborative',
      'user-library-read'
    ];
    
    const authUrl = `https://accounts.spotify.com/authorize?client_id=${clientId}&response_type=code&redirect_uri=${encodeURIComponent(redirectUri)}&scope=${encodeURIComponent(scopes.join(' '))}`;
    
    window.location.href = authUrl;
  };

  const logout = () => {
    setIsAuthenticated(false);
    setUserProfile(null);
    spotifyService.logout();
  };

  const handleAuthSuccess = async (code: string) => {
    try {
      console.log('Starting auth success handling...');
      setLoading(true);
      setError(null);
      
      console.log('Exchanging code for token...');
      const tokenResponse = await spotifyService.exchangeCodeForToken(code);
      console.log('Token exchange successful');
      
      console.log('Fetching user profile...');
      const profile = await spotifyService.getUserProfile();
      console.log('Profile fetched:', profile);
      
      setUserProfile(profile);
      setIsAuthenticated(true);
      console.log('Auth state updated');
    } catch (err) {
      console.error('Auth error:', err);
      setError(err instanceof Error ? err.message : 'Authentication failed');
      setIsAuthenticated(false);
      setUserProfile(null);
    } finally {
      setLoading(false);
    }
  };

  return (
    <SpotifyContext.Provider
      value={{
        isAuthenticated,
        userProfile,
        login,
        logout,
        loading,
        error,
        handleAuthSuccess,
      }}
    >
      {children}
    </SpotifyContext.Provider>
  );
};

export const useSpotify = () => {
  const context = useContext(SpotifyContext);
  if (context === undefined) {
    throw new Error('useSpotify must be used within a SpotifyProvider');
  }
  return context;
}; 