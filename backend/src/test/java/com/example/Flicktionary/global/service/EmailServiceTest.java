package com.example.Flicktionary.global.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;

@DisplayName("이메일 서비스 테스트")
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    private final String testSender = "sender@email.com";

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void injectAutowiredValues() {
        ReflectionTestUtils.setField(
                emailService,
                "emailSenderAddress",
                testSender
        );
    }

    @DisplayName("수신자의 이메일 주소와 이메일의 제목/본문이 주어졌을때, 텍스트 기반의 이메일을 발송한다.")
    @Test
    void givenRecipientAndSimpleEmailContentsWhenSendingSimpleEmailThenSendEmailWithContents() {
        String to = "test@email.com";
        String subject = "testSubject";
        String content = "testContent";
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        doNothing().when(javaMailSender).send(captor.capture());

        emailService.sendSimpleEmail(to, subject, content);
        SimpleMailMessage captured = captor.getValue();

        assertThat(captured).isNotNull();
        assertEquals(testSender, captured.getFrom());
        assertEquals(to, Objects.requireNonNull(captured.getTo())[0]);
        assertEquals(subject, captured.getSubject());
        assertEquals(content, captured.getText());
        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }
}