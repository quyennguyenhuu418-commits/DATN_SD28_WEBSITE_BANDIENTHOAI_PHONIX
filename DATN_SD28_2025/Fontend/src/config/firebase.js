// Import the functions you need from the SDKs you need
import { initializeApp } from 'firebase/app'
import { getAnalytics } from 'firebase/analytics'
import { getAuth, GoogleAuthProvider } from 'firebase/auth'

// Your web app's Firebase configuration
const firebaseConfig = {
  apiKey: 'AIzaSyD07RFQhglQJF61d19IF83TxYUIn-ZYsDY',
  authDomain: 'phonix-store.firebaseapp.com',
  projectId: 'phonix-store',
  storageBucket: 'phonix-store.firebasestorage.app',
  messagingSenderId: '221883949331',
  appId: '1:221883949331:web:8082a50a092b4d5e44fae6',
  measurementId: 'G-FNXDWCLJJR',
}

console.log('Initializing Firebase with config:', firebaseConfig)

// Initialize Firebase
const app = initializeApp(firebaseConfig)
console.log('Firebase app initialized:', app)

const analytics = getAnalytics(app)
console.log('Firebase analytics initialized:', analytics)

// Initialize Firebase Auth
const auth = getAuth(app)
console.log('Firebase auth initialized:', auth)

const googleProvider = new GoogleAuthProvider()
console.log('Google provider initialized:', googleProvider)

export { auth, googleProvider, analytics }
