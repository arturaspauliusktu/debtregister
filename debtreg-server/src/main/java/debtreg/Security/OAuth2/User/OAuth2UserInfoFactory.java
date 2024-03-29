package debtreg.Security.OAuth2.User;

import java.util.Map;

import debtreg.Entities.AuthProvider;
import debtreg.Exceptions.OAuth2AuthenticationProcessingException;


public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase(AuthProvider.facebook.toString())) {
            return new FacebookOAuth2UserInfo(attributes);
        // } else if (registrationId.equalsIgnoreCase(AuthProvider.github.toString())) {
        //     return new GithubOAuth2UserInfo(attributes);
        } else {
            throw new OAuth2AuthenticationProcessingException("Sorry! Login with " + registrationId + " is not supported yet.");
        }
    }
}