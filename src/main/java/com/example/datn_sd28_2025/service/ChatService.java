package com.example.datn_sd28_2025.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple Chat Service
 * 
 * Features:
 * - Staff auto-join when login
 * - Customer chat support
 * - Real-time messaging
 */
@Service
public class ChatService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Store online staff
    private final Map<String, StaffInfo> onlineStaff = new ConcurrentHashMap<>();
    
    // Store customer sessions
    private final Map<String, CustomerSession> customerSessions = new ConcurrentHashMap<>();
    
    // Store waiting customers
    private final Queue<CustomerSession> waitingCustomers = new LinkedList<>();
    
    // Store staff session data for persistence
    private final Map<String, StaffSessionData> staffSessionData = new ConcurrentHashMap<>();

    /**
     * Chat Message class
     */
    public static class ChatMessage {
        private String id;
        private String content;
        private String sender;
        private String customerName;
        private String staffName;
        private String timestamp;
        
        public ChatMessage() {}
        
        public ChatMessage(String id, String content, String sender, String customerName, String staffName, String timestamp) {
            this.id = id;
            this.content = content;
            this.sender = sender;
            this.customerName = customerName;
            this.staffName = staffName;
            this.timestamp = timestamp;
        }
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }
        
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        
        public String getStaffName() { return staffName; }
        public void setStaffName(String staffName) { this.staffName = staffName; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
    
    /**
     * Staff Session Data class for persistence
     */
    public static class StaffSessionData {
        private String staffId;
        private String staffName;
        private List<CustomerSession> waitingCustomers;
        private List<CustomerSession> activeChats;
        private CustomerSession selectedChat;
        private List<ChatMessage> messages;
        private long timestamp;
        
        public StaffSessionData() {}
        
        public StaffSessionData(String staffId, String staffName) {
            this.staffId = staffId;
            this.staffName = staffName;
            this.waitingCustomers = new ArrayList<>();
            this.activeChats = new ArrayList<>();
            this.selectedChat = null;
            this.messages = new ArrayList<>();
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters and setters
        public String getStaffId() { return staffId; }
        public void setStaffId(String staffId) { this.staffId = staffId; }
        
        public String getStaffName() { return staffName; }
        public void setStaffName(String staffName) { this.staffName = staffName; }
        
        public List<CustomerSession> getWaitingCustomers() { return waitingCustomers; }
        public void setWaitingCustomers(List<CustomerSession> waitingCustomers) { this.waitingCustomers = waitingCustomers; }
        
        public List<CustomerSession> getActiveChats() { return activeChats; }
        public void setActiveChats(List<CustomerSession> activeChats) { this.activeChats = activeChats; }
        
        public CustomerSession getSelectedChat() { return selectedChat; }
        public void setSelectedChat(CustomerSession selectedChat) { this.selectedChat = selectedChat; }
        
        public List<ChatMessage> getMessages() { return messages; }
        public void setMessages(List<ChatMessage> messages) { this.messages = messages; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
    
    /**
     * Staff joins the system (called when staff logs in)
     */
    public void staffJoin(String staffId, String staffName) {
        // Check if staff already exists
        if (onlineStaff.containsKey(staffId)) {
            System.out.println("Staff already joined: " + staffName + " (ID: " + staffId + ")");
            return;
        }
        
        StaffInfo staff = new StaffInfo(staffId, staffName, LocalDateTime.now());
        onlineStaff.put(staffId, staff);
        
        // Notify all clients about staff status
        messagingTemplate.convertAndSend("/topic/staff/status", Map.of(
            "type", "staff_joined",
            "staffId", staffId,
            "staffName", staffName,
            "onlineCount", onlineStaff.size()
        ));
        
        System.out.println("Staff joined: " + staffName + " (ID: " + staffId + ")");
        
        // Initialize or restore staff session data
        if (!staffSessionData.containsKey(staffId)) {
            staffSessionData.put(staffId, new StaffSessionData(staffId, staffName));
            System.out.println("Created new staff session data for: " + staffName);
        } else {
            System.out.println("Restored existing staff session data for: " + staffName);
        }
    }

    /**
     * Staff leaves the system
     */
    public void staffLeave(String staffId) {
        StaffInfo staff = onlineStaff.remove(staffId);
        if (staff != null) {
            // Notify all clients
            messagingTemplate.convertAndSend("/topic/staff/status", Map.of(
                "type", "staff_left",
                "staffId", staffId,
                "onlineCount", onlineStaff.size()
            ));
            
            System.out.println("Staff left: " + staff.getStaffName() + " (ID: " + staffId + ")");
        }
    }

    /**
     * Customer starts chat
     */
    public String customerJoin(String customerName, String phone, String email) {
        System.out.println("Customer join request: " + customerName + " - " + phone);
        
        // Check if customer already exists (more strict check)
        for (CustomerSession existingSession : customerSessions.values()) {
            if (existingSession.getCustomerName().equals(customerName) && 
                existingSession.getPhone().equals(phone) && 
                existingSession.getAssignedStaffId() == null) {
                System.out.println("Customer already exists, returning existing session: " + existingSession.getSessionId());
                return existingSession.getSessionId();
            }
        }
        
        // Check if customer is already in waiting queue
        for (CustomerSession waitingSession : waitingCustomers) {
            if (waitingSession.getCustomerName().equals(customerName) && 
                waitingSession.getPhone().equals(phone)) {
                System.out.println("Customer already in waiting queue, returning existing session: " + waitingSession.getSessionId());
                return waitingSession.getSessionId();
            }
        }
        
        String sessionId = "customer_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
        
        CustomerSession session = new CustomerSession(sessionId, customerName, phone, email, LocalDateTime.now());
        customerSessions.put(sessionId, session);
        
        // Add to waiting queue
        waitingCustomers.offer(session);
        
        // Notify staff about new customer
        messagingTemplate.convertAndSend("/topic/staff/notifications", Map.of(
            "type", "new_customer",
            "sessionId", sessionId,
            "customerName", customerName,
            "phone", phone,
            "email", email,
            "waitingCount", waitingCustomers.size()
        ));
        
        System.out.println("Customer joined: " + customerName + " (Session: " + sessionId + ")");
        return sessionId;
    }

    /**
     * Staff takes customer
     */
    public void staffTakeCustomer(String staffId, String sessionId) {
        CustomerSession session = customerSessions.get(sessionId);
        if (session != null && onlineStaff.containsKey(staffId)) {
            session.setAssignedStaffId(staffId);
            session.setAssignedStaffName(onlineStaff.get(staffId).getStaffName());
            session.setAssignedAt(LocalDateTime.now());
            
            // Remove from waiting queue
            waitingCustomers.removeIf(s -> s.getSessionId().equals(sessionId));
            
            // Notify customer - only send to specific customer session
            messagingTemplate.convertAndSend("/topic/customer/" + sessionId, Map.of(
                "type", "staff_assigned",
                "staffName", session.getAssignedStaffName(),
                "message", "Đã kết nối với nhân viên " + session.getAssignedStaffName()
            ));
            
            // Also send confirmation to customer joined topic (only for customer)
            messagingTemplate.convertAndSend("/topic/customer/joined", Map.of(
                "type", "staff_connected",
                "sessionId", sessionId,
                "staffName", session.getAssignedStaffName(),
                "message", "Nhân viên " + session.getAssignedStaffName() + " đã kết nối với bạn!"
            ));
            
            // Notify staff - only send to specific staff
            messagingTemplate.convertAndSend("/topic/staff/" + staffId, Map.of(
                "type", "customer_assigned",
                "sessionId", sessionId,
                "customerName", session.getCustomerName(),
                "phone", session.getPhone(),
                "email", session.getEmail()
            ));
            
        System.out.println("Staff " + staffId + " took customer " + sessionId);
        
        // Update staff session data
        if (staffSessionData.containsKey(staffId)) {
            StaffSessionData sessionData = staffSessionData.get(staffId);
            sessionData.setTimestamp(System.currentTimeMillis());
            
            // Add to active chats if not already exists
            boolean exists = sessionData.getActiveChats().stream()
                .anyMatch(chat -> chat.getSessionId().equals(sessionId));
            if (!exists) {
                sessionData.getActiveChats().add(session);
                System.out.println("Added customer to staff session data: " + sessionId);
            }
            
            // Remove from waiting if exists
            sessionData.getWaitingCustomers().removeIf(chat -> chat.getSessionId().equals(sessionId));
        }
    }
    }

    /**
     * Send message from staff to customer
     */
    public void sendStaffMessage(String staffId, String sessionId, String message) {
        System.out.println("Sending staff message - StaffId: " + staffId + ", SessionId: " + sessionId + ", Message: " + message);
        
        CustomerSession session = customerSessions.get(sessionId);
        if (session != null && session.getAssignedStaffId() != null && session.getAssignedStaffId().equals(staffId)) {
            System.out.println("Sending message to customer: " + sessionId);
            // Send to customer only once
            messagingTemplate.convertAndSend("/topic/customer/" + sessionId, Map.of(
                "type", "staff_message",
                "message", message,
                "staffName", session.getAssignedStaffName(),
                "timestamp", LocalDateTime.now().toString()
            ));
            System.out.println("Message sent to customer successfully");
        } else {
            System.out.println("Cannot send message - Session: " + (session != null ? "exists" : "null") + 
                             ", AssignedStaff: " + (session != null ? session.getAssignedStaffId() : "null") + 
                             ", Matches: " + (session != null && session.getAssignedStaffId() != null && session.getAssignedStaffId().equals(staffId)));
        }
    }

    /**
     * Send message from customer to staff
     */
    public void sendCustomerMessage(String sessionId, String message) {
        CustomerSession session = customerSessions.get(sessionId);
        if (session != null && session.getAssignedStaffId() != null) {
            // Send to staff only
            messagingTemplate.convertAndSend("/topic/staff/" + session.getAssignedStaffId(), Map.of(
                "type", "customer_message",
                "sessionId", sessionId,
                "customerName", session.getCustomerName(),
                "phone", session.getPhone(),
                "email", session.getEmail(),
                "message", message,
                "timestamp", LocalDateTime.now().toString()
            ));
        }
    }

    /**
     * Get waiting customers
     */
    public List<Map<String, Object>> getWaitingCustomers() {
        return waitingCustomers.stream()
            .map(session -> {
                Map<String, Object> result = new HashMap<>();
                result.put("sessionId", session.getSessionId());
                result.put("customerName", session.getCustomerName());
                result.put("phone", session.getPhone());
                result.put("joinedAt", session.getJoinedAt().toString());
                return result;
            })
            .toList();
    }

    /**
     * Get online staff
     */
    public List<Map<String, Object>> getOnlineStaff() {
        return onlineStaff.values().stream()
            .map(staff -> {
                Map<String, Object> result = new HashMap<>();
                result.put("staffId", staff.getStaffId());
                result.put("staffName", staff.getStaffName());
                result.put("joinedAt", staff.getJoinedAt().toString());
                return result;
            })
            .toList();
    }

    /**
     * Get customer session
     */
    public CustomerSession getCustomerSession(String sessionId) {
        return customerSessions.get(sessionId);
    }

    /**
     * Customer disconnects
     */
    public void customerDisconnect(String sessionId) {
        System.out.println("=== CUSTOMER DISCONNECT DEBUG ===");
        System.out.println("SessionId: " + sessionId);
        System.out.println("Customer sessions before: " + customerSessions.size());
        System.out.println("Waiting customers before: " + waitingCustomers.size());
        
        CustomerSession session = customerSessions.get(sessionId);
        if (session != null) {
            String staffId = session.getAssignedStaffId();
            System.out.println("Found session for: " + session.getCustomerName());
            System.out.println("Assigned staff: " + staffId);
            
            // Remove from customer sessions
            customerSessions.remove(sessionId);
            System.out.println("Removed from customer sessions");
            
            // Remove from waiting queue if still there
            waitingCustomers.removeIf(s -> s.getSessionId().equals(sessionId));
            System.out.println("Removed from waiting queue");
            
            // Notify staff if assigned
            if (staffId != null) {
                System.out.println("Sending disconnect notification to staff: " + staffId);
                messagingTemplate.convertAndSend("/topic/staff/" + staffId, Map.of(
                    "type", "customer_disconnect",
                    "sessionId", sessionId,
                    "customerName", session.getCustomerName()
                ));
            }
            
            // Notify all staff about customer disconnect
            System.out.println("Sending disconnect notification to all staff");
            messagingTemplate.convertAndSend("/topic/staff/notifications", Map.of(
                "type", "customer_disconnect",
                "sessionId", sessionId,
                "customerName", session.getCustomerName()
            ));
            
            System.out.println("Customer sessions after: " + customerSessions.size());
            System.out.println("Waiting customers after: " + waitingCustomers.size());
            System.out.println("Customer disconnected: " + session.getCustomerName() + " (Session: " + sessionId + ")");
        } else {
            System.out.println("Session not found: " + sessionId);
        }
        System.out.println("=== END DISCONNECT DEBUG ===");
    }

    // Inner classes
    public static class StaffInfo {
        private String staffId;
        private String staffName;
        private LocalDateTime joinedAt;

        public StaffInfo(String staffId, String staffName, LocalDateTime joinedAt) {
            this.staffId = staffId;
            this.staffName = staffName;
            this.joinedAt = joinedAt;
        }

        // Getters
        public String getStaffId() { return staffId; }
        public String getStaffName() { return staffName; }
        public LocalDateTime getJoinedAt() { return joinedAt; }
    }

    public static class CustomerSession {
        private String sessionId;
        private String customerName;
        private String phone;
        private String email;
        private LocalDateTime joinedAt;
        private String assignedStaffId;
        private String assignedStaffName;
        private LocalDateTime assignedAt;

        public CustomerSession(String sessionId, String customerName, String phone, String email, LocalDateTime joinedAt) {
            this.sessionId = sessionId;
            this.customerName = customerName;
            this.phone = phone;
            this.email = email;
            this.joinedAt = joinedAt;
        }

        // Getters and Setters
        public String getSessionId() { return sessionId; }
        public String getCustomerName() { return customerName; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
        public LocalDateTime getJoinedAt() { return joinedAt; }
        public String getAssignedStaffId() { return assignedStaffId; }
        public void setAssignedStaffId(String assignedStaffId) { this.assignedStaffId = assignedStaffId; }
        public String getAssignedStaffName() { return assignedStaffName; }
        public void setAssignedStaffName(String assignedStaffName) { this.assignedStaffName = assignedStaffName; }
        public LocalDateTime getAssignedAt() { return assignedAt; }
        public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    }
    
    /**
     * Get staff session data
     */
    public StaffSessionData getStaffSessionData(String staffId) {
        return staffSessionData.get(staffId);
    }
    
    /**
     * Update staff session data
     */
    public void updateStaffSessionData(String staffId, StaffSessionData data) {
        if (data != null) {
            data.setTimestamp(System.currentTimeMillis());
            staffSessionData.put(staffId, data);
            System.out.println("Updated staff session data for: " + staffId);
        }
    }
}
