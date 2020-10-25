package lionzxy.bots.technopark.api

import com.google.gson.annotations.SerializedName
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object TechnoparkUserDAO : LongIdTable("tp_user") {
        var tgUsername = text("tg_username").nullable()
        var tgId = integer("tg_id").nullable()
        val username = text("username").nullable()
        val profileLink = text("profile_link").nullable()
        val project = text("project").nullable()
        val isAccessAllowed = bool("isAccessAllowed").default(false)
}

class TechnoparkUserDB(id: EntityID<Long>) : LongEntity(id) {
        companion object : LongEntityClass<TechnoparkUserDB>(TechnoparkUserDAO)

        constructor(id: Long) : this(EntityID(id, TechnoparkUserDAO))

        var tgUsername by TechnoparkUserDAO.tgUsername
        var tgId by TechnoparkUserDAO.tgId
        var username by TechnoparkUserDAO.username
        var profileLink by TechnoparkUserDAO.profileLink
        var project by TechnoparkUserDAO.project
        var isAccessAllowed by TechnoparkUserDAO.isAccessAllowed
}

data class TechnoparkUser(
        @SerializedName("id")
        var id: Long? = 0,
        @SerializedName("username")
        var username: String? = "",
        @SerializedName("profile_link")
        var profileLink: String? = "",
        @SerializedName("project")
        var project: String? = "",
        @SerializedName("is_access_allowed")
        var isAccessAllowed: Boolean? = false)
