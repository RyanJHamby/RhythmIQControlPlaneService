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

class SpotifyService {
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

  async getLikedSongs(): Promise<SpotifyLikedSongsResponse> {
    const response = await fetch('/api/spotify/liked-songs', {
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include', // Important for cookies
    });

    if (!response.ok) {
      throw new Error('Failed to fetch liked songs');
    }

    return response.json();
  }

  logout() {
    // The backend will handle clearing the cookies
    fetch('/api/spotify/logout', {
      method: 'POST',
      credentials: 'include',
    });
  }
}

export const spotifyService = SpotifyService.getInstance(); 