import React from 'react';
import { StudyTopic, QuizAttempt, StudyTask } from '../types';
import { BarChart3, Award, CheckCircle2, BookOpen, Flame } from 'lucide-react';

interface ProgressScreenProps {
  topics: StudyTopic[];
  attempts: QuizAttempt[];
  tasks: StudyTask[];
}

export const ProgressScreen: React.FC<ProgressScreenProps> = ({ topics, attempts, tasks }) => {
  const completedTasksCount = tasks.filter(t => t.isCompleted).length;
  const masteredTopicsCount = topics.filter(t => t.masteryLevel === 'MASTERED').length;

  return (
    <div className="p-8 space-y-8 max-w-7xl mx-auto">
      <div>
        <h1 className="text-3xl font-bold text-[#1D1B20]">Study Progress & Analytics</h1>
        <p className="text-sm text-[#49454F]">Monitor mastery levels, quiz performance, and task completion.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div className="bg-[#FEF7FF] rounded-3xl p-6 border border-[#E7E0EC] shadow-sm space-y-2">
          <Flame className="w-6 h-6 text-[#6750A4]" />
          <p className="text-2xl font-bold text-[#1D1B20]">5 Days</p>
          <p className="text-xs text-[#79747E]">Current Streak</p>
        </div>
        <div className="bg-[#FEF7FF] rounded-3xl p-6 border border-[#E7E0EC] shadow-sm space-y-2">
          <Award className="w-6 h-6 text-[#6750A4]" />
          <p className="text-2xl font-bold text-[#1D1B20]">{masteredTopicsCount} / {topics.length}</p>
          <p className="text-xs text-[#79747E]">Mastered Topics</p>
        </div>
        <div className="bg-[#FEF7FF] rounded-3xl p-6 border border-[#E7E0EC] shadow-sm space-y-2">
          <CheckCircle2 className="w-6 h-6 text-[#6750A4]" />
          <p className="text-2xl font-bold text-[#1D1B20]">{completedTasksCount} / {tasks.length}</p>
          <p className="text-xs text-[#79747E]">Completed Tasks</p>
        </div>
        <div className="bg-[#FEF7FF] rounded-3xl p-6 border border-[#E7E0EC] shadow-sm space-y-2">
          <BookOpen className="w-6 h-6 text-[#6750A4]" />
          <p className="text-2xl font-bold text-[#1D1B20]">{attempts.length}</p>
          <p className="text-xs text-[#79747E]">Quizzes Taken</p>
        </div>
      </div>

      <div className="bg-[#FEF7FF] rounded-3xl p-8 border border-[#E7E0EC] shadow-sm space-y-6">
        <h2 className="text-xl font-bold text-[#1D1B20]">Topic Mastery Breakdown</h2>
        <div className="space-y-4">
          {topics.map(topic => (
            <div key={topic.id} className="p-4 rounded-2xl bg-[#F3EDF7] border border-[#E7E0EC] flex items-center justify-between">
              <div>
                <span className="text-xs font-bold text-[#6750A4]">{topic.subjectName}</span>
                <p className="text-sm font-semibold text-[#1D1B20]">{topic.title}</p>
              </div>
              <span className={`text-xs font-bold px-3 py-1 rounded-full ${
                topic.masteryLevel === 'MASTERED' ? 'bg-[#E9F4EE] text-green-700' :
                topic.masteryLevel === 'STRUGGLING' ? 'bg-[#F9DEDC] text-[#B3261E]' :
                'bg-[#EADDFF] text-[#21005D]'
              }`}>
                {topic.masteryLevel}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
