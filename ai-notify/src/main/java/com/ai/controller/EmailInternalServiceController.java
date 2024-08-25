package com.ai.controller;

import com.ai.util.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * @author 刘晨
 */

@RestController
@RequestMapping("/internal-service/notify")
public class EmailInternalServiceController {
    @Resource
    private JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String sender;

    @PostMapping("sendCode")
    public Result<Object> sendCode(@RequestBody String email, String code){
        sendMail(email, code);
        return Result.success();
    }

    private void sendMail(String email, String code) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();


        String htmlContent = """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8" />
                        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                        <title>Email Verification Page</title>
                        <style>
                            p,
                            a {
                                /* font-family: Inter; */
                                font-size: 12px;
                            }
                            p {
                                font-weight: 400;
                                line-height: 14.52px;
                                text-align: left;
                            }
                            a {
                                text-decoration: none;
                            }
                            a:hover {
                                text-decoration: underline;
                            }
                            body,
                            html {
                                margin: 0;
                                padding: 0;
                                width: 430px;
                                /* height: 932px; */
                                overflow: hidden;
                            }
                            .main-container {
                                width: 100%;
                                /* height: 100%; */
                                display: flex;
                                flex-direction: column;
                                align-items: flex-start;
                                padding: 24px; /* Uniform horizontal padding */
                                box-sizing: border-box; /* Include padding in width */
                            }
                            .content-container {
                                width: 100%;
                                /* height: 800px; */
                                background: white;
                                display: flex;
                                flex-direction: column;
                                border: 1px solid;
                            }
                            .section {
                                padding: 20px; /* Adjust vertical padding as needed */
                            }
                            .logo {
                                background: white;
                            }
                            .verify-text {
                                color: white;
                                background: orange;
                                text-align: left;
                                padding: 15px;
                            }
                            .verify-text p {
                                /* font-family: Inter; */
                                font-size: 20px;
                                font-weight: 900;
                                line-height: 24.2px;
                                text-align: left;
                            }
                            .gray-stroke {
                                border-top: 1px solid gray;
                                padding-top: 15px;
                            }
                            .code-container {
                                display: flex;
                                flex-direction: column;
                                align-items: center;
                                justify-content: space-between;
                            }
                            .code-container p {
                                width: 100%;
                                text-align: left;
                            }
                            .code {
                                width: 100%;
                                display: flex;
                                flex-direction: row;
                                align-items: center;
                                justify-content: center;
                                font-size: 48px; /* Adjust font size as needed */
                                font-weight: 900;
                                border: 2px solid rgba(0, 0, 0, 0.3);
                            }
                            .footer-links {
                                background: gray;
                                color: white;
                                text-align: center;
                                padding: 10px 0;
                                display: flex;
                                flex-direction: column;
                            }
                            .footer-links a {
                                color: white;
                                /* text-decoration: none; */
                                padding: 0 10px;
                            }
                            .footer {
                                background: orange;
                                color: white;
                                padding: 15px;
                                flex-grow: 1;
                                display: flex;
                                flex-direction: column;
                                align-items: center;
                            }
                            .footer-stroke {
                                margin-bottom: 10px;
                                padding-bottom: 12px;
                                width: 100%;
                                display: flex;
                                justify-content: center;
                                border-bottom: 0.25px solid rgba(255, 255, 255, 0.5);
                            }
                            .footer a {
                                color: white;
                            }
                        </style>
                    </head>
                    <body>
                    <div class="main-container">
                        <div
                                style="
                              display: flex;
                              justify-content: center;
                              width: 100%;
                              margin-bottom: 12px;
                            "
                        >
                            <a href="#" style="color: gray">View the web version of this message</a>
                        </div>
                        <div class="content-container">
                            <div class="section logo">
                                <img src="https://delivery-spring.oss-cn-hangzhou.aliyuncs.com/avatar/6942d43fa01c454f97c8ebac9e02caf7.svg" alt="Logo" height="33" />
                            </div>
                            <div class="section verify-text">
                                <p>Verifying your email address</p>
                            </div>
                            <div class="section">
                                <p>Hello,""" + email +
                                """
                                        !</p>
                                                <p>
                                                                You are receiving this email because we have received a request to register an account under your email. Please copy this code and use it to confirm that this is you, or click the link below to continue your registration process.
                                                            </p>
                                                                      
                                        <div class="code-container gray-stroke">
                                            <p>If this was you, here is your code:</p>
                                            <div class="code">
                                            """
                                    + code +
                                    """
                                    </div>
                                </div>
                                <p>This code will expire in 30 minutes</p>
                                <p>Auto generated email, please do not reply to this email!</p>
                            </div>
                            <div class="section footer-links">
                                <a href="#">This is not me</a>
                                <a href="#">Acumenbot Home</a>
                            </div>
                            <div class="section footer">
                                <!-- Section 1: Logo Image -->
                                <div class="footer-stroke">
                                    <img src="https://delivery-spring.oss-cn-hangzhou.aliyuncs.com/avatar/d32a21b31a734caebc12c148473e6960.png" alt="Contact us" style="height: 25px" />
                                </div>
                    
                                <!-- Section 2: Links -->
                                <div class="footer-stroke">
                                    <a href="#" style="margin: 0 10px">CONTACT US</a>
                                    <a href="#" style="margin: 0 10px">PRIVACY</a>
                                    <a href="#" style="margin: 0 10px">TERMS</a>
                                </div>
                    
                                <!-- Section 3: Company Address -->
                                <div class="footer-stroke">
                                    <p>address of the company</p>
                                </div>
                    
                                <!-- Section 4: Email Information and Unsubscribe Link -->
                                <div>
                                    <p>This email was sent to acumenbot@gmail.com</p>
                                    <p>
                                        <a href="#" style="text-decoration: underline"
                                        >Unsubscribe or change your email preferences</a
                                        >
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                    </body>
                    </html>
                    
                """;

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(sender);
            helper.setTo(email);
            helper.setSubject("Verifying your email address");
            helper.setText(htmlContent, true); // true indicates the text is HTML
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

        mailSender.send(mimeMessage);
    }
}
