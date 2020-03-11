package gr.uagean.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.uaegean.pojo.KeycloakSessionTO;
import gr.uaegean.pojo.MinEduAmkaResponse.AmkaResponse;
import gr.uaegean.services.PropertiesService;
import gr.uaegean.singleton.MemcacheSingleton;
import gr.uaegean.singleton.MinEduSingleton;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import net.spy.memcached.MemcachedClient;
import org.keycloak.OAuth2Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.utils.OIDCRedirectUriBuilder;
import org.keycloak.protocol.oidc.utils.OIDCResponseMode;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class AmkaSpRestResource {

    private final KeycloakSession session;
    private static MinEduSingleton MINEDUSING;

    @SuppressWarnings("unused")
    private final AuthenticationManager.AuthResult auth;

    private PropertiesService propServ;

    private final static Logger LOG = LoggerFactory.getLogger(AmkaSpRestResource.class);

    private MemcachedClient mcc;

    public AmkaSpRestResource(KeycloakSession session) {
        this.session = session;
        this.auth = new AppAuthManager().authenticateBearerToken(session, session.getContext().getRealm());
        try {
            this.propServ = new PropertiesService();
        } catch (IOException ex) {
            LOG.error("error reading properties");
            LOG.error(ex.getMessage());
        }
    }

    /**
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @param amka the users amka number
     * @param sessionId the current sesionId of the user authentication
     * @return server error in case of errors, bad request if the user is not
     * found
     *
     * OR the sessionid in case of * correct retrieval of user attributes. In
     * this case the response is cached with key the sessionId so that it can be
     * propaged to the authentication succeded endpoint of keycloak
     *
     * @throws URISyntaxException
     * @throws JsonProcessingException
     * @throws IOException
     */
    @POST
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Path("amka")
    public Response requestAmka(
            @Context HttpServletRequest httpServletRequest,
            @Context HttpServletResponse httpServletResponse,
            @FormParam("amka") String amka,
            @FormParam("surname") String surname,
            @FormParam("sessionId") String sessionId) throws URISyntaxException, JsonProcessingException, IOException {

        LOG.info("requestAmka:: I a request for" + amka + "and surname" + surname + " with sessionId " + sessionId);

        //get amka result from minEdu
        Optional<AmkaResponse> minEduAmka = MINEDUSING.getService().getAmkaFullResponse(amka, surname);
        if (minEduAmka.isPresent()) {
            ObjectMapper mapper = new ObjectMapper();
            MemcacheSingleton.getCache().add("amka-" + sessionId, 1000, mapper.writeValueAsString(minEduAmka.get()));
            LOG.info("will cahce with key: " + sessionId);
            LOG.info(minEduAmka.get().toString());
            return Response.status(Response.Status.OK).entity(sessionId).build();
        }
        return Response.serverError().build();
    }

    @GET
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN, MediaType.APPLICATION_FORM_URLENCODED})
    @Path("proceed")
    public Response transformClaimsToOIDCResponse(@QueryParam("sessionId") String sessionId) throws IOException {

        LOG.info("proceed!!!");
        // see also keycloak OIDCLoginProtocolc lass method authenticated
        OIDCResponseMode responseMode = OIDCResponseMode.QUERY;
        String proceedWithAuthenticationUrl = this.propServ.getProp("AUTH_PROCEED"); //http://localhost:8080/auth/realms/test/protocol/openid-connect/auth
        OIDCRedirectUriBuilder redirectUri = OIDCRedirectUriBuilder.fromUri(proceedWithAuthenticationUrl, responseMode);

        this.mcc = MemcacheSingleton.getCache();
        // retrieve from the cache the client attributes

        KeycloakSessionTO ksTo = (KeycloakSessionTO) mcc.get(sessionId);
        LOG.info(ksTo.toString());

        redirectUri.addParam(OAuth2Constants.RESPONSE_TYPE, ksTo.getResponseType());
        redirectUri.addParam(OAuth2Constants.CLIENT_ID, ksTo.getClientId());
        redirectUri.addParam(OAuth2Constants.REDIRECT_URI, ksTo.getClientRedirectUri());
        redirectUri.addParam(OAuth2Constants.STATE, ksTo.getState());
        redirectUri.addParam(OAuth2Constants.SCOPE, ksTo.getScope());
        redirectUri.addParam("sessionId", sessionId);
        LOG.info("proceed with SSI response concluded ok");

        return redirectUri.build();
    }

}
