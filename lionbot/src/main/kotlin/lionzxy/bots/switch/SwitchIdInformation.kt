package lionzxy.bots.switch

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object SwitchIdInformationDAO : LongIdTable("sw_tg") {
    val firstName = text("first_name").nullable()
    val lastName = text("last_name").nullable()
    val nickname = text("nickname").nullable()
    val sw = text("sw")
}

class SwitchIdInformation(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<SwitchIdInformation>(SwitchIdInformationDAO)

    constructor(id: Long) : this(EntityID(id, SwitchIdInformationDAO))

    var firstName by SwitchIdInformationDAO.firstName
    var lastName by SwitchIdInformationDAO.lastName
    var nickname by SwitchIdInformationDAO.nickname
    var sw by SwitchIdInformationDAO.sw
}

data class SwitchIdRowData(
    val id: Long,
    val firstName: String?,
    val lastName: String?,
    val nickname: String?,
    val sw: String
)
