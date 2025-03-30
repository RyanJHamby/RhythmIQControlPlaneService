import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSpotify } from '../../contexts/SpotifyContext';

export const SpotifyCallback: React.FC = () => {
  const navigate = useNavigate();
  const { handleAuthSuccess } = useSpotify();

  useEffect(() => {
    const handleCallback = async () => {
      const params = new URLSearchParams(window.location.search);
      const code = params.get('code');
      const error = params.get('error');

      if (error) {
        console.error('Spotify auth error:', error);
        navigate('/login');
        return;
      }

      if (!code) {
        console.error('No code received from Spotify');
        navigate('/login');
        return;
      }

      try {
        await handleAuthSuccess(code);
        navigate('/dashboard');
      } catch (error) {
        console.error('Error handling Spotify callback:', error);
        navigate('/login');
      }
    };

    handleCallback();
  }, [navigate, handleAuthSuccess]);

  return (
    <div className="flex items-center justify-center min-h-screen">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#1DB954] mx-auto"></div>
        <p className="mt-4 text-gray-600">Completing Spotify login...</p>
      </div>
    </div>
  );
}; 