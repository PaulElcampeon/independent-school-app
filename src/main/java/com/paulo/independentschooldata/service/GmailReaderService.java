//package com.paulo.independentschooldata.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.opencsv.CSVWriter;
//import com.paulo.independentschooldata.dto.SchoolExamResult;
//import com.paulo.independentschooldata.utils.SubjectParser;
//import jakarta.mail.*;
//import jakarta.mail.internet.InternetAddress;
//import org.jsoup.Jsoup;
//import org.springframework.ai.chat.messages.SystemMessage;
//import org.springframework.ai.chat.messages.UserMessage;
//import org.springframework.ai.openai.OpenAiChatModel;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Properties;
//
//@Service
//public class GmailReaderService {
//
//    private final OpenAiChatModel chatModel;
//
//    private final ObjectMapper objectMapper;
//
//    @Value("${spring.mail.username}")
//    private String username;
//
//    @Value("${spring.mail.password}")
//    private String appPassword;
//
//    public GmailReaderService(OpenAiChatModel chatModel, ObjectMapper objectMapper) {
//        this.chatModel = chatModel;
//        this.objectMapper = objectMapper;
//    }
//
//    public List<String> readEmails() {
////        List<SchoolExamResult> allResults = new ArrayList<>();
//        List<ExamResultsResponse> allResults = new ArrayList<>();
//
//
//        Properties props = new Properties();
//        props.put("mail.store.protocol", "imaps");
//        props.put("mail.imaps.host", "imap.gmail.com");
//        props.put("mail.imaps.port", "993");
//        props.put("mail.imaps.ssl.enable", "true");
//        List<String> schoolsSentEmails = new ArrayList<>();
//
//        try {
//            Session session = Session.getInstance(props);
//            Store store = session.getStore("imaps");
//            store.connect("imap.gmail.com", username, appPassword);
//
//            Arrays.stream(store.getSharedNamespaces()).toList().forEach(folder -> System.out.println(folder.getFullName()));
////            Folder sent = store.getFolder("[Gmail]/Sent Mail");
//            Folder sent = store.getFolder("Schools");
//
////            Folder[] folders = store.getDefaultFolder().list("*");
////            for (Folder folder : folders) {
////                System.out.println("Folder: " + folder.getFullName());
////            }
//
//            sent.open(Folder.READ_ONLY);
//
//            Message[] messages = sent.getMessages();
//            System.out.println("Total messages: " + messages.length);
//
////            int limit = Math.min(5, messages.length);
//            for (int i = 0; i < messages.length; i++) {
//                Message msg = messages[i];
//
//                String subject = msg.getSubject();
//
//                if (subject!= null && subject.contains("school information")) {
//                    System.out.println("Found relevant email at index: " + i);
//                    String schoolName = SubjectParser.extractSchoolName(subject);
//                    System.out.println(schoolName);
//                    schoolsSentEmails.add(schoolName);
//
//                    String email = "";
//                    String personal = "";
//
//                    if (msg.getFrom() != null && msg.getFrom().length > 0) {
//                        Address address = msg.getFrom()[0];
//                        if (address instanceof InternetAddress) {
//                            InternetAddress internetAddress = (InternetAddress) address;
//                            email = internetAddress.getAddress();  // This gives only the email address
//                            personal = internetAddress.getPersonal(); // Optional: gets the sender's name if available
////                            System.out.println("Email: " + email);
////                            System.out.println("Name: " + personal);
//                        }
//                    }
//
//
//                    String rawEmailText = getTextFromMessage(msg);
//
//                    String emailText = extractPlainText(rawEmailText);
//
////                    System.out.println(emailText);
//
////                    System.out.println("----------------------------------");
////                    System.out.println("Subject: " + msg.getSubject());
////                    System.out.println("From: " + msg.getFrom()[0]);
////                    System.out.println("Received: " + msg.getReceivedDate());
//                    List<org.springframework.ai.chat.messages.Message> gptmessages = List.of(
//                            new SystemMessage(
//                                    "I sent this message to many schools asking for their GCSE and A-Level results. " +
//                                            "Here is the message I sent:\n\n" +
//                                            "Hello, I am currently researching potential schools for my child and would appreciate it if you could provide some information regarding your most recent exam results. Specifically, I am interested in the following:\n" +
//                                            "- The percentage of students who achieved Grade 5 or above or Grade 4 or above in their GCSEs.\n" +
//                                            "- The percentage of students who achieved C+ in their A-Level results.\n\n" +
//                                            "I have attached the response received from the school. Your task is to extract the **GCSE results only** from the email response.\n\n" +
//                                            "If GCSE exam results are present in the email, return a JSON object with the following fields:\n" +
//                                            "{\n" +
//                                            "  \"reason\": \"results found\",\n" +
//                                            "  \"school\": \"<school name>\",\n" +
//                                            "  \"gcseResults\": {\n" +
//                                            "    \"grade4OrAbove\": <percentage if provided or null>,\n" +
//                                            "    \"grade5OrAbove\": <percentage if provided or null>\n" +
//                                            "  }\n" +
//                                            "}\n\n" +
//                                            "If no GCSE exam results are present in the email, return:\n" +
//                                            "{ \"reason\": \"no GCSE results present\", \"school\": \"<school name>\", \"gcseResults\": null }\n\n" +
//                                            "Always return **only the JSON object** (no code blocks, no explanations, no text outside the JSON). " +
//                                            "Use percentages as numbers (e.g., 65 for 65%) and include both grade 4 and grade 5 results if mentioned. " +
//                                            "If the school name is not explicitly stated in the response, leave the field empty or null."
//                            ),
//                            new UserMessage(emailText)
//                    );
//
//                    String response = chatModel.call(gptmessages.toArray(new org.springframework.ai.chat.messages.Message[0]));
//                    System.out.println(response);
//                    // 5️⃣ Parse JSON into DTO
//                    try {
////                        SchoolExamResult result = objectMapper.readValue(response, SchoolExamResult.class);
//                        ExamResultsResponse result = objectMapper.readValue(response, ExamResultsResponse.class);
//
//                        result.setEmail(email);
//                        result.setName(personal);
//                        allResults.add(result);
//                        System.out.println(result);
//                    } catch (Exception e) {
//                        System.err.println("Failed to parse JSON for school: " + schoolName + ". Raw response: " + response);
//                        // fallback: store as "no results"
////                        SchoolExamResult result = new SchoolExamResult();
//                        ExamResultsResponse result = new ExamResultsResponse();
//                        result.setSchool(schoolName);
//                        result.setEmail(email);
//                        result.setName(personal);
//                        result.setReason("failed to parse LLM response");
//                        allResults.add(result);
//                    }
//                }
////                System.out.println("Body: " + getTextFromMessage(msg));
//            }
//
//            sent.close(false);
//            store.close();
//            writeResultsToCSV(allResults, "school_results.csv");
//
//            return schoolsSentEmails;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return schoolsSentEmails;
//
//    }
//
//    private String getTextFromMessage(Message message) throws Exception {
//        Object content = message.getContent();
//
//        if (content instanceof String) {
//            return (String) content;
//        } else if (content instanceof Multipart) {
//            Multipart multipart = (Multipart) content;
//            for (int i = 0; i < multipart.getCount(); i++) {
//                BodyPart part = multipart.getBodyPart(i);
//                String disposition = part.getDisposition();
//
//                // skip attachments
//                if (disposition != null && (disposition.equalsIgnoreCase(Part.ATTACHMENT)
//                        || disposition.equalsIgnoreCase(Part.INLINE))) {
//                    continue;
//                }
//
//                String partContent = part.getContent().toString();
//                if (partContent != null && !partContent.isBlank()) {
//                    return partContent;
//                }
//            }
//        }
//
//        return "";
//    }
//
//    private String extractPlainText(String emailContent) {
//        // Jsoup parses HTML, removes tags, returns plain text
//        return Jsoup.parse(emailContent).text();
//    }
//
//
//    public void writeResultsToCSV(List<ExamResultsResponse> results, String filePath) throws IOException {
//        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
//            // write header
//            writer.writeNext(new String[]{"School Name", "Email", "Name", "Reason", "GCSE Results"});
//
//            for (ExamResultsResponse r : results) {
//                writer.writeNext(new String[]{
//                        r.getSchool(),
//                        r.getEmail() != null ? r.getEmail() : "",
//                        r.getName() != null ? r.getName() : "",
//                        r.getReason() != null ? r.getReason() : "",
//                        r.getGcseResults() != null ? r.getGcseResults().toString() : "",
//
////                        r.getALevelResults() != null ? r.getALevelResults().toString() : ""
//                });
//            }
//        }
//    }
//
////    private String getTextFromMessage(Message message) throws Exception {
////        if (message.isMimeType("text/plain")) {
////            return message.getContent().toString();
////        } else if (message.isMimeType("multipart/*")) {
////            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
////            return getTextFromMimeMultipart(mimeMultipart);
////        }
////        return "";
////    }
//
////    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws Exception {
////        StringBuilder result = new StringBuilder();
////        int count = mimeMultipart.getCount();
////        for (int i = 0; i < count; i++) {
////            var bodyPart = mimeMultipart.getBodyPart(i);
////            if (bodyPart.isMimeType("text/plain")) {
////                result.append(bodyPart.getContent());
////            } else if (bodyPart.isMimeType("text/html")) {
////                String html = (String) bodyPart.getContent();
////                result.append(org.jsoup.Jsoup.parse(html).text());
////            } else if (bodyPart.getContent() instanceof MimeMultipart) {
////                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
////            }
////        }
////        return result.toString();
////    }
//}
