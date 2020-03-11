/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uaegean.services.impl;

import gr.uaegean.pojo.GrantRequest;
import gr.uaegean.pojo.MinEduAmkaResponse;
import gr.uaegean.pojo.MinEduAmkaResponse.AmkaResponse;
import gr.uaegean.pojo.MinEduΑacademicResponse;
import gr.uaegean.pojo.TokenResponse;
import gr.uaegean.services.MinEduService;
import gr.uaegean.services.PropertiesService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author nikos
 */
@Service
public class MinEduServiceImpl implements MinEduService {

    private PropertiesService paramServ;
    private final String minEduTokenUri;
    private final String minEduTokenUser;
    private final String minEduTokenPass;
    private final String minEduTokenGrantType;
    private final String minEduAmkaQueryEndpoint;
    private LocalDateTime accessTokenExpiration;
    private Optional<String> activeToken;

    private final static Logger LOG = LoggerFactory.getLogger(MinEduServiceImpl.class);

    public MinEduServiceImpl(PropertiesService paramServ) {
        this.paramServ = paramServ;
        this.minEduTokenUri = paramServ.getProp("MINEDU_TOKEN_URL");
        this.minEduTokenPass = paramServ.getProp("MINEDU_TOKEN_PASSWORD");
        this.minEduTokenUser = paramServ.getProp("MINEDU_TOKEN_USERNAME");
        this.minEduTokenGrantType = paramServ.getProp("MINEDU_TOKEN_GRANTTYPE");
        this.minEduAmkaQueryEndpoint = paramServ.getProp("MINEDU_QUERY_AMKA_URL");

        this.accessTokenExpiration = LocalDateTime.now();
        this.activeToken = Optional.empty();
    }

    @Override
    public Optional<String> getAccessToken() {
        GrantRequest grantReq = new GrantRequest(minEduTokenUser, minEduTokenPass, minEduTokenGrantType);
        RestTemplate restTemplate = new RestTemplate();
        LOG.info("will get toke from theurl: " + minEduTokenUri);
        try {
            if (activeToken.isPresent() && accessTokenExpiration.isAfter(LocalDateTime.now().plusSeconds(30))) {
                LOG.info("MinEdu OAth token still alive " + activeToken.get());
                return activeToken;
            } else {
                LOG.info("will get new token ");
                TokenResponse tokResp = restTemplate.postForObject(minEduTokenUri, grantReq, TokenResponse.class);
                if (tokResp != null && tokResp.getSuccess().equals("true") && tokResp.getOauths() != null && tokResp.getOauths()[0].getOauth().getAccessToken() != null) {
                    LOG.info("retrieved token " + tokResp.getOauths()[0].getOauth().getAccessToken());
                    this.accessTokenExpiration = this.accessTokenExpiration.plusSeconds(tokResp.getOauths()[0].getOauth().getExpiresIn());
                    this.activeToken = Optional.of(tokResp.getOauths()[0].getOauth().getAccessToken());
                    return this.activeToken;
                }
            }
        } catch (HttpClientErrorException e) {
            LOG.error(e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<AmkaResponse> getAmkaFullResponse(String amka, String surname) {
        String minEduQueryIdUrl = this.minEduAmkaQueryEndpoint + "/" + amka + "/" + surname + "/extended";
        HttpHeaders requestHeaders = new HttpHeaders();
        Optional<String> accessToken = getAccessToken();
        if (accessToken.isPresent()) {
            RestTemplate restTemplate = new RestTemplate();
            requestHeaders.add("Authorization", "Bearer " + accessToken.get());
            HttpEntity<?> entity = new HttpEntity<>(requestHeaders);
            LOG.info("querying for amka " + amka + "with surname " + surname);
            try {
                ResponseEntity<MinEduAmkaResponse> amkaResponseEntity = restTemplate.exchange(minEduQueryIdUrl, HttpMethod.GET, entity, MinEduAmkaResponse.class);
                MinEduAmkaResponse amkaResp = amkaResponseEntity.getBody();
                return Optional.of(amkaResp.getResult());
            } catch (HttpClientErrorException e) {
                LOG.error(e.getMessage());
            }
        }
        LOG.error("no token found in response!");
        return Optional.empty();
    }

    @Override
    public Optional<MinEduΑacademicResponse.InspectionResult> getInspectioResultByAcademicId(String academicId, String selectedUniversityId, String esmoSessionId) throws HttpClientErrorException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Optional<String> getAcademicIdFromAMKA(String amkaNumber, String selectedUniversityId, String esmoSessionId) throws HttpClientErrorException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Optional<MinEduΑacademicResponse> getInspectioResponseByAcademicId(String academicId, String selectedUniversityId, String esmoSessionId) throws HttpClientErrorException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
