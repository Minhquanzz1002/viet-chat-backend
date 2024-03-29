package vn.edu.iuh.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import vn.edu.iuh.models.enums.FriendStatus;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Friend {
    @DocumentReference
    @Field("friend_id")
    private UserInfo user;
    private String displayName;
    private FriendStatus status;
    @JsonProperty("isBestFriend")
    @Field("is_best_friend")
    private boolean isBestFriend = false;

    public Friend(UserInfo user, String displayName, FriendStatus status) {
        this.user = user;
        this.displayName = displayName;
        this.status = status;
    }
}
