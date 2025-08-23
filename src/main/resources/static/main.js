//import { populateTasteProfile } from './profile.js';
//
//const getBaseUrl = () => {
//    return window.location.origin;
//};
//
//// ------------------------------------------------------------
//// HANDLERS FOR THE INDEX PAGE (Login Flow)
//// ------------------------------------------------------------
//const handleSpotifyLogin = () => {
//    const loginButton = document.querySelector('.btn');
//    if (loginButton) {
//        loginButton.addEventListener('click', () => {
//            console.log('Spotify login button clicked');
//            window.location.href = `${getBaseUrl()}/auth/login`;
//        });
//    }
//};
//
//// ------------------------------------------------------------
//// HANDLERS FOR THE DASHBOARD PAGE
//// ------------------------------------------------------------
//const getAndDisplayUser = async () => {
//    try {
//        console.log('Fetching user data from /auth/me');
//        const response = await fetch(`${getBaseUrl()}/auth/me`, {
//            credentials: 'include'
//        });
//        if (!response.ok) {
//            if (response.status === 401 || response.status === 403) {
//                console.error('Authentication failed. Redirecting to login.');
//                // Clear compatibility state on auth failure
//                if (typeof window.clearCompatibilityState === 'function') {
//                    window.clearCompatibilityState();
//                }
//                window.location.href = '/index.html';
//                return;
//            }
//            console.error(`Failed to fetch user data with status: ${response.status}`);
//            return;
//        }
//
//        const user = await response.json();
//        console.log('Current user:', user);
//
//        if (user && user.id) {
//            localStorage.setItem('currentUserId', user.id);
//            console.log('User ID stored in localStorage:', user.id);
//        } else {
//                    console.error('User ID not found in /auth/me response.');
//        }
//
//        const currentPath = window.location.pathname;
//
//        if (currentPath.includes('dashboard')) {
//            console.log('Updating dashboard UI');
//            const usernameDisplay = document.getElementById('username-display');
//            if (usernameDisplay) {
//                usernameDisplay.textContent = user.username;
//            } else {
//                console.log('username-display element not found; skipping username update.');
//            }
//
//            console.log('Fetching sync status from /sync/last-synced');
//            const syncStatusResponse = await fetch(`${getBaseUrl()}/sync/last-synced`, {
//                credentials: 'include'
//            });
//            if (!syncStatusResponse.ok) {
//                if (syncStatusResponse.status === 401 || response.status === 403) {
//                    console.error('Authentication failed on /last-synced. Redirecting to login.');
//                    // Clear compatibility state on auth failure
//                    if (typeof window.clearCompatibilityState === 'function') {
//                        window.clearCompatibilityState();
//                    }
//                    window.location.href = '/index.html';
//                    return;
//                }
//                console.error(`Failed to fetch sync status with status: ${syncStatusResponse.status}`);
//                return;
//            }
//            const syncStatus = await syncStatusResponse.json();
//            const hasSyncedRecently = syncStatus.hasSynced;
//
//            const syncView = document.getElementById('sync-view');
//            const profileView = document.getElementById('profile-view');
//            const staticContent = document.getElementById('static-content');
//            const header = document.getElementById('header');
//
//            if (hasSyncedRecently) {
//                if (syncView) syncView.classList.add('hidden');
//                if (profileView) profileView.classList.remove('hidden');
//                if (staticContent) staticContent.classList.remove('hidden');
//                if (header) header.classList.remove('hidden');
//                try {
//                    console.log('Populating taste profile');
//                    await populateTasteProfile();
//                } catch (profileError) {
//                    console.error('Failed to populate taste profile:', profileError);
//                }
//            } else {
//                // User hasn't synced recently - clear compatibility state
//                if (typeof window.clearCompatibilityState === 'function') {
//                    console.log('Clearing compatibility state - user not synced');
//                    window.clearCompatibilityState();
//                }
//                if (syncView) syncView.classList.remove('hidden');
//                if (profileView) profileView.classList.add('hidden');
//                if (staticContent) staticContent.classList.add('hidden');
//                if (header) header.classList.add('hidden');
//            }
//        } else if (currentPath.includes('connections')) {
//            console.log('Connections page: User data fetched, ready for dynamic logic.');
//        }
//
//    } catch (error) {
//        console.error('An unexpected error occurred in getAndDisplayUser:', error);
//    }
//};
//
//// Manages the sync process
//const handleSync = async () => {
//    const syncStatusElement = document.getElementById('sync-status');
//    const syncButton = document.getElementById('sync-button');
//    const syncView = document.getElementById('sync-view');
//    const profileView = document.getElementById('profile-view');
//    const staticContent = document.getElementById('static-content');
//    const header = document.getElementById('header');
//
//    syncButton.disabled = true;
//    syncStatusElement.textContent = 'Syncing your music data, please wait...';
//    syncStatusElement.style.color = '#b3b3b3';
//    syncButton.innerHTML = '<i class="ri-refresh-line spinning"></i> Syncing...';
//
//    // Clear compatibility state when starting new sync
//    if (typeof window.clearCompatibilityState === 'function') {
//        console.log('Clearing compatibility state - new sync started');
//        window.clearCompatibilityState();
//    }
//
//    try {
//        console.log('Starting sync process');
//        const response = await fetch(`${getBaseUrl()}/sync`, {
//            method: 'POST',
//            credentials: 'include',
//            headers: {
//                'Content-Type': 'application/json'
//            }
//        });
//
//        if (!response.ok) {
//            if (response.status === 401 || response.status === 403) {
//                console.error('Authentication failed during sync. Redirecting to login.');
//                window.location.href = '/index.html';
//                return;
//            }
//            console.error(`HTTP error during sync! status: ${response.status}`);
//            throw new Error(`HTTP error! status: ${response.status}`);
//        }
//
//        const data = await response.json();
//        const taskId = data.taskId;
//        console.log('Sync task started, taskId:', taskId);
//
//        const pollInterval = setInterval(async () => {
//            console.log('Polling sync status for taskId:', taskId);
//            const statusResponse = await fetch(`${getBaseUrl()}/sync/status?id=${taskId}`, {
//                credentials: 'include'
//            });
//
//            if (!statusResponse.ok) {
//                if (statusResponse.status === 401 || response.status === 403) {
//                    console.error('Authentication failed during status check. Redirecting to login.');
//                    clearInterval(pollInterval);
//                    window.location.href = '/index.html';
//                    return;
//                }
//                console.error(`HTTP error during status check! status: ${statusResponse.status}`);
//                clearInterval(pollInterval);
//                throw new Error(`HTTP error! status: ${statusResponse.status}`);
//            }
//
//            const statusData = await statusResponse.json();
//            syncStatusElement.textContent = `Sync status: ${statusData.status}`;
//
//            if (statusData.status === 'SUCCESS') {
//                clearInterval(pollInterval);
//                syncStatusElement.textContent = 'Sync complete! Your music is ready to match. 🎉';
//                syncStatusElement.style.color = '#1DB954';
//                // Switch views without reloading
//                if (syncView) syncView.classList.add('hidden');
//                if (profileView) profileView.classList.remove('hidden');
//                if (staticContent) staticContent.classList.remove('hidden');
//                if (header) header.classList.remove('hidden');
//                try {
//                    console.log('Populating taste profile after sync');
//                    await populateTasteProfile();
//                } catch (profileError) {
//                    console.error('Failed to populate taste profile:', profileError);
//                }
//            } else if (statusData.status === 'FAIL') {
//                clearInterval(pollInterval);
//                syncStatusElement.textContent = 'Sync failed. Please try again later.';
//                syncStatusElement.style.color = '#ff6347';
//                syncButton.disabled = false;
//                syncButton.innerHTML = '<i class="ri-refresh-line"></i> Sync My Music';
//            }
//        }, 5000);
//
//    } catch (error) {
//        console.error('There was a problem with the sync operation:', error);
//        syncStatusElement.textContent = 'An error occurred during sync. Please try again.';
//        syncStatusElement.style.color = '#ff6347';
//        syncButton.disabled = false;
//        syncButton.innerHTML = '<i class="ri-refresh-line"></i> Sync My Music';
//    }
//};
//
//// Manages the logout process
//const handleLogout = async () => {
//    try {
//        console.log('Logging out');
//
//        // Clear compatibility state on logout
//        if (typeof window.clearCompatibilityState === 'function') {
//            console.log('Clearing compatibility state on logout');
//            window.clearCompatibilityState();
//        }
//
//        localStorage.removeItem('currentUserId');
//
//        // Clear all app state
//        delete window.vibematchAppState;
//
//        await fetch(`${getBaseUrl()}/auth/logout`, {
//            credentials: 'include'
//        });
//        window.location.href = '/index.html';
//    } catch (error) {
//        console.error('Logout failed:', error);
//        window.location.href = '/index.html';
//    }
//};
//
//// ------------------------------------------------------------
//// INITIALIZATION AND EVENT LISTENERS
//// ------------------------------------------------------------
//document.addEventListener('DOMContentLoaded', () => {
//    const currentPath = window.location.pathname;
//    console.log('DOMContentLoaded, current path:', currentPath);
//
//    if (currentPath.includes('dashboard')) {
//        console.log('Initializing dashboard');
//        getAndDisplayUser();
//
//        const syncButton = document.getElementById('sync-button');
//        if (syncButton) {
//            syncButton.addEventListener('click', handleSync);
//        }
//
//        const logoutLink = document.getElementById('logout-link');
//        if (logoutLink) {
//            logoutLink.addEventListener('click', (e) => {
//                e.preventDefault();
//                console.log('Logout link clicked');
//                handleLogout();
//            });
//        }
//
//        setTimeout(() => {
//            if (typeof NavigationManager !== 'undefined') {
//                console.log('Creating NavigationManager for dashboard');
//                new NavigationManager();
//            } else {
//                console.error('NavigationManager not defined');
//            }
//        }, 100);
//
//    } else if (currentPath.includes('connections')) {
//        console.log('Initializing connections');
//        getAndDisplayUser();
//
//        const logoutLink = document.getElementById('logout-link');
//        if (logoutLink) {
//            logoutLink.addEventListener('click', (e) => {
//                e.preventDefault();
//                console.log('Logout link clicked');
//                handleLogout();
//            });
//        }
//
//        setTimeout(() => {
//            if (typeof NavigationManager !== 'undefined') {
//                console.log('Creating NavigationManager for connections');
//                new NavigationManager();
//            } else {
//                console.error('NavigationManager not defined');
//            }
//        }, 100);
//
//    } else if (currentPath === '/' || currentPath.includes('index.html')) {
//        console.log('Initializing index');
//        handleSpotifyLogin();
//    }
//});

import { populateTasteProfile } from './profile.js';

const getBaseUrl = () => {
    return window.location.origin;
};

// ------------------------------------------------------------
// HANDLERS FOR THE INDEX PAGE (Login Flow)
// ------------------------------------------------------------
const handleSpotifyLogin = () => {
    const loginButton = document.querySelector('.btn');
    if (loginButton) {
        loginButton.addEventListener('click', () => {
            console.log('Spotify login button clicked');
            window.location.href = `${getBaseUrl()}/auth/login`;
        });
    }
};

// ------------------------------------------------------------
// HANDLERS FOR THE DASHBOARD PAGE
// ------------------------------------------------------------
const getAndDisplayUser = async () => {
    try {
        console.log('Fetching user data from /auth/me');
        const response = await fetch(`${getBaseUrl()}/auth/me`, {
            credentials: 'include'
        });
        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                console.error('Authentication failed. Redirecting to login.');
                // Clear compatibility state on auth failure
                if (typeof window.clearCompatibilityState === 'function') {
                    window.clearCompatibilityState();
                }
                window.location.href = '/index.html';
                return;
            }
            console.error(`Failed to fetch user data with status: ${response.status}`);
            return;
        }

        const user = await response.json();
        console.log('Current user:', user);

        if (user && user.id) {
            localStorage.setItem('currentUserId', user.id);
            console.log('User ID stored in localStorage:', user.id);
        } else {
                    console.error('User ID not found in /auth/me response.');
        }

        const currentPath = window.location.pathname;

        if (currentPath.includes('dashboard')) {
            console.log('Updating dashboard UI');
            const usernameDisplay = document.getElementById('username-display');
            if (usernameDisplay) {
                usernameDisplay.textContent = user.username;
            } else {
                console.log('username-display element not found; skipping username update.');
            }

            console.log('Fetching sync status from /sync/last-synced');
            const syncStatusResponse = await fetch(`${getBaseUrl()}/sync/last-synced`, {
                credentials: 'include'
            });
            if (!syncStatusResponse.ok) {
                if (syncStatusResponse.status === 401 || response.status === 403) {
                    console.error('Authentication failed on /last-synced. Redirecting to login.');
                    // Clear compatibility state on auth failure
                    if (typeof window.clearCompatibilityState === 'function') {
                        window.clearCompatibilityState();
                    }
                    window.location.href = '/index.html';
                    return;
                }
                console.error(`Failed to fetch sync status with status: ${syncStatusResponse.status}`);
                return;
            }
            const syncStatus = await syncStatusResponse.json();
            const hasSyncedRecently = syncStatus.hasSynced;

            const syncView = document.getElementById('sync-view');
            const profileView = document.getElementById('profile-view');
            const staticContent = document.getElementById('static-content');
            const header = document.getElementById('header');

            if (hasSyncedRecently) {
                if (syncView) syncView.classList.add('hidden');
                if (profileView) profileView.classList.remove('hidden');
                if (staticContent) staticContent.classList.remove('hidden');
                if (header) header.classList.remove('hidden');
                try {
                    console.log('Populating taste profile');
                    await populateTasteProfile();
                    if (typeof window.loadRecommendations === 'function') {
                         window.loadRecommendations();
                    }
                } catch (profileError) {
                    console.error('Failed to populate taste profile:', profileError);
                }
            } else {
                // User hasn't synced recently - clear compatibility state
                if (typeof window.clearCompatibilityState === 'function') {
                    console.log('Clearing compatibility state - user not synced');
                    window.clearCompatibilityState();
                }
                if (syncView) syncView.classList.remove('hidden');
                if (profileView) profileView.classList.add('hidden');
                if (staticContent) staticContent.classList.add('hidden');
                if (header) header.classList.add('hidden');
            }
        } else if (currentPath.includes('connections')) {
            console.log('Connections page: User data fetched, ready for dynamic logic.');
        }

    } catch (error) {
        console.error('An unexpected error occurred in getAndDisplayUser:', error);
    }
};

// Manages the sync process
const handleSync = async () => {
    const syncStatusElement = document.getElementById('sync-status');
    const syncButton = document.getElementById('sync-button');
    const syncView = document.getElementById('sync-view');
    const profileView = document.getElementById('profile-view');
    const staticContent = document.getElementById('static-content');
    const header = document.getElementById('header');

    syncButton.disabled = true;
    syncStatusElement.textContent = 'Syncing your music data, please wait...';
    syncStatusElement.style.color = '#b3b3b3';
    syncButton.innerHTML = '<i class="ri-refresh-line spinning"></i> Syncing...';

    // Clear compatibility state when starting new sync
    if (typeof window.clearCompatibilityState === 'function') {
        console.log('Clearing compatibility state - new sync started');
        window.clearCompatibilityState();
    }

    try {
        console.log('Starting sync process');
        const response = await fetch(`${getBaseUrl()}/sync`, {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                console.error('Authentication failed during sync. Redirecting to login.');
                window.location.href = '/index.html';
                return;
            }
            console.error(`HTTP error during sync! status: ${response.status}`);
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        const taskId = data.taskId;
        console.log('Sync task started, taskId:', taskId);

        const pollInterval = setInterval(async () => {
            console.log('Polling sync status for taskId:', taskId);
            const statusResponse = await fetch(`${getBaseUrl()}/sync/status?id=${taskId}`, {
                credentials: 'include'
            });

            if (!statusResponse.ok) {
                if (statusResponse.status === 401 || response.status === 403) {
                    console.error('Authentication failed during status check. Redirecting to login.');
                    clearInterval(pollInterval);
                    window.location.href = '/index.html';
                    return;
                }
                console.error(`HTTP error during status check! status: ${statusResponse.status}`);
                clearInterval(pollInterval);
                throw new Error(`HTTP error! status: ${statusResponse.status}`);
            }

            const statusData = await statusResponse.json();
            syncStatusElement.textContent = `Sync status: ${statusData.status}`;

            if (statusData.status === 'SUCCESS') {
                clearInterval(pollInterval);
                syncStatusElement.textContent = 'Sync complete! Your music is ready to match. 🎉';
                syncStatusElement.style.color = '#1DB954';
                // Switch views without reloading
                if (syncView) syncView.classList.add('hidden');
                if (profileView) profileView.classList.remove('hidden');
                if (staticContent) staticContent.classList.remove('hidden');
                if (header) header.classList.remove('hidden');
                try {
                    console.log('Populating taste profile after sync');
                    await populateTasteProfile();
                    if (typeof window.loadRecommendations === 'function') {
                       window.loadRecommendations();
                    }
                } catch (profileError) {
                    console.error('Failed to populate taste profile:', profileError);
                }
            } else if (statusData.status === 'FAIL') {
                clearInterval(pollInterval);
                syncStatusElement.textContent = 'Sync failed. Please try again later.';
                syncStatusElement.style.color = '#ff6347';
                syncButton.disabled = false;
                syncButton.innerHTML = '<i class="ri-refresh-line"></i> Sync My Music';
            }
        }, 5000);

    } catch (error) {
        console.error('There was a problem with the sync operation:', error);
        syncStatusElement.textContent = 'An error occurred during sync. Please try again.';
        syncStatusElement.style.color = '#ff6347';
        syncButton.disabled = false;
        syncButton.innerHTML = '<i class="ri-refresh-line"></i> Sync My Music';
    }
};

// Manages the logout process
const handleLogout = async () => {
    try {
        console.log('Logging out');

        // Clear compatibility state on logout
        if (typeof window.clearCompatibilityState === 'function') {
            console.log('Clearing compatibility state on logout');
            window.clearCompatibilityState();
        }

        localStorage.removeItem('currentUserId');

        // Clear all app state
        delete window.vibematchAppState;

        await fetch(`${getBaseUrl()}/auth/logout`, {
            credentials: 'include'
        });
        window.location.href = '/index.html';
    } catch (error) {
        console.error('Logout failed:', error);
        window.location.href = '/index.html';
    }
};

// ------------------------------------------------------------
// INITIALIZATION AND EVENT LISTENERS
// ------------------------------------------------------------
document.addEventListener('DOMContentLoaded', () => {
    const currentPath = window.location.pathname;
    console.log('DOMContentLoaded, current path:', currentPath);

    if (currentPath.includes('dashboard')) {
        console.log('Initializing dashboard');
        getAndDisplayUser();

        const syncButton = document.getElementById('sync-button');
        if (syncButton) {
            syncButton.addEventListener('click', handleSync);
        }

        const logoutLink = document.getElementById('logout-link');
        if (logoutLink) {
            logoutLink.addEventListener('click', (e) => {
                e.preventDefault();
                console.log('Logout link clicked');
                handleLogout();
            });
        }

        setTimeout(() => {
            if (typeof NavigationManager !== 'undefined') {
                console.log('Creating NavigationManager for dashboard');
                new NavigationManager();
            } else {
                console.error('NavigationManager not defined');
            }
        }, 100);

    } else if (currentPath.includes('connections')) {
        console.log('Initializing connections');
        getAndDisplayUser();

        const logoutLink = document.getElementById('logout-link');
        if (logoutLink) {
            logoutLink.addEventListener('click', (e) => {
                e.preventDefault();
                console.log('Logout link clicked');
                handleLogout();
            });
        }

        setTimeout(() => {
            if (typeof NavigationManager !== 'undefined') {
                console.log('Creating NavigationManager for connections');
                new NavigationManager();
            } else {
                console.error('NavigationManager not defined');
            }
        }, 100);

    } else if (currentPath === '/' || currentPath.includes('index.html')) {
        console.log('Initializing index');
        handleSpotifyLogin();
    }
});