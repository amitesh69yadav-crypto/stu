import React from 'react';
import { Settings as SettingsIcon, User, Shield, Bell, RefreshCw } from 'lucide-react';
import { User as FirebaseUser } from '../firebase';

interface SettingsScreenProps {
  user: FirebaseUser | null;
  onSignOut: () => void;
}

export const SettingsScreen: React.FC<SettingsScreenProps> = ({ user, onSignOut }) => {
  return (
    <div className="p-8 space-y-8 max-w-5xl mx-auto">
      <div>
        <h1 className="text-3xl font-bold text-[#1D1B20]">Settings & Account</h1>
        <p className="text-sm text-[#49454F]">Manage account sync, preferences, and authentication.</p>
      </div>

      <div className="bg-[#FEF7FF] rounded-3xl p-8 border border-[#E7E0EC] shadow-sm space-y-6">
        <div className="flex items-center space-x-4 pb-6 border-b border-[#E7E0EC]">
          <div className="w-16 h-16 rounded-2xl bg-[#6750A4] text-white flex items-center justify-center font-bold text-2xl shadow-md">
            {user?.email?.substring(0, 2).toUpperCase() || 'SP'}
          </div>
          <div>
            <h2 className="text-xl font-bold text-[#1D1B20]">{user?.email || 'Scholar User'}</h2>
            <p className="text-xs text-[#79747E]">Authenticated via Google Sign-In & Firebase Auth</p>
          </div>
        </div>

        <div className="space-y-4">
          <div className="flex items-center justify-between p-4 rounded-2xl bg-[#F3EDF7]">
            <div className="flex items-center space-x-3">
              <RefreshCw className="w-5 h-5 text-[#6750A4]" />
              <div>
                <p className="text-sm font-semibold text-[#1D1B20]">Cloud Synchronisation</p>
                <p className="text-xs text-[#79747E]">Real-time Firestore sync with Android app</p>
              </div>
            </div>
            <span className="text-xs font-bold text-green-700 bg-[#E9F4EE] px-3 py-1 rounded-full">Active</span>
          </div>

          <div className="flex items-center justify-between p-4 rounded-2xl bg-[#F3EDF7]">
            <div className="flex items-center space-x-3">
              <Shield className="w-5 h-5 text-[#6750A4]" />
              <div>
                <p className="text-sm font-semibold text-[#1D1B20]">Firebase Database ID</p>
                <p className="text-xs text-[#79747E]">ai-studio-android-studypil-1b9a5a78-c2bd-40f4-9199-e1b41747056c</p>
              </div>
            </div>
            <span className="text-xs font-bold text-[#21005D] bg-[#EADDFF] px-3 py-1 rounded-full">Secured</span>
          </div>
        </div>

        <div className="pt-4">
          <button
            onClick={onSignOut}
            className="px-6 py-3 rounded-full bg-[#B3261E] text-white text-sm font-medium hover:bg-[#8c1d18] transition shadow-sm"
          >
            Sign Out of Account
          </button>
        </div>
      </div>
    </div>
  );
};
