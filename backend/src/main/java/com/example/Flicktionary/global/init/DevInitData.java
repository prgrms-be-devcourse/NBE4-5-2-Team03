package com.example.Flicktionary.global.init;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Profile("prod")
@Configuration
public class DevInitData {

    private static final String API_URL = "http://localhost:8080/v3/api-docs";
    private static final String API_JSON_FILE = "apiV1.json";

    @Bean
    public ApplicationRunner devApplicationRunner() {
        return args -> {
            generateApiJsonFile();
            executeCommand();
        };
    }

    private void executeCommand() {
        List<String> tsGenCommand = getTsGenCommand();
        ProcessBuilder processBuilder = new ProcessBuilder(tsGenCommand);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                reader.lines().forEach(System.out::println);
            }

            int exitCode = process.waitFor();
            System.out.println("프로세스 종료 코드: " + exitCode);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("명령 실행 중 오류 발생: " + tsGenCommand, e);
        }
    }

    private void generateApiJsonFile() {
        Path filePath = Path.of(API_JSON_FILE);

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("API 요청 실패: HTTP 상태 코드 " + response.statusCode());
            }

            Files.writeString(filePath, response.body(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("JSON 데이터가 " + filePath.toAbsolutePath() + "에 저장되었습니다.");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("API JSON 파일 생성 중 오류 발생: " + API_JSON_FILE, e);
        }
    }

    // OS에 따라 명령어 설정
    private static List<String> getTsGenCommand() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return List.of("cmd.exe", "/c", "npx --package typescript --package openapi-typescript --package punycode openapi-typescript "
                    + API_JSON_FILE + " -o ../frontend/src/lib/backend/apiV1/schema.d.ts");
        } else {
            return List.of("sh", "-c", "npx --package typescript --package openapi-typescript --package punycode openapi-typescript "
                    + API_JSON_FILE + " -o ../frontend/src/lib/backend/apiV1/schema.d.ts");
        }
    }
}