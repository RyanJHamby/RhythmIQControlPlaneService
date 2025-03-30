import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

interface SpotifyCallbackProps {
  onSuccess?: (token: string) => void;
}

export const SpotifyCallback: React.FC<SpotifyCallbackProps> = ({ onSuccess }) => {
  const navigate = useNavigate();

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
        const response = await fetch('/api/spotify/token', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ code }),
        });

        if (!response.ok) {
          throw new Error('Failed to exchange code for token');
        }

        const data = await response.json();
        if (onSuccess) {
          onSuccess(data.access_token);
        }
        navigate('/dashboard');
      } catch (error) {
        console.error('Error exchanging code for token:', error);
        navigate('/login');
      }
    };

    handleCallback();
  }, [navigate, onSuccess]);

  return (
    <div className="flex items-center justify-center min-h-screen">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#1DB954] mx-auto"></div>
        <p className="mt-4 text-gray-600">Completing Spotify login...</p>
      </div>
    </div>
  );
}; 