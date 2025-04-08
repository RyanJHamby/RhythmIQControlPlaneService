import { SpotifyLikedSongsResponse, SpotifyPlaylistsResponse, SpotifyUserProfile } from '../types/spotify';

export interface SpotifyTrack {
  track: {
    id: string;
    name: string;
    artists: Array<{
      name: string;
    }>;
    album: {
      name: string;
      images: Array<{
        url: string;
      }>;
    };
  };
}

export interface SpotifyPlaylist {
  id: string;
  name: string;
  images: { url: string }[];
  tracks: {
    total: number;
  };
  owner: {
    display_name: string;
  };
}

class SpotifyService {
  private static instance: SpotifyService;
  private accessToken: string | null = null;
  private sessionId: string | null = null;

  private constructor() {}

  static getInstance(): SpotifyService {
    if (!SpotifyService.instance) {
      SpotifyService.instance = new SpotifyService();
    }
    return SpotifyService.instance;
  }

  setAccessToken(token: string) {
    this.accessToken = token;
  }

  setSessionId(id: string) {
    this.sessionId = id;
  }

  async getUserProfile(): Promise<SpotifyUserProfile> {
    if (!this.sessionId) {
      throw new Error('No session ID found');
    }

    const response = await fetch(`/api/spotify/me?sessionId=${this.sessionId}`, {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.accessToken}`
      }
    });

    if (!response.ok) {
      throw new Error('Failed to fetch user profile');
    }

    return response.json();
  }

  async getUserPlaylists(): Promise<any> {
    const response = await fetch('/api/spotify/playlists', {
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include', // Important for cookies
    });

    if (!response.ok) {
      throw new Error('Failed to fetch user playlists');
    }

    return response.json();
  }

  async getPlaylistTracks(playlistId: string): Promise<any> {
    const response = await fetch(`/api/spotify/playlists/${playlistId}/tracks`, {
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include', // Important for cookies
    });

    if (!response.ok) {
      throw new Error('Failed to fetch playlist tracks');
    }

    return response.json();
  }

  async getLikedSongs(offset: number = 0): Promise<SpotifyLikedSongsResponse> {
    if (!this.sessionId) {
      throw new Error('No session ID found');
    }

    const response = await fetch(`/api/spotify/liked-songs?sessionId=${this.sessionId}&offset=${offset}`, {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.accessToken}`
      }
    });

    if (!response.ok) {
      throw new Error('Failed to fetch liked songs');
    }

    return response.json();
  }

  async getPlaylists(): Promise<SpotifyPlaylistsResponse> {
    if (!this.sessionId) {
      throw new Error('No session ID found');
    }

    const response = await fetch(`/api/spotify/playlists?sessionId=${this.sessionId}`, {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.accessToken}`
      }
    });

    if (!response.ok) {
      throw new Error('Failed to fetch playlists');
    }

    return response.json();
  }

  async logout(): Promise<void> {
    const response = await fetch('/api/spotify/logout', {
      method: 'POST',
      credentials: 'include'
    });

    if (!response.ok) {
      throw new Error('Failed to logout');
    }

    this.accessToken = null;
    this.sessionId = null;
  }
}

export const spotifyService = SpotifyService.getInstance(); 