import { initializeApp } from 'firebase/app';
import { getAuth, GoogleAuthProvider, signInWithPopup, signOut as fbSignOut, onAuthStateChanged, User } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';

const firebaseConfig = {
  apiKey: "AIzaSyAKVnvI_abR11B13I1PD5qERBjckyd_coE",
  authDomain: "gen-lang-client-0092333755.firebaseapp.com",
  projectId: "gen-lang-client-0092333755",
  storageBucket: "gen-lang-client-0092333755.firebasestorage.app",
  messagingSenderId: "924698065159",
  appId: "1:924698065159:web:d8e8dd1b5031e3acf26da2"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const googleProvider = new GoogleAuthProvider();

// Initialize Firestore using the exact provisioned database ID
const DATABASE_ID = "ai-studio-android-studypil-1b9a5a78-c2bd-40f4-9199-e1b41747056c";
export const db = getFirestore(app, DATABASE_ID);

export { signInWithPopup, fbSignOut as signOut, onAuthStateChanged };
export type { User };
