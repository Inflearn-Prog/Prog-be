package com.progbe.global.alert;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

 //Kibana의 Alerting/Connector는 Basic 라이선스에서 Webhook 커넥터 지원 안함(Gold 이상 필요)
 //앱에서 직접 Elasticsearch를 조회해 Discord로 알림을 보내기로 함

@Slf4j
@Component
public class DiscordAlertScheduler {

    private static final int ERROR_5XX_THRESHOLD = 10;
    private static final int SLOW_REQUEST_THRESHOLD = 20;

    private static final String COUNT_QUERY_TEMPLATE = """
            {
              "query": {
                "bool": {
                  "filter": [
                    { "range": { "@timestamp": { "gte": "now-5m" } } },
                    %s
                  ]
                }
              }
            }
            """;

    @Value("${alert.discord.webhook-url:}")
    private String discordWebhookUrl;

    @Value("${alert.elasticsearch.url:http://elasticsearch:9200}")
    private String elasticsearchUrl;

    @Value("${alert.elasticsearch.username:elastic}")
    private String esUsername;

    @Value("${alert.elasticsearch.password:}")
    private String esPassword;

    private final RestClient restClient = RestClient.create();

    private final AtomicBoolean error5xxActive = new AtomicBoolean(false);
    private final AtomicBoolean slowRequestActive = new AtomicBoolean(false);

    @Scheduled(fixedRateString = "${alert.check-interval-ms:300000}")
    public void checkAlerts() {
        if (discordWebhookUrl == null || discordWebhookUrl.isBlank()) {
            return;
        }

        checkThreshold("5xx 서버 에러 급등", "{ \"range\": { \"status\": { \"gte\": 500 } } }",
                ERROR_5XX_THRESHOLD, error5xxActive, "최근 5분간 5xx 에러");
        checkThreshold("느린 응답 급증", "{ \"term\": { \"isSlow\": true } }",
                SLOW_REQUEST_THRESHOLD, slowRequestActive, "최근 5분간 2초 초과 요청");
    }

    private void checkThreshold(String ruleName, String filterClause, int threshold,
                                 AtomicBoolean activeState, String description) {
        try {
            long count = countDocuments(filterClause);
            boolean exceeded = count > threshold;

            if (exceeded && activeState.compareAndSet(false, true)) {
                sendDiscordMessage("[Prog-be] 🚨 " + ruleName + "\n"
                        + description + " " + count + "건 감지 (임계치 " + threshold + "건 초과)");
            } else if (!exceeded && activeState.compareAndSet(true, false)) {
                sendDiscordMessage("[Prog-be] ✅ " + ruleName + " 정상화됨");
            }
        } catch (Exception e) {
            log.warn("알림 체크 실패: {}", ruleName, e);
        }
    }

    @SuppressWarnings("unchecked")
    private long countDocuments(String filterClause) {
        String body = COUNT_QUERY_TEMPLATE.formatted(filterClause);
        Map<String, Object> response = restClient.post()
                .uri(elasticsearchUrl + "/progbe-app-*/_count")
                .headers(h -> h.setBasicAuth(esUsername, esPassword))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);
        return response == null ? 0 : ((Number) response.get("count")).longValue();
    }

    private void sendDiscordMessage(String content) {
        restClient.post()
                .uri(discordWebhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("content", content))
                .retrieve()
                .toBodilessEntity();
    }
}
