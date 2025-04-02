import React, { useEffect, useState } from 'react';
import { spotifyService, SpotifyTrack } from '../../services/spotifyService';
import './LikedSongs.css';

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
        setError('Failed to fetch liked songs');
        console.error('Error fetching liked songs:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchSongs();
  }, []);

  if (loading) return <div className="liked-songs-container">Loading...</div>;
  if (error) return <div className="liked-songs-container error">{error}</div>;
  if (!songs.length) return <div className="liked-songs-container">No liked songs found</div>;

  const displaySongs = songs.slice(0, 4);

  return (
    <div className="liked-songs-container">
      <h2>Recently Liked</h2>
      <div className="liked-songs-grid">
        {displaySongs.map((item) => (
          <div key={item.track.id} className="song-tile">
            <img 
              src={item.track.album.images[0]?.url || 'default-album-art.png'} 
              alt={item.track.album.name}
              className="album-art"
            />
            <div className="song-info">
              <h3>{item.track.name}</h3>
              <p>{item.track.artists[0].name}</p>
              <p className="album-name">{item.track.album.name}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}; 