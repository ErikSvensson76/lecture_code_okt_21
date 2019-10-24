package se.lexicon.erik.model;

import java.time.LocalDate;

public class Task {
    private int taskId;
    private String description;
    private LocalDate deadLine;
    private boolean done;
    private Person assignee;

    public Task(int taskId, String description, LocalDate deadLine, boolean done, Person assignee) {
        this.taskId = taskId;
        this.description = description;
        this.deadLine = deadLine;
        this.done = done;
        this.assignee = assignee;
    }

    public Task(String description, LocalDate deadLine) {
        this(0,description,deadLine, false, null);
    }

    public int getTaskId() {
        return taskId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDeadLine() {
        return deadLine;
    }

    public void setDeadLine(LocalDate deadLine) {
        this.deadLine = deadLine;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public Person getAssignee() {
        return assignee;
    }

    public void setAssignee(Person assignee) {
        this.assignee = assignee;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Task{");
        sb.append("taskId=").append(taskId);
        sb.append(", description='").append(description).append('\'');
        sb.append(", deadLine=").append(deadLine);
        sb.append(", done=").append(done);
        sb.append(", assignee=").append(assignee);
        sb.append('}');
        return sb.toString();
    }
}
