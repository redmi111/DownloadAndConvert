package com.eztool.mysimpleapp.model;

public class AudioModel {

    private String input;
    private String output;
    private int id;

    public AudioModel(String input, String output, int id){
        this.input = input;
        this.output = output;
        this.id = id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public int getId() {
        return id;
    }

    public String getInput() {
        return input;
    }

    public String getOutput() {
        return output;
    }
}
