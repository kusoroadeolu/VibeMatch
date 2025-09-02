
# VibeMatch API

Welcome to **VibeMatch** – your Spotify-powered music compatibility & taste profiling API! 🎶

## About VibeMatch

VibeMatch is a Spotify-inspired social music platform that lets users discover, compare, and connect based on their musical taste. Through smart matching, personalized recommendations, and real-time syncing of Spotify data, VibeMatch helps users find friends with compatible music profiles and share their favorite tracks effortlessly.

All endpoints are **JWT-authenticated** unless stated otherwise.

---

<details>
<summary>👑 Authentication (`/auth`)</summary>

| Endpoint                         | Description                                           |
| -------------------------------- | ----------------------------------------------------- |
| `GET /auth/login`                | Redirects user to Spotify OAuth page.                 |
| `GET /auth/callback?code={code}` | Handles OAuth callback, logs in user, returns tokens. |
| `GET /auth/me`                   | Get current user info. ✅ JWT required                 |
| `GET /auth/refresh`              | Refresh Spotify token. ✅ JWT required                 |
| `GET /auth/access-token`         | Get stored Spotify token. ✅ JWT required              |
| `GET /auth/logout`               | Logout user and invalidate session. ✅ JWT required    |

**Example Login Response:**

```json
{
  "username": "victor",
  "refreshToken": "AQDqwe90asdk...",
  "jwtToken": "eyJhbGciOiJIUzI1NiIsInR5..."
}
```

</details>

---

<details>
<summary>🛤️ Syncing (`/sync`)</summary>

* `POST /sync` → Start asynchronous Spotify data sync. Returns `taskId`.
* `GET /sync/status?id={taskId}` → Check sync status: `PENDING`, `RETRYING`, `SUCCESS`, `FAIL`.

**Example Task Response:**

```json
{
  "taskId": "f9c3f871-51a4-45e7-9e40-0f245a3bba90"
}
```

</details>

---

<details>
<summary>🎧 Taste Profiles (`/profile`)</summary>

* `GET /profile/me` → Get your profile.
* `GET /profile/{id}` → Get another user's public profile.
* `DELETE /profile/me` → Delete your profile.

**Example Profile Response:**

```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "username": "musiclover42",
  "isPublic": true,
  "topGenres": [
    { "name": "Pop", "percentage": 40.5, "artistCount": 12 },
    { "name": "Indie Rock", "percentage": 25.3, "artistCount": 6 }
  ],
  "topArtists": [
    { "name": "Taylor Swift", "rank": 1 },
    { "name": "The Strokes", "rank": 2 }
  ],
  "mainstreamScore": 73.4,
  "discoveryPattern": "Exploratory",
  "lastUpdated": "2025-08-17T12:34:56"
}
```

</details>

---

<details>
<summary>❤️ Compatibility (`/compatibility`)</summary>

* `GET /compatibility/{id}` → Check compatibility with a user.
* `GET /compatibility/discover` → Find compatible users ranked by score.

**Example Insight:**

* Shared artists & genres
* Compatibility scores (`discoveryCompatibilityScore`, `tasteCompatibilityScore`)
* “Why compatible” insights

</details>

---

<details>
<summary>🔗 Connections (`/connections`)</summary>

Manage your music friendships:

* `GET /connections` → Active connections
* `GET /connections/received` → Pending received requests
* `GET /connections/sent` → Pending sent requests
* `POST /connections/request/{userId}` → Send request
* `PUT /connections/{userId}/accept` → Accept request
* `DELETE /connections/{userId}` → Remove connection

</details>

---

<details>
<summary>🏹 Recommendations (`/recommendations`)</summary>

* `POST /recommendations/{friendId}` → Send track/album/artist recommendation
* `GET /recommendations` → Get received recommendations

**Example Request:**

```json
{
  "spotifyUrl": "https://open.spotify.com/track/1234567890",
  "recommendedName": "Blinding Lights",
  "recommendedToId": "user-uuid-123",
  "type": "track"
}
```

</details>

---

### 🗑️ Notes

* All endpoints require **JWT-authenticated users** with role `USER`.
* UUIDs represent user IDs.
* Some GET endpoints currently return `302 FOUND` instead of `200 OK`.

---

### Technologies Used

**Backend:** Java 21, Spring Boot, Spring Security  
**Database:** PostgreSQL  
**Messaging / Queue:** RabbitMQ  
**Caching:** Redis  
**Containerization:** Docker  
**Frontend:** JavaScript, HTML, CSS
**DEMO AT** vibematch-od18.onrender.com
