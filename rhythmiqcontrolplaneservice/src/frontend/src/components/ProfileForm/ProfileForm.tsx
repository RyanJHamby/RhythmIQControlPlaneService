import React, { useState, useEffect } from 'react';
import { Profile, CreateProfileRequest, UpdateProfileRequest } from '../../services/profileService';
import { FormFields } from './FormFields';
import { FormActions } from './FormActions';

interface ProfileFormProps {
  profileId?: string;
  onSubmit: (data: CreateProfileRequest | UpdateProfileRequest) => Promise<void>;
  onCancel: () => void;
}

export const ProfileForm: React.FC<ProfileFormProps> = ({ profileId, onSubmit, onCancel }) => {
  const [formData, setFormData] = useState<CreateProfileRequest>({
    username: '',
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '',
    password: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (profileId) {
      fetchProfile();
    }
  }, [profileId]);

  const fetchProfile = async () => {
    try {
      setLoading(true);
      const response = await fetch(`/api/profiles/${profileId}`);
      if (!response.ok) throw new Error('Failed to fetch profile');
      const data: Profile = await response.json();
      setFormData({
        username: data.username,
        firstName: data.firstName,
        lastName: data.lastName,
        email: data.email,
        phoneNumber: data.phoneNumber || '',
        password: ''
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch profile');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const submitData = profileId 
        ? { ...formData, email: undefined, password: undefined }
        : formData;
      
      await onSubmit(submitData);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save profile');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="text-center">Loading...</div>;
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      )}
      
      <FormFields 
        formData={formData}
        handleChange={handleChange}
        isEditing={!!profileId}
      />
      
      <FormActions 
        onCancel={onCancel}
        loading={loading}
      />
    </form>
  );
}; 