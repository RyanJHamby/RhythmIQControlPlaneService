import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'https://api.rhythmiq.com';

export interface Profile {
  profileId: string;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
}

export interface CreateProfileRequest {
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  password: string;
}

export interface UpdateProfileRequest {
  username?: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  password?: string;
}

export interface ProfileResponse {
  success: boolean;
  message: string;
  profile?: Profile;
}

export interface CreateProfileResponse {
  id: string;
  message: string;
}

export interface UpdateProfileResponse {
  message: string;
}

export interface ListProfilesResponse {
  profiles: Profile[];
}

const profileService = {
  createProfile: async (data: CreateProfileRequest): Promise<CreateProfileResponse> => {
    const response = await axios.post(`${API_BASE_URL}/profiles`, data);
    return response.data;
  },

  getProfile: async (profileId: string): Promise<ProfileResponse> => {
    const response = await axios.get(`${API_BASE_URL}/profiles/${profileId}`);
    return response.data;
  },

  updateProfile: async (profileId: string, data: UpdateProfileRequest): Promise<UpdateProfileResponse> => {
    const response = await axios.put(`${API_BASE_URL}/profiles/${profileId}`, data);
    return response.data;
  },

  deleteProfile: async (profileId: string): Promise<void> => {
    await axios.delete(`${API_BASE_URL}/profiles/${profileId}`);
  },

  listProfiles: async (): Promise<ListProfilesResponse> => {
    const response = await axios.get(`${API_BASE_URL}/profiles`);
    return response.data;
  }
};

export default profileService; 