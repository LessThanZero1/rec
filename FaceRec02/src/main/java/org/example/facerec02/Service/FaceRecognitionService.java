package org.example.facerec02.Service;

import org.example.facerec02.Config.MultipartFileResource;
import org.example.facerec02.Entity.FaceRecognitionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
@Service
public class FaceRecognitionService {
    @Autowired
    private RestTemplate restTemplate;

    private final String FASTAPI_URL = "http://localhost:8000/predict";  // FastAPI地址

    public FaceRecognitionResult recognizeFace(MultipartFile photo) throws IOException {
        // 将MultipartFile包装为MultipartFileResource
        MultipartFileResource resource = new MultipartFileResource(photo);

        // 创建请求实体
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // 调用FastAPI接口
        ResponseEntity<Map> response = restTemplate.exchange(FASTAPI_URL, HttpMethod.POST, requestEntity, Map.class);

        // 解析返回结果
        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null) {
            String prediction = (String) responseBody.get("prediction");
            Double confidence = (Double) responseBody.get("confidence");

            return new FaceRecognitionResult(prediction, confidence);
        }

        throw new IOException("Failed to get recognition result from FastAPI service");
    }

    public static class FaceRecognitionResult {
        private String prediction;
        private Double confidence;

        public FaceRecognitionResult(String prediction, Double confidence) {
            this.prediction = prediction;
            this.confidence = confidence;
        }

        public String getPrediction() {
            return prediction;
        }

        public Double getConfidence() {
            return confidence;
        }

        public boolean isGenuine() {
            return "real".equalsIgnoreCase(prediction);
        }
    }
}
