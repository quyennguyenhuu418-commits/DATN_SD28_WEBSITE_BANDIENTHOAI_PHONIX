// Debug script to test WebSocket messages
// Run this in browser console on staff dashboard page

console.log('=== WebSocket Debug Script ===');

// Check if WebSocket is connected
function checkWebSocket() {
    console.log('Checking WebSocket connection...');
    
    // Find the Vue component instance
    const app = document.querySelector('#app').__vue__;
    if (app && app.$children && app.$children[0]) {
        const staffDashboard = app.$children[0];
        console.log('Staff Dashboard component found:', staffDashboard);
        console.log('Is online:', staffDashboard.isOnline);
        console.log('Staff ID:', staffDashboard.staffId);
        console.log('Staff Name:', staffDashboard.staffName);
        console.log('Waiting customers:', staffDashboard.waitingCustomers);
        console.log('Active sessions:', staffDashboard.activeSessions);
        
        // Test WebSocket
        if (staffDashboard.testWebSocket) {
            staffDashboard.testWebSocket();
        }
        
        return staffDashboard;
    } else {
        console.error('Staff Dashboard component not found');
        return null;
    }
}

// Monitor WebSocket messages
function monitorWebSocket() {
    console.log('Monitoring WebSocket messages...');
    
    // Override console.log to catch WebSocket messages
    const originalLog = console.log;
    console.log = function(...args) {
        if (args[0] && args[0].includes && args[0].includes('Received staff notification')) {
            console.warn('🔔 WebSocket Message Detected:', ...args);
        }
        originalLog.apply(console, args);
    };
}

// Test customer join
function testCustomerJoin() {
    console.log('Testing customer join...');
    
    // Open customer chat widget
    const chatWidget = document.querySelector('.chat-widget');
    if (chatWidget) {
        const chatButton = chatWidget.querySelector('.chat-button');
        if (chatButton) {
            chatButton.click();
            console.log('Opened chat widget');
            
            // Fill form
            setTimeout(() => {
                const nameInput = chatWidget.querySelector('input[placeholder*="tên"]');
                const phoneInput = chatWidget.querySelector('input[placeholder*="điện thoại"]');
                const messageInput = chatWidget.querySelector('textarea[placeholder*="Tin nhắn"]');
                
                if (nameInput) nameInput.value = 'Test Customer';
                if (phoneInput) phoneInput.value = '0123456789';
                if (messageInput) messageInput.value = 'Test message';
                
                console.log('Filled form data');
                
                // Click start chat
                const startBtn = chatWidget.querySelector('.start-chat-btn');
                if (startBtn) {
                    startBtn.click();
                    console.log('Started chat');
                }
            }, 1000);
        }
    }
}

// Run debug
console.log('Starting WebSocket debug...');
checkWebSocket();
monitorWebSocket();

// Export functions for manual testing
window.debugWebSocket = {
    checkWebSocket,
    monitorWebSocket,
    testCustomerJoin
};

console.log('Debug functions available: window.debugWebSocket');
console.log('Usage:');
console.log('  - window.debugWebSocket.checkWebSocket()');
console.log('  - window.debugWebSocket.testCustomerJoin()');


