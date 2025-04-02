interface SpotifyTokenResponse {
  success: boolean;
}

interface SpotifyUserProfile {
  id: string;
  display_name: string;
  email: string;
  images: Array<{
    url: string;
    height: number;
    width: number;
  }>;
}

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

export interface SpotifyLikedSongsResponse {
  items: SpotifyTrack[];
  total: number;
  limit: number;
  offset: number;
  next: string | null;
  previous: string | null;
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

export interface SpotifyPlaylistsResponse {
  items: SpotifyPlaylist[];
  total: number;
}

export class SpotifyService {
  private static instance: SpotifyService;
  private accessToken: string | null = null;
  private tokenExpiry: number | null = null;

  private constructor() {}

  static getInstance(): SpotifyService {
    if (!SpotifyService.instance) {
      SpotifyService.instance = new SpotifyService();
    }
    return SpotifyService.instance;
  }

  async exchangeCodeForToken(code: string): Promise<SpotifyTokenResponse> {
    const response = await fetch('/api/spotify/token', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ code }),
      credentials: 'include', // Important for cookies
    });

    if (!response.ok) {
      throw new Error('Failed to exchange code for token');
    }

    return { success: true } as SpotifyTokenResponse;
  }

  async getUserProfile(): Promise<SpotifyUserProfile> {
    const response = await fetch('/api/spotify/me', {
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include', // Important for cookies
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
    const response = await fetch(`https://your-api-gateway-url/prod/spotify/liked-songs?offset=${offset}`, {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (!response.ok) {
      throw new Error('Failed to fetch liked songs');
    }

    return response.json();
  }

  async getPlaylists(): Promise<SpotifyPlaylistsResponse> {
    const response = await fetch('https://your-api-gateway-url/prod/spotify/playlists', {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (!response.ok) {
      throw new Error('Failed to fetch playlists');
    }

    return response.json();
  }

  logout() {
    this.accessToken = null;
  }
}

export const spotifyService = SpotifyService.getInstance(); 