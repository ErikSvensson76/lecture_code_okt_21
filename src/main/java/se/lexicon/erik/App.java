package se.lexicon.erik;

import se.lexicon.erik.data.PersonDao;
import se.lexicon.erik.data.TaskDao;
import se.lexicon.erik.model.Person;


public class App 
{
    public static void main( String[] args ) {
        PersonDao dao = new PersonDao();
        TaskDao taskDao = new TaskDao();

        System.out.println(taskDao.findByTaskId(1));
    }
}
