# FinTrack — Deployment Guide

## Backend → Railway

1. Go to https://railway.app and sign up (free)
2. Click "New Project" → "Deploy from GitHub repo"
3. Connect your GitHub and select this repo
4. Set the root directory to: `/backend`
5. Railway will detect the Dockerfile and build automatically
6. Once deployed, copy your Railway public URL (e.g. `https://fintrack-backend.railway.app`)

## Frontend → Vercel

1. Go to https://vercel.com and sign up (free)
2. Click "New Project" → import your GitHub repo
3. Set root directory to: `/frontend`
4. Add environment variable:
   - **Key**:   `VITE_API_URL`
   - **Value**: `https://your-railway-url.railway.app`   *(paste Railway URL here)*
5. Click **Deploy**
6. Your app will be live at `https://your-app.vercel.app`

## Local Development

**Terminal 1 — start backend**
```bash
cd backend
javac -d out $(find . -name "*.java")
java -cp out com.financetracker.main.Main
```

**Terminal 2 — start frontend**
```bash
cd frontend
npm install
npm run dev
```

**Visit** `http://localhost:5173`
