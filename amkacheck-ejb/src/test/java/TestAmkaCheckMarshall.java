
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.uaegean.pojo.MinEduAmkaResponse;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author nikos
 */
public class TestAmkaCheckMarshall {

    @Test
    public void testMarshallUnknown() throws IOException {

        String response = "{\n"
                + "  \"ServiceCallID\": \"76022757-303d-4b5f-88da-d61d67c800a2\",\n"
                + "  \"code\": 200,\n"
                + "  \"success\": true,\n"
                + "  \"Result\": {\n"
                + "    \"birth_municipality_greek_code\": \"ΑΤΤΙ\",\n"
                + "    \"birth_date\": \"\",\n"
                + "    \"father_en\": \"\",\n"
                + "    \"father_gr\": \"\",\n"
                + "    \"tid\": \"\",\n"
                + "    \"birth_country\": \"ΕΛΛΑΔΑ\",\n"
                + "    \"ssn\": \"\",\n"
                + "    \"birth_country_code\": \"\",\n"
                + "    \"last_mod_date\": \"\", \n"
                + "    \"surname_cur_gr\": \"ΓΕΩΡΓΑΚΟΠΟΥΛΟΣ\",\n"
                + "    \"id_type\": \"Τ\", \n"
                + "    \"surname_cur_en\": \"GEORGAKOPOULOS\",\n"
                + "    \"surname_birth_gr\": \"ΓΕΩΡΓΑΚΟΠΟΥΛΟΣ\",\n"
                + "    \"death_note\": \"\",\n"
                + "    \"citizenship\": \"ΕΛΛΑΔΑ\",\n"
                + "    \"sex\": \"ΑΡΡΕΝ\",\n"
                + "    \"surname_birth_en\": \"GEORGAKOPOULOS\",\n"
                + "    \"match\": \"true\",\n"
                + "    \"citizenship_code\": \"GR\",\n"
                + "    \"bdate_istrue\": \"\",\n"
                + "    \"birth_municipality\": \"ΧΟΛΑΡΓΟΥ\",\n"
                + "    \"death_date\": \"\",\n"
                + "    \"amka_cur\": \"\",\n"
                + "    \"mother_en\": \"\",\n"
                + "    \"mother_gr\": \"\",\n"
                + "    \"id_num\": \"\", \n"
                + "    \"name_gr\": \"ΠΑΝΑΓΙΩΤΗΣ\",\n"
                + "    \"id_in\": \"\",\n"
                + "    \"id_creation_year\": \"2009\", \n"
                + "    \"name_en\": \"PANAGIOTIS\"\n"
                + "  },\n"
                + "  \"timestamp\": \"2019-10-12T18:03:00+03:00\"\n"
                + "}";

        ObjectMapper mapper = new ObjectMapper();
        MinEduAmkaResponse parsed = mapper.readValue(response, MinEduAmkaResponse.class);

        assertEquals(parsed.getResult().getNameEn(), "PANAGIOTIS");

    }
}
