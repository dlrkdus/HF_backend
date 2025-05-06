package com.hf.healthfriend.global.config;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AWSConfig {
    @Value("${aws.region}")
    private String region;

    @Autowired
    private Environment environment;

    @PostConstruct
    public void checkEnvironmentVariables() {
        System.out.println("AWS_ACCESS_KEY_ID: " + environment.getProperty("AWS_ACCESS_KEY_ID"));
        System.out.println("AWS_SECRET_ACCESS_KEY: " + environment.getProperty("AWS_SECRET_ACCESS_KEY"));
    }

    @Bean
    public SqsAsyncClient sqsClient() {
        return SqsAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory() {
        return SqsMessageListenerContainerFactory
                .builder()
                .configure(sqsContainerOptionsBuilder ->
                        sqsContainerOptionsBuilder
                                .maxConcurrentMessages(10) // 컨테이너의 스레드 풀 크기
                                .maxMessagesPerPoll(10) // 한 번의 폴링 요청으로 수신할 수 있는 최대 메시지 수를 지정
                )
                .sqsAsyncClient(sqsClient())
                .build();
    }

    @Bean
    @Profile("prod")
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    @Profile("prod")
    public S3Presigner s3Pesigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
