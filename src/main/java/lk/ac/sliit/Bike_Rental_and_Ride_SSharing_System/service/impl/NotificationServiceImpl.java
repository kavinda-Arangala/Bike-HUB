package lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.impl;


import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.request.AdminNotificationRequest;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.dto.response.NotificationResponse;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.Notification;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.entity.User;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.NotificationChannel;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.enums.NotificationType;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.exception.ResourceNotFoundException;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.NotificationRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.repository.UserRepository;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.EmailService;
import lk.ac.sliit.Bike_Rental_and_Ride_SSharing_System.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User findUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .userId(n.getUser().getId())
                .username(n.getUser().getUsername())
                .type(n.getType().name())
                .channel(n.getChannel().name())
                .title(n.getTitle())
                .message(n.getMessage())
                .referenceId(n.getReferenceId())
                .referenceType(n.getReferenceType())
                .read(n.isRead())
                .emailSent(n.isEmailSent())
                .createdAt(n.getCreatedAt())
                .readAt(n.getReadAt())
                .build();
    }

    // ── User operations ───────────────────────────────────────────────────────

    @Override
    public List<NotificationResponse> getMyNotifications(String username) {
        User user = findUserOrThrow(username);
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponse> getMyUnreadNotifications(String username) {
        User user = findUserOrThrow(username);
        return notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount(String username) {
        User user = findUserOrThrow(username);
        return notificationRepository.countByUserIdAndReadFalse(user.getId());
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(String username, Long notificationId) {
        User user = findUserOrThrow(username);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("This notification does not belong to you.");
        }

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        return toResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public int markAllAsRead(String username) {
        User user = findUserOrThrow(username);
        int count = notificationRepository.markAllAsRead(user.getId());
        log.info("Marked {} notifications as read for user {}", count, username);
        return count;
    }

    @Override
    @Transactional
    public void deleteMyNotification(String username, Long notificationId) {
        User user = findUserOrThrow(username);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("This notification does not belong to you.");
        }

        notificationRepository.delete(notification);
        log.info("Notification {} deleted by user {}", notificationId, username);
    }

    // ── Core send method ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public void sendNotification(User user,
                                 NotificationType type,
                                 NotificationChannel channel,
                                 String title,
                                 String message,
                                 Long referenceId,
                                 String referenceType) {
        boolean emailSent = false;

        // Send email if channel is EMAIL or BOTH
        if (channel == NotificationChannel.EMAIL || channel == NotificationChannel.BOTH) {
            String html = emailService.buildEmailHtml(user.getUsername(), title, message);
            emailService.sendHtmlEmail(user.getEmail(), title, html);
            emailSent = true;
        }

        // Save in-app notification if channel is IN_APP or BOTH
        if (channel == NotificationChannel.IN_APP || channel == NotificationChannel.BOTH) {
            Notification notification = Notification.builder()
                    .user(user)
                    .type(type)
                    .channel(channel)
                    .title(title)
                    .message(message)
                    .referenceId(referenceId)
                    .referenceType(referenceType)
                    .emailSent(emailSent)
                    .build();
            notificationRepository.save(notification);
        }

        // If EMAIL only — still save a record so admin can audit
        if (channel == NotificationChannel.EMAIL) {
            Notification notification = Notification.builder()
                    .user(user)
                    .type(type)
                    .channel(channel)
                    .title(title)
                    .message(message)
                    .referenceId(referenceId)
                    .referenceType(referenceType)
                    .emailSent(true)
                    .read(true) // email-only notifications auto-marked read
                    .build();
            notificationRepository.save(notification);
        }

        log.info("Notification [{}] sent to user {} via {}", type, user.getUsername(), channel);
    }

    // ── Shortcut helpers ──────────────────────────────────────────────────────

    @Override
    public void notifyWelcome(User user) {
        sendNotification(user,
                NotificationType.ACCOUNT_WELCOME,
                NotificationChannel.BOTH,
                "Welcome to Bike Rental & Ride Sharing! 🚲",
                "Hi <strong>" + user.getUsername() + "</strong>! Your account has been created successfully. " +
                        "Browse available bikes and start your first rental today!",
                user.getId(), "USER");
    }

    @Override
    public void notifyPasswordChanged(User user) {
        sendNotification(user,
                NotificationType.PASSWORD_CHANGED,
                NotificationChannel.BOTH,
                "Your password was changed",
                "Your account password was recently changed. If you did not make this change, " +
                        "please contact support immediately.",
                user.getId(), "USER");
    }

    @Override
    public void notifyRentalConfirmed(User user, Long rentalId, String bikeTitle,
                                      String startDate, String endDate, String fare) {
        sendNotification(user,
                NotificationType.RENTAL_CONFIRMED,
                NotificationChannel.BOTH,
                "Rental Confirmed ✅ — " + bikeTitle,
                "Your rental for <strong>" + bikeTitle + "</strong> has been confirmed.<br/>" +
                        "📅 From: " + startDate + "<br/>" +
                        "📅 To: " + endDate + "<br/>" +
                        "💰 Estimated fare: LKR " + fare + "<br/><br/>" +
                        "Enjoy your ride!",
                rentalId, "RENTAL");
    }

    @Override
    public void notifyRentalStarted(User user, Long rentalId, String bikeTitle) {
        sendNotification(user,
                NotificationType.RENTAL_STARTED,
                NotificationChannel.IN_APP,
                "Rental Started 🚴 — " + bikeTitle,
                "Your rental for <strong>" + bikeTitle + "</strong> has officially started. " +
                        "Ride safe and have fun!",
                rentalId, "RENTAL");
    }

    @Override
    public void notifyRentalCompleted(User user, Long rentalId, String bikeTitle, String finalFare) {
        sendNotification(user,
                NotificationType.RENTAL_COMPLETED,
                NotificationChannel.BOTH,
                "Rental Completed 🏁 — " + bikeTitle,
                "Your rental for <strong>" + bikeTitle + "</strong> is now complete.<br/>" +
                        "💰 Final fare: LKR " + finalFare + "<br/><br/>" +
                        "Please proceed to payment, and don't forget to leave a review!",
                rentalId, "RENTAL");
    }

    @Override
    public void notifyRentalCancelled(User user, Long rentalId, String bikeTitle, String reason) {
        sendNotification(user,
                NotificationType.RENTAL_CANCELLED,
                NotificationChannel.BOTH,
                "Rental Cancelled ❌ — " + bikeTitle,
                "Your rental for <strong>" + bikeTitle + "</strong> has been cancelled.<br/>" +
                        "📝 Reason: " + reason + "<br/><br/>" +
                        "If you have any questions, please contact support.",
                rentalId, "RENTAL");
    }

    @Override
    public void notifyPaymentSuccess(User user, Long paymentId, String amount, String bikeTitle) {
        sendNotification(user,
                NotificationType.PAYMENT_SUCCESS,
                NotificationChannel.BOTH,
                "Payment Successful 💳",
                "Your payment of <strong>LKR " + amount + "</strong> for <strong>" +
                        bikeTitle + "</strong> has been received successfully.<br/><br/>" +
                        "Thank you for using Bike Rental & Ride Sharing!",
                paymentId, "PAYMENT");
    }

    @Override
    public void notifyPaymentRefunded(User user, Long paymentId, String refundAmount) {
        sendNotification(user,
                NotificationType.PAYMENT_REFUNDED,
                NotificationChannel.BOTH,
                "Refund Processed 💰",
                "A refund of <strong>LKR " + refundAmount + "</strong> has been processed " +
                        "to your account.<br/><br/>" +
                        "Please allow 3–5 business days for the amount to reflect.",
                paymentId, "PAYMENT");
    }

    @Override
    public void notifyReviewReceived(User bikeOwner, String reviewerName,
                                     String bikeTitle, int rating) {
        String stars = "⭐".repeat(rating);
        sendNotification(bikeOwner,
                NotificationType.REVIEW_RECEIVED,
                NotificationChannel.IN_APP,
                "New Review on " + bikeTitle,
                "<strong>" + reviewerName + "</strong> left a " + stars + " review on " +
                        "your bike <strong>" + bikeTitle + "</strong>.",
                null, null);
    }

    // ── Admin operations ──────────────────────────────────────────────────────

    @Override
    public List<NotificationResponse> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void adminSendNotification(AdminNotificationRequest request) {
        NotificationChannel channel = NotificationChannel.valueOf(request.getChannel().toUpperCase());

        if (request.getUserId() != null) {
            // Send to specific user
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));
            sendNotification(user, NotificationType.ADMIN_BROADCAST, channel,
                    request.getTitle(), request.getMessage(), null, null);
        } else {
            // Broadcast to ALL users
            List<User> allUsers = userRepository.findAll();
            for (User user : allUsers) {
                sendNotification(user, NotificationType.ADMIN_BROADCAST, channel,
                        request.getTitle(), request.getMessage(), null, null);
            }
            log.info("Admin broadcast sent to {} users", allUsers.size());
        }
    }
}