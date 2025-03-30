import React from 'react';
import { SpotifyLogin } from '../components/SpotifyLogin/SpotifyLogin';

export const LoginPage: React.FC = () => {
  return (
    <div className="min-h-screen bg-gray-100 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
          Welcome to RhythmIQ
        </h2>
        <p className="mt-2 text-center text-sm text-gray-600">
          Connect your Spotify account to get started
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
          <div className="space-y-6">
            <div className="text-center">
              <SpotifyLogin />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}; 