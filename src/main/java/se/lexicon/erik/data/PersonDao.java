package se.lexicon.erik.data;

import se.lexicon.erik.model.Person;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Erik Svensson
 */
public class PersonDao {

    public Person create(Person newPerson){
        if(newPerson.getPersonId() != 0){
            return newPerson;
        }

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try{ //DANGER ZONE
            connection = Database.getConnection();
            statement = connection.prepareStatement(Queries.PERSIST_PERSON.getQuery(), Statement.RETURN_GENERATED_KEYS);
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

        }catch (SQLException ex){ //SAFETY NET
            ex.printStackTrace();
        }finally { //DESSUTOM KÖR ALLID DETTA
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
        PreparedStatement statement = connection.prepareStatement(Queries.FIND_PERSON_BY_ID.getQuery());
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


    /**
     *
     * @param person Person to update
     * @return Updated person
     * @throws IllegalArgumentException when Person is not yet persisted
     */
    public Person update(Person person) throws IllegalArgumentException{
        if(person.getPersonId() == 0){
            throw new IllegalArgumentException("Can not update object, person is not yet persisted");
        }
        try(
                Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(Queries.UPDATE_PERSON.getQuery())
        ){

            statement.setString(1,person.getFirstName());   //UPDATE first_name
            statement.setString(2,person.getLastName());    //UPDATE last_name
            statement.setObject(3,person.getBirthDate());   //UPDATE birth_date
            statement.setInt(4, person.getPersonId());      //WHERE person_id = personId
            statement.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return person;
    }

    /**
     *
     * @param  id int
     * @return boolean true if removed
     */
    public boolean delete(int id){
        boolean deleted = false;
        try(
                Connection connection = Database.getConnection();
                PreparedStatement statement = connection.prepareStatement(Queries.DELETE_PERSON.getQuery())
        ){
            statement.setInt(1,id);
            int numUpdates = statement.executeUpdate();
            deleted = numUpdates > 0;


        }catch (SQLException ex){
            ex.printStackTrace();
        }

        return deleted;
    }

    public List<Person> findByLastName(String lastName){
        List<Person> result = new ArrayList<>();
        try(
                Connection connection = Database.getConnection();
                PreparedStatement statement = createFindByLastName(connection, lastName);
                ResultSet resultSet = statement.executeQuery();
                ){

            while(resultSet.next()){
                result.add(personFromResultSet(resultSet));
            }

        }catch (SQLException ex){
            ex.printStackTrace();
        }
        return result;
    }

    private PreparedStatement createFindByLastName(Connection connection, String lastName) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(Queries.FIND_PEOPLE_BY_LAST_NAME.getQuery());
        statement.setString(1, lastName.concat("%"));
        return statement;
    }

}













