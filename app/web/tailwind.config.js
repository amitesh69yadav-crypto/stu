/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#6750A4',
        'primary-container': '#EADDFF',
        'on-primary': '#FFFFFF',
        'on-primary-container': '#21005D',
        surface: '#FEF7FF',
        'surface-variant': '#E7E0EC',
        'on-surface': '#1D1B20',
        'on-surface-variant': '#49454F',
        outline: '#79747E',
        background: '#F3EDF7',
        error: '#B3261E',
        'error-container': '#F9DEDC',
      }
    },
  },
  plugins: [],
}
