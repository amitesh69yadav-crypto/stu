export interface Subject {
  id: string;
  userId: string;
  name: string;
  code: string;
  colorHex: string;
  createdAt: number;
}

export interface TimetableSession {
  id: string;
  userId: string;
  subjectId: string;
  subjectName: string;
  topic: string;
  date: string; // YYYY-MM-DD
  startTime: string; // HH:mm
  endTime: string; // HH:mm
  location: string;
  notes: string;
  recurringDays: string;
  status: string; // SCHEDULED, COMPLETED, MISSED
  isAlarmEnabled: boolean;
}

export interface StudyTopic {
  id: string;
  userId: string;
  subjectId: string;
  subjectName: string;
  title: string;
  dateStudied: string; // YYYY-MM-DD
  descriptionNotes: string;
  masteryLevel: string; // UNTESTED, STRUGGLING, FAMILIAR, MASTERED
  repetitionIntervalDays: number;
  nextRevisionDate: string;
  timesTested: number;
  timesCorrect: number;
}

export interface StudyTask {
  id: string;
  userId: string;
  subjectName: string;
  title: string;
  dueDate: string; // YYYY-MM-DD
  type: string; // ASSIGNMENT, EXAM, TASK
  isCompleted: boolean;
  priority: string; // HIGH, MEDIUM, LOW
}

export interface QuizAttempt {
  id: string;
  userId: string;
  title: string;
  dateAttempted: string;
  totalQuestions: number;
  correctCount: number;
  scorePercentage: number;
}

export interface TutorSession {
  id: string;
  userId: string;
  subjectName: string;
  title: string;
  previewSnippet: string;
  createdAt: number;
}

export interface TutorMessage {
  id: string;
  sessionId: string;
  role: 'user' | 'model';
  text: string;
  timestamp: number;
}
