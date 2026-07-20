## WEB Version 
```txt
apps/web-app
├── public/                     # static assets (images, fonts)
│
├── src/
│   ├── app/                   # Next.js App Router (preferred)
│   │   ├── layout.tsx
│   │   ├── page.tsx
│   │   │
│   │   ├── (auth)/            # route groups
│   │   │   ├── login/
│   │   │   └── register/
│   │   │
│   │   ├── (chat)/
│   │   │   ├── conversations/
│   │   │   └── messages/
│   │   │
│   │   └── (dashboard)/
│   │       ├── shards/        # shard admin UI (🔥 good use of HTMX-like ideas)
│   │       └── users/
│   │
│   ├── components/            # reusable UI components
│   │   ├── ui/                # buttons, inputs, modals
│   │   ├── layout/            # navbar, sidebar
│   │   └── chat/              # chat-specific components
│   │
│   ├── features/              # domain-driven frontend modules (IMPORTANT)
│   │   ├── auth/
│   │   │   ├── api.ts
│   │   │   ├── hooks.ts
│   │   │   └── components/
│   │   │
│   │   ├── chat/
│   │   │   ├── api.ts
│   │   │   ├── hooks.ts
│   │   │   ├── store.ts       # Zustand / Redux
│   │   │   └── components/
│   │   │
│   │   └── shard/
│   │       ├── api.ts
│   │       └── components/
│   │
│   ├── lib/                   # low-level utilities
│   │   ├── api-client.ts      # calls API Gateway
│   │   ├── websocket.ts       # realtime (chat)
│   │   ├── auth.ts            # token handling
│   │   └── utils.ts
│   │
│   ├── hooks/                 # global hooks
│   │   ├── useAuth.ts
│   │   └── useWebSocket.ts
│   │
│   ├── styles/
│   │   └── globals.css
│   │
│   ├── types/                 # shared TS types (from backend contracts)
│   │   ├── user.ts
│   │   ├── chat.ts
│   │   └── shard.ts
│   │
│   └── config/
│       ├── env.ts
│       └── constants.ts
│
├── package.json
├── tsconfig.json
└── next.config.js
```