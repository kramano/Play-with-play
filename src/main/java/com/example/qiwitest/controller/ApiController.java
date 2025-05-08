package com.example.qiwitest.controller;

import com.example.qiwitest.dto.RequestDto;
import com.example.qiwitest.dto.ResponseDto;
import com.example.qiwitest.model.Client;
import com.example.qiwitest.service.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    private static final String CREATE_AGT = "CREATE-AGT";
    private static final String GET_BALANCE = "GET-BALANCE";

    private static final int CLIENT_DOES_NOT_EXIST = 3;
    private static final int OK = 0;
    private static final int WRONG_PASSWORD = 4;
    private static final int CLIENT_ALREADY_EXISTS = 1;
    private static final int TECHNICAL_ERROR = 2;

    private final ClientService clientService;

    @Autowired
    public ApiController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping(value = "/", 
                consumes = MediaType.APPLICATION_XML_VALUE, 
                produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<ResponseDto> process(@RequestBody RequestDto request) {
        logger.debug("Incoming request: {}", request);

        // Validate request
        String login = request.getExtraValue("login");
        if (login == null) {
            logger.info("Bad request: missing parameter [login]");
            return ResponseEntity.badRequest().body(null);
        }

        String password = request.getExtraValue("password");
        if (password == null) {
            logger.info("Bad request: missing parameter [password]");
            return ResponseEntity.badRequest().body(null);
        }

        String type = request.getRequestType();
        if (type == null) {
            logger.info("Bad request: missing parameter [request-type]");
            return ResponseEntity.badRequest().body(null);
        }

        ResponseDto response;
        if (type.equals(CREATE_AGT)) {
            response = createClient(login, password);
        } else if (type.equals(GET_BALANCE)) {
            response = getBalance(login, password);
        } else {
            logger.info("Bad request: unknown request type [{}]", type);
            return ResponseEntity.badRequest().body(null);
        }

        return ResponseEntity.ok(response);
    }

    private ResponseDto getBalance(String login, String password) {
        Client client;
        try {
            client = clientService.findByLogin(login);
        } catch (Exception e) {
            return new ResponseDto(TECHNICAL_ERROR);
        }

        if (client == null) {
            return new ResponseDto(CLIENT_DOES_NOT_EXIST);
        } else {
            if (clientService.isPasswordCorrect(client, password)) {
                ResponseDto response = new ResponseDto(OK);
                BigDecimal balance = clientService.getBalance(client);
                response.addExtra("balance", balance.toString());
                return response;
            } else {
                return new ResponseDto(WRONG_PASSWORD);
            }
        }
    }

    private ResponseDto createClient(String login, String password) {
        Client client;
        try {
            client = clientService.findByLogin(login);
        } catch (Exception e) {
            return new ResponseDto(TECHNICAL_ERROR);
        }

        if (client == null) {
            try {
                clientService.createClient(login, password);
                return new ResponseDto(OK);
            } catch (Exception e) {
                return new ResponseDto(TECHNICAL_ERROR);
            }
        } else {
            return new ResponseDto(CLIENT_ALREADY_EXISTS);
        }
    }
}