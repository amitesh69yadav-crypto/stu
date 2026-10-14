import React, { useState } from 'react';
import { Subject, TimetableSession } from '../types';
import { addSessionToFirestore, deleteSessionFromFirestore } from '../services/db';
import { Calendar as CalendarIcon, Plus, Trash2, Clock, MapPin } from 'lucide-react';

interface TimetableScreenProps {
  userId: string;
  subjects: Subject[];
  sessions: TimetableSession[];
}

export const TimetableScreen: React.FC<TimetableScreenProps> = ({ userId, subjects, sessions }) => {
  const today = new Date().toISOString().split('T')[0];
  const [selectedDate, setSelectedDate] = useState(today);
  const [isAdding, setIsAdding] = useState(false);

  const [subjectId, setSubjectId] = useState(subjects[0]?.id || '');
  const [subjectName, setSubjectName] = useState(subjects[0]?.name || '');
  const [topic, setTopic] = useState('');
  const [date, setDate] = useState(today);
  const [startTime, setStartTime] = useState('09:00');
  const [endTime, setEndTime] = useState('10:30');
  const [location, setLocation] = useState('Campus Lecture Hall');
  const [notes, setNotes] = useState('');

  const filteredSessions = sessions.filter(s => s.date === selectedDate);

  const handleAddSession = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!topic.trim()) return;

    const sub = subjects.find(s => s.id === subjectId);
    await addSessionToFirestore(userId, {
      subjectId: subjectId || '1',
      subjectName: sub ? sub.name : 'General',
      topic,
      date,
      startTime,
      endTime,
      location,
      notes,
      recurringDays: '',
      status: 'SCHEDULED',
      isAlarmEnabled: true
    });

    setTopic('');
    setNotes('');
    setIsAdding(false);
  };

  const handleDelete = async (id: string) => {
    await deleteSessionFromFirestore(userId, id);
  };

  return (
    <div className="p-8 space-y-8 max-w-7xl mx-auto">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold text-[#1D1B20]">Study Timetable</h1>
          <p className="text-sm text-[#49454F]">Manage daily lectures, study blocks, and reminders synced across devices.</p>
        </div>
        <div className="flex items-center space-x-3">
          <input
            type="date"
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
            className="px-4 py-2.5 rounded-full bg-[#FEF7FF] border border-[#79747E]/30 text-sm font-medium text-[#1D1B20] shadow-sm"
          />
          <button
            onClick={() => setIsAdding(true)}
            className="px-5 py-2.5 rounded-full bg-[#6750A4] text-white font-medium hover:bg-[#523d85] transition shadow-sm flex items-center space-x-2"
          >
            <Plus className="w-4 h-4" />
            <span>Add Session</span>
          </button>
        </div>
      </div>

      {/* Add Session Modal */}
      {isAdding && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-[#FEF7FF] rounded-3xl p-8 max-w-lg w-full shadow-2xl border border-[#E7E0EC] space-y-6">
            <h2 className="text-xl font-bold text-[#1D1B20]">Add Timetable Session</h2>
            <form onSubmit={handleAddSession} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-[#49454F] mb-1">Subject</label>
                <select
                  value={subjectId}
                  onChange={(e) => {
                    setSubjectId(e.target.value);
                    const s = subjects.find(sub => sub.id === e.target.value);
                    if (s) setSubjectName(s.name);
                  }}
                  className="w-full px-4 py-3 rounded-2xl bg-[#F3EDF7] border border-[#E7E0EC] text-sm text-[#1D1B20]"
                >
                  {subjects.map(s => (
                    <option key={s.id} value={s.id}>{s.name} ({s.code})</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold text-[#49454F] mb-1">Topic / Description</label>
                <input
                  type="text"
                  required
                  value={topic}
                  onChange={(e) => setTopic(e.target.value)}
                  placeholder="e.g., AVL Tree Rotations"
                  className="w-full px-4 py-3 rounded-2xl bg-[#F3EDF7] border border-[#E7E0EC] text-sm text-[#1D1B20]"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-[#49454F] mb-1">Date</label>
                  <input
                    type="date"
                    required
                    value={date}
                    onChange={(e) => setDate(e.target.value)}
                    className="w-full px-4 py-3 rounded-2xl bg-[#F3EDF7] border border-[#E7E0EC] text-sm text-[#1D1B20]"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-[#49454F] mb-1">Location</label>
                  <input
                    type="text"
                    value={location}
                    onChange={(e) => setLocation(e.target.value)}
                    className="w-full px-4 py-3 rounded-2xl bg-[#F3EDF7] border border-[#E7E0EC] text-sm text-[#1D1B20]"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-[#49454F] mb-1">Start Time</label>
                  <input
                    type="time"
                    required
                    value={startTime}
                    onChange={(e) => setStartTime(e.target.value)}
                    className="w-full px-4 py-3 rounded-2xl bg-[#F3EDF7] border border-[#E7E0EC] text-sm text-[#1D1B20]"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-[#49454F] mb-1">End Time</label>
                  <input
                    type="time"
                    required
                    value={endTime}
                    onChange={(e) => setEndTime(e.target.value)}
                    className="w-full px-4 py-3 rounded-2xl bg-[#F3EDF7] border border-[#E7E0EC] text-sm text-[#1D1B20]"
                  />
                </div>
              </div>

              <div className="flex justify-end space-x-3 pt-4">
                <button
                  type="button"
                  onClick={() => setIsAdding(false)}
                  className="px-6 py-2.5 rounded-full border border-[#79747E]/30 text-sm font-medium text-[#49454F]"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-6 py-2.5 rounded-full bg-[#6750A4] text-white text-sm font-medium hover:bg-[#523d85] shadow-sm"
                >
                  Save Session
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Sessions List */}
      <div className="space-y-4">
        <h2 className="text-xl font-bold text-[#1D1B20]">Sessions for {selectedDate}</h2>
        {filteredSessions.length === 0 ? (
          <div className="bg-[#FEF7FF] rounded-3xl p-12 text-center border border-[#E7E0EC]">
            <CalendarIcon className="w-12 h-12 text-[#79747E] mx-auto mb-3" />
            <p className="text-sm font-medium text-[#49454F]">No study sessions found for this date.</p>
            <p className="text-xs text-[#79747E] mt-1">Click "Add Session" to schedule your study block.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredSessions.map((session) => (
              <div key={session.id} className="bg-[#FEF7FF] rounded-3xl p-6 border border-[#E7E0EC] shadow-sm flex flex-col justify-between space-y-4">
                <div className="space-y-2">
                  <div className="flex justify-between items-start">
                    <span className="text-xs font-bold text-[#6750A4] bg-[#EADDFF] px-2.5 py-1 rounded-full">
                      {session.subjectName}
                    </span>
                    <button
                      onClick={() => handleDelete(session.id)}
                      className="text-[#79747E] hover:text-[#B3261E] transition p-1"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                  <h3 className="font-bold text-[#1D1B20] text-base">{session.topic}</h3>
                  <div className="flex items-center space-x-2 text-xs text-[#79747E]">
                    <Clock className="w-3.5 h-3.5 text-[#6750A4]" />
                    <span>{session.startTime} - {session.endTime}</span>
                  </div>
                  <div className="flex items-center space-x-2 text-xs text-[#79747E]">
                    <MapPin className="w-3.5 h-3.5 text-[#6750A4]" />
                    <span>{session.location}</span>
                  </div>
                </div>
                {session.notes && (
                  <p className="text-xs text-[#49454F] bg-[#F3EDF7] p-3 rounded-2xl">
                    {session.notes}
                  </p>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
