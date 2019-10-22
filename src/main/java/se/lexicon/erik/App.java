package se.lexicon.erik;

import se.lexicon.erik.data.Database;
import se.lexicon.erik.data.PersonDao;
import se.lexicon.erik.model.Person;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) {
        PersonDao dao = new PersonDao();
        dao.findByLastName("s").forEach(System.out::println);
    }
}
