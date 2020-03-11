/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uaegean.services;

import gr.uaegean.pojo.MinEduAmkaResponse.AmkaResponse;
import gr.uaegean.pojo.MinEduΑacademicResponse;
import gr.uaegean.pojo.MinEduΑacademicResponse.InspectionResult;
import java.util.Optional;
import org.springframework.web.client.HttpClientErrorException;

/**
 *
 * @author nikos
 */
public interface MinEduService {

    public Optional<String> getAccessToken() throws HttpClientErrorException;

    public Optional<InspectionResult> getInspectioResultByAcademicId(String academicId, String selectedUniversityId, String esmoSessionId) throws HttpClientErrorException;

    public Optional<String> getAcademicIdFromAMKA(String amkaNumber, String selectedUniversityId, String esmoSessionId) throws HttpClientErrorException;

    public Optional<MinEduΑacademicResponse> getInspectioResponseByAcademicId(String academicId, String selectedUniversityId, String esmoSessionId) throws HttpClientErrorException;

    public Optional<AmkaResponse> getAmkaFullResponse(String amka, String surname);

}
