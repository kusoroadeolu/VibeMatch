

// Dynamic Navigation System for VibeMatch
class NavigationManager {
    constructor() {
        this.currentView = 'dashboard'; // Default view
        this.viewMappings = {
            'dashboard': 'profile-view' // Map to profile-view for dashboard's dynamic content
        };
        this.pageRedirects = {
            'connections': '/connections.html',
            'dashboard': '/dashboard.html'
        };
        this.navLinks = document.querySelectorAll('nav a[data-view]');
        this.init();
    }

    init() {
        console.log('NavigationManager initialized. Current path:', window.location.pathname, 'Hash:', window.location.hash);
        this.setupNavigation();
        const currentPath = window.location.pathname;
        const initialViewFromHash = window.location.hash ? window.location.hash.substring(1) : null;

        // Clear hash to prevent conflicts
        if (window.location.hash) {
            console.log('Clearing URL hash');
            history.replaceState({}, '', window.location.pathname);
        }

        if (currentPath.includes('dashboard')) {
            console.log('On dashboard, setting active link only (sync status controls views)');
            // Don't call this.showView('dashboard') - let main.js control the views
            const initialNavLink = document.querySelector(`nav a[data-view="dashboard"]`);
            if (initialNavLink) {
                this.setActiveNavLink(initialNavLink);
            }
        } else if (currentPath.includes('connections')) {
            console.log('On connections, setting active link');
            const initialNavLink = document.querySelector(`nav a[data-view="connections"]`);
            if (initialNavLink) {
                this.setActiveNavLink(initialNavLink);
            }
        } else if (initialViewFromHash && this.pageRedirects[initialViewFromHash]) {
            console.log('Redirecting to:', this.pageRedirects[initialViewFromHash]);
            window.location.href = this.pageRedirects[initialViewFromHash];
        } else {
            console.log('No valid view or redirect, staying on current page');
        }
    }

    setupNavigation() {
        console.log('Setting up navigation. Found links:', this.navLinks.length);
        this.navLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const viewName = link.dataset.view;
                console.log('Nav link clicked:', viewName);

                if (viewName === 'logout') {
                    console.log('Handling logout');
                    this.handleLogout();
                } else if (this.pageRedirects[viewName]) {
                    console.log('Redirecting to:', this.pageRedirects[viewName]);
                    window.location.href = this.pageRedirects[viewName];
                } else if (viewName) {
                    console.log('Showing in-page view:', viewName);
                    this.showView(viewName);
                    this.setActiveNavLink(link);
                    history.pushState({ view: viewName }, '', `#${viewName}`);
                } else {
                    console.error("Navigation link missing 'data-view' attribute or viewName is empty.", link);
                }
            });
        });

        window.addEventListener('popstate', (event) => {
            const viewFromHistory = event.state ? event.state.view : null;
            const currentPath = window.location.pathname;
            console.log('Popstate event, view:', viewFromHistory, 'Current path:', currentPath);

            if (viewFromHistory && this.pageRedirects[viewFromHistory] && !currentPath.includes(viewFromHistory)) {
                console.log('Popstate redirecting to:', this.pageRedirects[viewFromHistory]);
                window.location.href = this.pageRedirects[viewFromHistory];
                return;
            }
            if (currentPath.includes('dashboard')) {
                this.showView(viewFromHistory || 'dashboard');
                const navLink = document.querySelector(`nav a[data-view="${viewFromHistory || 'dashboard'}"]`);
                if (navLink) {
                    this.setActiveNavLink(navLink);
                }
            } else {
                console.log('Popstate: No action needed for non-dashboard page');
            }
        });
    }

    setActiveNavLink(activeLink) {
        this.navLinks.forEach(link => {
            link.classList.remove('active');
        });
        activeLink.classList.add('active');
        console.log('Set active link:', activeLink.dataset.view);
    }

    showView(viewName) {
        if (!window.location.pathname.includes('dashboard')) {
            console.log('Skipping showView for non-dashboard page');
            return;
        }

        const targetElementId = this.viewMappings[viewName];
        if (!targetElementId) {
            console.error(`Error: No HTML element ID mapped for view name '${viewName}' in NavigationManager.viewMappings.`);
            return;
        }

        const targetViewElement = document.getElementById(targetElementId);
        if (!targetViewElement) {
            console.error(`Error: View element with ID '${targetElementId}' not found in the DOM for view name '${viewName}'.`);
            return;
        }

        this.currentView = viewName;

        Object.values(this.viewMappings).forEach(id => {
            const viewElement = document.getElementById(id);
            if (viewElement) {
                viewElement.classList.add('hidden');
            }
        });

        targetViewElement.classList.remove('hidden');
        console.log('Showing view:', viewName);
    }

    handleLogout() {
        const logoutLink = document.getElementById('logout-link');
        if (logoutLink) {
            console.log('Triggering logout link click');
            logoutLink.click();
        } else {
            console.error("Logout link with ID 'logout-link' not found.");
            if (typeof handleLogout === 'function') {
                console.log('Calling handleLogout');
                handleLogout();
            } else {
                console.log('Redirecting to /index.html');
                window.location.href = '/index.html';
            }
        }
    }
}