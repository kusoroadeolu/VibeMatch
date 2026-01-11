# VibeMatch

> Find your musical soulmate. Connect with people who actually *get* your taste in music.

VibeMatch is a music-based social platform that analyzes your Spotify listening habits and connects you with people who vibe on your wavelength. Whether you're both deep into obscure indie or you share an unironic love for 2000s pop-punk, VibeMatch finds your people.

## What It Does

### The Core Loop

1. **Connect your Spotify** - We pull your top artists, tracks, and recent listening history
2. **Get your taste profile** - See what makes your music taste unique (or mainstream, no judgment)
3. **Discover compatible users** - Find people with similar vibes based on actual math
4. **Connect & share** - Send recommendations to your new music buddies

### Your Taste Profile

When you sync your Spotify data, VibeMatch builds a profile that captures:

- **Top Genres** - Your five most-listened-to genres with percentages (e.g., "indie rock: 24%")
- **Top Artists** - Your current top 5 artists with their rankings
- **Mainstream Score** - How popular your taste is (0-1 scale, where 1 means you love the charts)
- **Discovery Pattern** - Whether you're always hunting for new music or replaying your favorites

You'll also get a personalized description like:
- "Your playlist is your comfort zone" (low discovery)
- "You mix old favorites with new finds" (balanced)
- "You live for that perfect deep cut" (high discovery)

## How It Works

### The Architecture

VibeMatch is built on a modern Spring Boot stack designed for performance and scalability:

```
User → Spotify OAuth → Spring Security (JWT)
                            ↓
                    Controllers Layer
                            ↓
                    Service Layer (Business Logic)
                            ↓
                ┌───────────┴───────────┐
                ↓                       ↓
         PostgreSQL DB            Redis Cache
                ↓                       ↓
         User Data              Tokens & Profiles
         Artists/Tracks         Task Status
         Connections
                ↓
         RabbitMQ Queue
         (Async Operations)
```

### Authentication & Security

**OAuth2 Flow with Spotify**
- User clicks "Login with Spotify"
- Redirected to Spotify's authorization page
- After approval, Spotify sends back an authorization code
- Backend exchanges code for access token and refresh token
- Access token stored in Redis (1 hour expiry)
- Refresh token saved to database (used to get new access tokens)
- JWT issued to user for app authentication

**Security Layers**
- Spring Security with method-level authorization (`@PreAuthorize`)
- JWT tokens stored in HTTP-only cookies (XSS protection)
- CSRF disabled (using JWT, stateless sessions)
- Refresh tokens rotated on use
- Access tokens auto-refresh when expired

### Data Synchronization Pipeline

One of the most complex parts of the system - here's how we keep your music data fresh:

**Initial Sync Process**
1. User triggers sync via `/sync` endpoint
2. System checks: has user synced in last 12 hours? Is sync already running?
3. If valid, generates unique task ID and queues to RabbitMQ
4. Task status cached in Redis as "PENDING"

**Async Processing (RabbitMQ Consumer)**
```
Message Received → Extract Spotify ID
                        ↓
                Get Access Token (cached or refresh)
                        ↓
            Fetch from Spotify API (3 parallel calls)
            - Top 50 Artists (medium term)
            - Top 50 Tracks (medium term)  
            - Recent 50 Tracks (short term)
                        ↓
                Delete Old Data (prevent duplicates)
                        ↓
                Save New Data (batch insert)
                        ↓
            Calculate Taste Profile Metrics
                        ↓
                Cache Profile (24hr TTL)
                        ↓
            Update Task Status → "SUCCESS"
```

**Rate Limit Handling**
- Spotify returns 429 (Too Many Requests)
- Message sent to Dead Letter Exchange (DLX)
- Waits for retry period specified by Spotify (usually 30-60 seconds)
- Message re-queued to main sync queue
- Task status updated to "RETRYING"
- Max 2 retry attempts before marking as "FAILED"

**Scheduled Auto-Sync**
- Cron job runs every 2 hours
- Finds users who haven't synced in 24 hours
- Automatically queues them for sync
- Prevents stale data without user intervention

### Caching Strategy

Smart caching keeps the app fast while respecting data freshness:

**Redis Cache Layers**

1. **Token Cache** (1 hour TTL)
    - Stores Spotify access tokens
    - On cache miss → fetch from DB and refresh if expired
    - Prevents unnecessary refresh token calls

2. **Taste Profile Cache** (24 hour TTL)
    - Stores calculated profiles (expensive computation)
    - On cache miss → recalculate from DB data
    - Evicted on user sync

3. **Task Status Cache** (sync duration)
    - Tracks ongoing sync operations
    - Prevents duplicate syncs
    - Client can poll for status updates

**Cache-Aside Pattern**
```java
// Example from TokenCacheService
public TokenDto getCachedToken(String spotifyId) {
    // Try cache first
    TokenDto cached = redis.get(spotifyId);
    
    if (cached == null) {
        // Cache miss - refresh token
        cached = tokenRefreshService.refresh(spotifyId);
        redis.set(spotifyId, cached, 1_HOUR);
    }
    
    return cached;
}
```

### Database Design

**Key Entities & Relationships**

```
User (1) ←→ (Many) UserArtist
     (1) ←→ (Many) UserTopTrack
     (1) ←→ (Many) UserRecentTrack
     (1) ←→ (1) TasteProfile
     (1) ←→ (Many) CompatibilityScore
     (Many) ←→ (Many) Connection [self-referential]
     (Many) ←→ (Many) Recommendation
```

**Notable Design Decisions**

- **Composite Keys**: CompatibilityScore uses composite key (userId, targetUserId)
- **Canonical Ordering**: Connections stored with userA.id < userB.id (prevents duplicates)
- **Embedded Collections**: TasteProfile stores top genres/artists as @ElementCollection
- **Unique Constraints**: User-Artist pairs unique (user_id, artist_spotify_id)
- **Indexes**: Strategic indexes on foreign keys and frequently queried columns

**Data Integrity**
- Cascade deletes on user relationships (orphan removal)
- Unique constraints prevent duplicate entries
- Not-null constraints on critical fields
- Pessimistic locking on user updates during sync

### The Compatibility Engine

This is where the magic happens - converting music data into meaningful connections.

**Calculation Workflow**
```
User A → Discovery Endpoint
            ↓
Find All Public Users (User B, C, D...)
            ↓
Filter Out Existing Connections
            ↓
For Each Target User:
    ↓
    Calculate Taste Compatibility
    (shared artists, genres, weighted by popularity)
    ↓
    Calculate Discovery Compatibility
    (listening patterns, mainstream scores)
    ↓
    Generate "Why Compatible" Reasons
    ↓
    Check Thresholds (70% taste, 60% discovery)
    ↓
    If Pass: Save CompatibilityScore
    If Fail: Skip (not shown to user)
            ↓
Sort by Taste Score (then Discovery Score)
            ↓
Return Top Matches
```

**Mathematical Foundation**

*Taste Compatibility*
- Uses cosine similarity for artist vectors
- Weights artists by rank and popularity
- Applies Jaccard similarity for genre overlap
- Scales by depth of shared content

*Discovery Compatibility*
- Compares discovery rates (1 - overlap in artist variety)
- Compares mainstream scores (average popularity)
- Weighted combination (70% discovery, 30% mainstream)

**Performance Optimizations**
- Batch processing: Calculate all compatibilities in one pass
- Database-level filtering: Only public profiles
- In-memory computation: All data loaded once
- Results cached: Recalculated on schedule, not per request

### Connection Management

**Connection Lifecycle**
```
User A sends request to User B
    ↓
Connection created:
    - userA, userB (canonical order)
    - requester: A, receiver: B
    - isConnected: false
    ↓
User B accepts request
    ↓
Connection updated:
    - isConnected: true
    - connectedSince: now()
    ↓
Both users can now:
    - Send recommendations
    - View full compatibility details
    ↓
Either user can remove connection
    ↓
Connection deleted (cascade cleanup)
```

**Smart Request Handling**
- If User B already sent request to User A → auto-accept both
- Prevent duplicate requests in both directions
- Atomic operations (transactions prevent race conditions)

### Recommendation System

Currently simple, room to grow:

**Current Implementation**
- Connected users can recommend tracks, albums, or artists
- Stores: Spotify URL, name, type, timestamp
- Recipient sees: recommender name, item details, when sent

**Potential Enhancements**
- AI-powered recommendations based on compatibility
- Track if recommendations were listened to
- Build recommendation graph (what leads to discovery)
- Collaborative filtering (if you liked X, try Y)

## Technical Deep Dive

### The Compatibility Math

This is where we turn music data into meaningful connections.

**Taste Compatibility Score (0-1)**

Measures how similar your actual music taste is.

```
Taste Score = (Artist Similarity + Genre Overlap) × 0.5 × Shared Ratio
```

Where:
- **Artist Similarity**: Cosine similarity of weighted vectors
    - Each artist gets a weight: `(0.75 × rank_weight) + (0.25 × popularity/100)`
    - For mainstream listeners: `(0.4 × rank_weight) + (0.6 × popularity/100)`
    - Higher-ranked artists matter more

- **Genre Overlap**: Jaccard similarity
    - `intersection(genres_A, genres_B) / union(genres_A, genres_B)`
    - Simple but effective for categorical data

- **Shared Ratio**: `√(shared_artists / min(total_A, total_B))`
    - Rewards depth of overlap
    - Square root prevents penalizing users with many artists

**Discovery Compatibility Score (0-1)**

Measures if you listen the same way.

```
Discovery Score = (0.7 × Discovery Similarity) + (0.3 × Mainstream Similarity)
```

Where:
- **Discovery Similarity**: `1 - |discovery_A - discovery_B|`
    - Discovery rate: `1 - jaccard(top_track_artists, recent_track_artists)`
    - High discovery = always listening to new artists

- **Mainstream Similarity**: `1 - |mainstream_A - mainstream_B|`
    - Mainstream score: weighted average of artist/track popularity
    - `(0.5 × artist_pop) + (0.3 × top_track_pop) + (0.2 × recent_track_pop)`

**Why These Formulas Work**
- Cosine similarity captures directional alignment (you both prioritize similar artists)
- Jaccard works well for sets (genres, artist IDs)
- Weighting by rank > popularity unless user is mainstream (then flip it)
- Discovery matters more than mainstream (70/30 split) - how you listen > what's popular

**Compatibility Thresholds**

Only matches that meet both criteria are shown:
- Taste Compatibility ≥ 70%
- Discovery Compatibility ≥ 60%

This keeps quality high and noise low.

### Concurrency & Race Conditions

**Problem**: Multiple sync requests, token refreshes, or compatibility calculations

**Solutions**:
1. **Pessimistic Locking** on user updates during sync
   ```java
   @Lock(LockModeType.PESSIMISTIC_WRITE)
   Optional<User> findUserBySpotifyIdWithLock(String spotifyId);
   ```

2. **Idempotent Operations** - syncs can be retried safely
    - Delete old data before inserting new
    - Unique constraints prevent duplicates

3. **Distributed Locks via RabbitMQ**
    - Only one sync message per user in queue
    - Task status in Redis prevents duplicate triggers

4. **Transactional Boundaries**
    - `@Transactional` on all write operations
    - Rollback on failure keeps data consistent

### Error Handling & Resilience

**Retry Strategies**

1. **Spotify API Calls** - `@Retryable` with exponential backoff
   ```java
   @Retryable(
       retryFor = AuthorizationException.class,
       maxAttempts = 5,
       backoff = @Backoff(delay = 1000, multiplier = 2)
   )
   ```
    - Token expired → evict cache → retry with fresh token
    - Rate limit → send to DLX → retry after Spotify's timeout

2. **Database Operations** - Custom retry logic
    - Compatibility calculations: 2 attempts with 2s backoff
    - Connection conflicts: Let constraint violations bubble up

**Failure Recovery**

- **Sync Failures**: Task marked "FAILED", user can retry after cooldown
- **Token Issues**: Auto-refresh on 401, re-login on 403
- **RabbitMQ Down**: Messages persist in queue until broker recovers
- **Redis Down**: Graceful degradation (slower, hits DB directly)


## Project Structure

```
src/main/java/com/victor/VibeMatch/
│
├── auth/                          # OAuth & JWT authentication
│   ├── SpotifyAuthController      # OAuth flow handlers
│   ├── UserAuthController         # Token refresh, logout
│   └── service/
│       ├── SpotifyAuthService     # Spotify API integration
│       └── UserAuthService        # User session management
│
├── user/                          # User domain
│   ├── User                       # Core entity
│   ├── UserRepository
│   └── service/
│       ├── UserQueryService       # Read operations
│       └── UserCommandService     # Write operations
│
├── userartist/                    # Artist listening data
│   ├── UserArtist                 # Artist entity with ranking
│   └── mapper/
│       └── UserArtistMapper       # Spotify DTO → Entity
│
├── usertrack/                     # Track listening data
│   ├── top/                       # Long-term favorites
│   │   └── UserTopTrack
│   └── recent/                    # Recent plays
│       └── UserRecentTrack
│
├── tasteprofile/                  # Calculated profiles
│   ├── TasteProfile               # Aggregated metrics
│   ├── TasteProfileCalculationService
│   ├── TasteProfilePersistenceService
│   └── utils/
│       └── TasteProfileUtils      # Math helpers
│
├── compatibility/                 # Matching engine
│   ├── CompatibilityScore         # Stored match results
│   ├── CompatibilityCalculationService
│   ├── CompatibilityScoreBatchService
│   └── embeddables/
│       └── CompatibilityWrapper   # Shared artists/genres
│
├── connections/                   # Social connections
│   ├── Connection                 # Friend relationships
│   ├── ConnectionService          # Request/accept logic
│   └── ConnectionQueryService
│
├── recommendations/               # Music sharing
│   ├── Recommendation             # Sent recommendations
│   └── RecommendationService
│
├── synchandler/                   # Async sync orchestration
│   ├── SyncController             # Trigger endpoint
│   ├── SyncOrchestrator          # Coordinates sync flow
│   ├── impl/
│   │   ├── SyncListener           # RabbitMQ consumer
│   │   ├── UserArtistSyncService
│   │   └── UserTrackSyncService
│   └── services/
│       └── TaskService            # Track sync status
│
├── spotify/                       # Spotify API client
│   ├── SpotifyDataOrchestratorService
│   ├── client/
│   │   ├── SpotifyArtistClientService
│   │   └── SpotifyTrackClientService
│   ├── dto/                       # Spotify response models
│   └── factory/
│       └── SpotifyDataFactory     # Service creation
│
├── cache/                         # Redis caching
│   ├── TokenCacheService
│   ├── TasteProfileCacheService
│   └── TaskCacheService
│
├── security/                      # Spring Security config
│   ├── SecurityConfig
│   ├── JwtFilter                  # Token validation
│   ├── CustomUserDetailsService
│   └── UserPrincipal              # Auth principal
│
├── jwt/                           # JWT utilities
│   ├── JwtService
│   └── JwtConfigProperties
│
├── rabbitmq/                      # Message queue config
│   └── RabbitConfig               # Queues, exchanges, DLX
│
├── scheduledtasks/                # Cron jobs
│   └── ScheduledTasksHandler      # Auto-sync, recalculation
│
├── math/                          # Math utilities
│   └── MathUtils                  # Similarity calculations
│
└── exceptions/                    # Custom exceptions
    ├── GlobalExceptionHandler     # Centralized error handling
    └── ...                        # Domain-specific exceptions
```

### Design Patterns Used

**Service Layer Pattern**
- Separation between controllers and business logic
- Query/Command services (CQRS-lite)
- Keeps controllers thin

**Factory Pattern**
- `SpotifyDataFactory` creates appropriate service based on data type
- Eliminates if/else chains
- Easy to extend with new data types

**Strategy Pattern**
- Different compatibility calculation strategies
- Pluggable algorithms for future A/B testing

**Repository Pattern**
- Spring Data JPA abstracts database access
- Custom queries where needed
- Clean separation from business logic

**Observer Pattern** (via RabbitMQ)
- Sync events published to queue
- Listeners react asynchronously
- Decouples sync triggering from execution

**Builder Pattern**
- Entity creation (Lombok `@Builder`)
- Clean, readable object construction

## Getting Started

### Prerequisites

- Java 17+
- PostgreSQL 14+
- Redis 6+
- RabbitMQ 3.9+ (or CloudAMQP account)
- Spotify Developer Account

### Spotify App Setup

1. Go to [Spotify Developer Dashboard](https://developer.spotify.com/dashboard)
2. Create a new app
3. Add redirect URI: `http://localhost:8080/auth/callback`
4. Note your Client ID and Client Secret
5. Required scopes:
    - `user-top-read` - Access top artists and tracks
    - `user-read-recently-played` - Access recently played tracks
    - `user-read-email` - Access email address

### Local Development Setup

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/vibematch.git
cd vibematch
```

2. **Configure application.properties**
```properties
# Spotify OAuth
spring.security.oauth2.client.registration.spotify.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.spotify.client-secret=YOUR_CLIENT_SECRET
spring.security.oauth2.client.registration.spotify.redirect-uri=http://localhost:8080/auth/callback
spring.security.oauth2.client.registration.spotify.scope=user-top-read,user-read-recently-played,user-read-email

# Spotify API Endpoints
spring.security.oauth2.client.provider.spotify.authorization-uri=https://accounts.spotify.com/authorize
spring.security.oauth2.client.provider.spotify.token-uri=https://accounts.spotify.com/api/token
spring.security.oauth2.client.provider.spotify.user-info-uri=https://api.spotify.com/v1/me

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/vibematch
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# RabbitMQ (local)
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# Or RabbitMQ (CloudAMQP)
spring.rabbitmq.factory.cloud-host=your-instance.rmq.cloudamqp.com
spring.rabbitmq.factory.port=5671
spring.rabbitmq.factory.username=your_username
spring.rabbitmq.factory.v-host=/
spring.rabbitmq.factory.password=your_password

# JWT
jwt.secret=your-very-long-secret-key-at-least-256-bits
jwt.expiration=3600

# Cookies
cookies.secure=false
cookies.http-only=true
cookies.max-age=604800

# Cache TTL (hours)
spring.cache.token-time-in-hours=1
spring.cache.profile-time-in-hours=24

# Scheduled tasks
scheduled.refresh-user-data=7200000
scheduled.threshold-in-hours=24
scheduled.compatibility-cron=0 0 2 * * ?
```

3. **Start required services**
```bash
# PostgreSQL (if using Docker)
docker run --name postgres -e POSTGRES_PASSWORD=yourpassword -p 5432:5432 -d postgres:14

# Redis (if using Docker)
docker run --name redis -p 6379:6379 -d redis:6

# RabbitMQ (if using Docker)
docker run --name rabbitmq -p 5672:5672 -p 15672:15672 -d rabbitmq:3-management
```

4. **Build and run**
```bash
./mvnw clean install
./mvnw spring-boot:run
```

5. **Access the application**
```
http://localhost:8080
```

### First Time Setup
1. Navigate to `http://localhost:8080`
2. Click "Login with Spotify"
3. Authorize the app
4. Click "Sync My Data" to import your listening history
5. Wait for sync to complete (check status with task ID)
6. View your taste profile at `/profile/me`
7. Discover compatible users at `/compatibility/discover`

## API Reference

## API Reference

### Authentication

**Login with Spotify**
```http
GET /auth/login
```
Redirects to Spotify OAuth page

**OAuth Callback**
```http
GET /auth/callback?code={authorization_code}
```
Handles Spotify callback, sets JWT cookie, redirects to dashboard

**Refresh Access Token**
```http
GET /auth/refresh
Authorization: Bearer {jwt_token}
```
Returns new Spotify access token

**Get Current User**
```http
GET /auth/me
Authorization: Bearer {jwt_token}
```
Returns authenticated user details

**Logout**
```http
GET /auth/logout
Authorization: Bearer {jwt_token}
```
Clears session and cookies

### Data Synchronization
### Data Synchronization

**Trigger Sync**
```http
POST /sync
Authorization: Bearer {jwt_token}

Response: 202 Accepted
{
  "taskId": "uuid-string"
}
```
Starts async sync process (12-hour cooldown between syncs)

**Check Sync Status**
```http
GET /sync/status?id={taskId}
Authorization: Bearer {jwt_token}

Response: 200 OK
{
  "taskId": "uuid-string",
  "status": "PENDING" | "SUCCESS" | "RETRYING" | "FAIL"
}
```

**Check if Synced Recently**
```http
GET /sync/last-synced
Authorization: Bearer {jwt_token}

Response: 200 OK
{
  "hasSynced": true
}
```
Returns true if synced in last 24 hours

### Taste Profiles

**Get Your Profile**
```http
GET /profile/me
Authorization: Bearer {jwt_token}

Response: 200 OK
{
  "userId": "uuid",
  "username": "john_doe",
  "isPublic": true,
  "topGenres": [
    {
      "name": "Indie Rock",
      "percentage": 24.5,
      "artistCount": 12
    }
  ],
  "topArtists": [
    {
      "name": "Radiohead",
      "rank": 1
    }
  ],
  "mainstreamScore": 0.65,
  "howYouListen": "You mix old favorites with new finds",
  "lastUpdated": "2024-01-15T10:30:00"
}
```

**Get User's Public Profile**
```http
GET /profile/{userId}
Authorization: Bearer {jwt_token}

Response: 200 OK
{...}  # Same structure as above
```
Only works if target user has public profile

**Delete Your Profile Cache**
```http
DELETE /profile/me
Authorization: Bearer {jwt_token}

Response: 204 No Content
```
Forces recalculation on next fetch

### Compatibility

**Get Compatibility with User**
```http
GET /compatibility/{userId}
Authorization: Bearer {jwt_token}

Response: 200 OK
{
  "user": {
    "userId": "uuid-1",
    "username": "john_doe"
  },
  "targetUser": {
    "userId": "uuid-2",
    "username": "jane_smith"
  },
  "discoveryCompatibilityScore": 0.75,
  "tasteCompatibilityScore": 0.82,
  "sharedArtists": [
    {
      "artistName": "Radiohead",
      "yourRank": 1,
      "theirRank": 3
    }
  ],
  "sharedGenres": [
    {
      "genreName": "Indie Rock",
      "yourPercentage": 24.5,
      "theirPercentage": 18.2
    }
  ],
  "whyCompatible": [
    "You have 3 artists in common",
    "You both enjoy Indie Rock",
    "You have very similar music discovery patterns"
  ],
  "lastCalculated": "2024-01-15T10:30:00"
}
```

**Discover Compatible Users**
```http
GET /compatibility/discover
Authorization: Bearer {jwt_token}

Response: 200 OK
[
  {...},  # Array of compatibility scores
  {...}
]
```
Returns all users meeting compatibility thresholds (70% taste, 60% discovery)

### Connections

**Get All Connections**
```http
GET /connections
Authorization: Bearer {jwt_token}

Response: 200 OK
[
  {
    "requestedBy": "john_doe",
    "sentTo": "jane_smith",
    "requestedById": "uuid-1",
    "sentToId": "uuid-2",
    "isConnected": true,
    "connectedSince": "2024-01-15T10:30:00"
  }
]
```

**Get Pending Received Requests**
```http
GET /connections/received
Authorization: Bearer {jwt_token}

Response: 200 OK
[
  {
    "requestedBy": "alice_jones",
    "sentTo": "john_doe",
    "requestedById": "uuid-3",
    "sentToId": "uuid-1",
    "isConnected": false,
    "sentAt": "2024-01-15T10:30:00"
  }
]
```

**Get Pending Sent Requests**
```http
GET /connections/sent
Authorization: Bearer {jwt_token}

Response: 200 OK
[...]  # Similar structure
```

**Get Connection with Specific User**
```http
GET /connections/{userId}
Authorization: Bearer {jwt_token}

Response: 200 OK
{...}  # Connection details
```

**Send Connection Request**
```http
POST /connections/request/{userId}
Authorization: Bearer {jwt_token}

Response: 201 Created
{
  "activeConnectionResponseDto": null,  # If instantly connected
  "inactiveConnectionResponseDto": {...}  # If pending
}
```
Auto-accepts if target user already sent you a request

**Accept Connection Request**
```http
PUT /connections/{userId}/accept
Authorization: Bearer {jwt_token}

Response: 200 OK
{
  "requestedBy": "alice_jones",
  "sentTo": "john_doe",
  "requestedById": "uuid-3",
  "sentToId": "uuid-1",
  "isConnected": true,
  "connectedSince": "2024-01-15T12:00:00"
}
```

**Remove Connection**
```http
DELETE /connections/{userId}
Authorization: Bearer {jwt_token}

Response: 204 No Content
```
Removes connection or pending request

### Recommendations

**Send Recommendation**
```http
POST /recommendations/{friendId}
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "spotifyUrl": "spotify:track:3n3Ppam7vgaVa1iaRUc9Lp",
  "recommendedName": "Mr. Brightside",
  "type": "track"
}

Response: 201 Created
```
Only works if connected with target user

**Get Your Recommendations**
```http
GET /recommendations
Authorization: Bearer {jwt_token}

Response: 200 OK
[
  {
    "recommenderUsername": "alice_jones",
    "recommendedItemName": "Mr. Brightside",
    "spotifyUrl": "spotify:track:3n3Ppam7vgaVa1iaRUc9Lp",
    "type": "track",
    "recommendedAt": "2024-01-15T14:30:00"
  }
]
```

## Tech Stack

**Backend**
- Spring Boot 3.x
- Spring Security (OAuth2 + JWT)
- Spring Data JPA (Hibernate)
- Spring Cache (Redis)
- Spring AMQP (RabbitMQ)
- Spring Scheduling

**Storage**
- PostgreSQL 14+ (primary database)
- Redis 6+ (caching, task tracking)
- RabbitMQ 3.9+ (async message queue)

**External APIs**
- Spotify Web API (OAuth2, user data)

**Utilities**
- Lombok (boilerplate reduction)
- Jackson (JSON serialization)
- jjwt (JWT tokens)

## Deployment

### Environment Variables

```bash
# Required
SPOTIFY_CLIENT_ID=your_client_id
SPOTIFY_CLIENT_SECRET=your_client_secret
DATABASE_URL=jdbc:postgresql://host:5432/dbname
DATABASE_USERNAME=username
DATABASE_PASSWORD=password
REDIS_URL=redis://host:6379
RABBITMQ_URL=amqp://user:pass@host:5672

# Optional
JWT_SECRET=your-256-bit-secret
JWT_EXPIRATION=3600
CACHE_PROFILE_HOURS=24
SYNC_COOLDOWN_HOURS=12
```

### Docker Deployment

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

```bash
# Build
docker build -t vibematch .

# Run
docker run -p 8080:8080 \
  -e SPOTIFY_CLIENT_ID=xxx \
  -e DATABASE_URL=xxx \
  vibematch
```

### Production Considerations

- Use connection pooling (HikariCP configured)
- Enable HTTPS (set `cookies.secure=true`)
- Configure CORS for frontend domain
- Set up monitoring (Actuator endpoints available)
- Use managed services (AWS RDS, ElastiCache, AmazonMQ)
- Enable Spring Boot Actuator health checks
- Configure proper logging levels
- Set up alerting for failed syncs

## License

MIT License - see LICENSE file for details

## Acknowledgments

- Spotify Web API for making music data accessible
- The Spring team for an excellent framework
- Everyone who believes music taste matters

---

**DEMO AT**: https://vibematch-od18.onrender.com
*Built with* ❤️ *by music nerds, for music nerds*