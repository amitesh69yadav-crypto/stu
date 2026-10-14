import React from 'react';
import { LayoutDashboard, Calendar, CheckSquare, BookOpen, MessageSquareText, Award, BarChart3, Settings, LogOut } from 'lucide-react';

interface SidebarProps {
  currentTab: string;
  setCurrentTab: (tab: string) => void;
  onSignOut: () => void;
  userEmail?: string;
}

export const Sidebar: React.FC<SidebarProps> = ({ currentTab, setCurrentTab, onSignOut, userEmail }) => {
  const menuItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'timetable', label: 'Timetable', icon: Calendar },
    { id: 'tasks', label: 'Tasks & Topics', icon: CheckSquare },
    { id: 'ai-tutor', label: 'AI Tutor', icon: MessageSquareText },
    { id: 'quiz', label: 'Quizzes', icon: Award },
    { id: 'progress', label: 'Progress', icon: BarChart3 },
    { id: 'settings', label: 'Settings', icon: Settings },
  ];

  return (
    <aside className="w-72 bg-[#FEF7FF] border-r border-[#E7E0EC] flex flex-col h-screen sticky top-0 shadow-sm">
      {/* App Branding */}
      <div className="p-6 flex items-center space-x-3 border-b border-[#E7E0EC]">
        <div className="w-10 h-10 rounded-xl bg-[#6750A4] text-white flex items-center justify-center font-bold text-xl shadow-md">
          SP
        </div>
        <div>
          <h1 className="font-bold text-[#1D1B20] text-lg leading-tight">StudyPilot AI</h1>
          <p className="text-xs text-[#79747E]">Academic Companion</p>
        </div>
      </div>

      {/* Navigation Items */}
      <nav className="flex-1 px-4 py-6 space-y-1.5 overflow-y-auto">
        {menuItems.map((item) => {
          const Icon = item.icon;
          const isActive = currentTab === item.id;
          return (
            <button
              key={item.id}
              onClick={() => setCurrentTab(item.id)}
              className={`w-full flex items-center space-x-3 px-4 py-3 rounded-full text-sm font-medium transition-all ${
                isActive
                  ? 'bg-[#EADDFF] text-[#21005D]'
                  : 'text-[#49454F] hover:bg-[#F3EDF7] hover:text-[#1D1B20]'
              }`}
            >
              <Icon className={`w-5 h-5 ${isActive ? 'text-[#21005D]' : 'text-[#49454F]'}`} />
              <span>{item.label}</span>
            </button>
          );
        })}
      </nav>

      {/* User Footer / Sign Out */}
      <div className="p-4 border-t border-[#E7E0EC] bg-[#F3EDF7]/50">
        <div className="flex items-center justify-between mb-3 px-2">
          <div className="truncate">
            <p className="text-xs font-semibold text-[#1D1B20] truncate">{userEmail || 'Scholar User'}</p>
            <p className="text-[10px] text-[#79747E]">Synced via Firebase</p>
          </div>
        </div>
        <button
          onClick={onSignOut}
          className="w-full flex items-center justify-center space-x-2 px-4 py-2.5 rounded-full border border-[#79747E]/30 text-[#B3261E] hover:bg-[#F9DEDC]/50 text-sm font-medium transition"
        >
          <LogOut className="w-4 h-4" />
          <span>Sign Out</span>
        </button>
      </div>
    </aside>
  );
};
