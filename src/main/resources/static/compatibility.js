// compatibility.js - Dynamic compatibility feature for VibeMatch
class CompatibilityManager {
    constructor() {
        this.compatibilityData = null;
        this.isLoading = false;
        this.baseUrl = window.location.origin;
        this.init();
    }

    init() {
        // Only initialize if we're on the connections page
        if (!window.location.pathname.includes('connections')) {
            return;
        }

        console.log('CompatibilityManager initialized');
        this.setupFindButton();

        // Delay state restoration to ensure DOM is fully ready
        setTimeout(() => {
            this.restoreState();
        }, 200);
    }

    setupFindButton() {
        const findButton = document.querySelector('.find-compatible .btn');
        if (findButton) {
            findButton.addEventListener('click', () => {
                this.findCompatibleUsers();
            });
        }
    }

    async findCompatibleUsers() {
        if (this.isLoading) return;

        const findButton = document.querySelector('.find-compatible .btn');
        const container = this.getCompatibilityContainer();

        if (!findButton || !container) return;

        this.isLoading = true;

        // Update button state
        const originalText = findButton.innerHTML;
        findButton.innerHTML = '<i class="ri-refresh-line"></i> Finding Compatible Users...';
        findButton.disabled = true;
        findButton.style.opacity = '0.7';

        // Clear container
        container.innerHTML = '';

        try {
            console.log('Fetching compatible users from /compatibility/discover');
            const response = await fetch(`${this.baseUrl}/compatibility/discover`, {
                credentials: 'include'
            });

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    console.error('Authentication failed. Redirecting to login.');
                    window.location.href = '/index.html';
                    return;
                }
                if (response.status === 404 || response.status === 204) {
                    // No compatible users found (404 response)
                    this.showNoUsersMessage(container);
                    this.compatibilityData = [];
                    this.saveState();
                    return;
                }
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const compatibilityData = await response.json();
            console.log('Compatibility data received:', compatibilityData);

            // Handle both 404 responses AND empty arrays
            if (!compatibilityData || compatibilityData.length === 0) {
                console.log('No compatible users found (empty response)');
                this.showNoUsersMessage(container);
                this.compatibilityData = [];
            } else {
                console.log(`Found ${compatibilityData.length} compatible users`);
                this.renderCompatibilityCards(container, compatibilityData);
                this.compatibilityData = compatibilityData;

                // Update button text to indicate refresh functionality
                const findButton = document.querySelector('.find-compatible .btn');
                if (findButton) {
                    findButton.innerHTML = '<i class="ri-refresh-line"></i> Refresh Compatible Users';
                }
            }

            this.saveState();

        } catch (error) {
            console.error('Error fetching compatible users:', error);
            this.showErrorMessage(container, 'Failed to find compatible users. Please try again.');
        } finally {
            this.isLoading = false;
            // Restore button state
            findButton.innerHTML = originalText;
            findButton.disabled = false;
            findButton.style.opacity = '1';
        }
    }

    getCompatibilityContainer() {
        // Find the section with "Compatibility Cards" title
        const sections = document.querySelectorAll('section');
        const compatibilitySection = Array.from(sections).find(section =>
            section.querySelector('h2')?.textContent.includes('Compatibility Cards')
        );

        if (compatibilitySection) {
            return compatibilitySection.querySelector('.card-grid');
        }

        console.error('Compatibility section not found');
        return null;
    }

    showNoUsersMessage(container) {
        container.innerHTML = `
            <div style="grid-column: 1 / -1; text-align: center; padding: 60px 20px; color: #b3b3b3;">
                <i class="ri-user-search-line" style="font-size: 48px; margin-bottom: 20px; display: block; color: #535353;"></i>
                <h3 style="font-size: 24px; margin-bottom: 10px; color: #ffffff;">No compatible users found</h3>
                <p>We couldn't find any users with similar music tastes right now. Try again later as new users join VibeMatch!</p>
            </div>
        `;
    }

    showErrorMessage(container, message) {
        container.innerHTML = `
            <div style="grid-column: 1 / -1; text-align: center; padding: 60px 20px; color: #b3b3b3;">
                <i class="ri-error-warning-line" style="font-size: 48px; margin-bottom: 20px; display: block; color: #e74c3c;"></i>
                <h3 style="font-size: 24px; margin-bottom: 10px; color: #ffffff;">Oops! Something went wrong</h3>
                <p>${message}</p>
            </div>
        `;
    }

    renderCompatibilityCards(container, compatibilityData) {
        container.innerHTML = '';

        compatibilityData.forEach((match, index) => {
            const card = this.createCompatibilityCard(match, index);
            container.appendChild(card);
        });
    }

    createCompatibilityCard(match, index) {
        const card = document.createElement('div');
        card.className = 'compatibility-card';
        card.dataset.cardIndex = index;

        // Convert scores to percentages
        const discoveryScore = Math.round(match.discoveryCompatibilityScore * 100);
        const tasteScore = Math.round(match.tasteCompatibilityScore * 100);

        // Determine score classes
        const discoveryClass = this.getScoreClass(discoveryScore);
        const tasteClass = this.getScoreClass(tasteScore);

        // Format last calculated date
        const lastCalculated = new Date(match.lastCalculated).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });

        card.innerHTML = `
            <h3 class="card-username">${match.targetUser.username}</h3>
            <div class="compatibility-scores">
                <div class="score">
                    <div class="score-label">Discovery Match</div>
                    <div class="score-value ${discoveryClass}">${discoveryScore}%</div>
                </div>
                <div class="score">
                    <div class="score-label">Taste Match</div>
                    <div class="score-value ${tasteClass}">${tasteScore}%</div>
                </div>
            </div>
            <div class="card-actions">
                <button class="btn btn-small btn-request" data-user-id="${match.targetUser.userId}">Send Connection Request</button>
                <button class="btn btn-small btn-details">Show Details</button>
            </div>
            <div class="card-details">
                ${this.renderDetails(match)}
                <div class="calculated-date">Last calculated: ${lastCalculated}</div>
            </div>
        `;

        // Add event listeners
        this.setupCardEventListeners(card, match);

        return card;
    }

    renderDetails(match) {
        let html = '';

        // Shared Artists section
        if (match.sharedArtists && match.sharedArtists.length > 0) {
            html += `
                <div class="detail-section">
                    <h4 class="detail-title">Shared Artists</h4>
                    ${match.sharedArtists.map(artist => `
                        <div class="shared-item">
                            <span>${artist.artistName}</span>
                            <span>Your #${artist.yourRank}, Their #${artist.theirRank}</span>
                        </div>
                    `).join('')}
                </div>
            `;
        }

        // Shared Genres section
        if (match.sharedGenres && match.sharedGenres.length > 0) {
            html += `
                <div class="detail-section">
                    <h4 class="detail-title">Shared Genres</h4>
                    ${match.sharedGenres.map(genre => `
                        <div class="shared-item">
                            <span>${genre.genreName}</span>
                            <span>You: ${genre.yourPercentage.toFixed(1)}%, Them: ${genre.theirPercentage.toFixed(1)}%</span>
                        </div>
                    `).join('')}
                </div>
            `;
        }

        // Why Compatible section
        if (match.whyCompatible && match.whyCompatible.length > 0) {
            html += `
                <div class="detail-section">
                    <h4 class="detail-title">Why Compatible</h4>
                    <div class="why-compatible">
                        ${match.whyCompatible.join('. ')}
                    </div>
                </div>
            `;
        }

        return html;
    }

    setupCardEventListeners(card, match) {
        // Details toggle button
        const detailsButton = card.querySelector('.btn-details');
        const detailsSection = card.querySelector('.card-details');

        if (detailsButton && detailsSection) {
            detailsButton.addEventListener('click', (e) => {
                e.preventDefault(); // Prevent any default behavior
                e.stopPropagation(); // Stop event bubbling

                const isExpanded = card.classList.contains('expanded');

                if (isExpanded) {
                    card.classList.remove('expanded');
                    detailsSection.style.display = 'none';
                    detailsButton.textContent = 'Show Details';
                } else {
                    card.classList.add('expanded');
                    detailsSection.style.display = 'block';
                    detailsButton.textContent = 'Hide Details';
                }
            });
        }

        // Connection request button - THIS IS THE KEY FIX
        const requestButton = card.querySelector('.btn-request');
        if (requestButton) {
            requestButton.addEventListener('click', (e) => {
                e.preventDefault(); // 🔥 CRITICAL - Prevents form submission/redirect
                e.stopPropagation(); // 🔥 CRITICAL - Stops event bubbling
                this.sendConnectionRequest(match.targetUser.userId, match.targetUser.username, requestButton);
            });
        }
    }

    async sendConnectionRequest(userId, username, button) {
        if (button.disabled) return;

        const originalText = button.textContent;
        button.textContent = 'Sending...';
        button.disabled = true;

        try {
            // FIX: Update the URL to match your API endpoint
            const response = await fetch(`${this.baseUrl}/connections/request/${userId}`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                }
                // Remove the body since userId is in the URL path
            });

            if (!response.ok) {
                if (response.status === 401) {
                    console.error('Authentication failed. Redirecting to login.');
                    window.location.href = '/index.html';
                    return;
                }

                if (response.status === 403) {
                     // Instead of redirecting, show a proper error message
                     console.error('Forbidden - insufficient permissions or CSRF issue');
                     const errorData = await response.json().catch(() => ({}));
                     throw new Error(errorData.message || 'Access denied');
                }



                throw new Error(`HTTP error! status: ${response.status}`);
            }

            // Success
            button.textContent = 'Request Sent!';
            button.style.backgroundColor = '#535353';
            button.style.cursor = 'default';

            setTimeout(() => {
                button.textContent = 'Sent';
                button.style.backgroundColor = '#535353';
            }, 2000);

        } catch (error) {
            console.error('Error sending connection request:', error);
            button.textContent = 'Failed - Try Again';
            button.disabled = false;
            button.style.backgroundColor = '#e74c3c';

            setTimeout(() => {
                button.textContent = originalText;
                button.style.backgroundColor = '#1DB954';
            }, 3000);
        }
    }

    getScoreClass(score) {
        if (score >= 80) return 'high';
        if (score >= 60) return 'medium';
        return 'low';
    }

    // Enhanced state management for cross-page persistence
    saveState() {
        if (this.compatibilityData !== null) {
            // Use sessionStorage equivalent with window object for longer persistence
            window.vibematchAppState = window.vibematchAppState || {};
            window.vibematchAppState.compatibility = {
                data: this.compatibilityData,
                timestamp: Date.now(),
                hasSearched: true
            };
            console.log('Compatibility state saved:', window.vibematchAppState.compatibility);
        }
    }

    restoreState() {
        // Check if we have saved state (persist for entire session)
        if (window.vibematchAppState?.compatibility) {
            const { data, timestamp, hasSearched } = window.vibematchAppState.compatibility;

            console.log('Found saved compatibility state:', { hasSearched, dataLength: data?.length, timestamp });

            if (hasSearched) {
                this.compatibilityData = data;

                // Use setTimeout to ensure DOM is ready
                setTimeout(() => {
                    const container = this.getCompatibilityContainer();

                    if (container) {
                        if (data && data.length > 0) {
                            console.log('Restoring compatibility cards');
                            this.renderCompatibilityCards(container, data);
                        } else {
                            console.log('Restoring no users message');
                            this.showNoUsersMessage(container);
                        }

                        // Update button text to show it's already been searched
                        const findButton = document.querySelector('.find-compatible .btn');
                        if (findButton) {
                            findButton.innerHTML = '<i class="ri-refresh-line"></i> Refresh Compatible Users';
                        }
                    } else {
                        console.error('Container not found during state restore');
                    }
                }, 50);
            }
        } else {
            console.log('No saved compatibility state found');
        }
    }

    clearState() {
        if (window.vibematchAppState?.compatibility) {
            delete window.vibematchAppState.compatibility;
        }
        this.compatibilityData = null;
    }

    // Method to reset state (useful for logout or when sync status changes)
    static clearAllState() {
        if (window.vibematchAppState?.compatibility) {
            delete window.vibematchAppState.compatibility;
        }
    }
}

// Initialize the compatibility manager when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    // Small delay to ensure other scripts have loaded
    setTimeout(() => {
        if (window.location.pathname.includes('connections')) {
            console.log('Initializing CompatibilityManager');
            window.compatibilityManager = new CompatibilityManager();
        }
    }, 100);
});

// Global function to clear compatibility state (called on logout or sync changes)
window.clearCompatibilityState = () => {
    CompatibilityManager.clearAllState();
    if (window.compatibilityManager) {
        window.compatibilityManager.clearState();
    }
};

// Export for potential use by other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = CompatibilityManager;
}