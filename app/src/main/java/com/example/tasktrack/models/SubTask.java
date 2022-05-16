package com.example.tasktrack.models;

import java.io.Serializable;

public class SubTask implements Serializable {
    private long id;
    private String name;
    private boolean done;
    private Task parent;

    public SubTask(String name) {
        this.name = name;
    }

    public SubTask(Task parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public SubTask(String name, boolean done) {
        this.name = name;
        this.done = done;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Task getParent() {
        return parent;
    }

    public void setParent(Task parent) {
        this.parent = parent;
    }
}
