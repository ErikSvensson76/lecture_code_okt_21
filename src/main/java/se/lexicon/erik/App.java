package se.lexicon.erik;

import se.lexicon.erik.data.Database;
import se.lexicon.erik.data.PersonDao;
import se.lexicon.erik.model.Person;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) {
        PersonDao dao = new PersonDao();
        System.out.println(dao.findById(1));
    }
}
