package vn.edu.iuh.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.dto.*;
import vn.edu.iuh.exceptions.FileUploadException;
import vn.edu.iuh.models.Friend;
import vn.edu.iuh.models.UserInfo;
import vn.edu.iuh.models.enums.FriendStatus;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.services.UserInfoService;
import vn.edu.iuh.services.impl.S3Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/users")
@Tag(name = "User Information Controller", description = "Quản lý thông tin người dùng")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private final UserInfoService userInfoService;
    private final S3Service s3Service;

    @Operation(
            summary = "Cập nhật avatar",
            description = """
                    Cập nhật avatar của người dùng\n
                    <strong>Lỗi nếu:</strong>\n
                    * File rỗng
                    * File không phải ảnh
                    * File kích thước lớn hơn 5MB
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @PostMapping("/profile/upload-avatar")
    public UploadFileResponseDTO uploadAvatar(@RequestPart("avatar") MultipartFile avatarFile, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info(avatarFile.toString());
        if (avatarFile.isEmpty()) {
            throw new FileUploadException("File không được rỗng rỗng");
        }
        if (!isImageFile(avatarFile)) {
            throw new FileUploadException("Chỉ chấp nhận file ảnh (image/*)");
        }
        if (!isValidFileSize(avatarFile)) {
            throw new FileUploadException("Kích thước file không lớn hơn 5MB");
        }
        String originalFilename = avatarFile.getOriginalFilename();
        String filename = userPrincipal.getId() + Objects.requireNonNull(originalFilename).substring(originalFilename.lastIndexOf("."));
        String linkAvatar = s3Service.uploadFile(avatarFile, filename, "avatar");
        userInfoService.updateAvatar(userPrincipal, linkAvatar);
        return UploadFileResponseDTO.builder()
                .success(true)
                .linkAvatar(linkAvatar)
                .build();
    }

    @Operation(
            summary = "Cập nhật ảnh bìa",
            description = """
                    Cập nhật ảnh bìa của người dùng\n
                    <strong>Lỗi nếu:</strong>\n
                    * File rỗng
                    * File không phải ảnh
                    * File kích thước lớn hơn 5MB
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @PostMapping("/profile/upload-cover-image")
    public UploadFileResponseDTO uploadCoverImage(@RequestPart("image") MultipartFile coverImageFile, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (coverImageFile.isEmpty()) {
            throw new FileUploadException("File không được rỗng rỗng");
        }
        if (!isImageFile(coverImageFile)) {
            throw new FileUploadException("Chỉ chấp nhận file ảnh (image/*)");
        }
        if (!isValidFileSize(coverImageFile)) {
            throw new FileUploadException("Kích thước file không lớn hơn 5MB");
        }
        String originalFilename = coverImageFile.getOriginalFilename();
        String filename = userPrincipal.getId() + Objects.requireNonNull(originalFilename).substring(originalFilename.lastIndexOf("."));
        String linkCoverImage = s3Service.uploadFile(coverImageFile, filename, "cover-image");
        userInfoService.updateCoverImage(userPrincipal, linkCoverImage);
        return UploadFileResponseDTO.builder()
                .success(true)
                .linkAvatar(linkCoverImage)
                .build();
    }

    private boolean isImageFile(MultipartFile file) {
        return file.getContentType() != null && file.getContentType().startsWith("image");
    }

    private boolean isValidFileSize(MultipartFile file) {
        return file.getSize() <= MAX_FILE_SIZE;
    }

    @Operation(
            summary = "Tìm kiếm người dùng bằng số điện thoại",
            description = "Tìm kiếm người dùng bằng số điện thoại. Dùng cho phần tìm kiếm để kết bạn"
    )
    @GetMapping("/profile/{phone}")
    public UserInfo getUserInfoByPhone(@PathVariable String phone, @RequestHeader("User-Agent") String agent) {
        log.info(agent);
        return userInfoService.findUserInfo(phone);
    }

    @Operation(
            summary = "Lấy danh sách tất cả nhóm của người dùng",
            description = "Lấy danh sách nhóm mà người dùng đang là tham gia. Chỉ trả về các thông tin cơ bản phục vụ cho render danh sách nhóm",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @GetMapping("/profile/groups")
    public List<GroupDTO> getAllGroups(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return userInfoService.findAllGroupToUserInfoByUserId(userPrincipal.getId());
    }

    @Operation(
            summary = "Chấp nhận lời mời kết bạn",
            description = """
                    Chấp nhận lời mời kết bạn từ người khác\n
                    <strong>Lỗi nếu: </strong> không tìm thấy lời mời kết bạn (status != PENDING)
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @PostMapping("/profile/friends/accept")
    public Friend acceptFriendRequest(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody FriendRequestDTO friendRequestDTO) {
        return userInfoService.acceptFriendRequest(friendRequestDTO, userPrincipal);
    }

    @Operation(
            summary = "Không chấp nhận lời mời kết bạn",
            description = """
                    Không chấp nhận lời mời kết bạn từ người khác\n
                    <strong>Lỗi nếu: </strong> không tìm thấy lời mời kết bạn (status != PENDING)
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @PostMapping("/profile/friends/decline")
    public Friend declineFriendRequest(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody FriendRequestDTO friendRequestDTO) {
        return userInfoService.declineFriendRequest(friendRequestDTO, userPrincipal);
    }

    @Operation(
            summary = "Chặn bạn bè",
            description = """
                    Chặn một người dùng khác thông qua ID. Người bị chặn sẽ không thể tìm thấy, gửi tin nhắn, xem profile\n
                    <strong>Chú ý: </strong> nếu tài khoản đã bị khóa trước đó sẽ trả về lỗi
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @PostMapping("/profile/friends/block")
    public Friend blockFriend(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody FriendRequestDTO friendRequestDTO) {
        return userInfoService.blockFriend(friendRequestDTO, userPrincipal);
    }

    @Operation(
            summary = "Bỏ chặn bạn bè",
            description = """
                    Bỏ chặn một người dùng khác thông qua ID\n
                    <strong>Chú ý: </strong> nếu tài khoản không bị khóa trước đó sẽ trả về lỗi
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @PostMapping("/profile/friends/unblock")
    public Friend unblockFriend(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody FriendRequestDTO friendRequestDTO) {
        return userInfoService.unblockFriend(friendRequestDTO, userPrincipal);
    }

    @Operation(
            summary = "Gửi lời mời kết bạn theo số điện thoại",
            description = """
                    Gửi lời mời kết bạn bằng ID của đối phương\n
                    <strong>Lỗi nếu:</strong>\n
                    * Bạn đã chặn đối phương (status == BLOCK)
                    * Bạn đã bị chặn đối phương (status == BLOCKED)
                    * Bạn đã gửi lời mời kết bạn cho đối phương trước đó (status == FRIEND_REQUEST)
                    * Bạn có lời mời kết bạn từ đối phương (status == PENDING)
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @PostMapping("/profile/friends/by-phone")
    public Friend addFriendByPhone(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody PhoneNumberDTO phoneNumberDTO) {
        return userInfoService.addFriendByPhone(phoneNumberDTO, userPrincipal);
    }

    @Operation(
            summary = "Cập nhật tên gợi nhớ",
            description = "Cập nhật tên gợi nhớ",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @PutMapping("/profile/friends/{friend-id}")
    public Friend updateDisplayNameOfFriendById(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable("friend-id") String friendId) {
        return null;
    }

    @Operation(
            summary = "Gửi lời mời kết bạn theo ID",
            description = """
                    Gửi lời mời kết bạn bằng ID của đối phương\n
                    <strong>Lỗi nếu:</strong>\n
                    * Bạn đã chặn đối phương (status == BLOCK)
                    * Bạn đã bị chặn đối phương (status == BLOCKED)
                    * Bạn đã gửi lời mời kết bạn cho đối phương trước đó (status == FRIEND_REQUEST)
                    * Bạn có lời mời kết bạn từ đối phương (status == PENDING)
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @PostMapping("/profile/friends")
    public Friend addFriend(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody FriendRequestDTO friendRequestDTO) {
        return userInfoService.addFriendByUserId(friendRequestDTO, userPrincipal);
    }

    @Operation(
            summary = "Lấy danh sách bạn bè, chặn, lời mời kết bạn của người dùng",
            description = """
                    Lấy danh sách bạn bè, chặn, chờ kết bạn của người dùng
                    + request: danh sách lời mời kết bạn
                    + friend: danh sách bạn bè
                    + block: danh sách bị chặn 
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @GetMapping("/profile/friends")
    public List<Friend> getAllFriends(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestParam FriendTypeRequest type) {
        List<Friend> friends = userInfoService.findUserInfo(userPrincipal.getUsername()).getFriends();
        return friends.stream().filter(friend -> friend.getStatus().equals(mapToFriendStatus(type))).collect(Collectors.toList());
    }

    private FriendStatus mapToFriendStatus(FriendTypeRequest type) {
        return switch (type) {
            case friend -> FriendStatus.FRIEND;
            case request -> FriendStatus.PENDING;
            case block -> FriendStatus.BLOCKED;
        };
    }

    @Operation(
            summary = "Xóa kết bạn",
            description = """
                    Xóa kết bạn. Không xóa dữ liệu mối liên hệ giữa 2 người mà đổi trạng thái sang người lạ (status == STRANGER). Để duy trì tên gợi nhớ của người dùng (display_name)\n
                    <strong>Lưu ý:</strong> nếu 2 người dùng chưa kết bạn thì trả về lỗi
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @DeleteMapping("/profile/friends/{friend-id}")
    public Friend deleteFriend(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable(name = "friend-id") String friendId) {
        return userInfoService.deleteFriend(friendId, userPrincipal);
    }

    @Operation(
            summary = "Tạo thông tin người dùng",
            description = "Tạo thông tin người dùng",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @PostMapping("/profile")
    public UserInfo createUserInfo(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UserInfoDTO userInfoDTO) {
        return userInfoService.createUserInfo(userDetails.getUsername(), userInfoDTO);
    }

    @Operation(
            summary = "Lấy thông tin người dùng",
            description = "Lấy thông tin của người dùng dựa trên chuỗi JWT trong header",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @GetMapping("/profile")
    public UserInfo getUserInfo(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return userInfoService.findUserInfoByUserId(userPrincipal.getId());
    }

    @Operation(
            summary = "Cập nhật thông tin người dùng",
            description = """
                    Cập nhật thông tin người dùng: họ đệm, tên, bio, ảnh avatar, ảnh nền trang cá nhân, giới tính, ngày sinh.<b> Các thông tin không thay đổi có thể không cần truyền <b>
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @PutMapping("/profile")
    public UserInfo updateUserInfo(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UserInfoDTO userInfoDTO) {
        return userInfoService.updateUserInfo(userDetails.getUsername(), userInfoDTO);
    }
}
