import com.google.gson.annotations.SerializedName
import com.umc.edison.data.model.user.UserEntity
import com.umc.edison.data.model.user.UserWithTokenEntity
import com.umc.edison.remote.model.RemoteMapper
import com.umc.edison.remote.model.login.IdentityResponse

data class SignUpResponse(
    @SerializedName("memberId")
    val memberId: Long,
    @SerializedName("email")
    val email: String,
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String,
    @SerializedName("identities")
    val identities: List<IdentityResponse>
): RemoteMapper<UserWithTokenEntity> {
    override fun toData(): UserWithTokenEntity =
        UserWithTokenEntity(
            user = toUserEntity(),
            accessToken = accessToken,
            refreshToken = refreshToken
        )

    fun toUserEntity(): UserEntity =
        UserEntity(
            id = memberId,
            nickname = nickname,
            profileImage = null,
            email = email
        )
}
