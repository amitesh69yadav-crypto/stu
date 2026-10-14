import React, { useState, useEffect } from 'react';
import { auth, onAuthStateChanged, signOut as fbSignOut, User } from './firebase';
import { LoginScreen } from './screens/LoginScreen';
import { DashboardScreen } from './screens/DashboardScreen';
import { TimetableScreen } from './screens/TimetableScreen';
import { TasksScreen } from './screens/TasksScreen';
import { AiTutorScreen } from './screens/AiTutorScreen';
import { QuizScreen } from './screens/QuizScreen';
import { ProgressScreen } from './screens/ProgressScreen';
import { SettingsScreen } from './screens/SettingsScreen';
import { Sidebar } from './components/Sidebar';
import { Subject, TimetableSession, StudyTask, StudyTopic, QuizAttempt } from './types';
import { subscribeToSubjects, subscribeToSessions, subscribeToTasks, subscribeToTopics, subscribeToQuizAttempts, seedInitialDataIfEmpty } from './services/db';

export function App() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [currentTab, setCurrentTab] = useState('dashboard');

  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [sessions, setSessions] = useState<TimetableSession[]>([]);
  const [tasks, setTasks] = useState<StudyTask[]>([]);
  const [topics, setTopics] = useState<StudyTopic[]>([]);
  const [quizAttempts, setQuizAttempts] = useState<QuizAttempt[]>([]);

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (currentUser) => {
      setUser(currentUser);
      setLoading(false);
      if (currentUser) {
        await seedInitialDataIfEmpty(currentUser.uid);
      }
    });
    return () => unsubscribe();
  }, []);

  useEffect(() => {
    if (!user) return;

    const unsubSubjects = subscribeToSubjects(user.uid, setSubjects);
    const unsubSessions = subscribeToSessions(user.uid, setSessions);
    const unsubTasks = subscribeToTasks(user.uid, setTasks);
    const unsubTopics = subscribeToTopics(user.uid, setTopics);
    const unsubAttempts = subscribeToQuizAttempts(user.uid, setQuizAttempts);

    return () => {
      unsubSubjects();
      unsubSessions();
      unsubTasks();
      unsubTopics();
      unsubAttempts();
    };
  }, [user]);

  const handleSignOut = async () => {
    await fbSignOut(auth);
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-[#F3EDF7] flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#6750A4]"></div>
      </div>
    );
  }

  if (!user) {
    return <LoginScreen onLoginSuccess={() => {}} />;
  }

  return (
    <div className="flex min-h-screen bg-[#F3EDF7]">
      <Sidebar
        currentTab={currentTab}
        setCurrentTab={setCurrentTab}
        onSignOut={handleSignOut}
        userEmail={user.email || undefined}
      />
      <main className="flex-1 overflow-y-auto">
        {currentTab === 'dashboard' && (
          <DashboardScreen
            subjects={subjects}
            sessions={sessions}
            tasks={tasks}
            topics={topics}
            onNavigate={setCurrentTab}
          />
        )}
        {currentTab === 'timetable' && (
          <TimetableScreen
            userId={user.uid}
            subjects={subjects}
            sessions={sessions}
          />
        )}
        {currentTab === 'tasks' && (
          <TasksScreen
            userId={user.uid}
            subjects={subjects}
            tasks={tasks}
            topics={topics}
          />
        )}
        {currentTab === 'ai-tutor' && <AiTutorScreen />}
        {currentTab === 'quiz' && (
          <QuizScreen
            userId={user.uid}
            topics={topics}
            attempts={quizAttempts}
          />
        )}
        {currentTab === 'progress' && (
          <ProgressScreen
            topics={topics}
            attempts={quizAttempts}
            tasks={tasks}
          />
        )}
        {currentTab === 'settings' && (
          <SettingsScreen
            user={user}
            onSignOut={handleSignOut}
          />
        )}
      </main>
    </div>
  );
}

export default App;
