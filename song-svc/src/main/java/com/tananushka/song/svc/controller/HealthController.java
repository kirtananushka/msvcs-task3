package com.tananushka.song.svc.controller;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HealthController {

    @Value("${app.name}")
    private String appName;

    @GetMapping
    public ResponseEntity<HealthInfo> customHealthCheck() {
        HealthInfo info = new HealthInfo();
        info.setAppName(appName);
        info.setStatus("OK");
        return ResponseEntity.ok(info);
    }

    @Getter
    public static class HealthInfo {

        private String appName;

        private String status;

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
