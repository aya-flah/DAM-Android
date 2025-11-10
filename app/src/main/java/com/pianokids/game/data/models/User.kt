import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("photoUrl")
    val photoUrl: String? = null, // Make sure this matches backend

    @SerializedName("provider")
    val provider: String,

    @SerializedName("score")
    val score: Int,

    @SerializedName("level")
    val level: Int
)