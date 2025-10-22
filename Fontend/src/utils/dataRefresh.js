/**
 * Utility functions for data refresh and reload
 */

/**
 * Force refresh all data by triggering a custom event
 * This can be listened to by components that need to refresh their data
 */
export function triggerDataRefresh() {
  const event = new CustomEvent('data-refresh', {
    detail: { timestamp: Date.now() }
  })
  window.dispatchEvent(event)
}

/**
 * Force reload the entire page
 * Use this when you need to ensure all data is fresh
 */
export function forcePageReload(delay = 1000) {
  setTimeout(() => {
    window.location.reload()
  }, delay)
}

/**
 * Soft refresh - reload only the current route
 * This preserves the current state but refreshes the component
 */
export function softRefresh(router) {
  const currentRoute = router.currentRoute.value
  router.replace({ path: currentRoute.path, query: { ...currentRoute.query, _t: Date.now() } })
}

/**
 * Check if we need to refresh data based on login state
 * This can be used to determine if a refresh is needed
 */
export function shouldRefreshData() {
  // Check if we just logged in (you can customize this logic)
  const lastLogin = localStorage.getItem('lastLogin')
  const now = Date.now()
  
  if (lastLogin) {
    const timeDiff = now - parseInt(lastLogin)
    // If login was within last 5 seconds, we might need to refresh
    return timeDiff < 5000
  }
  
  return false
}

/**
 * Mark login time for refresh detection
 */
export function markLoginTime() {
  localStorage.setItem('lastLogin', Date.now().toString())
}

/**
 * Clear login time marker
 */
export function clearLoginTime() {
  localStorage.removeItem('lastLogin')
}
