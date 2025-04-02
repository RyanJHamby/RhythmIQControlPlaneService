import React, { useEffect, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useSpotify } from '../../contexts/SpotifyContext';

export const SpotifyCallback: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { handleAuthSuccess } = useSpotify();
  const isProcessing = useRef(false);
  const hasNavigated = useRef(false);

  useEffect(() => {
    const handleCallback = async () => {
      console.log('Starting callback handling...');
      
      if (isProcessing.current || hasNavigated.current) {
        console.log('Already processing or navigated, skipping...');
        return;
      }

      const params = new URLSearchParams(location.search);
      const code = params.get('code');
      const error = params.get('error');

      console.log('URL params:', { code: code ? 'present' : 'missing', error });

      if (error) {
        console.error('Spotify auth error:', error);
        hasNavigated.current = true;
        navigate('/login', { replace: true });
        return;
      }

      if (!code) {
        console.error('No code received from Spotify');
        hasNavigated.current = true;
        navigate('/login', { replace: true });
        return;
      }

      try {
        console.log('Processing auth code...');
        isProcessing.current = true;
        await handleAuthSuccess(code);
        console.log('Auth success, navigating to dashboard...');
        hasNavigated.current = true;
        navigate('/dashboard', { replace: true });
      } catch (error) {
        console.error('Error handling Spotify callback:', error);
        hasNavigated.current = true;
        navigate('/login', { replace: true });
      } finally {
        isProcessing.current = false;
      }
    };

    handleCallback();
  }, [navigate, location, handleAuthSuccess]);

  return (
    <div className="flex items-center justify-center min-h-screen">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#1DB954] mx-auto"></div>
        <p className="mt-4 text-gray-600">Completing Spotify login...</p>
      </div>
    </div>
  );
}; 