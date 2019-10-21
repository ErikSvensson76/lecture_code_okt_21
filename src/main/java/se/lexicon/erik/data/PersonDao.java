package se.lexicon.erik.data;

import se.lexicon.erik.model.Person;

import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;


public class PersonDao {
    /*
    INSERT INTO table_name (column1, column2, column3, ...)
    VALUES (value1, value2, value3, ...);
     */
    private static final String CREATE = "INSERT INTO persons (first_name, last_name, birth_date)" +
            "VALUES(?, ?, ?)";
    private static final String FIND_BY_ID = "SELECT * FROM persons WHERE person_id = ?";

    public Person create(Person newPerson){
        if(newPerson.getPersonId() != 0){
            return newPerson;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try{
            connection = Database.getConnection();
            statement = connection.prepareStatement(CREATE, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, newPerson.getFirstName());
            statement.setString(2, newPerson.getLastName());
            statement.setObject(3, newPerson.getBirthDate());
            statement.executeUpdate();
            resultSet = statement.getGeneratedKeys();
            int personId = 0;
            while(resultSet.next()){
                personId = resultSet.getInt(1);
            }


            newPerson = new Person(
                    personId,                       //personId taken from getGeneratedKeys()
                    newPerson.getFirstName(),       //firstName
                    newPerson.getLastName(),        //lastName
                    newPerson.getBirthDate()        //birthDate
            );

        }catch (SQLException ex){
            ex.printStackTrace();
        }finally {
            try{
                if(resultSet != null){
                    resultSet.close();
                }
                if(statement != null){
                    statement.close();
                }
                if(connection != null){
                    connection.close();
                }
            }catch (SQLException ex){
                ex.printStackTrace();
            }
        }
        return newPerson;
    }

    private PreparedStatement createFindById(Connection connection, int id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(FIND_BY_ID);
        statement.setInt(1,id);
        return statement;
    }

    private Person personFromResultSet(ResultSet resultSet) throws SQLException {
        return new Person(
            resultSet.getInt("person_id"),
            resultSet.getString("first_name"),
            resultSet.getString("last_name"),
            resultSet.getObject("birth_date", LocalDate.class)
        );
    }

    public Optional<Person> findById(int personId){
        Person found = null;

        try(
                Connection connection = Database.getConnection();
                PreparedStatement statement = createFindById(connection, personId);
                ResultSet resultSet = statement.executeQuery()
        ){
            while(resultSet.next()){
                found = personFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(found == null){
            return Optional.empty();
        }else{
            return Optional.of(found);
        }
    }
}
