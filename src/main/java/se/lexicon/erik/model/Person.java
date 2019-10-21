package se.lexicon.erik.model;

import java.time.LocalDate;
import java.util.Objects;

public class Person {

    private int personId;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;

    public Person(int personId, String firstName, String lastName, LocalDate birthDate) {
        this.personId = personId;
        setFirstName(firstName);
        setLastName(lastName);
        setBirthDate(birthDate);
    }

    public Person(String firstName, String lastName, LocalDate birthDate) {
        this(0, firstName, lastName, birthDate);
    }

    public int getPersonId() {
        return personId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return personId == person.personId &&
                Objects.equals(firstName, person.firstName) &&
                Objects.equals(lastName, person.lastName) &&
                Objects.equals(birthDate, person.birthDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personId, firstName, lastName, birthDate);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Person{");
        sb.append("personId=").append(personId);
        sb.append(", firstName='").append(firstName).append('\'');
        sb.append(", lastName='").append(lastName).append('\'');
        sb.append(", birthDate=").append(birthDate);
        sb.append('}');
        return sb.toString();
    }
}
