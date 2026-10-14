import { initializeApp } from 'firebase/app';
import { getAuth, GoogleAuthProvider, signInWithPopup, signOut as fbSignOut, onAuthStateChanged, User } from 'firebase/auth';
import { getFirestore, initializeFirestore, persistentLocalCache, persistentMultipleTabManager } from 'firebase/firestore';

const firebaseConfig = {
  apiKey: "AIzaSyBzgEv1bxATfLztChjm8V0Iy-utWn9CHq8",
  authDomain: "gen-lang-client-0092333755.firebaseapp.com",
  projectId: "gen-lang-client-0092333755",
  storageBucket: "gen-lang-client-0092333755.firebasestorage.app",
  messagingSenderId: "924698065159",
  appId: "1:924698065159:web:studypilot"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const googleProvider = new GoogleAuthProvider();

// Initialize Firestore with the exact database ID used by Android
export const db = initializeFirestore(app, {
  localCache: persistentLocalCache({ tabManager: persistentMultipleTabManager() })
}, "ai-studio-android-studypil-1b9a5a78-c2bd-40f4-9199-e1b41747056c");

export { signInWithPopup, fbSignOut as signOut, onAuthStateChanged };
export type { User };
