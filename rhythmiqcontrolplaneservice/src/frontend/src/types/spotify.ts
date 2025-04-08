export interface SpotifyUserProfile {
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
    added_at: string;
    track: {
        id: string;
        name: string;
        artists: Array<{
            id: string;
            name: string;
        }>;
        album: {
            id: string;
            name: string;
            images: Array<{
                url: string;
                height: number;
                width: number;
            }>;
        };
        duration_ms: number;
        preview_url: string | null;
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
    description: string | null;
    images: Array<{
        url: string;
        height: number;
        width: number;
    }>;
    owner: {
        id: string;
        display_name: string;
    };
    tracks: {
        total: number;
    };
}

export interface SpotifyPlaylistsResponse {
    items: SpotifyPlaylist[];
    total: number;
} 