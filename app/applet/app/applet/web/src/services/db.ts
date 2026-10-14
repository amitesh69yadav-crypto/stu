import { db } from '../firebase';
import { 
  collection, doc, getDocs, addDoc, updateDoc, deleteDoc, onSnapshot, query, where, orderBy, setDoc 
} from 'firebase/firestore';
import { Subject, TimetableSession, StudyTopic, StudyTask, QuizAttempt, TutorSession, TutorMessage } from '../types';

export const subscribeToSubjects = (userId: string, callback: (subjects: Subject[]) => void) => {
  const q = collection(db, `users/${userId}/subjects`);
  return onSnapshot(q, (snapshot) => {
    const list: Subject[] = [];
    snapshot.forEach((docSnap) => {
      list.push({ id: docSnap.id, ...docSnap.data() } as Subject);
    });
    callback(list);
  }, (error) => {
    console.error("Error subscribing to subjects:", error);
  });
};

export const addSubjectToFirestore = async (userId: string, subject: Omit<Subject, 'id' | 'userId'>) => {
  const ref = collection(db, `users/${userId}/subjects`);
  await addDoc(ref, { ...subject, userId, createdAt: Date.now() });
};

export const deleteSubjectFromFirestore = async (userId: string, subjectId: string) => {
  await deleteDoc(doc(db, `users/${userId}/subjects`, subjectId));
};

export const subscribeToSessions = (userId: string, callback: (sessions: TimetableSession[]) => void) => {
  const q = collection(db, `users/${userId}/sessions`);
  return onSnapshot(q, (snapshot) => {
    const list: TimetableSession[] = [];
    snapshot.forEach((docSnap) => {
      list.push({ id: docSnap.id, ...docSnap.data() } as TimetableSession);
    });
    callback(list);
  }, (error) => {
    console.error("Error subscribing to sessions:", error);
  });
};

export const addSessionToFirestore = async (userId: string, session: Omit<TimetableSession, 'id' | 'userId'>) => {
  const ref = collection(db, `users/${userId}/sessions`);
  await addDoc(ref, { ...session, userId });
};

export const updateSessionInFirestore = async (userId: string, sessionId: string, updates: Partial<TimetableSession>) => {
  await updateDoc(doc(db, `users/${userId}/sessions`, sessionId), updates);
};

export const deleteSessionFromFirestore = async (userId: string, sessionId: string) => {
  await deleteDoc(doc(db, `users/${userId}/sessions`, sessionId));
};

export const subscribeToTopics = (userId: string, callback: (topics: StudyTopic[]) => void) => {
  const q = collection(db, `users/${userId}/topics`);
  return onSnapshot(q, (snapshot) => {
    const list: StudyTopic[] = [];
    snapshot.forEach((docSnap) => {
      list.push({ id: docSnap.id, ...docSnap.data() } as StudyTopic);
    });
    callback(list);
  }, (error) => {
    console.error("Error subscribing to topics:", error);
  });
};

export const addTopicToFirestore = async (userId: string, topic: Omit<StudyTopic, 'id' | 'userId'>) => {
  const ref = collection(db, `users/${userId}/topics`);
  await addDoc(ref, { ...topic, userId });
};

export const updateTopicInFirestore = async (userId: string, topicId: string, updates: Partial<StudyTopic>) => {
  await updateDoc(doc(db, `users/${userId}/topics`, topicId), updates);
};

export const deleteTopicFromFirestore = async (userId: string, topicId: string) => {
  await deleteDoc(doc(db, `users/${userId}/topics`, topicId));
};

export const subscribeToTasks = (userId: string, callback: (tasks: StudyTask[]) => void) => {
  const q = collection(db, `users/${userId}/tasks`);
  return onSnapshot(q, (snapshot) => {
    const list: StudyTask[] = [];
    snapshot.forEach((docSnap) => {
      list.push({ id: docSnap.id, ...docSnap.data() } as StudyTask);
    });
    callback(list);
  }, (error) => {
    console.error("Error subscribing to tasks:", error);
  });
};

export const addTaskToFirestore = async (userId: string, task: Omit<StudyTask, 'id' | 'userId'>) => {
  const ref = collection(db, `users/${userId}/tasks`);
  await addDoc(ref, { ...task, userId });
};

export const updateTaskInFirestore = async (userId: string, taskId: string, updates: Partial<StudyTask>) => {
  await updateDoc(doc(db, `users/${userId}/tasks`, taskId), updates);
};

export const deleteTaskFromFirestore = async (userId: string, taskId: string) => {
  await deleteDoc(doc(db, `users/${userId}/tasks`, taskId));
};

export const subscribeToQuizAttempts = (userId: string, callback: (attempts: QuizAttempt[]) => void) => {
  const q = collection(db, `users/${userId}/quizAttempts`);
  return onSnapshot(q, (snapshot) => {
    const list: QuizAttempt[] = [];
    snapshot.forEach((docSnap) => {
      list.push({ id: docSnap.id, ...docSnap.data() } as QuizAttempt);
    });
    callback(list);
  }, (error) => {
    console.error("Error subscribing to quiz attempts:", error);
  });
};

export const addQuizAttemptToFirestore = async (userId: string, attempt: Omit<QuizAttempt, 'id' | 'userId'>) => {
  const ref = collection(db, `users/${userId}/quizAttempts`);
  await addDoc(ref, { ...attempt, userId });
};

// Seed initial data ONLY if user has no existing data in any collection
export const seedInitialDataIfEmpty = async (userId: string) => {
  const subjectsRef = collection(db, `users/${userId}/subjects`);
  const sessionsRef = collection(db, `users/${userId}/sessions`);
  const topicsRef = collection(db, `users/${userId}/topics`);
  const tasksRef = collection(db, `users/${userId}/tasks`);

  const [subjectsSnap, sessionsSnap, topicsSnap, tasksSnap] = await Promise.all([
    getDocs(subjectsRef),
    getDocs(sessionsRef),
    getDocs(topicsRef),
    getDocs(tasksRef)
  ]);

  // Do not seed if the user already has any existing data in any collection
  if (!subjectsSnap.empty || !sessionsSnap.empty || !topicsSnap.empty || !tasksSnap.empty) {
    return;
  }

  const today = new Date().toISOString().split('T')[0];
  const sub1 = await addDoc(subjectsRef, { userId, name: "Data Structures & Algorithms", code: "CS201", colorHex: "#6750A4", createdAt: Date.now() });
  const sub2 = await addDoc(subjectsRef, { userId, name: "Linear Algebra & Calculus", code: "MATH220", colorHex: "#79747E", createdAt: Date.now() });
  const sub3 = await addDoc(subjectsRef, { userId, name: "Classical & Quantum Physics", code: "PHYS140", colorHex: "#4F6D7A", createdAt: Date.now() });

  await addDoc(sessionsRef, {
    userId, subjectId: sub1.id, subjectName: "Data Structures & Algorithms",
    topic: "AVL Tree Rotations & Self-Balancing", date: today, startTime: "09:00", endTime: "10:30",
    location: "Hall C, Turing Building", notes: "Review lecture notes and AVL rotation steps.", status: "SCHEDULED", isAlarmEnabled: true
  });
  await addDoc(sessionsRef, {
    userId, subjectId: sub2.id, subjectName: "Linear Algebra & Calculus",
    topic: "Eigenvalues & Characteristic Equations", date: today, startTime: "11:00", endTime: "12:30",
    location: "Lecture Theatre 2", notes: "Solve practice problem set 3.", status: "SCHEDULED", isAlarmEnabled: true
  });

  await addDoc(topicsRef, {
    userId, subjectId: sub1.id, subjectName: "Data Structures & Algorithms",
    title: "AVL Tree Rotations", dateStudied: today, descriptionNotes: "Self-balancing binary search trees maintain O(log n) height.",
    masteryLevel: "FAMILIAR", repetitionIntervalDays: 3, nextRevisionDate: today, timesTested: 2, timesCorrect: 2
  });
  await addDoc(topicsRef, {
    userId, subjectId: sub2.id, subjectName: "Linear Algebra & Calculus",
    title: "Eigenvalues and eigenvectors", dateStudied: today, descriptionNotes: "Roots of det(A - lambda*I) = 0.",
    masteryLevel: "UNTESTED", repetitionIntervalDays: 1, nextRevisionDate: today, timesTested: 0, timesCorrect: 0
  });

  await addDoc(tasksRef, {
    userId, subjectName: "Data Structures & Algorithms", title: "Midterm Examination (Sorting & Trees)",
    dueDate: today, type: "EXAM", isCompleted: false, priority: "HIGH"
  });
  await addDoc(tasksRef, {
    userId, subjectName: "Linear Algebra & Calculus", title: "Problem Set 4: Vector Spaces",
    dueDate: today, type: "ASSIGNMENT", isCompleted: false, priority: "MEDIUM"
  });
};
