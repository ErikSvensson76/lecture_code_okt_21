package se.lexicon.erik.data;

import se.lexicon.erik.model.Task;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
                task.setAssignee(personDao.findById(resultSet.getInt("person_id")).orElseThrow(IllegalArgumentException::new));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return task == null ? Optional.empty() : Optional.of(task);
    }

    public List<Task>  findByDoneStatus(boolean isDone){
        List<Task> result = new ArrayList<>();
        try(
                Connection connection = Database.getConnection();
                PreparedStatement statement = createFindByDone(connection, isDone);
                ResultSet resultSet = statement.executeQuery()
                ) {
            while(resultSet.next()){
                Task task = createTaskFromResultSet(resultSet);
                task.setAssignee(personDao.findById(resultSet.getInt("person_id")).orElseThrow(IllegalArgumentException::new));
                result.add(task);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Task> findByPersonId(int personId){
        List<Task> result = new ArrayList<>();
        try(
                Connection connection = Database.getConnection();
                PreparedStatement statement = createFindByPersonId(connection, personId);
                ResultSet resultSet = statement.executeQuery();
                ) {
            while(resultSet.next()){
                Task task = createTaskFromResultSet(resultSet);
                task.setAssignee(personDao.findById(personId).orElseThrow(IllegalArgumentException::new));
                result.add(task);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Optional<Task> findClosestUndoneTaskByPersonId(int personId){
        Task task = null;
        try(
                Connection connection = Database.getConnection();
                PreparedStatement statement = createClosestUndoneTaskByPersonId(connection, personId);
                ResultSet resultSet = statement.executeQuery();
                ){

            while(resultSet.next()){
                task = createTaskFromResultSet(resultSet);
                task.setAssignee(personDao.findById(personId).orElseThrow(IllegalArgumentException::new));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return task != null ? Optional.of(task) : Optional.empty();
    }

    public Task update(Task task){
        if(task.getAssignee() == null){
            throw new IllegalArgumentException("Task has no assignee.");
        }
        if(task.getTaskId() == 0){
            throw new IllegalArgumentException("Task with id "+ task.getTaskId() +" need to be stored in the database before updating");
        }

        task.setAssignee(task.getAssignee().getPersonId() == 0 ? personDao.create(task.getAssignee()) : personDao.update(task.getAssignee()));

        try(
                Connection connection = Database.getConnection();
                PreparedStatement statement = createUpdateTask(connection, task)
                ){

            statement.execute();

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return task;

    }

    private PreparedStatement createUpdateTask(Connection connection, Task task) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(Queries.UPDATE_TASK.getQuery());
        statement.setString(1,task.getDescription());           //DESCRIPTION
        statement.setObject(2,task.getDeadLine());              //DEADLINE
        statement.setBoolean(3, task.isDone());                 //DONE
        statement.setInt(4,task.getAssignee().getPersonId());   //ASSIGNEE_ID
        statement.setInt(5,task.getTaskId());                   //TASK_ID
        return statement;
    }

    private PreparedStatement createClosestUndoneTaskByPersonId(Connection connection, int personId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(Queries.SELECT_CLOSEST_UNDONE_TASK_BY_PERSON_ID.getQuery());
        statement.setInt(1, personId);
        return statement;
    }

    private PreparedStatement createFindByPersonId(Connection connection, int personId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(Queries.SELECT_FROM_TASKS_WHERE_PERSON_ID.getQuery());
        statement.setInt(1, personId);
        return statement;
    }


    private PreparedStatement createFindByDone(Connection connection, boolean isDone) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(Queries.SELECT_FROM_TASKS_WHERE_DONE.getQuery());
        statement.setBoolean(1 ,isDone);
        return statement;
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
