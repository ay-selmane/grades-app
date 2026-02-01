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
        console.log(`🔄 Marking ${type} notifications as read...`);
        const response = await fetch(`/api/notifications/mark-type-read/${type}`, {
            method: 'PUT',
            credentials: 'include'
        });
        
        if (response.ok) {
            console.log(`✅ ${type} notifications marked as read successfully`);
            // Wait a moment then update badges to show the change
            setTimeout(() => {
                updateNotificationBadges();
            }, 100);
        } else {
            console.warn(`⚠️ Failed to mark ${type} as read:`, response.status);
        }
    } catch (error) {
        console.error(`❌ Error marking ${type} notifications as read:`, error);
    }
}

/**
 * Initialize notification system
 * Called when the page loads
 */
function initNotifications() {
    // Mark current page notifications as read when arriving
    markCurrentPageAsRead();
    
    // Initial fetch
    updateNotificationBadges();
    
    // Set up auto-refresh every 30 seconds
    if (notificationInterval) {
        clearInterval(notificationInterval);
    }
    notificationInterval = setInterval(updateNotificationBadges, 30000);
    
    console.log('🔔 Notification system initialized');
}

/**
 * Mark notifications as read based on current page
 */
async function markCurrentPageAsRead() {
    const currentPath = window.location.pathname;
    
    if (currentPath.includes('/grades')) {
        console.log('📖 On grades page, marking grade notifications as read...');
        await markTypeAsRead('GRADE_PUBLISHED');
    } else if (currentPath.includes('/dashboard')) {
        console.log('📖 On dashboard page, marking feed notifications as read...');
        await markTypeAsRead('URGENT_POST');
    } else if (currentPath.includes('/schedule')) {
        console.log('📖 On schedule page, marking schedule notifications as read...');
        await markTypeAsRead('SCHEDULE_CHANGE');
    }
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
