package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple Chat Controller
 * 
 * Handles:
 * - Staff join/leave
 * - Customer chat
 * - Real-time messaging
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Staff joins the system
     */
    @MessageMapping("/staff/join")
    @SendTo("/topic/staff/status")
    public Map<String, Object> staffJoin(Map<String, Object> payload) {
        try {
            String staffId = (String) payload.get("staffId");
            String staffName = (String) payload.get("staffName");

            chatService.staffJoin(staffId, staffName);

            return Map.of(
                "type", "staff_joined",
                "staffId", staffId,
                "staffName", staffName,
                "success", true
            );
        } catch (Exception e) {
            return Map.of(
                "type", "error",
                "message", "Lỗi khi tham gia hệ thống: " + e.getMessage(),
                "success", false
            );
        }
    }

    /**
     * Staff leaves the system
     */
    @MessageMapping("/staff/leave")
    @SendTo("/topic/staff/status")
    public Map<String, Object> staffLeave(Map<String, Object> payload) {
        try {
            String staffId = (String) payload.get("staffId");
            chatService.staffLeave(staffId);

            return Map.of(
                "type", "staff_left",
                "staffId", staffId,
                "success", true
            );
        } catch (Exception e) {
            return Map.of(
                "type", "error",
                "message", "Lỗi khi rời khỏi hệ thống: " + e.getMessage(),
                "success", false
            );
        }
    }

    /**
     * Customer starts chat
     */
    @MessageMapping("/customer/join")
    public void customerJoin(Map<String, Object> payload) {
        try {
            String customerName = (String) payload.get("customerName");
            String phone = (String) payload.get("phone");
            String email = (String) payload.get("email");

            String sessionId = chatService.customerJoin(customerName, phone, email);

            // Send confirmation to customer
            messagingTemplate.convertAndSend("/topic/customer/joined", Map.of(
                "type", "customer_joined",
                "sessionId", sessionId,
                "success", true,
                "message", "Đã kết nối thành công! Vui lòng chờ nhân viên hỗ trợ..."
            ));
            
            // Also send to specific customer session
            messagingTemplate.convertAndSend("/topic/customer/" + sessionId, Map.of(
                "type", "customer_joined",
                "sessionId", sessionId,
                "success", true,
                "message", "Đã kết nối thành công! Vui lòng chờ nhân viên hỗ trợ..."
            ));
            } catch (Exception e) {
            System.err.println("Error in customer join: " + e.getMessage());
        }
    }

    /**
     * Staff takes customer
     */
    @MessageMapping("/staff/take-customer")
    public void staffTakeCustomer(Map<String, Object> payload) {
        try {
            String staffId = (String) payload.get("staffId");
            String sessionId = (String) payload.get("sessionId");
            chatService.staffTakeCustomer(staffId, sessionId);
        } catch (Exception e) {
            System.err.println("Error taking customer: " + e.getMessage());
        }
    }

    /**
     * Send message from staff to customer
     */
    @MessageMapping("/staff/send-message")
    public void sendStaffMessage(Map<String, Object> payload) {
        try {
            String staffId = (String) payload.get("staffId");
            String sessionId = (String) payload.get("sessionId");
            String message = (String) payload.get("message");
            chatService.sendStaffMessage(staffId, sessionId, message);
        } catch (Exception e) {
            System.err.println("Error sending staff message: " + e.getMessage());
        }
    }

    /**
     * Send message from customer to staff
     */
    @MessageMapping("/customer/send-message")
    public void sendCustomerMessage(Map<String, Object> payload) {
        try {
            System.out.println("=== CUSTOMER SEND MESSAGE CONTROLLER ===");
            System.out.println("Received payload: " + payload);
            
            String sessionId = (String) payload.get("sessionId");
            String message = (String) payload.get("message");
            
            System.out.println("SessionId: " + sessionId);
            System.out.println("Message: " + message);
            
            if (sessionId == null || message == null) {
                System.out.println("❌ Missing sessionId or message");
                return;
            }
            
            chatService.sendCustomerMessage(sessionId, message);
            System.out.println("✅ Customer message processed successfully");
        } catch (Exception e) {
            System.err.println("❌ Error sending customer message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Customer disconnects
     */
    @MessageMapping("/customer/disconnect")
    public void customerDisconnect(Map<String, Object> payload) {
        try {
            System.out.println("=== CHAT CONTROLLER DISCONNECT ===");
            System.out.println("Received disconnect payload: " + payload);
            String sessionId = (String) payload.get("sessionId");
            System.out.println("SessionId from payload: " + sessionId);
            chatService.customerDisconnect(sessionId);
            System.out.println("=== END CONTROLLER DISCONNECT ===");
        } catch (Exception e) {
            System.err.println("Error in customer disconnect: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Customer disconnects (backup endpoint)
     */
    @MessageMapping("/customer/joined")
    public void customerJoinedBackup(Map<String, Object> payload) {
        try {
            System.out.println("=== CUSTOMER JOINED BACKUP ===");
            System.out.println("Received payload: " + payload);
            
            String type = (String) payload.get("type");
            if ("customer_disconnect".equals(type)) {
                String sessionId = (String) payload.get("sessionId");
                System.out.println("Backup disconnect for sessionId: " + sessionId);
                chatService.customerDisconnect(sessionId);
                System.out.println("=== END BACKUP DISCONNECT ===");
            } else {
                System.out.println("Not a disconnect message, ignoring");
            }
        } catch (Exception e) {
            System.err.println("Error in customer joined backup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get waiting customers (REST endpoint)
     */
    @GetMapping("/waiting-customers")
    public Map<String, Object> getWaitingCustomers() {
                return Map.of(
            "success", true,
            "customers", chatService.getWaitingCustomers()
        );
    }

    /**
     * Get online staff (REST endpoint)
     */
    @GetMapping("/online-staff")
    public Map<String, Object> getOnlineStaff() {
            return Map.of(
            "success", true,
            "staff", chatService.getOnlineStaff()
            );
    }
    
    /**
     * Get staff session data
     */
    @GetMapping("/staff/session-data/{staffId}")
    public ResponseEntity<Map<String, Object>> getStaffSessionData(@PathVariable String staffId) {
        try {
            System.out.println("Getting staff session data for: " + staffId);
            var sessionData = chatService.getStaffSessionData(staffId);
            
            if (sessionData != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("staffId", sessionData.getStaffId());
                response.put("staffName", sessionData.getStaffName());
                response.put("waitingCustomers", sessionData.getWaitingCustomers());
                response.put("activeChats", sessionData.getActiveChats());
                response.put("selectedChat", sessionData.getSelectedChat());
                response.put("messages", sessionData.getMessages());
                response.put("timestamp", sessionData.getTimestamp());
                
                System.out.println("Staff session data found: " + sessionData.getActiveChats().size() + " active chats");
                return ResponseEntity.ok(response);
            } else {
                System.out.println("No session data found for staff: " + staffId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error getting staff session data: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
