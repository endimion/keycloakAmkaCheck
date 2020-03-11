/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.uagean.authenticators;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.uaegean.pojo.MinEduAmkaResponse;
import gr.uaegean.singleton.MemcacheSingleton;
import java.io.IOException;
import net.spy.memcached.MemcachedClient;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.springframework.util.StringUtils;

/**
 *
 * @author nikos
 */
public class AfterAmkaAuthenticator implements Authenticator {

//    protected ParameterService paramServ = new ParameterServiceImpl();
    private static Logger LOG = Logger.getLogger(AfterAmkaAuthenticator.class);

    private ObjectMapper mapper;
    private MemcachedClient mcc;

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        try {
            KeycloakSession session = context.getSession();
            RealmModel realm = context.getRealm();
            mapper = new ObjectMapper();

            LOG.info("reached after-Amka-authenticator!!!!!");

            String sessionId = context.getHttpRequest().getUri().getQueryParameters().getFirst("sessionId");
            LOG.info(sessionId);
            if (StringUtils.isEmpty(sessionId)) {
                context.attempted();
                return;
            }
            this.mcc = MemcacheSingleton.getCache();
            LOG.info("looking for: " + "user amka details" + String.valueOf(sessionId));
            String minEduAmkaRespString = (String) this.mcc.get("amka-" + String.valueOf(sessionId));
            LOG.info("GOT the following amka details" + minEduAmkaRespString);

            ObjectMapper mapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            MinEduAmkaResponse minEduAmkaResp = mapper.readValue(minEduAmkaRespString, MinEduAmkaResponse.class);

            // since we are not storing the users we use as a username the did
            UserModel user = KeycloakModelUtils.findUserByNameOrEmail(session, realm, minEduAmkaResp.getResult().getAmkaCurrent());
            if (user == null) {
                // since we are not storing the users we use as a username the did
                user = session.users().addUser(realm, minEduAmkaResp.getResult().getAmkaCurrent());
            }
            user.setEnabled(true);

            if (minEduAmkaResp.getResult() != null) {
                user.setFirstName(minEduAmkaResp.getResult().getNameEn());
                user.setLastName(minEduAmkaResp.getResult().getSurnameCurEn());
                user.setEmail(minEduAmkaResp.getResult().getAmkaCurrent() + "@amka");
                user.setEmailVerified(true);
                user.setSingleAttribute("amka-latinLastName", minEduAmkaResp.getResult().getSurnameCurEn());
                user.setSingleAttribute("amka-latinFirstName", minEduAmkaResp.getResult().getNameEn());
                user.setSingleAttribute("amka-amka", minEduAmkaResp.getResult().getAmkaCurrent());
                user.setSingleAttribute("amka-birthMunicipalityGreekCode", minEduAmkaResp.getResult().getBirthMunicipalityGreekCode());
                user.setSingleAttribute("amka-birthDate", minEduAmkaResp.getResult().getBirthDate());
                user.setSingleAttribute("amka-fatherEN", minEduAmkaResp.getResult().getFatherEN());
                user.setSingleAttribute("amka-fatherGR", minEduAmkaResp.getResult().getFatherGR());
                user.setSingleAttribute("amka-tid", minEduAmkaResp.getResult().getTid());
                user.setSingleAttribute("amka-birthCountry", minEduAmkaResp.getResult().getBirthCountry());
                user.setSingleAttribute("amka-ssn", minEduAmkaResp.getResult().getSsn());
                user.setSingleAttribute("amka-birthCountryCode", minEduAmkaResp.getResult().getBirthCountryCode());
                user.setSingleAttribute("amka-lastModDate", minEduAmkaResp.getResult().getLastModDate());
                user.setSingleAttribute("amka-surnameCurGr", minEduAmkaResp.getResult().getSurnameCurGr());
                user.setSingleAttribute("amka-idType", minEduAmkaResp.getResult().getIdType());
                user.setSingleAttribute("amka-surnameCurEn", minEduAmkaResp.getResult().getSurnameCurEn());
                user.setSingleAttribute("amka-surnameBirthGr", minEduAmkaResp.getResult().getSurnameBirthGr());
                user.setSingleAttribute("amka-deathNote", minEduAmkaResp.getResult().getDeathNote());
                user.setSingleAttribute("amka-citizenship", minEduAmkaResp.getResult().getCitizenship());
                user.setSingleAttribute("amka-sex", minEduAmkaResp.getResult().getSex());
                user.setSingleAttribute("amka-surnameBirthEn", minEduAmkaResp.getResult().getSurnameBirthEn());
                user.setSingleAttribute("amka-match", minEduAmkaResp.getResult().getMatch());
                user.setSingleAttribute("amka-citizenshipCode", minEduAmkaResp.getResult().getCitizenshipCode());
                user.setSingleAttribute("amka-bdateIsTrue", minEduAmkaResp.getResult().getBdateIsTrue());
                user.setSingleAttribute("amka-birthMunicipality", minEduAmkaResp.getResult().getBirthMunicipality());
                user.setSingleAttribute("amka-deathDate", minEduAmkaResp.getResult().getDeathDate());
                user.setSingleAttribute("amka-amkaCurrent", minEduAmkaResp.getResult().getAmkaCurrent());
                user.setSingleAttribute("amka-motherEn", minEduAmkaResp.getResult().getMotherEn());
                user.setSingleAttribute("amka-motherGr", minEduAmkaResp.getResult().getMotherGr());
                user.setSingleAttribute("amka-idNum", minEduAmkaResp.getResult().getIdNum());
                user.setSingleAttribute("amka-nameGr", minEduAmkaResp.getResult().getNameGr());
                user.setSingleAttribute("amka-idIn", minEduAmkaResp.getResult().getIdIn());
                user.setSingleAttribute("amka-idCreationYear", minEduAmkaResp.getResult().getIdCreationYear());
                user.setSingleAttribute("amka-nameEn", minEduAmkaResp.getResult().getNameEn());
            }

            context.setUser(user);
            context.success();
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
            //TODO failure is better?
            LOG.info("will continue with attempted");
            context.attempted();
        }
    }

    @Override
    public void action(AuthenticationFlowContext afc) {
        LOG.info("AFTER eidas actionImp called");
        LOG.info(afc.getUser());
        if (afc.getUser() != null) {
            afc.success();
        } else {
            afc.attempted();
        }
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession ks, RealmModel rm, UserModel um) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession ks, RealmModel rm, UserModel um) {
    }

    @Override
    public void close() {

    }

}
