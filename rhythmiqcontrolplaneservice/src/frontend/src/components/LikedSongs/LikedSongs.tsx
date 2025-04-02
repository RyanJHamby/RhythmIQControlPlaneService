import React, { useEffect, useState } from 'react';
import { spotifyService } from '../../services/spotifyService';
import type { SpotifyTrack } from '../../services/spotifyService';

export const LikedSongs: React.FC = () => {
  const [songs, setSongs] = useState<SpotifyTrack[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchSongs = async () => {
      try {
        const response = await spotifyService.getLikedSongs();
        setSongs(response.items);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to fetch liked songs');
      } finally {
        setLoading(false);
      }
    };

    fetchSongs();
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center p-8">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-4 bg-red-50 text-red-700 rounded-md">
        {error}
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <h2 className="text-2xl font-bold text-gray-900">Liked Songs</h2>
      <div className="grid gap-4">
        {songs.map((item) => (
          <div key={item.track.id} className="flex items-center space-x-4 p-4 bg-white rounded-lg shadow">
            {item.track.album.images[0] && (
              <img
                src={item.track.album.images[0].url}
                alt={item.track.album.name}
                className="w-16 h-16 rounded"
              />
            )}
            <div>
              <h3 className="font-medium text-gray-900">{item.track.name}</h3>
              <p className="text-sm text-gray-500">{item.track.artists.map(a => a.name).join(', ')}</p>
              <p className="text-sm text-gray-500">{item.track.album.name}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}; 