import React, { createContext, useContext, useState, useEffect } from 'react';
import { spotifyService } from '../services/spotifyService';
import { SpotifyUserProfile } from '../types/spotify';

interface SpotifyContextType {
    isAuthenticated: boolean;
    userProfile: SpotifyUserProfile | null;
    isLoading: boolean;
    error: string | null;
    login: () => void;
    logout: () => void;
}

const SpotifyContext = createContext<SpotifyContextType | undefined>(undefined);

export const SpotifyProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [userProfile, setUserProfile] = useState<SpotifyUserProfile | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        checkAuth();
    }, []);

    const checkAuth = async () => {
        try {
            setIsLoading(true);
            setError(null);

            // Check URL for token and session ID
            const urlParams = new URLSearchParams(window.location.search);
            const token = urlParams.get('token');
            const sessionId = urlParams.get('sessionId');

            if (token && sessionId) {
                spotifyService.setAccessToken(token);
                spotifyService.setSessionId(sessionId);
                setIsAuthenticated(true);
                await getUserProfile();
                // Clear URL parameters
                window.history.replaceState({}, document.title, window.location.pathname);
            } else {
                setIsAuthenticated(false);
            }
        } catch (err) {
            setError(err instanceof Error ? err.message : 'An error occurred');
            setIsAuthenticated(false);
        } finally {
            setIsLoading(false);
        }
    };

    const getUserProfile = async () => {
        try {
            const profile = await spotifyService.getUserProfile();
            setUserProfile(profile);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to fetch user profile');
            setIsAuthenticated(false);
        }
    };

    const login = async () => {
        try {
            // Instead of making a fetch request, directly redirect to the login endpoint
            window.location.href = 'http://localhost:8080/api/spotify/login';
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to login');
        }
    };

    const logout = async () => {
        try {
            await spotifyService.logout();
            setIsAuthenticated(false);
            setUserProfile(null);
            setError(null);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to logout');
        }
    };

    return (
        <SpotifyContext.Provider value={{
            isAuthenticated,
            userProfile,
            isLoading,
            error,
            login,
            logout
        }}>
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