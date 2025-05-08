package com.example.qiwitest.controller;

import com.example.qiwitest.dto.RequestDto;
import com.example.qiwitest.dto.ResponseDto;
import com.example.qiwitest.service.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

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
    public Mono<ResponseEntity<ResponseDto>> process(@RequestBody RequestDto request) {
        logger.debug("Incoming request: {}", request);

        // Validate request
        String login = request.getExtraValue("login");
        if (login == null) {
            logger.info("Bad request: missing parameter [login]");
            return Mono.just(ResponseEntity.badRequest().body(null));
        }

        String password = request.getExtraValue("password");
        if (password == null) {
            logger.info("Bad request: missing parameter [password]");
            return Mono.just(ResponseEntity.badRequest().body(null));
        }

        String type = request.getRequestType();
        if (type == null) {
            logger.info("Bad request: missing parameter [request-type]");
            return Mono.just(ResponseEntity.badRequest().body(null));
        }

        Mono<ResponseDto> responseMono;
        if (type.equals(CREATE_AGT)) {
            responseMono = createClient(login, password);
        } else if (type.equals(GET_BALANCE)) {
            responseMono = getBalance(login, password);
        } else {
            logger.info("Bad request: unknown request type [{}]", type);
            return Mono.just(ResponseEntity.badRequest().body(null));
        }

        return responseMono.map(ResponseEntity::ok);
    }

    private Mono<ResponseDto> getBalance(String login, String password) {
        return clientService.findByLogin(login)
            .flatMap(client -> 
                clientService.isPasswordCorrect(Mono.just(client), password)
                    .flatMap(isCorrect -> {
                        if (isCorrect) {
                            return clientService.getBalance(Mono.just(client))
                                .map(balance -> {
                                    ResponseDto response = new ResponseDto(OK);
                                    response.addExtra("balance", balance.toString());
                                    return response;
                                });
                        } else {
                            return Mono.just(new ResponseDto(WRONG_PASSWORD));
                        }
                    })
            )
            .switchIfEmpty(Mono.just(new ResponseDto(CLIENT_DOES_NOT_EXIST)))
            .onErrorReturn(new ResponseDto(TECHNICAL_ERROR));
    }

    private Mono<ResponseDto> createClient(String login, String password) {
        return clientService.findByLogin(login)
            .flatMap(client -> Mono.just(new ResponseDto(CLIENT_ALREADY_EXISTS)))
            .switchIfEmpty(
                clientService.createClient(login, password)
                    .map(client -> new ResponseDto(OK))
                    .onErrorReturn(new ResponseDto(TECHNICAL_ERROR))
            )
            .onErrorReturn(new ResponseDto(TECHNICAL_ERROR));
    }
}
