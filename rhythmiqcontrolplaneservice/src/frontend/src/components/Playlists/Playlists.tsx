import React, { useEffect, useState } from 'react';
import { spotifyService, SpotifyPlaylist } from '../../services/spotifyService';
import './Playlists.css';

const DEFAULT_PLAYLIST_ART = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIiB2aWV3Qm94PSIwIDAgMTAwIDEwMCI+PHJlY3Qgd2lkdGg9IjEwMCIgaGVpZ2h0PSIxMDAiIGZpbGw9IiNlZWUiLz48cGF0aCBkPSJNMzAgMzBoNDB2NDBoLTQweiIgZmlsbD0iIzk5OSIvPjwvc3ZnPg==';

interface PlaylistCategory {
  title: string;
  playlists: SpotifyPlaylist[];
}

export const Playlists: React.FC = () => {
  const [categories, setCategories] = useState<PlaylistCategory[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchPlaylists = async () => {
      try {
        const response = await spotifyService.getPlaylists();
        const playlists = response.items;
        
        // Organize playlists into categories
        const categories: PlaylistCategory[] = [
          {
            title: 'Recently Updated',
            playlists: playlists.slice(0, 5)
          },
          {
            title: 'Your Playlists',
            playlists: playlists.filter(p => p.owner.display_name === 'You')
          },
          {
            title: 'Followed Playlists',
            playlists: playlists.filter(p => p.owner.display_name !== 'You')
          }
        ];

        setCategories(categories);
      } catch (err) {
        setError('Failed to fetch playlists');
        console.error('Error fetching playlists:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchPlaylists();
  }, []);

  if (loading) return <div className="playlists-container">Loading...</div>;
  if (error) return <div className="playlists-container error">{error}</div>;

  return (
    <div className="playlists-container">
      {categories.map((category, index) => (
        <div key={index} className="playlist-category">
          <h2>{category.title}</h2>
          <div className="playlists-grid">
            {category.playlists.map((playlist) => (
              <div key={playlist.id} className="playlist-tile">
                <img 
                  src={playlist.images?.[0]?.url || DEFAULT_PLAYLIST_ART} 
                  alt={playlist.name}
                  className="playlist-art"
                  onError={(e) => {
                    const target = e.target as HTMLImageElement;
                    target.src = DEFAULT_PLAYLIST_ART;
                  }}
                />
                <div className="playlist-info">
                  <h3>{playlist.name}</h3>
                  <p>{playlist.owner.display_name}</p>
                  <p className="track-count">{playlist.tracks.total} tracks</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}; 