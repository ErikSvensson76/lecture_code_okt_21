package se.lexicon.erik.data;

import se.lexicon.erik.model.Task;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Erik Svensson
 */
public class TaskDao {

    private PersonDao personDao = new PersonDao();

    /**
     * Responsible for persisting a new Task to the database. The task need to have a Person assigned to it.
     * The Person assigned to the task will also be persisted if not already.
     * @param newTask - Non persisted Task object
     * @throws IllegalArgumentException when no assignee to the task is detected
     * @throws IllegalArgumentException when Task is already persisted
     * @return Persisted task
     *
     */
    public Task create(Task newTask) throws IllegalArgumentException{
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

    /**
     * Finds a particular task by its unique id
     * @param taskId - int taskId
     * @return Optional of task
     */
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

    /**
     * Finds all Task items with matching done status
     * @param  isDone  boolean isDone
     * @return List of all matching Task items
     */
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

    /**
     * Finds all Task items with matching personId
     * @param personId int personId
     * @return List of all Task items assigned to a particular Person
     */
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

    /**
     * Finds the the closest Task with done status false that is assigned to a particular Person
     * @param personId int personId belonging to Person
     * @return Optional of task if matching task was found Optional.empty otherwise
     */
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

    /**
     * Updates a task in the database. Task needs to have person assigned to it.
     * The method will update or persist assigned person if needed.
     * @param task A Task object
     * @return updated Task object
     * @throws IllegalArgumentException when assignee is null
     * @throws IllegalArgumentException when task is not yet persisted in the database
     */
    public Task update(Task task) throws IllegalArgumentException{
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
