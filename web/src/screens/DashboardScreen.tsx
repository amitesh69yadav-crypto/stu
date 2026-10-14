import React from 'react';
import { Subject, TimetableSession, StudyTask, StudyTopic } from '../types';
import { Calendar, CheckCircle2, Clock, Sparkles, BookOpen, Flame, Award } from 'lucide-react';

interface DashboardScreenProps {
  subjects: Subject[];
  sessions: TimetableSession[];
  tasks: StudyTask[];
  topics: StudyTopic[];
  onNavigate: (tab: string) => void;
}

export const DashboardScreen: React.FC<DashboardScreenProps> = ({ subjects, sessions, tasks, topics, onNavigate }) => {
  const today = new Date().toISOString().split('T')[0];
  const todaySessions = sessions.filter(s => s.date === today);
  const pendingTasks = tasks.filter(t => !t.isCompleted);
  const strugglingTopics = topics.filter(t => t.masteryLevel === 'STRUGGLING' || t.timesTested === 0);

  return (
    <div className="p-8 space-y-8 max-w-7xl mx-auto">
      {/* Welcome Banner */}
      <div className="bg-gradient-to-r from-[#6750A4] to-[#4F378B] rounded-3xl p-8 text-white shadow-lg flex items-center justify-between">
        <div>
          <div className="flex items-center space-x-2 bg-white/10 px-3 py-1 rounded-full w-fit mb-3 text-xs font-medium">
            <Sparkles className="w-3.5 h-3.5" />
            <span>AI-Powered Academic Hub</span>
          </div>
          <h1 className="text-3xl font-bold mb-2">Welcome back, Scholar!</h1>
          <p className="text-[#EADDFF] text-sm max-w-xl">
            You have {todaySessions.length} sessions scheduled today and {pendingTasks.length} pending tasks. Keep up your momentum!
          </p>
        </div>
        <div className="hidden md:flex items-center space-x-4 bg-white/10 p-4 rounded-2xl backdrop-blur-md">
          <div className="text-center px-4 border-r border-white/20">
            <Flame className="w-6 h-6 text-[#FFB4AB] mx-auto mb-1" />
            <p className="text-2xl font-bold">5 Days</p>
            <p className="text-[10px] text-[#EADDFF]">Streak</p>
          </div>
          <div className="text-center px-4">
            <Award className="w-6 h-6 text-[#FFD8E4] mx-auto mb-1" />
            <p className="text-2xl font-bold">{topics.length}</p>
            <p className="text-[10px] text-[#EADDFF]">Topics</p>
          </div>
        </div>
      </div>

      {/* Grid of Main Highlights */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Today's Timetable */}
        <div className="bg-[#FEF7FF] rounded-3xl p-6 border border-[#E7E0EC] shadow-sm space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="font-bold text-[#1D1B20] flex items-center space-x-2">
              <Calendar className="w-5 h-5 text-[#6750A4]" />
              <span>Today's Timetable</span>
            </h2>
            <button onClick={() => onNavigate('timetable')} className="text-xs text-[#6750A4] font-semibold hover:underline">
              View All
            </button>
          </div>

          {todaySessions.length === 0 ? (
            <p className="text-xs text-[#79747E] py-6 text-center">No sessions scheduled for today. Add one in Timetable!</p>
          ) : (
            <div className="space-y-3">
              {todaySessions.map((session) => (
                <div key={session.id} className="p-4 rounded-2xl bg-[#F3EDF7] border border-[#E7E0EC] space-y-1">
                  <div className="flex justify-between items-start">
                    <span className="text-xs font-bold text-[#6750A4]">{session.subjectName}</span>
                    <span className="text-[11px] bg-[#EADDFF] text-[#21005D] px-2 py-0.5 rounded-full font-medium">
                      {session.startTime} - {session.endTime}
                    </span>
                  </div>
                  <p className="text-sm font-semibold text-[#1D1B20]">{session.topic}</p>
                  <p className="text-xs text-[#79747E]">{session.location}</p>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Pending Tasks */}
        <div className="bg-[#FEF7FF] rounded-3xl p-6 border border-[#E7E0EC] shadow-sm space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="font-bold text-[#1D1B20] flex items-center space-x-2">
              <CheckCircle2 className="w-5 h-5 text-[#6750A4]" />
              <span>Priority Tasks</span>
            </h2>
            <button onClick={() => onNavigate('tasks')} className="text-xs text-[#6750A4] font-semibold hover:underline">
              View All
            </button>
          </div>

          {pendingTasks.length === 0 ? (
            <p className="text-xs text-[#79747E] py-6 text-center">All tasks completed! Great job.</p>
          ) : (
            <div className="space-y-3">
              {pendingTasks.slice(0, 3).map((task) => (
                <div key={task.id} className="p-4 rounded-2xl bg-[#F3EDF7] border border-[#E7E0EC] flex items-center justify-between">
                  <div>
                    <span className="text-[10px] font-bold uppercase tracking-wider text-[#79747E]">{task.subjectName}</span>
                    <p className="text-sm font-semibold text-[#1D1B20]">{task.title}</p>
                    <p className="text-xs text-[#B3261E]">Due: {task.dueDate}</p>
                  </div>
                  <span className={`text-[10px] px-2.5 py-1 rounded-full font-bold ${
                    task.priority === 'HIGH' ? 'bg-[#F9DEDC] text-[#B3261E]' : 'bg-[#EADDFF] text-[#21005D]'
                  }`}>
                    {task.priority}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* AI Recommendations */}
        <div className="bg-[#FEF7FF] rounded-3xl p-6 border border-[#E7E0EC] shadow-sm space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="font-bold text-[#1D1B20] flex items-center space-x-2">
              <Sparkles className="w-5 h-5 text-[#6750A4]" />
              <span>What Should I Do Now?</span>
            </h2>
            <button onClick={() => onNavigate('ai-tutor')} className="text-xs text-[#6750A4] font-semibold hover:underline">
              AI Tutor
            </button>
          </div>

          <div className="p-4 rounded-2xl bg-[#EADDFF]/50 border border-[#6750A4]/20 space-y-3">
            <p className="text-xs font-medium text-[#21005D]">
              Recommended focus for your next study block:
            </p>
            {strugglingTopics.length > 0 ? (
              <div className="space-y-1">
                <p className="text-sm font-bold text-[#1D1B20]">{strugglingTopics[0].title}</p>
                <p className="text-xs text-[#49454F]">Subject: {strugglingTopics[0].subjectName}</p>
              </div>
            ) : (
              <p className="text-xs text-[#49454F]">Review your latest quiz results or start a practice session with Gemini AI.</p>
            )}
            <button
              onClick={() => onNavigate('ai-tutor')}
              className="w-full py-2 bg-[#6750A4] text-white rounded-full text-xs font-semibold hover:bg-[#523d85] transition shadow-sm"
            >
              Ask AI Tutor for Explanation
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
