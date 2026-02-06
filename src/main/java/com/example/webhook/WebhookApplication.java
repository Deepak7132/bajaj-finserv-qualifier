package com.example.webhook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class WebhookApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(WebhookApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        RestTemplate restTemplate = new RestTemplate();

        // STEP 1: Call generateWebhook API
        String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        Map<String, String> request = new HashMap<>();
        request.put("name", "Deepak Dagar");
        request.put("regNo", "250850120059");
        request.put("email", "deepakdagarr67@gmail.com");

        ResponseEntity<Map> response =
                restTemplate.postForEntity(generateUrl, request, Map.class);

        String webhookUrl = (String) response.getBody().get("webhook");
        String accessToken = (String) response.getBody().get("accessToken");

        System.out.println("Webhook URL: " + webhookUrl);
        System.out.println("Access Token: " + accessToken);

        // STEP 2: FINAL SQL QUERY (ODD QUESTION)
        String finalQuery = "WITH valid_payments AS (SELECT * FROM PAYMENTS WHERE EXTRACT(DAY FROM PAYMENT_TIME) <> 1), employee_salary AS (SELECT e.EMP_ID, e.FIRST_NAME, e.LAST_NAME, e.DOB, e.DEPARTMENT, SUM(vp.AMOUNT) AS total_salary FROM EMPLOYEE e JOIN valid_payments vp ON e.EMP_ID = vp.EMP_ID GROUP BY e.EMP_ID, e.FIRST_NAME, e.LAST_NAME, e.DOB, e.DEPARTMENT), ranked_salary AS (SELECT *, ROW_NUMBER() OVER (PARTITION BY DEPARTMENT ORDER BY total_salary DESC) as rn FROM employee_salary) SELECT d.DEPARTMENT_NAME, rs.total_salary AS SALARY, CONCAT(rs.FIRST_NAME, ' ', rs.LAST_NAME) AS EMPLOYEE_NAME, DATE_PART('year', AGE(CURRENT_DATE, rs.DOB)) AS AGE FROM ranked_salary rs JOIN DEPARTMENT d ON rs.DEPARTMENT = d.DEPARTMENT_ID WHERE rs.rn = 1;";

        // STEP 3: Send finalQuery to webhook
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);

        Map<String, String> body = new HashMap<>();
        body.put("finalQuery", finalQuery);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> result =
                restTemplate.postForEntity(webhookUrl, entity, String.class);

        System.out.println("Submission Response: " + result.getBody());
    }
}
