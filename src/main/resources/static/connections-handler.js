

class ConnectionsManager {
    constructor() {
        this.baseUrl = window.location.origin;
        this.isLoading = false;
        this.init();
    }

    init() {
        // Only initialize if we're on the connections page
        if (!window.location.pathname.includes('connections')) {
            return;
        }

        console.log('ConnectionsManager initialized');
        this.setupEventListeners();
        this.loadConnections();
    }

    // UI update methods for initial load
    async updateActiveConnectionsUI(connections) {
        const grid = document.getElementById('active-connections-grid');
        if (!grid) return;

        grid.innerHTML = '';
        console.log('Active connections loaded:', connections);

        if (!connections || connections.length === 0) {
            grid.innerHTML = `
                <div style="grid-column: 1 / -1; text-align: center; padding: 40px; color: #b3b3b3;">
                    <i class="ri-user-line" style="font-size: 48px; margin-bottom: 20px; display: block; color: #535353;"></i>
                    <h3>No Active Connections</h3>
                    <p>You haven't connected with anyone yet.</p>
                </div>
            `;
            return;
        }

        // Create cards asynchronously
        for (const connection of connections) {
            const card = await this.createActiveConnectionCard(connection);
            grid.appendChild(card);
        }
    }

    setupEventListeners() {
        // Accept Request buttons
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('btn-accept')) {
                e.preventDefault();
                e.stopPropagation();
                this.handleAcceptRequest(e.target);
            }
        });

        // Cancel/Decline Request buttons
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('btn-cancel')) {
                e.preventDefault();
                e.stopPropagation();
                this.handleDeclineRequest(e.target);
            }
        });

        // View Taste Profile buttons
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('btn-view')) {
                e.preventDefault();
                e.stopPropagation();
                this.handleViewProfile(e.target);
            }
        });

        // Send Recommendation buttons
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('btn-recommend')) {
                e.preventDefault();
                e.stopPropagation();
                this.handleSendRecommendation(e.target);
            }
        });

        // Close modals on Escape key
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.closeAllModals();
            }
        });
    }

    async loadConnections() {
        try {
            // Load all connection data
            const [activeConnections, receivedRequests, sentRequests] = await Promise.all([
                this.fetchConnections(),
                this.fetchReceivedRequests(),
                this.fetchSentRequests()
            ]);


            this.updateActiveConnectionsUI(activeConnections);
            this.updateReceivedRequestsUI(receivedRequests);
            this.updateSentRequestsUI(sentRequests);

            // Debug the created cards
            setTimeout(() => {
                const allCards = document.querySelectorAll('.connection-card');
                console.log('All created cards:', allCards);
                allCards.forEach((card, index) => {
                    console.log(`Card ${index}:`, {
                        dataset: card.dataset,
                        userId: card.dataset.userId,
                        username: card.querySelector('.connection-username')?.textContent
                    });
                });
            }, 1000);

        } catch (error) {
            console.error('Error loading connections:', error);
        }
    }

    async fetchConnections() {
        const response = await fetch(`${this.baseUrl}/connections`, {
            credentials: 'include',
            redirect: 'follow'
        });

        // Handle 302 as success (API returns 302 instead of 200)
        if (response.status === 302 || response.ok) {
            if (response.status === 401) {
                window.location.href = '/index.html';
                return;
            }
            return await response.json();
        }

        if (response.status === 401) {
            window.location.href = '/index.html';
            return;
        }

        throw new Error(`HTTP error! status: ${response.status}`);
    }

    async fetchReceivedRequests() {
        const response = await fetch(`${this.baseUrl}/connections/received`, {
            credentials: 'include',
            redirect: 'follow'
        });

        // Handle 302 as success (API returns 302 instead of 200)
        if (response.status === 302 || response.ok) {
            if (response.status === 401) {
                window.location.href = '/index.html';
                return;
            }
            return await response.json();
        }

        throw new Error(`HTTP error! status: ${response.status}`);
    }

    async fetchSentRequests() {
        const response = await fetch(`${this.baseUrl}/connections/sent`, {
            credentials: 'include',
            redirect: 'follow'
        });

        // Handle 302 as success (API returns 302 instead of 200)
        if (response.status === 302 || response.ok) {
            if (response.status === 401) {
                window.location.href = '/index.html';
                return;
            }
            return await response.json();
        }

        throw new Error(`HTTP error! status: ${response.status}`);
    }

    async handleAcceptRequest(button) {
        if (button.disabled || this.isLoading) return;

        const card = button.closest('.connection-card');
        const username = card.querySelector('.connection-username').textContent;
        const userId = this.getUserIdFromCard(card);

        // ✅ Better error handling
        if (!userId) {
            console.error('Could not find user ID for accept request');
            console.error('Card data:', card.dataset);
            console.error('Available data attributes:', Object.keys(card.dataset));

            // Show user-friendly error
            const originalText = button.textContent;
            button.textContent = 'Error - Missing ID';
            button.style.backgroundColor = '#e74c3c';

            setTimeout(() => {
                button.textContent = originalText;
                button.style.backgroundColor = '#1DB954';
            }, 3000);
            return;
        }

        // Validate that userId is a proper UUID format
        const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
        if (!uuidRegex.test(userId)) {
            console.error('Invalid UUID format for user ID:', userId);

            const originalText = button.textContent;
            button.textContent = 'Error - Invalid ID';
            button.style.backgroundColor = '#e74c3c';

            setTimeout(() => {
                button.textContent = originalText;
                button.style.backgroundColor = '#1DB954';
            }, 3000);
            return;
        }

        console.log(`Accepting request from user: ${username} (ID: ${userId})`);

        const originalText = button.textContent;
        button.textContent = 'Accepting...';
        button.disabled = true;

        try {
            const response = await fetch(`${this.baseUrl}/connections/${userId}/accept`, {
                method: 'PUT',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                if (response.status === 401) {
                    window.location.href = '/index.html';
                    return;
                }
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const connectionData = await response.json();

            // Remove from received requests section
            this.removeCardFromSection(card, 'Requests Received');

            // Add to active connections section
            this.addToActiveConnections(username, userId, connectionData);

            console.log('Connection request accepted successfully');

        } catch (error) {
            console.error('Error accepting connection request:', error);
            button.textContent = 'Failed - Try Again';
            button.style.backgroundColor = '#e74c3c';

            setTimeout(() => {
                button.textContent = originalText;
                button.disabled = false;
                button.style.backgroundColor = '#1DB954';
            }, 3000);
        }
    }

    // Decline/Cancel Connection Request
    async handleDeclineRequest(button) {
        if (button.disabled || this.isLoading) return;

        const card = button.closest('.connection-card');
        const username = card.querySelector('.connection-username').textContent;
        const userId = this.getUserIdFromCard(card);

       if (!userId) {
           console.error('Could not find user ID for decline request');
           this.debugCardData(card); // Use the debug method
           return;
       }

            // Validate UUID format
            const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
       if (!uuidRegex.test(userId)) {
           console.error('Invalid UUID format for user ID:', userId);
           return;
       }

        console.log(`Declining request from user: ${username} (ID: ${userId})`);

        const originalText = button.textContent;
        button.textContent = 'Removing...';
        button.disabled = true;

        try {
            const response = await fetch(`${this.baseUrl}/connections/${userId}`, {
                method: 'DELETE',
                credentials: 'include'
            });

            if (!response.ok) {
                if (response.status === 401) {
                    window.location.href = '/index.html';
                    return;
                }
                // Note: API currently returns 404 after successful deletion
                if (response.status !== 404) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
            }

            // Remove the card from DOM
            card.remove();
            console.log('Connection request declined/cancelled successfully');

        } catch (error) {
            console.error('Error declining connection request:', error);
            button.textContent = 'Failed - Try Again';
            button.style.backgroundColor = '#e74c3c';

            setTimeout(() => {
                button.textContent = originalText;
                button.disabled = false;
                button.style.backgroundColor = 'transparent';
            }, 3000);
        }
    }

    // View Taste Profile Modal
    async handleViewProfile(button) {
        const card = button.closest('.connection-card');
        const username = card.querySelector('.connection-username').textContent;
        const userId = this.getUserIdFromCard(card);

        if (!userId) {
            console.error('Could not find user ID for profile view');
            return;
        }

        try {
            const response = await fetch(`${this.baseUrl}/profile/${userId}`, {
                credentials: 'include'
            });

            if (!response.ok) {
                if (response.status === 401) {
                    window.location.href = '/index.html';
                    return;
                }
                if (response.status === 404) {
                    alert('Profile not found or is private');
                    return;
                }
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const profileData = await response.json();
            this.showProfileModal(profileData);

        } catch (error) {
            console.error('Error fetching profile:', error);
            alert('Failed to load profile. Please try again.');
        }
    }

    // Send Recommendation Modal
    async handleSendRecommendation(button) {
        const card = button.closest('.connection-card');
        const username = card.querySelector('.connection-username').textContent;
        const userId = this.getUserIdFromCard(card);

        if (!userId) {
            console.error('Could not find user ID for recommendation');
            return;
        }

        this.showRecommendationModal(username, userId);
    }

    // Create and show profile modal
    showProfileModal(profileData) {
        this.closeAllModals();

        const modal = document.createElement('div');
        modal.className = 'modal-overlay';
        modal.innerHTML = `
            <div class="modal-content profile-modal">
                <div class="modal-header">
                    <h2>${profileData.username}'s Taste Profile</h2>
                    <button class="modal-close">&times;</button>
                </div>
                <div class="modal-body">
                    <div class="profile-section">
                        <h3>Top Genres</h3>
                        <div class="genres-list">
                            ${profileData.topGenres ? profileData.topGenres.map(genre => `
                                <div class="genre-item">
                                    <span class="genre-name">${genre.name}</span>
                                    <span class="genre-percentage">${genre.percentage.toFixed(1)}%</span>
                                </div>
                            `).join('') : '<p>No genre data available</p>'}
                        </div>
                    </div>

                    <div class="profile-section">
                        <h3>Top Artists</h3>
                        <div class="artists-list">
                            ${profileData.topArtists ? profileData.topArtists.map(artist => `
                                <div class="artist-item">
                                    <span class="artist-rank">#${artist.rank}</span>
                                    <span class="artist-name">${artist.name}</span>
                                </div>
                            `).join('') : '<p>No artist data available</p>'}
                        </div>
                    </div>

                    <div class="profile-stats">
                        <div class="stat-item">
                            <label>Mainstream Score:</label>
                            <span>${profileData.mainstreamScore ? (profileData.mainstreamScore * 100).toFixed(1) : 'N/A'}</span>                        </div>
                        <div class="stat-item">
                            <label>How You Listen: </label>
                            <span>${profileData.howYouListen || 'N/A'}</span>
                        </div>
                        <div class="stat-item">
                            <label>Last Updated:</label>
                            <span>${profileData.lastUpdated ? new Date(profileData.lastUpdated).toLocaleDateString() : 'N/A'}</span>
                        </div>
                    </div>
                </div>
            </div>
        `;

        this.addModalStyles(modal);
        document.body.appendChild(modal);

        // Event listeners
        modal.querySelector('.modal-close').addEventListener('click', () => this.closeModal(modal));
        modal.addEventListener('click', (e) => {
            if (e.target === modal) this.closeModal(modal);
        });
    }

    // Create and show recommendation modal
    showRecommendationModal(username, userId) {
        this.closeAllModals();

        const modal = document.createElement('div');
        modal.className = 'modal-overlay';
        modal.innerHTML = `
            <div class="modal-content recommendation-modal">
                <div class="modal-header">
                    <h2>Send Recommendation to ${username}</h2>
                    <button class="modal-close">&times;</button>
                </div>
                <div class="modal-body">
                    <form class="recommendation-form">
                        <div class="form-group">
                            <label for="spotifyUrl">Spotify URL:</label>
                            <input type="url" id="spotifyUrl" name="spotifyUrl"
                                   placeholder="https://open.spotify.com/track/..." required>
                        </div>

                        <div class="form-group">
                            <label for="recommendedName">Name:</label>
                            <input type="text" id="recommendedName" name="recommendedName"
                                   placeholder="Song/Album/Artist name" required>
                        </div>

                        <div class="form-group">
                            <label for="type">Type:</label>
                            <select id="type" name="type" required>
                                <option value="">Select type...</option>
                                <option value="track">Track</option>
                                <option value="album">Album</option>
                                <option value="artist">Artist</option>
                            </select>
                        </div>

                        <div class="form-actions">
                            <button type="submit" class="btn">Send Recommendation</button>
                            <button type="button" class="btn btn-cancel">Cancel</button>
                        </div>

                        <div class="form-message"></div>
                    </form>
                </div>
            </div>
        `;

        this.addModalStyles(modal);
        document.body.appendChild(modal);

        // Event listeners
        const form = modal.querySelector('.recommendation-form');
        const closeButton = modal.querySelector('.modal-close');
        const cancelButton = modal.querySelector('.btn-cancel');

        form.addEventListener('submit', (e) => this.handleRecommendationSubmit(e, userId, modal));
        closeButton.addEventListener('click', () => this.closeModal(modal));
        cancelButton.addEventListener('click', () => this.closeModal(modal));
        modal.addEventListener('click', (e) => {
            if (e.target === modal) this.closeModal(modal);
        });
    }

    async handleRecommendationSubmit(e, userId, modal) {
        e.preventDefault();

        const form = e.target;
        const formData = new FormData(form);
        const submitButton = form.querySelector('button[type="submit"]');
        const messageDiv = form.querySelector('.form-message');

        const data = {
            spotifyUrl: formData.get('spotifyUrl'),
            recommendedName: formData.get('recommendedName'),
            recommendedToId: userId,
            type: formData.get('type')
        };

        submitButton.textContent = 'Sending...';
        submitButton.disabled = true;
        messageDiv.textContent = '';

        try {
            const response = await fetch(`${this.baseUrl}/recommendations/${userId}`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });

            if (!response.ok) {
                if (response.status === 401) {
                    window.location.href = '/index.html';
                    return;
                }
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            messageDiv.textContent = 'Recommendation sent successfully!';
            messageDiv.style.color = '#1DB954';

            setTimeout(() => {
                this.closeModal(modal);
            }, 2000);

        } catch (error) {
            console.error('Error sending recommendation:', error);
            messageDiv.textContent = 'Failed to send recommendation. Please try again.';
            messageDiv.style.color = '#e74c3c';

            submitButton.textContent = 'Send Recommendation';
            submitButton.disabled = false;
        }
    }

    addModalStyles(modal) {
        const style = document.createElement('style');
        style.textContent = `
            .modal-overlay {
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background-color: rgba(0, 0, 0, 0.8);
                display: flex;
                justify-content: center;
                align-items: center;
                z-index: 1000;
            }

            .modal-content {
                background-color: #181818;
                border-radius: 8px;
                max-width: 600px;
                max-height: 80vh;
                overflow-y: auto;
                box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
            }

            .modal-header {
                padding: 20px;
                border-bottom: 1px solid #282828;
                display: flex;
                justify-content: space-between;
                align-items: center;
            }

            .modal-header h2 {
                margin: 0;
                font-size: 24px;
            }

            .modal-close {
                background: none;
                border: none;
                color: #b3b3b3;
                font-size: 30px;
                cursor: pointer;
                padding: 0;
                width: 30px;
                height: 30px;
                display: flex;
                align-items: center;
                justify-content: center;
            }

            .modal-close:hover {
                color: #ffffff;
            }

            .modal-body {
                padding: 20px;
            }

            .profile-section {
                margin-bottom: 25px;
            }

            .profile-section h3 {
                font-size: 18px;
                margin-bottom: 15px;
                color: #1DB954;
            }

            .genre-item, .artist-item {
                display: flex;
                justify-content: space-between;
                padding: 8px 0;
                border-bottom: 1px solid #282828;
            }

            .genre-item:last-child, .artist-item:last-child {
                border-bottom: none;
            }

            .artist-rank {
                color: #1DB954;
                font-weight: bold;
                margin-right: 10px;
            }

            .profile-stats {
                background-color: #282828;
                padding: 15px;
                border-radius: 6px;
            }

            .stat-item {
                display: flex;
                justify-content: space-between;
                margin-bottom: 10px;
            }

            .stat-item:last-child {
                margin-bottom: 0;
            }

            .stat-item label {
                font-weight: 500;
                color: #b3b3b3;
            }

            .form-group {
                margin-bottom: 20px;
            }

            .form-group label {
                display: block;
                margin-bottom: 8px;
                font-weight: 500;
            }

            .form-group input, .form-group select {
                width: 100%;
                padding: 12px;
                background-color: #282828;
                border: 1px solid #535353;
                border-radius: 4px;
                color: #ffffff;
                font-size: 16px;
            }

            .form-group input:focus, .form-group select:focus {
                outline: none;
                border-color: #1DB954;
            }

            .form-actions {
                display: flex;
                gap: 15px;
                margin-top: 25px;
            }

            .form-actions .btn {
                flex: 1;
            }

            .form-actions .btn-cancel {
                background-color: transparent;
                border: 1px solid #535353;
                color: #ffffff;
            }

            .form-actions .btn-cancel:hover {
                background-color: #282828;
            }

            .form-message {
                margin-top: 15px;
                text-align: center;
                font-weight: 500;
            }
        `;

        if (!document.querySelector('#modal-styles')) {
            style.id = 'modal-styles';
            document.head.appendChild(style);
        }
    }

    closeModal(modal) {
        modal.remove();
    }

    closeAllModals() {
        const modals = document.querySelectorAll('.modal-overlay');
        modals.forEach(modal => modal.remove());
    }

    // Helper methods for UI updates
    removeCardFromSection(card, sectionTitle) {
        card.remove();
    }

    addToActiveConnections(username, userId, connectionData) {
        const activeSection = this.findSectionByTitle('Active Connections');
        if (!activeSection) return;

        const cardGrid = activeSection.querySelector('.card-grid');
        const newCard = document.createElement('div');
        newCard.className = 'connection-card';
        newCard.dataset.userId = userId;

        newCard.innerHTML = `
            <div class="avatar"><i class="ri-user-line"></i></div>
            <h3 class="connection-username">${username}</h3>
            <div class="connection-match">Connected!</div>
            <div class="connection-actions">
                <button class="btn btn-small btn-view">View Taste Profile</button>
                <button class="btn btn-small btn-recommend">Send Recommendation</button>
            </div>
        `;

        cardGrid.appendChild(newCard);
    }

    findSectionByTitle(title) {
        const sections = document.querySelectorAll('section');
        return Array.from(sections).find(section =>
            section.querySelector('h2')?.textContent.includes(title)
        );
    }

    // Replace your existing getUserIdFromCard method with this:
    getUserIdFromCard(card) {
        // Try to get from data attribute first
        if (card.dataset.userId) {
            console.log('Found user ID from data attribute:', card.dataset.userId);
            return card.dataset.userId;
        }

        // If that fails, log the issue and return null
        console.error('User ID not found in card. Card HTML:', card.outerHTML);
        console.error('Available data attributes:', Object.keys(card.dataset));

        return null;
    }

    // Also add this method to help debug what's in your cards:
    debugCardData(card) {
        console.log('Card dataset:', card.dataset);
        console.log('Card HTML:', card.outerHTML);
        const usernameEl = card.querySelector('.connection-username');
        console.log('Username element:', usernameEl?.textContent);
    }

    // UI update methods for initial load
    async updateActiveConnectionsUI(connections) {
        const grid = document.getElementById('active-connections-grid');
        if (!grid) return;

        grid.innerHTML = '';
        console.log('Active connections loaded:', connections);

        if (!connections || connections.length === 0) {
            grid.innerHTML = `
                <div style="grid-column: 1 / -1; text-align: center; padding: 40px; color: #b3b3b3;">
                    <i class="ri-user-line" style="font-size: 48px; margin-bottom: 20px; display: block; color: #535353;"></i>
                    <h3>No Active Connections</h3>
                    <p>You haven't connected with anyone yet.</p>
                </div>
            `;
            return;
        }

        // Create cards asynchronously
        for (const connection of connections) {
            const card = await this.createActiveConnectionCard(connection);
            grid.appendChild(card);
        }
    }

    async updateReceivedRequestsUI(requests) {
        const grid = document.getElementById('received-requests-grid');
        if (!grid) return;

        grid.innerHTML = '';
        console.log('Received requests loaded:', requests);

        if (!requests || requests.length === 0) {
            grid.innerHTML = `
                <div style="grid-column: 1 / -1; text-align: center; padding: 40px; color: #b3b3b3;">
                    <i class="ri-user-received-line" style="font-size: 48px; margin-bottom: 20px; display: block; color: #535353;"></i>
                    <h3>No Pending Requests</h3>
                    <p>You don't have any connection requests to review.</p>
                </div>
            `;
            return;
        }

        // Create cards asynchronously
        for (const request of requests) {
            const card = await this.createReceivedRequestCard(request);
            grid.appendChild(card);
        }
    }

    async updateSentRequestsUI(requests) {
        const grid = document.getElementById('sent-requests-grid');
        if (!grid) return;

        grid.innerHTML = '';
        console.log('Sent requests loaded:', requests);

        if (!requests || requests.length === 0) {
            grid.innerHTML = `
                <div style="grid-column: 1 / -1; text-align: center; padding: 40px; color: #b3b3b3;">
                    <i class="ri-user-shared-line" style="font-size: 48px; margin-bottom: 20px; display: block; color: #535353;"></i>
                    <h3>No Sent Requests</h3>
                    <p>You haven't sent any connection requests yet.</p>
                </div>
            `;
            return;
        }

        // Create cards asynchronously
        for (const request of requests) {
            const card = await this.createSentRequestCard(request);
            grid.appendChild(card);
        }
    }

   // Replace your card creation methods with these diagnostic versions

   async createActiveConnectionCard(connection) {
       console.log('🟢 Creating ACTIVE connection card with data:', connection);

       const card = document.createElement('div');
       card.className = 'connection-card';

       const currentUserId = await this.getCurrentUserId();
       console.log('🟢 Current user ID:', currentUserId);

       const friendId = connection.requestedById === currentUserId ?
           connection.sentToId : connection.requestedById;

       console.log('🟢 Active Connection Analysis:', {
           connectionData: connection,
           currentUserId: currentUserId,
           friendId: friendId,
           hasConnectedSince: !!connection.connectedSince,
           connectedSince: connection.connectedSince
       });

       card.dataset.userId = friendId;

       // Check if this is actually an active connection
       if (!connection.connectedSince) {
           console.error('🚨 PROBLEM: Active connection missing connectedSince field!');
           console.error('🚨 This might be an inactive connection in active section');
       }

       const connectedDate = connection.connectedSince ?
           new Date(connection.connectedSince).toLocaleDateString() :
           'Unknown Date';

       const friendUsername = connection.requestedById === currentUserId ?
           connection.sentTo : connection.requestedBy;

       console.log('🟢 Using friend username:', friendUsername);

       card.innerHTML = `
           <div class="avatar"><i class="ri-user-line"></i></div>
           <h3 class="connection-username">${friendUsername}</h3>
           <div class="connection-match">Connected since ${connectedDate}</div>
           <div class="connection-actions">
               <button class="btn btn-small btn-view">View Taste Profile</button>
               <button class="btn btn-small btn-recommend">Send Recommendation</button>
           </div>
       `;

       console.log('🟢 Created active connection card for:', friendUsername);
       return card;
   }

   async createReceivedRequestCard(request) {
       console.log('🟡 Creating RECEIVED request card with data:', request);

       const card = document.createElement('div');
       card.className = 'connection-card';
       card.dataset.userId = request.requestedById;

       console.log('🟡 Received Request Analysis:', {
           requestData: request,
           requestedById: request.requestedById,
           requestedBy: request.requestedBy,
           hasConnectedSince: !!request.connectedSince,
           sentAt: request.sentAt
       });

       // Check if this looks like an active connection
       if (request.connectedSince) {
           console.error('🚨 PROBLEM: Received request has connectedSince field!');
           console.error('🚨 This might be an active connection in received section');
       }

       const sentDate = new Date(request.sentAt).toLocaleDateString();
       const requesterUsername = request.requestedBy;

       card.innerHTML = `
           <div class="avatar"><i class="ri-user-line"></i></div>
           <h3 class="connection-username">${requesterUsername}</h3>
           <div class="sent-date">Received: ${sentDate}</div>
           <div class="connection-actions">
               <button class="btn btn-small btn-accept">Accept</button>
               <button class="btn btn-small btn-view">View Taste Profile</button>
           </div>
       `;

       console.log('🟡 Created received request card for:', requesterUsername);
       return card;
   }

   async createSentRequestCard(request) {
       console.log('🔴 Creating SENT request card with data:', request);

       const card = document.createElement('div');
       card.className = 'connection-card';
       card.dataset.userId = request.sentToId;

       console.log('🔴 Sent Request Analysis:', {
           requestData: request,
           sentToId: request.sentToId,
           sentTo: request.sentTo,
           hasConnectedSince: !!request.connectedSince,
           sentAt: request.sentAt
       });

       // Check if this looks like an active connection
       if (request.connectedSince) {
           console.error('🚨 PROBLEM: Sent request has connectedSince field!');
           console.error('🚨 This might be an active connection in sent section');
       }

       const sentDate = new Date(request.sentAt).toLocaleDateString();
       const recipientUsername = request.sentTo;

       card.innerHTML = `
           <div class="avatar"><i class="ri-user-line"></i></div>
           <h3 class="connection-username">${recipientUsername}</h3>
           <div class="pending-badge">Pending</div>
           <div class="sent-date">Sent: ${sentDate}</div>
           <div class="connection-actions">
               <button class="btn btn-small btn-cancel">Cancel Request</button>
           </div>
       `;

       console.log('🔴 Created sent request card for:', recipientUsername);
       return card;
   }

    // This method belongs inside your ConnectionsManager class
    async getCurrentUserId() {
        const storedId = localStorage.getItem('currentUserId');
        if (!storedId) {
            console.error('Current user ID not found in localStorage. User may not be logged in.');
            // Consider a redirect to the login page here
            window.location.href = '/index.html';
            return null;
        }
        console.log('Found stored user ID:', storedId);
        return storedId;
    }
}


// Initialize the connections manager when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    setTimeout(() => {
        if (window.location.pathname.includes('connections')) {
            console.log('Initializing ConnectionsManager');
            window.connectionsManager = new ConnectionsManager();
        }
    }, 100);
});

// Export for potential use by other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ConnectionsManager;
}
