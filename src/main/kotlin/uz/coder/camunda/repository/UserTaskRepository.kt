package uz.coder.camunda.repository

import uz.coder.camunda.model.UserTask
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserTaskRepository : JpaRepository<UserTask, Long> {

    fun findByTaskId(taskId: String): UserTask?

    fun findByProcessInstanceId(processInstanceId: String): List<UserTask>

    fun findByAssignee(assignee: String): List<UserTask>

    fun findByStatus(status: String): List<UserTask>

    fun findByStatusAndAssignee(status: String, assignee: String): List<UserTask>
}
 