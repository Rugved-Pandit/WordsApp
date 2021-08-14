package com.example.wordsapp.Classes;

public class WordClass {

    private String word, meaning;

    public WordClass(String word, String meaning) {
        this.word = word;
        this.meaning = meaning;
    }

    public WordClass() { }

    public String getWord() {
        return word;
    }

    public String getMeaning() {
        return meaning;
    }
}
