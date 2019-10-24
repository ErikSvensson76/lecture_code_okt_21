package se.lexicon.erik.data;

public enum Queries {
    PERSIST_PERSON("INSERT INTO persons (first_name, last_name, birth_date) VALUES(?, ?, ?)"),
    FIND_PERSON_BY_ID("SELECT * FROM persons WHERE person_id = ?"),
    UPDATE_PERSON("UPDATE persons SET first_name = ?, last_name = ?, birth_date = ? WHERE person_id = ?"),
    DELETE_PERSON("DELETE FROM persons WHERE person_id = ?"),
    FIND_PEOPLE_BY_LAST_NAME("DELETE FROM persons WHERE person_id = ?"),
    PERSIST_TASK( "INSERT INTO tasks (description, deadline, done, person_id) VALUES (?,?,?,?)"),
    FIND_TASK_BY_TASK_ID("SELECT * FROM tasks WHERE task_id = ?");


    private String query;

    Queries(String query) {
        this.query = query;
    }

    public String toString(){
        return this.query;
    }

    public String getQuery(){
        return toString();
    }


}
