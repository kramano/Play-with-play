package com.example.qiwitest.controller;

import com.example.qiwitest.dto.RequestDto;
import com.example.qiwitest.model.Client;
import com.example.qiwitest.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApiController.class)
public class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    private Client testClient;

    @BeforeEach
    public void setUp() {
        testClient = new Client("123456", "pwd", new BigDecimal("0.0000"));
    }

    @Test
    public void shouldCreateNewClientSuccess() throws Exception {
        // Arrange
        when(clientService.findByLogin("123456")).thenReturn(null);
        when(clientService.createClient("123456", "pwd")).thenReturn(testClient);

        // Act & Assert
        mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_XML)
                .content(createClientXml()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML))
                .andExpect(xpath("/response/result-code").string("0"));
    }

    @Test
    public void shouldNotCreateNewClientWithExistingLogin() throws Exception {
        // Arrange
        when(clientService.findByLogin("123456")).thenReturn(testClient);

        // Act & Assert
        mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_XML)
                .content(createClientXml()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML))
                .andExpect(xpath("/response/result-code").string("1"));
    }

    @Test
    public void shouldReturnBalanceForExistingClient() throws Exception {
        // Arrange
        when(clientService.findByLogin("123456")).thenReturn(testClient);
        when(clientService.isPasswordCorrect(testClient, "pwd")).thenReturn(true);
        when(clientService.getBalance(testClient)).thenReturn(new BigDecimal("0.0000"));

        // Act & Assert
        mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_XML)
                .content(getBalanceXml()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML))
                .andExpect(xpath("/response/result-code").string("0"))
                .andExpect(xpath("/response/extra[@name='balance']").string("0.0000"));
    }

    @Test
    public void shouldReturnErrorForNoClient() throws Exception {
        // Arrange
        when(clientService.findByLogin("123456")).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_XML)
                .content(getBalanceXml()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML))
                .andExpect(xpath("/response/result-code").string("3"));
    }

    @Test
    public void shouldReturnErrorForWrongPassword() throws Exception {
        // Arrange
        when(clientService.findByLogin("123456")).thenReturn(testClient);
        when(clientService.isPasswordCorrect(testClient, "pwd")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_XML)
                .content(getBalanceXml()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML))
                .andExpect(xpath("/response/result-code").string("4"));
    }

    @Test
    public void shouldReturnErrorForUnknownType() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_XML)
                .content(unknownRequestXml()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnErrorForMissingParameter() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_XML)
                .content(missingParamXml()))
                .andExpect(status().isBadRequest());
    }

    private String createClientXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<request>\n" +
                "        <request-type>CREATE-AGT</request-type>\n" +
                "        <extra name=\"login\">123456</extra>\n" +
                "        <extra name=\"password\">pwd</extra>\n" +
                "</request>";
    }

    private String getBalanceXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<request>\n" +
                "        <request-type>GET-BALANCE</request-type>\n" +
                "        <extra name=\"login\">123456</extra>\n" +
                "        <extra name=\"password\">pwd</extra>\n" +
                "</request>";
    }

    private String unknownRequestXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<request>\n" +
                "        <request-type>UNKNOWN</request-type>\n" +
                "        <extra name=\"login\">123456</extra>\n" +
                "        <extra name=\"password\">pwd</extra>\n" +
                "</request>";
    }

    private String missingParamXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<request>\n" +
                "        <extra name=\"login\">123456</extra>\n" +
                "        <extra name=\"password\">pwd</extra>\n" +
                "</request>";
    }
}