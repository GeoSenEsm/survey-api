package com.survey.application.services;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RandomCredentialsGenerator implements CredentialsGenerator{
    private static final String WORDS_FILE_PATH = "data/polish_words.txt";
    private final ResourceLoader resourceLoader;
    private List<String> words;

    @Autowired
    public RandomCredentialsGenerator(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init(){
        try {
            words = readWordsFromFile();
        } catch (IOException e) {
            String errorMessage = "Error occurred while reading the words file: " + e.getMessage();
            throw new RuntimeException(errorMessage, e);
        }
    }

    @Override
    public String getRandomPassword() {
        if (words == null) {
            throw new IllegalStateException("Words list has not been initialized.");
        }

        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();

        String randomWord1 = words.get(random.nextInt(words.size()));
        String randomWord2 = words.get(random.nextInt(words.size()));
        int randomNumber1 = random.nextInt(10);
        int randomNumber2 = random.nextInt(10);

        sb.append(randomWord1)
                .append('-')
                .append(randomWord2)
                .append(randomNumber1)
                .append(randomNumber2);

        return sb.toString();
    }

    private List<String> readWordsFromFile() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + WORDS_FILE_PATH);

        try (InputStream inputStream = resource.getInputStream();
             InputStreamReader streamReader = new InputStreamReader(inputStream);
             BufferedReader reader = new BufferedReader(streamReader)) {
            return reader.lines().collect(Collectors.toList());
        } catch (FileNotFoundException ex) {
            throw new FileNotFoundException("Words file not found: " + WORDS_FILE_PATH);
        }

    }

}
