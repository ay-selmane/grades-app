/**
 * Notification Badge System
 * Fetches and displays notification counts in sidebar badges
 * Auto-refreshes every 30 seconds
 */

let notificationInterval = null;

/**
 * Fetches notification counts from the API and updates badges
 */
async function updateNotificationBadges() {
    try {
        console.log('📡 Fetching notification counts...');
        const response = await fetch('/api/notifications/counts-by-type', {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Accept': 'application/json'
            }
        });
        
        if (response.ok) {
            const counts = await response.json();
            console.log('✅ Notifications received:', counts);
            
            // Update badge for Grades
            updateBadge('grades-badge', counts.Grades || 0);
            
            // Update badge for Feed
            updateBadge('feed-badge', counts.Feed || 0);
            
            // Update badge for Schedule
            updateBadge('schedule-badge', counts.Schedule || 0);
            
            // Update total count in navbar (if exists)
            const total = (counts.Grades || 0) + (counts.Feed || 0) + (counts.Schedule || 0);
            updateBadge('total-notifications-badge', total);
            
            console.log('📊 Badge counts - Grades:', counts.Grades || 0, 'Feed:', counts.Feed || 0, 'Schedule:', counts.Schedule || 0);
        } else {
            console.warn('⚠️ Failed to fetch notifications:', response.status, response.statusText);
        }
    } catch (error) {
        console.error('❌ Error fetching notifications:', error);
    }
}

/**
 * Updates a specific badge element
 * @param {string} badgeId - The ID of the badge element
 * @param {number} count - The count to display
 */
function updateBadge(badgeId, count) {
    const badge = document.getElementById(badgeId);
    if (badge) {
        if (count > 0) {
            badge.textContent = count > 99 ? '99+' : count;
            badge.style.display = 'inline-block';
            // Add pulse animation for new notifications
            badge.classList.add('badge-pulse');
            setTimeout(() => badge.classList.remove('badge-pulse'), 1000);
            console.log(`🔵 Badge ${badgeId} updated to: ${count}`);
        } else {
            badge.style.display = 'none';
            console.log(`⚪ Badge ${badgeId} hidden (count: 0)`);
        }
    } else {
        console.warn(`⚠️ Badge element not found: ${badgeId}`);
    }
}

/**
 * Marks all notifications of a specific type as read
 * @param {string} type - The notification type (GRADE_PUBLISHED, URGENT_POST, SCHEDULE_CHANGE)
 */
async function markTypeAsRead(type) {
    try {
        const response = await fetch(`/api/notifications/mark-type-read/${type}`, {
            method: 'PUT',
            credentials: 'include'
        });
        
        if (response.ok) {
            // Immediately update badges after marking as read
            updateNotificationBadges();
        }
    } catch (error) {
        console.error('Error marking notifications as read:', error);
    }
}

/**
 * Initialize notification system
 * Called when the page loads
 */
function initNotifications() {
    // Initial fetch
    updateNotificationBadges();
    
    // Set up auto-refresh every 30 seconds
    if (notificationInterval) {
        clearInterval(notificationInterval);
    }
    notificationInterval = setInterval(updateNotificationBadges, 30000);
    
    // Add click handlers to sidebar links to mark notifications as read when leaving
    setupNotificationClickHandlers();
    
    console.log('🔔 Notification system initialized');
}

/**
 * Store the current page path to detect navigation
 */
let currentPagePath = window.location.pathname;

/**
 * Setup click handlers for sidebar links
 * Marks notifications as read when user LEAVES a section (clicks away from it)
 */
function setupNotificationClickHandlers() {
    const currentPath = window.location.pathname;
    
    // Get all navigation links
    const gradesLink = document.querySelector('a[href*="/grades"]');
    const feedLink = document.querySelector('a[href*="/dashboard"]');
    const scheduleLink = document.querySelector('a[href*="/schedule"]');
    
    // When clicking ANY navigation link, mark the CURRENT page's notifications as read
    const allNavLinks = [gradesLink, feedLink, scheduleLink].filter(link => link !== null);
    
    allNavLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            // Determine what type of page we're currently on and mark it as read
            if (currentPath.includes('/grades')) {
                // We're leaving grades page - mark grade notifications as read
                console.log('📖 Leaving grades page, marking as read...');
                fetch('/api/notifications/mark-type-read/GRADE_PUBLISHED', {
                    method: 'PUT',
                    credentials: 'include',
                    keepalive: true  // Ensure request completes even during navigation
                }).catch(err => console.error('Error:', err));
            } else if (currentPath.includes('/dashboard')) {
                // We're leaving dashboard/feed page - mark feed notifications as read
                console.log('📖 Leaving feed page, marking as read...');
                fetch('/api/notifications/mark-type-read/URGENT_POST', {
                    method: 'PUT',
                    credentials: 'include',
                    keepalive: true
                }).catch(err => console.error('Error:', err));
            } else if (currentPath.includes('/schedule')) {
                // We're leaving schedule page - mark schedule notifications as read
                console.log('📖 Leaving schedule page, marking as read...');
                fetch('/api/notifications/mark-type-read/SCHEDULE_CHANGE', {
                    method: 'PUT',
                    credentials: 'include',
                    keepalive: true
                }).catch(err => console.error('Error:', err));
            }
            // Note: We don't prevent default, so navigation continues normally
            // The next page will fetch fresh badge counts automatically on load
        });
    });
    
    console.log('✅ Click handlers attached to navigation links');
}

/**
 * Cleanup when page is unloaded
 */
window.addEventListener('beforeunload', () => {
    if (notificationInterval) {
        clearInterval(notificationInterval);
    }
});

// Auto-initialize when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initNotifications);
} else {
    initNotifications();
}

// Add visible indicator that script loaded
console.log('🚀 notifications.js loaded successfully!');
