package se.lexicon.erik.data;

import se.lexicon.erik.model.Task;

import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

public class TaskDao {

    private PersonDao personDao = new PersonDao();

    public Task create(Task newTask){
        if(newTask.getAssignee() == null){
            throw new IllegalArgumentException("Task had no assignee.");
        }
        if(newTask.getTaskId() > 0){
            throw new IllegalArgumentException("Task with id "+ newTask.getTaskId() +" is already created in the database");
        }

        if(newTask.getAssignee().getPersonId() == 0){
            newTask.setAssignee(personDao.create(newTask.getAssignee()));
        }

        ResultSet keySet = null;

        try(
                Connection connection = Database.getConnection();
                PreparedStatement statement = createPersistStatement(connection, newTask)
                ){

            statement.execute();
            keySet = statement.getGeneratedKeys();
            int taskId = 0;
            while(keySet.next()){
                taskId = keySet.getInt(1);
            }

            newTask = new Task(taskId, newTask.getDescription(), newTask.getDeadLine(),newTask.isDone(),newTask.getAssignee());

        }catch (SQLException ex){
            ex.printStackTrace();
        }finally {
            if(keySet != null){
                try {
                    keySet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return newTask;
    }

    public Optional<Task> findByTaskId(int taskId){
        Task task = null;
        try(Connection connection = Database.getConnection();
            PreparedStatement statement = createFindByTaskId(connection, taskId);
            ResultSet resultSet = statement.executeQuery();
        ) {

            while(resultSet.next()){
                task = createTaskFromResultSet(resultSet);
                task.setAssignee(personDao.findById(resultSet.getInt("person_id")).orElseThrow(IllegalAccessError::new));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return task == null ? Optional.empty() : Optional.of(task);
    }

    private Task createTaskFromResultSet(ResultSet resultSet) throws SQLException {
        return new Task(
            resultSet.getInt("task_id"),
            resultSet.getString("description"),
            resultSet.getObject("deadline", LocalDate.class),
            resultSet.getBoolean("done"),
            null
        );
    }

    private PreparedStatement createFindByTaskId(Connection connection, int taskId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(Queries.FIND_TASK_BY_TASK_ID.getQuery());
        statement.setInt(1,taskId);
        return statement;
    }

    private PreparedStatement createPersistStatement(Connection connection, Task newTask) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(Queries.PERSIST_TASK.getQuery(), Statement.RETURN_GENERATED_KEYS);
        statement.setString(1,newTask.getDescription());
        statement.setObject(2,newTask.getDeadLine());
        statement.setBoolean(3, newTask.isDone());
        statement.setInt(4,newTask.getAssignee().getPersonId());
        return statement;
    }

}
