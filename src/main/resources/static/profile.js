// profile.js - Updated to work with your actual HTML structure

const getBaseUrl = () => {
    return window.location.origin;
};

// Function to fetch and display the user's taste profile
export const populateTasteProfile = async () => {
    try {
        const profileResponse = await fetch(`${getBaseUrl()}/profile/me`, {
            credentials: 'include' // Include credentials for authentication
        });

        if (!profileResponse.ok) {
            if (profileResponse.status === 401 || profileResponse.status === 403) {
                console.error('Authentication failed when fetching profile. Redirecting to login.');
                window.location.href = '/index.html';
                return;
            }
            throw new Error(`Failed to fetch user profile data. Status: ${profileResponse.status}`);
        }

        const profileData = await profileResponse.json();
        console.log('Profile data received:', profileData);

        // Update username in the welcome banner
        const usernameDisplay = document.getElementById('username-display');
        if (usernameDisplay && profileData.username) {
            usernameDisplay.textContent = profileData.username;
        }

        // Populate Top Genres
        populateTopGenres(profileData.topGenres);

        // Populate Top Artists
        populateTopArtists(profileData.topArtists);

        // Update Mainstream Score
        updateMainstreamScore(profileData.mainstreamScore);

        // Update How You Listen and Last Updated
        updateHowYouListenInfo(profileData.howYouListen, profileData.lastUpdated);

    } catch (error) {
        console.error('Error fetching and populating profile data:', error);
        showErrorMessage('Failed to load your taste profile. Please try refreshing the page.');
    }
};

// Function to populate top genres in the existing HTML structure
const populateTopGenres = (topGenres) => {
    if (!topGenres || topGenres.length === 0) return;

    // Find the Top Genres card (first profile-card)
    const genreCards = document.querySelectorAll('.profile-card');
    const genreCard = Array.from(genreCards).find(card =>
        card.querySelector('h3')?.textContent === 'Top Genres'
    );

    if (!genreCard) {
        console.error('Top Genres card not found');
        return;
    }

    // Clear existing genre items except the header
    const existingItems = genreCard.querySelectorAll('.genre-item');
    existingItems.forEach(item => item.remove());

    // Add new genre items
    topGenres.forEach(genre => {
        const genreItem = document.createElement('div');
        genreItem.className = 'genre-item';
        genreItem.innerHTML = `
            <div>
                <span class="genre-name">${genre.name}</span>
                <div class="genre-artist-count">${genre.artistCount} artists</div>
            </div>
            <span class="genre-percentage">${genre.percentage.toFixed(1)}%</span>
        `;
        genreCard.appendChild(genreItem);
    });
};

// Function to populate top artists in the existing HTML structure
const populateTopArtists = (topArtists) => {
    if (!topArtists || topArtists.length === 0) return;

    // Find the Top Artists card (second profile-card)
    const profileCards = document.querySelectorAll('.profile-card');
    const artistCard = Array.from(profileCards).find(card =>
        card.querySelector('h3')?.textContent === 'Top Artists'
    );

    if (!artistCard) {
        console.error('Top Artists card not found');
        return;
    }

    // Clear existing artist items except the header
    const existingItems = artistCard.querySelectorAll('.artist-item');
    existingItems.forEach(item => item.remove());

    // Add new artist items
    topArtists.forEach(artist => {
        const artistItem = document.createElement('div');
        artistItem.className = 'artist-item';
        artistItem.innerHTML = `
            <span class="artist-name">${artist.name}</span>
            <span class="artist-rank">#${artist.rank}</span>
        `;
        artistCard.appendChild(artistItem);
    });
};

// Function to update mainstream score
const updateMainstreamScore = (mainstreamScore) => {
    if (mainstreamScore === undefined || mainstreamScore === null) return;

    // Find the Mainstream Score card
    const profileCards = document.querySelectorAll('.profile-card');
    const scoreCard = Array.from(profileCards).find(card =>
        card.querySelector('h3')?.textContent === 'Mainstream Score'
    );

    if (!scoreCard) {
        console.error('Mainstream Score card not found');
        return;
    }

    // Convert decimal to percentage and round to 1 decimal place
    const percentageScore = Math.round(mainstreamScore * 100 * 10) / 10;

    // Update the score value
    const scoreValueElement = scoreCard.querySelector('.score-value');
    if (scoreValueElement) {
        scoreValueElement.textContent = percentageScore;
    }

    // Update the description text
    const descriptionElement = scoreCard.querySelector('p');
    if (descriptionElement) {
        let description;
        if (percentageScore < 30) {
            description = 'Your taste is quite niche with mostly underground and alternative artists.';
        } else if (percentageScore < 50) {
            description = 'Your taste leans towards niche with a good mix of popular and underground artists.';
        } else if (percentageScore < 70) {
            description = 'Your taste is moderately mainstream with a good balance of popular and niche artists.';
        } else {
            description = 'Your taste is quite mainstream, focusing on popular and widely-known artists.';
        }
        descriptionElement.textContent = description;
    }
};

// Function to update how you listen and last updated info
const updateHowYouListenInfo = (howYouListen, lastUpdated) => {
    // Find the How You Listen card
    const profileCards = document.querySelectorAll('.profile-card');
    const howYouListenCard = Array.from(profileCards).find(card =>
        card.querySelector('h3')?.textContent === 'How You Listen'
    );

    if (!howYouListenCard) {
        console.error('How You Listen card not found');
        return;
    }

    // Update how you listen text (keeping the original class name for now)
    const patternElement = howYouListenCard.querySelector('.how-you-listen');
    if (patternElement && howYouListen) {
        patternElement.textContent = howYouListen;
    }

    // Update last updated text
    const lastUpdatedElement = howYouListenCard.querySelector('.last-updated');
    if (lastUpdatedElement && lastUpdated) {
        const formattedDate = new Date(lastUpdated).toLocaleString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
        lastUpdatedElement.textContent = `Last updated: ${formattedDate}`;
    }
};

// Function to show error messages to the user
const showErrorMessage = (message) => {
    // You can customize this to show errors in your preferred way
    const syncStatus = document.getElementById('sync-status');
    if (syncStatus) {
        syncStatus.textContent = message;
        syncStatus.style.color = '#ff6347';
    }
};

// Helper function to check if profile view is visible and populate if needed
export const checkAndPopulateProfile = () => {
    const profileView = document.getElementById('profile-view');
    if (profileView && !profileView.classList.contains('hidden')) {
        populateTasteProfile();
    }
};

