import React, { useState } from 'react';
import { auth, googleProvider, signInWithPopup } from '../firebase';
import { Sparkles, GraduationCap } from 'lucide-react';

interface LoginScreenProps {
  onLoginSuccess: () => void;
}

export const LoginScreen: React.FC<LoginScreenProps> = ({ onLoginSuccess }) => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleGoogleSignIn = async () => {
    setIsLoading(true);
    setError(null);
    try {
      await signInWithPopup(auth, googleProvider);
      onLoginSuccess();
    } catch (err: any) {
      console.error("Sign in error:", err);
      setError(err.message || "Failed to sign in with Google.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#F3EDF7] flex items-center justify-center p-6">
      <div className="max-w-md w-full bg-[#FEF7FF] rounded-3xl p-8 shadow-xl border border-[#E7E0EC] text-center">
        <div className="w-16 h-16 bg-[#6750A4] text-white rounded-2xl mx-auto flex items-center justify-center shadow-lg mb-6">
          <GraduationCap className="w-8 h-8" />
        </div>
        
        <h1 className="text-2xl font-bold text-[#1D1B20] mb-2">StudyPilot AI</h1>
        <p className="text-sm text-[#49454F] mb-8">
          Your AI-powered academic companion. Log in with Google to sync your study schedule, tasks, and notes across all your devices in real time.
        </p>

        {error && (
          <div className="mb-6 p-4 rounded-xl bg-[#F9DEDC] text-[#B3261E] text-xs font-medium">
            {error}
          </div>
        )}

        <button
          onClick={handleGoogleSignIn}
          disabled={isLoading}
          className="w-full py-3.5 px-6 rounded-full bg-[#6750A4] text-white font-medium hover:bg-[#523d85] transition shadow-md flex items-center justify-center space-x-3 disabled:opacity-50"
        >
          <Sparkles className="w-5 h-5" />
          <span>{isLoading ? 'Signing in...' : 'Sign in with Google'}</span>
        </button>

        <p className="mt-6 text-[11px] text-[#79747E]">
          Connected to Firebase Cloud Firestore. Same user ID syncs across Android & Web.
        </p>
      </div>
    </div>
  );
};
