// recommendations.js
const getBaseUrl = () => {
    return window.location.origin;
};

class RecommendationsManager {
    constructor() {
        this.baseUrl = getBaseUrl();
        this.apiEndpoint = '/recommendations'; // Changed from /api/recommendations to /recommendations
        this.recommendationsContainer = null;
        this.isLoading = false;
    }

    init() {
        this.recommendationsContainer = document.querySelector('.recommendations-grid');
        // Only load recommendations if the profile view is visible (user is authenticated and synced)
        const profileView = document.getElementById('profile-view');
        if (this.recommendationsContainer && profileView && !profileView.classList.contains('hidden')) {
            this.loadRecommendations();
        }
    }

    async loadRecommendations() {
        if (this.isLoading) return;

        this.isLoading = true;
        this.showLoadingState();

        try {
            console.log('Loading recommendations from:', `${this.baseUrl}${this.apiEndpoint}`);
            console.log('Using credentials: include');

            const response = await fetch(`${this.baseUrl}${this.apiEndpoint}`, {
                method: 'GET',
                credentials: 'include' // Include credentials for authentication
            });

            console.log('Recommendations response status:', response.status);
            console.log('Recommendations response headers:', [...response.headers.entries()]);

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    console.error('Authentication failed when fetching recommendations. Status:', response.status);
                    console.error('Current cookies:', document.cookie);
                    console.error('Cookies available:', document.cookie.length > 0 ? 'YES' : 'NO');

                    // Let's try a different endpoint path
                    if (this.apiEndpoint === '/recommendations') {
                        console.log('Trying alternative endpoint: /api/recommendations');
                        this.apiEndpoint = '/api/recommendations';
                        this.isLoading = false;
                        return this.loadRecommendations(); // Retry with different path
                    }

                    throw new Error(`Authentication failed: ${response.status}`);
                }
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const recommendations = await response.json();
            console.log('Recommendations loaded:', recommendations);
            this.renderRecommendations(recommendations);
        } catch (error) {
            console.error('Error loading recommendations:', error);
            this.showErrorState();
        } finally {
            this.isLoading = false;
        }
    }

    showLoadingState() {
        if (!this.recommendationsContainer) return;

        this.recommendationsContainer.innerHTML = `
            <div class="loading-state" style="grid-column: 1 / -1; text-align: center; padding: 40px;">
                <i class="ri-loader-4-line" style="font-size: 32px; color: #1DB954; animation: spin 1s linear infinite;"></i>
                <p style="margin-top: 16px; color: #b3b3b3;">Loading your recommendations...</p>
            </div>
        `;
    }

    showErrorState() {
        if (!this.recommendationsContainer) return;

        this.recommendationsContainer.innerHTML = `
            <div class="error-state" style="grid-column: 1 / -1; text-align: center; padding: 40px;">
                <i class="ri-error-warning-line" style="font-size: 32px; color: #ff6b6b; margin-bottom: 16px;"></i>
                <p style="color: #b3b3b3; margin-bottom: 16px;">Failed to load recommendations</p>
                <button onclick="recommendationsManager.loadRecommendations()"
                        style="background-color: #1DB954; color: #121212; border: none; padding: 8px 16px; border-radius: 20px; cursor: pointer;">
                    Try Again
                </button>
            </div>
        `;
    }

    showEmptyState() {
        if (!this.recommendationsContainer) return;

        this.recommendationsContainer.innerHTML = `
            <div class="empty-state" style="grid-column: 1 / -1; text-align: center; padding: 40px;">
                <i class="ri-music-2-line" style="font-size: 32px; color: #b3b3b3; margin-bottom: 16px;"></i>
                <p style="color: #b3b3b3;">No recommendations yet</p>
                <p style="color: #666; font-size: 14px; margin-top: 8px;">Connect with other users to start receiving music recommendations!</p>
            </div>
        `;
    }

    renderRecommendations(recommendations) {
        if (!this.recommendationsContainer) return;

        console.log('Rendering recommendations:', recommendations);

        if (!recommendations || recommendations.length === 0) {
            this.showEmptyState();
            return;
        }

        const recommendationCards = recommendations.map(rec => {
            console.log('Processing recommendation:', rec);
            return this.createRecommendationCard(rec);
        }).join('');
        this.recommendationsContainer.innerHTML = recommendationCards;
    }

    createRecommendationCard(recommendation) {
        const {
            recommenderUsername,
            recommendedItemName,
            spotifyUrl,
            type,
            recommendedAt
        } = recommendation;

        // Add null checks and default values
        const safeRecommenderUsername = recommenderUsername || 'Unknown User';
        const safeRecommendedItemName = recommendedItemName || 'Unknown Item';
        const safeSpotifyUrl = spotifyUrl || '#';
        const safeType = type || 'unknown';

        // Format the date
        const formattedDate = this.formatRecommendationDate(recommendedAt);

        // Get the appropriate icon and link text based on type
        const { linkText, typeClass } = this.getTypeInfo(safeType);

        return `
            <div class="recommendation-card">
                <div class="recommender">
                    <div class="recommender-avatar">
                        <i class="ri-user-smile-line"></i>
                    </div>
                    <div class="recommender-name">${this.escapeHtml(safeRecommenderUsername)}</div>
                </div>
                <h4 class="recommendation-title">${this.escapeHtml(safeRecommendedItemName)}</h4>
                <div class="recommendation-type ${typeClass}">${this.capitalizeFirst(safeType)}</div>
                <a href="${this.escapeHtml(safeSpotifyUrl)}" class="spotify-link" target="_blank" rel="noopener noreferrer">
                    <i class="ri-spotify-fill"></i>
                    ${linkText}
                </a>
                <div class="recommendation-date">${formattedDate}</div>
            </div>
        `;
    }

    getTypeInfo(type) {
        switch (type.toLowerCase()) {
            case 'track':
                return { linkText: 'Listen on Spotify', typeClass: 'type-track' };
            case 'album':
                return { linkText: 'Listen on Spotify', typeClass: 'type-album' };
            case 'artist':
                return { linkText: 'Check on Spotify', typeClass: 'type-artist' };
            default:
                return { linkText: 'Open on Spotify', typeClass: 'type-default' };
        }
    }

    formatRecommendationDate(dateString) {
        try {
            const date = new Date(dateString);
            const now = new Date();
            const diffInMs = now - date;
            const diffInDays = Math.floor(diffInMs / (1000 * 60 * 60 * 24));

            if (diffInDays === 0) {
                return 'Recommended today';
            } else if (diffInDays === 1) {
                return 'Recommended yesterday';
            } else if (diffInDays < 7) {
                return `Recommended ${diffInDays} days ago`;
            } else if (diffInDays < 14) {
                return 'Recommended 1 week ago';
            } else if (diffInDays < 21) {
                return 'Recommended 2 weeks ago';
            } else if (diffInDays < 30) {
                return 'Recommended 3 weeks ago';
            } else {
                return `Recommended ${Math.floor(diffInDays / 30)} month${Math.floor(diffInDays / 30) > 1 ? 's' : ''} ago`;
            }
        } catch (error) {
            console.error('Error formatting date:', error);
            return 'Recently recommended';
        }
    }

    capitalizeFirst(str) {
        return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
    }

    escapeHtml(text) {
        // Handle null, undefined, or non-string values
        if (text == null) return '';
        if (typeof text !== 'string') return String(text);

        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return text.replace(/[&<>"']/g, function(m) { return map[m]; });
    }

    // Method to refresh recommendations (can be called externally)
    refresh() {
        this.loadRecommendations();
    }
}

// Initialize the recommendations manager
const recommendationsManager = new RecommendationsManager();

// Function to be called after successful authentication and profile loading
window.loadRecommendations = () => {
    recommendationsManager.loadRecommendations();
};

// Auto-initialize when DOM is loaded, but only if user is authenticated
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => recommendationsManager.init());
} else {
    recommendationsManager.init();
}

// recommendationsManager is available globally for use in other scripts