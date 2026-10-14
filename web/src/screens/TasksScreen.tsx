import React, { useState } from 'react';
import { Subject, StudyTask, StudyTopic } from '../types';
import { addTaskToFirestore, updateTaskInFirestore, deleteTaskFromFirestore, addTopicToFirestore, deleteTopicFromFirestore } from '../services/db';
import { CheckSquare, Plus, Trash2, CheckCircle, Circle, BookOpen, Award } from 'lucide-react';

interface TasksScreenProps {
  userId: string;
  subjects: Subject[];
  tasks: StudyTask[];
  topics: StudyTopic[];
}

export const TasksScreen: React.FC<TasksScreenProps> = ({ userId, subjects, tasks, topics }) => {
  const [activeSubTab, setActiveSubTab] = useState<'tasks' | 'topics'>('tasks');
  const [isAddingTask, setIsAddingTask] = useState(false);
  const [isAddingTopic, setIsAddingTopic] = useState(false);

  // Task form state
  const [taskTitle, setTaskTitle] = useState('');
  const [taskSubjectName, setTaskSubjectName] = useState(subjects[0]?.name || 'General');
  const [taskDueDate, setTaskDueDate] = useState(new Date().toISOString().split('T')[0]);
  const [taskPriority, setTaskPriority] = useState('MEDIUM');
  const [taskType, setTaskType] = useState('ASSIGNMENT');

  // Topic form state
  const [topicTitle, setTopicTitle] = useState('');
  const [topicSubjectId, setTopicSubjectId] = useState(subjects[0]?.id || '');
  const [topicSubjectName, setTopicSubjectName] = useState(subjects[0]?.name || '');
  const [topicNotes, setTopicNotes] = useState('');

  const handleAddTask = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!taskTitle.trim()) return;
    await addTaskToFirestore(userId, {
      subjectName: taskSubjectName,
      title: taskTitle,
      dueDate: taskDueDate,
      type: taskType,
      isCompleted: false,
      priority: taskPriority
    });
    setTaskTitle('');
    setIsAddingTask(false);
  };

  const handleToggleTask = async (task: StudyTask) => {
    await updateTaskInFirestore(userId, task.id, { isCompleted: !task.isCompleted });
  };

  const handleDeleteTask = async (id: string) => {
    await deleteTaskFromFirestore(userId, id);
  };

  const handleAddTopic = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!topicTitle.trim()) return;
    const sub = subjects.find(s => s.id === topicSubjectId);
    await addTopicToFirestore(userId, {
      subjectId: topicSubjectId || '1',
      subjectName: sub ? sub.name : topicSubjectName,
      title: topicTitle,
      dateStudied: new Date().toISOString().split('T')[0],
      descriptionNotes: topicNotes,
      masteryLevel: 'UNTESTED',
      repetitionIntervalDays: 1,
      nextRevisionDate: new Date().toISOString().split('T')[0],
      timesTested: 0,
      timesCorrect: 0
    });
    setTopicTitle('');
    setTopicNotes('');
    setIsAddingTopic(false);
  };

  const handleDeleteTopic = async (id: string) => {
    await deleteTopicFromFirestore(userId, id);
  };

  return (
    <div className="p-8 space-y-8 max-w-7xl mx-auto">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold text-[#1D1B20]">Tasks & Topics</h1>
          <p className="text-sm text-[#49454F]">Track assignments, exams, and study topics synced in real time.</p>
        </div>
        <div className="flex items-center space-x-3">
          <div className="bg-[#E7E0EC] p-1 rounded-full flex space-x-1">
            <button
              onClick={() => setActiveSubTab('tasks')}
              className={`px-5 py-2 rounded-full text-xs font-bold transition ${
                activeSubTab === 'tasks' ? 'bg-[#6750A4] text-white shadow-sm' : 'text-[#49454F]'
              }`}
            >
              Tasks ({tasks.length})
            </button>
            <button
              onClick={() => setActiveSubTab('topics')}
              className={`px-5 py-2 rounded-full text-xs font-bold transition ${
                activeSubTab === 'topics' ? 'bg-[#6750A4] text-white shadow-sm' : 'text-[#49454F]'
              }`}
            >
              Study Topics ({topics.length})
            </button>
          </div>
          <button
            onClick={() => activeSubTab === 'tasks' ? setIsAddingTask(true) : setIsAddingTopic(true)}
            className="px-5 py-2.5 rounded-full bg-[#6750A4] text-white font-medium hover:bg-[#523d85] transition shadow-sm flex items-center space-x-2"
          >
            <Plus className="w-4 h-4" />
            <span>{activeSubTab === 'tasks' ? 'Add Task' : 'Add Topic'}</span>
          </button>
        </div>
      </div>

      {/* Add Task Modal */}
      {isAddingTask && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-[#FEF7FF] rounded-3xl p-8 max-w-lg w-full shadow-2xl border border-[#E7E0EC] space-y-6">
            <h2 className="text-xl font-bold text-[#1D1B20]">Add Study Task</h2>
            <form onSubmit={handleAddTask} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-[#49454F] mb-1">Title</label>
                <input
                  type="text"
                  required
                  value={taskTitle}
                  onChange={(e) => setTaskTitle(e.target.value)}
                  placeholder="e.g., Problem Set 4"
                  className="w-full px-4 py-3 rounded-2xl bg-[#F3EDF7] border border-[#E7E0EC] text-sm text-[#1D1B20]"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-[#49454F] mb-1">Subject Name</label>
                <select
                  value={taskSubjectName}
                  onChange={(e) => setTaskSubjectName(e.target.value)}
                  className="w-full px-4 py-3 rounded-2xl bg-[#F3EDF7] border border-[#E7E0EC] text-sm text-[#1D1B20]"
                >
                  {subjects.map(s => (
                    <option key={s.id} value={s.name}>{s.name}</option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-[#49454F] mb-1">Due Date</label>
                  <input
                    type="date"
                    required
                    value={taskDueDate}
                    onChange={(e) => setTaskDueDate(e.target.value)}
                    className="w-full px-4 py-3 rounded-2xl bg-[#F3EDF7] border border-[#E7E0EC] text-sm text-[#1D1B20]"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-[#49454F] mb-1">Priority</label>
                  <select
                    value={taskPriority}
                    onChange={(e) => setTaskPriority(e.target.value)}
                    className="w-full px-4 py-3 rounded-2xl bg-[#F3EDF7] border border-[#E7E0EC] text-sm text-[#1D1B20]"
                  >
                    <option value="HIGH">High</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="LOW">Low</option>
                  </select>
                </div>
              </div>

              <div className="flex justify-end space-x-3 pt-4">
                <button
                  type="button"
                  onClick={() => setIsAddingTask(false)}
                  className="px-6 py-2.5 rounded-full border border-[#79747E]/30 text-sm font-medium text-[#49454F]"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-6 py-2.5 rounded-full bg-[#6750A4] text-white text-sm font-medium hover:bg-[#523d85]"
                >
                  Save Task
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Add Topic Modal */}
      {isAddingTopic && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-[#FEF7FF] rounded-3xl p-8 max-w-lg w-full shadow-2xl border border-[#E7E0EC] space-y-6">
            <h2 className="text-xl font-bold text-[#1D1B20]">Add Study Topic</h2>
            <form onSubmit={handleAddTopic} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-[#49454F] mb-1">Topic Title</label>
                <input
                  type="text"
                  required
                  value={topicTitle}
                  onChange={(e) => setTopicTitle(e.target.value)}
                  placeholder="e.g., AVL Tree Rotations"
                  className="w-full px-4 py-3 rounded-2xl bg-[#F3EDF7] border border-[#E7E0EC] text-sm text-[#1D1B20]"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-[#49454F] mb-1">Subject</label>
                <select
                  value={topicSubjectId}
                  onChange={(e) => {
                    setTopicSubjectId(e.target.value);
                    const s = subjects.find(sub => sub.id === e.target.value);
                    if (s) setTopicSubjectName(s.name);
                  }}
                  className="w-full px-4 py-3 rounded-2xl bg-[#F3EDF7] border border-[#E7E0EC] text-sm text-[#1D1B20]"
                >
                  {subjects.map(s => (
                    <option key={s.id} value={s.id}>{s.name}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold text-[#49454F] mb-1">Description & Notes</label>
                <textarea
                  rows={3}
                  value={topicNotes}
                  onChange={(e) => setTopicNotes(e.target.value)}
                  placeholder="Summary of key concepts..."
                  className="w-full px-4 py-3 rounded-2xl bg-[#F3EDF7] border border-[#E7E0EC] text-sm text-[#1D1B20]"
                />
              </div>

              <div className="flex justify-end space-x-3 pt-4">
                <button
                  type="button"
                  onClick={() => setIsAddingTopic(false)}
                  className="px-6 py-2.5 rounded-full border border-[#79747E]/30 text-sm font-medium text-[#49454F]"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-6 py-2.5 rounded-full bg-[#6750A4] text-white text-sm font-medium hover:bg-[#523d85]"
                >
                  Save Topic
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Content Rendering */}
      {activeSubTab === 'tasks' ? (
        <div className="space-y-4">
          {tasks.length === 0 ? (
            <div className="bg-[#FEF7FF] rounded-3xl p-12 text-center border border-[#E7E0EC]">
              <CheckSquare className="w-12 h-12 text-[#79747E] mx-auto mb-3" />
              <p className="text-sm font-medium text-[#49454F]">No tasks found.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {tasks.map((task) => (
                <div key={task.id} className="bg-[#FEF7FF] rounded-3xl p-6 border border-[#E7E0EC] shadow-sm flex flex-col justify-between space-y-4">
                  <div className="space-y-2">
                    <div className="flex justify-between items-start">
                      <span className="text-xs font-bold text-[#6750A4] bg-[#EADDFF] px-2.5 py-1 rounded-full">
                        {task.subjectName}
                      </span>
                      <button onClick={() => handleDeleteTask(task.id)} className="text-[#79747E] hover:text-[#B3261E] transition">
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                    <h3 className={`font-bold text-base ${task.isCompleted ? 'line-through text-[#79747E]' : 'text-[#1D1B20]'}`}>
                      {task.title}
                    </h3>
                    <p className="text-xs text-[#B3261E]">Due: {task.dueDate}</p>
                  </div>
                  <div className="flex items-center justify-between pt-2 border-t border-[#E7E0EC]">
                    <button
                      onClick={() => handleToggleTask(task)}
                      className="flex items-center space-x-2 text-xs font-semibold text-[#6750A4] hover:underline"
                    >
                      {task.isCompleted ? <CheckCircle className="w-4 h-4 text-green-600" /> : <Circle className="w-4 h-4" />}
                      <span>{task.isCompleted ? 'Completed' : 'Mark Complete'}</span>
                    </button>
                    <span className={`text-[10px] px-2.5 py-1 rounded-full font-bold ${
                      task.priority === 'HIGH' ? 'bg-[#F9DEDC] text-[#B3261E]' : 'bg-[#EADDFF] text-[#21005D]'
                    }`}>
                      {task.priority}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      ) : (
        <div className="space-y-4">
          {topics.length === 0 ? (
            <div className="bg-[#FEF7FF] rounded-3xl p-12 text-center border border-[#E7E0EC]">
              <BookOpen className="w-12 h-12 text-[#79747E] mx-auto mb-3" />
              <p className="text-sm font-medium text-[#49454F]">No study topics found.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {topics.map((topic) => (
                <div key={topic.id} className="bg-[#FEF7FF] rounded-3xl p-6 border border-[#E7E0EC] shadow-sm flex flex-col justify-between space-y-4">
                  <div className="space-y-2">
                    <div className="flex justify-between items-start">
                      <span className="text-xs font-bold text-[#6750A4] bg-[#EADDFF] px-2.5 py-1 rounded-full">
                        {topic.subjectName}
                      </span>
                      <button onClick={() => handleDeleteTopic(topic.id)} className="text-[#79747E] hover:text-[#B3261E] transition">
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                    <h3 className="font-bold text-[#1D1B20] text-base">{topic.title}</h3>
                    <p className="text-xs text-[#49454F] bg-[#F3EDF7] p-3 rounded-2xl">{topic.descriptionNotes || 'No notes added.'}</p>
                  </div>
                  <div className="flex items-center justify-between text-xs pt-2 border-t border-[#E7E0EC]">
                    <span className="text-[#79747E]">Mastery:</span>
                    <span className="font-bold text-[#6750A4] uppercase text-[11px] bg-[#EADDFF] px-2.5 py-1 rounded-full">
                      {topic.masteryLevel}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
};
