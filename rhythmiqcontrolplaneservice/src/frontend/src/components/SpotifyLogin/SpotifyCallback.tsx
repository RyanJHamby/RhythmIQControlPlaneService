import React, { useEffect, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useSpotify } from '../../contexts/SpotifyContext';

export const SpotifyCallback: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated } = useSpotify();
  const isProcessing = useRef(false);
  const hasNavigated = useRef(false);

  useEffect(() => {
    if (isProcessing.current || hasNavigated.current) return;

    isProcessing.current = true;
    console.log('Callback processing...');

    // If we're authenticated, navigate to dashboard
    if (isAuthenticated) {
      console.log('Already authenticated, redirecting to dashboard');
      hasNavigated.current = true;
      navigate('/dashboard');
    } else {
      console.log('Not authenticated, redirecting to login');
      hasNavigated.current = true;
      navigate('/login');
    }
  }, [isAuthenticated, navigate]);

  return (
    <div className="flex items-center justify-center min-h-screen">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#1DB954] mx-auto"></div>
        <p className="mt-4 text-gray-600">Processing authentication...</p>
      </div>
    </div>
  );
}; 