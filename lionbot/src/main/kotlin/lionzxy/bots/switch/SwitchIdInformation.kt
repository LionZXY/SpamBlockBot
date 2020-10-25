package lionzxy.bots.switch

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object SwitchIdInformationDAO : IntIdTable("sw_tg") {
    val firstName = text("first_name").nullable()
    val lastName = text("last_name").nullable()
    val nickname = text("nickname").nullable()
    val sw = text("sw")
}

class SwitchIdInformation(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<SwitchIdInformation>(SwitchIdInformationDAO)

    constructor(id: Int) : this(EntityID(id, SwitchIdInformationDAO))

    var firstName by SwitchIdInformationDAO.firstName
    var lastName by SwitchIdInformationDAO.lastName
    var nickname by SwitchIdInformationDAO.nickname
    var sw by SwitchIdInformationDAO.sw
}

data class SwitchIdRowData(val id: Int,
                           val firstName: String?,
                           val lastName: String?,
                           val nickname: String?,
                           val sw: String)
