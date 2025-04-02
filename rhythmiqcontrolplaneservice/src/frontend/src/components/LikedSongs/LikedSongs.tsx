import React, { useEffect, useState, useRef, useCallback } from 'react';
import { spotifyService, SpotifyTrack } from '../../services/spotifyService';
import './LikedSongs.css';

export const LikedSongs: React.FC = () => {
  const [songs, setSongs] = useState<SpotifyTrack[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [offset, setOffset] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const observer = useRef<IntersectionObserver>();
  const lastSongRef = useCallback((node: HTMLDivElement) => {
    if (loading || isLoadingMore) return;
    if (observer.current) observer.current.disconnect();
    observer.current = new IntersectionObserver(entries => {
      if (entries[0].isIntersecting && hasMore && !isLoadingMore) {
        loadMore();
      }
    });
    if (node) observer.current.observe(node);
  }, [loading, hasMore, isLoadingMore]);

  const loadMore = async () => {
    if (isLoadingMore) return;
    setIsLoadingMore(true);
    try {
      const response = await spotifyService.getLikedSongs(offset + 20);
      if (response.items.length === 0) {
        setHasMore(false);
        return;
      }
      setSongs(prev => [...prev, ...response.items]);
      setOffset(prev => prev + 20);
    } catch (err) {
      console.error('Error loading more songs:', err);
    } finally {
      setIsLoadingMore(false);
    }
  };

  useEffect(() => {
    const fetchSongs = async () => {
      try {
        const response = await spotifyService.getLikedSongs(0);
        setSongs(response.items);
        setHasMore(response.items.length === 20);
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

  return (
    <div className="liked-songs-container">
      <h2>Recently Liked</h2>
      <div className="liked-songs-grid">
        {songs.map((item, index) => (
          <div 
            key={item.track.id} 
            className="song-tile"
            ref={index === songs.length - 1 ? lastSongRef : undefined}
          >
            <img 
              src={item.track.album.images[0]?.url || 'default-album-art.png'} 
              alt={item.track.album.name}
              className="album-art"
            />
            <div className="song-info">
              <h3>{item.track.name}</h3>
              <p>{item.track.artists[0].name}</p>
            </div>
          </div>
        ))}
        {isLoadingMore && (
          <div className="song-tile loading">
            <div className="album-art loading-placeholder" />
            <div className="song-info">
              <div className="loading-text" />
              <div className="loading-text" />
            </div>
          </div>
        )}
      </div>
    </div>
  );
}; 