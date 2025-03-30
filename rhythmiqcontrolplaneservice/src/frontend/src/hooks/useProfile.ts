import { useState, useCallback } from 'react';
import profileService, { Profile, CreateProfileRequest, UpdateProfileRequest } from '../services/profileService';

export const useProfile = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [profile, setProfile] = useState<Profile | null>(null);

  const createProfile = useCallback(async (data: CreateProfileRequest) => {
    try {
      setLoading(true);
      setError(null);
      const response = await profileService.createProfile(data);
      return response;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create profile');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const getProfile = useCallback(async (profileId: string) => {
    try {
      setLoading(true);
      setError(null);
      const response = await profileService.getProfile(profileId);
      if (response.success && response.profile) {
        setProfile(response.profile);
      }
      return response;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get profile');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const updateProfile = useCallback(async (profileId: string, data: UpdateProfileRequest) => {
    try {
      setLoading(true);
      setError(null);
      const response = await profileService.updateProfile(profileId, data);
      return response;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update profile');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const deleteProfile = useCallback(async (profileId: string) => {
    try {
      setLoading(true);
      setError(null);
      await profileService.deleteProfile(profileId);
      setProfile(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete profile');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    profile,
    loading,
    error,
    createProfile,
    getProfile,
    updateProfile,
    deleteProfile
  };
}; 