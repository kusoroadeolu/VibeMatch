VibeMatch Auth API
==================

This document describes the authentication endpoints for the VibeMatch API.  
All endpoints are prefixed with /auth.

------------------------------------------------------------
1. Login with Spotify
------------------------------------------------------------
Endpoint:
GET /auth/login

Description:
Redirects the user to Spotify’s OAuth 2.0 authorization page.

Response:
- 302 Found → Redirect to Spotify’s authorization page.
  (No JSON body returned)

------------------------------------------------------------
2. Spotify Callback
------------------------------------------------------------
Endpoint:
GET /auth/callback?code={authorization_code}

Description:
Handles Spotify OAuth callback, exchanges authorization code for tokens,
fetches user profile, and logs the user in.

Response:
- 200 OK → Returns login information.

Example JSON:
{
"username": "victor",
"refreshToken": "AQDqwe90asdk...",
"jwtToken": "eyJhbGciOiJIUzI1NiIsInR5..."
}

------------------------------------------------------------
3. Get Current User
------------------------------------------------------------
Endpoint:
GET /auth/me

Auth Required: YES (JWT Bearer token in Authorization header)

Description:
Fetches the authenticated user’s data.

Response:
- 200 OK → User data retrieved.

Example JSON:
{
"username": "victor",
"email": "victor@example.com",
"country": "NG",
"spotifyId": "spotify:12345",
"imageUrl": "https://i.scdn.co/image/xyz"
}

------------------------------------------------------------
4. Refresh Access Token
------------------------------------------------------------
Endpoint:
GET /auth/refresh

Auth Required: YES

Description:
Refreshes the Spotify access token for the logged-in user.

Response:
- 200 OK → New access token returned.

Example JSON:
{
"accessToken": "BQCUZAjXo...",
"tokenType": "Bearer",
"createdAt": "2025-08-17T14:05:32",
"expiresIn": 3600,
"refreshToken": "AQDqwe90asdk...",
"scope": "user-read-private user-read-email"
}

------------------------------------------------------------
5. Get Access Token
------------------------------------------------------------
Endpoint:
GET /auth/access-token

Auth Required: YES

Description:
Returns the current Spotify access token stored for the user.

Response:
- 200 OK → Access token string returned.

Example JSON:
"BQCUZAjXo..."

------------------------------------------------------------
6. Logout
------------------------------------------------------------
Endpoint:
GET /auth/logout

Auth Required: YES

Description:
Logs out the user and invalidates their session.

Response:
- 200 OK → Logout successful (no body)

------------------------------------------------------------


=====================
SYNCING ENDPOINTS
=====================

1. POST /sync
---------------------
Description:
Starts syncing the current authenticated user’s Spotify data.
The sync process runs asynchronously and may take up to 60 seconds.
Returns a task ID for checking status.

Authentication:
Required (Bearer token, role: USER)

Request:
No request body required.

Response:
Status: 202 ACCEPTED
Example JSON:
{
"taskId": "f9c3f871-51a4-45e7-9e40-0f245a3bba90"
}

Error Responses:
401 UNAUTHORIZED – Missing or invalid token.
403 FORBIDDEN – Insufficient permissions.

---------------------

2. GET /sync/status?id={taskId}
---------------------
Description:
Checks the status of a previously triggered sync task.

Authentication:
Required (Bearer token, role: USER)

Query Params:
id (string, required) – The sync task ID returned by POST /sync.

Response:
Status: 200 OK
Example JSON:
{
"taskId": "f9c3f871-51a4-45e7-9e40-0f245a3bba90",
"status": "IN_PROGRESS"
}

    Possible status values:
        - PENDING
        - RETRYING
        - SUCCESS
        - FAIL

Error Responses:
400 BAD REQUEST – Missing or invalid task ID.
401 UNAUTHORIZED – Missing or invalid token.
403 FORBIDDEN – Insufficient permissions.
404 NOT FOUND – Task ID not found.

=====================

========================================
Taste Profile API Endpoints
========================================

BASE URL: /profile

----------------------------------------
1. GET /profile/me
----------------------------------------
Description:
Fetch the authenticated user’s own taste profile.

Authentication:
✔ Requires USER role

Request:
- No body required.

Response: 200 OK
Example:
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

----------------------------------------
2. GET /profile/{id}
----------------------------------------
Description:
Fetch another user’s public taste profile by ID.

Authentication:
✔ Requires USER role

Path Parameter:
- id (UUID): target user’s ID.

Response: 200 OK
Example:
{
"userId": "321e4567-e89b-12d3-a456-426614174999",
"username": "altfan88",
"isPublic": true,
"topGenres": [
{ "name": "Alternative", "percentage": 55.2, "artistCount": 8 },
{ "name": "Hip-Hop", "percentage": 20.1, "artistCount": 4 }
],
"topArtists": [
{ "name": "Arctic Monkeys", "rank": 1 },
{ "name": "Kendrick Lamar", "rank": 2 }
],
"mainstreamScore": 45.6,
"discoveryPattern": "Niche Explorer",
"lastUpdated": "2025-08-15T08:22:10"
}

Errors:
- 404 Not Found → if the user’s profile doesn’t exist or is private.
- 400 Bad Request → if invalid UUID.

----------------------------------------
3. DELETE /profile/me
----------------------------------------
Description:
Delete the authenticated user’s taste profile.

Authentication:
✔ Requires USER role

Request:
- No body required.

_Response: 204 No Content
(No response body)

Errors:
- 404 Not Found → if no profile exists to delete.

========================================
DTO Reference
========================================

TasteProfileResponseDto {
userId: string (UUID)
username: string
isPublic: boolean
topGenres: GenreDto[]
topArtists: ArtistDto[]
mainstreamScore: double
discoveryPattern: string
lastUpdated: string (ISO timestamp)
}

GenreDto {
name: string
percentage: double
artistCount: int
}

ArtistDto {
name: string
rank: int
}

============================
COMPATIBILITY ENDPOINTS
============================_

1) GET /compatibility/{id}
----------------------------
Description:
Calculates and retrieves the compatibility score between the logged-in user and the target user.

Path Variable:
id (UUID) - The target user ID

Response (200 OK):
{
"user": {
"userId": "c8f8b2d1-1234-4f88-a321-abcdef123456",
"username": "YourUsername"
},
"targetUser": {
"userId": "d9e1b2c3-5678-4b90-b456-abcdef654321",
"username": "TargetUser"
},
"discoveryCompatibilityScore": 0.82,
"tasteCompatibilityScore": 0.76,
"sharedArtists": [
{
"artistName": "Kendrick Lamar",
"yourRank": 1,
"theirRank": 2
},
{
"artistName": "Drake",
"yourRank": 3,
"theirRank": 5
}
],
"sharedGenres": [
{
"genreName": "Hip Hop",
"yourPercentage": 40.0,
"theirPercentage": 35.0
},
{
"genreName": "Afrobeat",
"yourPercentage": 25.0,
"theirPercentage": 30.0
}
],
"whyCompatible": [
"You both listen heavily to Hip Hop",
"Both love Afrobeat and share top artists"
],
"lastCalculated": "2025-08-17T14:45:00"
}

Error Responses:
- 400 Bad Request → Invalid UUID format
- 404 Not Found → Target user not found

---------------------------------------------------

2) GET /compatibility/discover
-------------------------------
Description:
Finds a batch of compatible users for the logged-in user, ranked by compatibility.

Response (200 OK):
[
{
"user": {
"userId": "c8f8b2d1-1234-4f88-a321-abcdef123456",
"username": "YourUsername"
},
"targetUser": {
"userId": "a1b2c3d4-9876-4321-bbbb-abcdef112233",
"username": "Alice"
},
"discoveryCompatibilityScore": 0.89,
"tasteCompatibilityScore": 0.82,
"sharedArtists": [
{
"artistName": "Burna Boy",
"yourRank": 2,
"theirRank": 1
}
],
"sharedGenres": [
{
"genreName": "Afrobeat",
"yourPercentage": 30.0,
"theirPercentage": 45.0
}
],
"whyCompatible": [
"Strong overlap in Afrobeat listening habits"
],
"lastCalculated": "2025-08-17T14:50:00"
},
{
"user": {
"userId": "c8f8b2d1-1234-4f88-a321-abcdef123456",
"username": "YourUsername"
},
"targetUser": {
"userId": "b2c3d4e5-5432-4444-cccc-abcdef445566",
"username": "David"
},
"discoveryCompatibilityScore": 0.74,
"tasteCompatibilityScore": 0.70,
"sharedArtists": [],
"sharedGenres": [
{
"genreName": "Pop",
"yourPercentage": 15.0,
"theirPercentage": 20.0
}
],
"whyCompatible": [
"Both enjoy modern pop alongside other genres"
],
"lastCalculated": "2025-08-17T14:50:00"
}
]

Error Responses:
- 404 Not Found → No compatible users found

===============================
CONNECTIONS API ENDPOINT LIST
===============================

BASE URL: /connections
AUTH: Requires ROLE_USER (JWT-based auth)

--------------------------------------------------
1. GET /connections
   Description: Get all active connections for the authenticated user
   Response: 302 FOUND
   Example Response:
   [
   {
   "requestedBy": "user-uuid-123",
   "sentTo": "user-uuid-456",
   "isConnected": true,
   "connectedSince": "2025-08-17T10:15:30"
   }
   ]

--------------------------------------------------
2. GET /connections/received
   Description: Get all pending connection requests received
   Response: 302 FOUND
   Example Response:
   [
   {
   "requestedBy": "user-uuid-789",
   "sentTo": "user-uuid-123",
   "isConnected": false,
   "sentAt": "2025-08-17T10:20:00"
   }
   ]

--------------------------------------------------
3. GET /connections/sent
   Description: Get all pending connection requests sent
   Response: 302 FOUND
   Example Response:
   [
   {
   "requestedBy": "user-uuid-123",
   "sentTo": "user-uuid-789",
   "isConnected": false,
   "sentAt": "2025-08-17T10:25:00"
   }
   ]

--------------------------------------------------
4. GET /connections/{userId}
   Description: Get active connection between the authenticated user and another user
   Response: 302 FOUND
   Example Response:
   {
   "requestedBy": "user-uuid-123",
   "sentTo": "user-uuid-456",
   "isConnected": true,
   "connectedSince": "2025-08-17T10:30:00"
   }

--------------------------------------------------
5. POST /connections/request/{userId}
   Description: Send a connection request to another user
   Response: 201 CREATED
   Example Response:
   {
   "activeConnectionResponseDto": null,
   "inactiveConnectionResponseDto": {
   "requestedBy": "user-uuid-123",
   "sentTo": "user-uuid-456",
   "isConnected": false,
   "sentAt": "2025-08-17T10:35:00"
   }
   }

--------------------------------------------------
6. DELETE /connections/{userId}
   Description: Remove an existing connection
   Response: 200 OK (recommended) OR 404 NOT_FOUND (current implementation)
   Example Response:
   (No Content)

--------------------------------------------------
7. PUT /connections/{userId}/accept
   Description: Accept a pending connection request
   Response: 200 OK
   Example Response:
   {
   "requestedBy": "user-uuid-789",
   "sentTo": "user-uuid-123",
   "isConnected": true,
   "connectedSince": "2025-08-17T10:40:00"
   }

===============================
NOTES
===============================
- Currently using 302 FOUND for GET responses, but 200 OK is the conventional status.
- DELETE endpoint currently returns 404 NOT_FOUND after removal. Change to 200 OK or 204 NO_CONTENT for correctness.
- All endpoints require a valid JWT-authenticated user with role USER.
- UUIDs in responses represent user IDs.

========================
Recommendation Endpoints
========================

Base URL: /recommendations


1. Send a Recommendation
-------------------------
Endpoint:
POST /recommendations/{friendId}

Description:
Send a track/album/artist recommendation to a friend.

Path Parameters:
- friendId (UUID): The ID of the friend receiving the recommendation.

Request Body (JSON):
{
"spotifyUrl": "https://open.spotify.com/track/1234567890",
"recommendedName": "Blinding Lights",
"recommendedToId" : "user-uuid-123"
"type": "track"
}

Responses:
201 Created
- Successfully sent the recommendation.
400 Bad Request
- Invalid UUID or missing required fields.
401 Unauthorized
- User not authenticated.


2. Get Recommendations
------------------------
Endpoint:
GET /recommendations

Description:
Retrieve all recommendations sent to the authenticated user.

Response Body (200 OK):
[
{
"recommenderUsername": "alice",
"spotifyUrl": "https://open.spotify.com/track/1234567890",
"recommendedToName": "victor",
"type": "track",
"recommendedAt": "2025-08-17T14:45:30"
},
{
"recommenderUsername": "bob",
"spotifyUrl": "https://open.spotify.com/album/9876543210",
"recommendedToName": "victor",
"type": "album",
"recommendedAt": "2025-08-17T12:15:10"
}
]

Responses:
200 OK
- Returns the list of recommendations.
401 Unauthorized
- User not authenticated.


