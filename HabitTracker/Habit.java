package com.example.mobiledevproject;

public class Habit {
    private String name;
    private String type;
    private boolean checked;

    public Habit() {
    }

    public Habit(String name, String type, boolean checked) {
        this.name = name;
        this.type = type;
        this.checked = checked;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
