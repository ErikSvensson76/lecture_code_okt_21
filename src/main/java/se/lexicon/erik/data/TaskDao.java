package se.lexicon.erik.data;

import se.lexicon.erik.model.Task;

import java.sql.*;

public class TaskDao {

    private PersonDao personDao = new PersonDao();



    /*
    INSERT INTO table_name (column1, column2, column3, ...)
    VALUES (value1, value2, value3, ...);
     */
    private static final String CREATE
            = "INSERT INTO tasks (description, deadline, done, person_id)" +
            " VALUES (?,?,?,?)";

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

    private PreparedStatement createPersistStatement(Connection connection, Task newTask) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(CREATE, Statement.RETURN_GENERATED_KEYS);
        statement.setString(1,newTask.getDescription());
        statement.setObject(2,newTask.getDeadLine());
        statement.setBoolean(3, newTask.isDone());
        statement.setInt(4,newTask.getAssignee().getPersonId());
        return statement;
    }

}
